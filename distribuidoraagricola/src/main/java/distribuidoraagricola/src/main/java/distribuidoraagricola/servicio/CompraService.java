package distribuidoraagricola.servicio;

import distribuidoraagricola.dao.CompraDb;
import distribuidoraagricola.dao.ProductoDb;
import distribuidoraagricola.dao.ProveedorDb;
import distribuidoraagricola.modelo.BitacoraMovimiento;
import distribuidoraagricola.modelo.Compra;
import distribuidoraagricola.modelo.DetalleCompra;
import distribuidoraagricola.modelo.ProductoAgricola;
import distribuidoraagricola.modelo.Proveedor;
import distribuidoraagricola.modelo.Usuario;
import distribuidoraagricola.persistencia.PersistenciaUtil;

import jakarta.persistence.EntityManager;

/**
 * Regla de negocio de las compras: registrar la compra junto con su
 * detalle y, en la misma transaccion, aumentar la existencia de cada
 * producto comprado. Si algo falla a la mitad, se revierte todo
 * (rollback) para que el inventario nunca quede a medias.
 *
 * Una vez guardada la compra, queda en la bitacora de movimientos a
 * nombre del usuario que la registro ('realizadoPor').
 */
public class CompraService {

    private final CompraDb compraDb = new CompraDb();
    private final ProductoDb productoDb = new ProductoDb();
    private final ProveedorDb proveedorDb = new ProveedorDb();
    private final BitacoraService bitacora = new BitacoraService();

    public String registrarCompra(Compra compra, Usuario realizadoPor) {
        if (compra.getDetalle().isEmpty()) {
            return "La compra debe tener al menos un producto.";
        }

        double total = 0;
        for (DetalleCompra d : compra.getDetalle()) {
            total += d.getSubtotal();
        }
        compra.setTotal(total);

        int idCompra;
        EntityManager em = PersistenciaUtil.crearEntityManager();
        try {
            em.getTransaction().begin();

            idCompra = compraDb.insertarCompra(em, compra);

            for (DetalleCompra d : compra.getDetalle()) {
                compraDb.insertarDetalleCompra(em, idCompra, d);

                ProductoAgricola producto = productoDb.buscarPorId(em, d.getIdProducto());
                if (producto == null) {
                    throw new IllegalStateException("No existe el producto #" + d.getIdProducto());
                }
                // 'producto' esta gestionado por 'em': basta con cambiarlo aqui,
                // JPA detecta el cambio solo (dirty checking) y lo guarda al
                // hacer commit. No hace falta un UPDATE manual.
                producto.setExistencia(producto.getExistencia() + d.getCantidad());
            }

            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return "Error al registrar la compra: " + e.getMessage();
        } finally {
            em.close();
        }

        registrarEnBitacora(compra, idCompra, total, realizadoPor);
        return "Compra #" + idCompra + " registrada correctamente. Inventario actualizado.";
    }

    /** La compra ya quedo guardada; esto solo deja constancia (no lanza excepcion). */
    private void registrarEnBitacora(Compra compra, int idCompra, double total, Usuario realizadoPor) {
        Proveedor proveedor = proveedorDb.buscarPorId(compra.getIdProveedor());
        String nombreProveedor = proveedor != null ? proveedor.getNombre() : "#" + compra.getIdProveedor();

        StringBuilder productos = new StringBuilder();
        for (DetalleCompra d : compra.getDetalle()) {
            if (productos.length() > 0) {
                productos.append(", ");
            }
            productos.append(d.getNombreProducto()).append(" (+").append(d.getCantidad()).append(")");
        }

        bitacora.registrarMovimiento(realizadoPor, BitacoraMovimiento.AGREGAR, BitacoraMovimiento.COMPRAS,
                "Compra #" + idCompra + " al proveedor '" + nombreProveedor + "' por "
                + BitacoraService.quetzales(total) + "; inventario aumentado: " + productos);
    }
}
