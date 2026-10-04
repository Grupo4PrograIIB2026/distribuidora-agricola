package distribuidoraagricola.servicio;

import distribuidoraagricola.dao.ClienteDb;
import distribuidoraagricola.dao.FacturaDb;
import distribuidoraagricola.dao.PedidoDb;
import distribuidoraagricola.dao.ProductoDb;
import distribuidoraagricola.modelo.BitacoraMovimiento;
import distribuidoraagricola.modelo.Cliente;
import distribuidoraagricola.modelo.DetallePedido;
import distribuidoraagricola.modelo.EstadoPedido;
import distribuidoraagricola.modelo.Pedido;
import distribuidoraagricola.modelo.ProductoAgricola;
import distribuidoraagricola.modelo.Usuario;
import distribuidoraagricola.persistencia.PersistenciaUtil;

import jakarta.persistence.EntityManager;

/**
 * Reglas de negocio del ciclo de vida de un pedido:
 *
 *   registrarPedido  -> guarda el pedido y su detalle, estado REGISTRADO.
 *                       Todavia no toca el inventario.
 *   confirmarPedido  -> valida que haya existencia suficiente de cada
 *                       producto antes de pasar a CONFIRMADO. Si algun
 *                       producto no alcanza, el pedido NO se confirma.
 *   despacharPedido  -> descuenta el inventario y genera la factura,
 *                       todo dentro de una misma transaccion (si algo
 *                       falla, no queda inventario ni factura a medias).
 *   cancelarPedido   -> permite cancelar mientras no se haya despachado.
 *
 * Cada una de estas operaciones, cuando se completa, queda en la bitacora
 * de movimientos a nombre del usuario que la hizo ('realizadoPor').
 */
public class PedidoService {

    private final PedidoDb pedidoDb = new PedidoDb();
    private final ProductoDb productoDb = new ProductoDb();
    private final FacturaDb facturaDb = new FacturaDb();
    private final ClienteDb clienteDb = new ClienteDb();
    private final BitacoraService bitacora = new BitacoraService();

    public int registrarPedido(Pedido pedido, Usuario realizadoPor) {
        if (pedido.getDetalle().isEmpty()) {
            return -1;
        }
        double total = 0;
        for (DetallePedido d : pedido.getDetalle()) {
            total += d.getSubtotal();
        }
        pedido.setTotal(total);
        pedido.setEstado(EstadoPedido.REGISTRADO);

        int idPedido;
        EntityManager em = PersistenciaUtil.crearEntityManager();
        try {
            em.getTransaction().begin();

            idPedido = pedidoDb.insertarPedido(em, pedido);
            for (DetallePedido d : pedido.getDetalle()) {
                pedidoDb.insertarDetallePedido(em, idPedido, d);
            }

            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.out.println("Error al registrar el pedido: " + e.getMessage());
            return -1;
        } finally {
            em.close();
        }

        // El pedido ya quedo guardado; esto solo deja constancia (no lanza excepcion).
        Cliente cliente = clienteDb.buscarPorId(pedido.getIdCliente());
        String nombreCliente = cliente != null ? cliente.getNombre() : "#" + pedido.getIdCliente();
        StringBuilder productos = new StringBuilder();
        for (DetallePedido d : pedido.getDetalle()) {
            if (productos.length() > 0) {
                productos.append(", ");
            }
            productos.append(d.getNombreProducto()).append(" x").append(d.getCantidad());
        }
        bitacora.registrarMovimiento(realizadoPor, BitacoraMovimiento.AGREGAR, BitacoraMovimiento.PEDIDOS,
                "Pedido #" + idPedido + " registrado para el cliente '" + nombreCliente + "' por "
                + BitacoraService.quetzales(pedido.getTotal()) + ": " + productos);
        return idPedido;
    }

    public String confirmarPedido(int idPedido, Usuario realizadoPor) {
        Pedido pedido = pedidoDb.buscarPorId(idPedido);
        if (pedido == null) {
            return "El pedido #" + idPedido + " no existe.";
        }
        if (pedido.getEstado() != EstadoPedido.REGISTRADO) {
            return "Solo se pueden confirmar pedidos en estado Registrado (este esta en "
                    + pedido.getEstado() + ").";
        }

        for (DetallePedido d : pedido.getDetalle()) {
            int existencia = productoDb.consultarExistencia(d.getIdProducto());
            if (existencia < d.getCantidad()) {
                return "Existencia insuficiente para '" + d.getNombreProducto()
                        + "' (disponible: " + existencia + ", solicitado: " + d.getCantidad() + "). "
                        + "El pedido no se confirmo.";
            }
        }

        boolean ok = pedidoDb.actualizarEstado(idPedido, EstadoPedido.CONFIRMADO);
        if (ok) {
            registrarCambioEstado(realizadoPor, pedido, EstadoPedido.CONFIRMADO, null);
        }
        return ok ? "Pedido #" + idPedido + " confirmado correctamente."
                   : "No se pudo confirmar el pedido.";
    }

    public String despacharPedido(int idPedido, Usuario realizadoPor) {
        Pedido pedido = pedidoDb.buscarPorId(idPedido);
        if (pedido == null) {
            return "El pedido #" + idPedido + " no existe.";
        }
        if (pedido.getEstado() != EstadoPedido.CONFIRMADO) {
            return "Solo se pueden despachar pedidos en estado Confirmado (este esta en "
                    + pedido.getEstado() + ").";
        }

        EntityManager em = PersistenciaUtil.crearEntityManager();
        try {
            em.getTransaction().begin();

            for (DetallePedido d : pedido.getDetalle()) {
                ProductoAgricola producto = productoDb.buscarPorId(em, d.getIdProducto());
                if (producto == null || producto.getExistencia() < d.getCantidad()) {
                    em.getTransaction().rollback();
                    return "Existencia insuficiente para '" + d.getNombreProducto()
                            + "' al momento de despachar. El pedido no se despacho.";
                }
            }

            for (DetallePedido d : pedido.getDetalle()) {
                ProductoAgricola producto = productoDb.buscarPorId(em, d.getIdProducto());
                producto.setExistencia(producto.getExistencia() - d.getCantidad());
            }

            pedidoDb.actualizarEstado(em, idPedido, EstadoPedido.DESPACHADO);
            facturaDb.generar(em, idPedido, pedido.getTotal());

            em.getTransaction().commit();

            StringBuilder inventario = new StringBuilder();
            for (DetallePedido d : pedido.getDetalle()) {
                if (inventario.length() > 0) {
                    inventario.append(", ");
                }
                inventario.append(d.getNombreProducto()).append(" (-").append(d.getCantidad()).append(")");
            }
            registrarCambioEstado(realizadoPor, pedido, EstadoPedido.DESPACHADO,
                    "inventario descontado: " + inventario + "; factura generada por "
                    + BitacoraService.quetzales(pedido.getTotal()));
            return "Pedido #" + idPedido + " despachado. Inventario actualizado y factura generada.";
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return "Error al despachar el pedido: " + e.getMessage();
        } finally {
            em.close();
        }
    }

    public String cancelarPedido(int idPedido, Usuario realizadoPor) {
        Pedido pedido = pedidoDb.buscarPorId(idPedido);
        if (pedido == null) {
            return "El pedido #" + idPedido + " no existe.";
        }
        if (pedido.getEstado() == EstadoPedido.DESPACHADO || pedido.getEstado() == EstadoPedido.CANCELADO) {
            return "No se puede cancelar un pedido en estado " + pedido.getEstado() + ".";
        }
        boolean ok = pedidoDb.actualizarEstado(idPedido, EstadoPedido.CANCELADO);
        if (ok) {
            registrarCambioEstado(realizadoPor, pedido, EstadoPedido.CANCELADO, null);
        }
        return ok ? "Pedido #" + idPedido + " cancelado." : "No se pudo cancelar el pedido.";
    }

    /**
     * Deja en la bitacora el cambio de estado de un pedido. 'pedido' todavia
     * trae el estado anterior (se leyo antes de cambiarlo). No lanza excepcion.
     */
    private void registrarCambioEstado(Usuario realizadoPor, Pedido pedido,
                                       EstadoPedido nuevoEstado, String adicional) {
        String detalle = "Pedido #" + pedido.getIdPedido() + " de '" + pedido.getNombreCliente()
                + "': estado " + pedido.getEstado() + " -> " + nuevoEstado
                + (adicional == null ? "" : "; " + adicional);
        bitacora.registrarMovimiento(realizadoPor, BitacoraMovimiento.MODIFICAR,
                BitacoraMovimiento.PEDIDOS, detalle);
    }
}
