package distribuidoraagricola;

import java.util.ArrayList;

/**
 * Controla el ciclo del pedido sin modificar la clase Pedido:
 *  - confirmar el pedido
 *  - completar el pedido (ENTREGADO) y generar la factura
 *  - cancelar el pedido devolviendo las existencias al inventario
 *
 * NOTA: Pedido.agregarProducto() ya descuenta el stock al registrar el pedido,
 * por eso aqui NO se vuelve a descontar al completarlo (evita descontar doble).
 */
public class GestorPedidos {
    private final ArrayList<Factura> facturas = new ArrayList<>();
    private int siguienteFactura = 1;

    public ArrayList<Factura> getFacturas() { return facturas; }

    public void confirmarPedido(Pedido pedido) {
        if (!pedido.getEstado().equals("PENDIENTE")) {
            System.out.println("Solo se puede confirmar un pedido PENDIENTE (actual: " + pedido.getEstado() + ").");
            return;
        }
        if (pedido.getDetalles().isEmpty()) {
            System.out.println("No se puede confirmar un pedido sin productos.");
            return;
        }
        pedido.cambiarEstado("CONFIRMADO");
    }

    /** Completa el pedido: lo marca ENTREGADO y genera su factura. */
    public Factura completarPedido(Pedido pedido) {
        String estado = pedido.getEstado();
        if (estado.equals("ENTREGADO") || estado.equals("CANCELADO")) {
            System.out.println("El pedido ya esta " + estado + ", no se puede completar.");
            return null;
        }
        if (estado.equals("PENDIENTE")) {
            System.out.println("Primero debe confirmar el pedido.");
            return null;
        }
        pedido.cambiarEstado("ENTREGADO");
        Factura factura = new Factura(siguienteFactura++, pedido);
        facturas.add(factura);
        System.out.println("Factura #" + factura.getId() + " generada.");
        return factura;
    }

    /** Cancela el pedido y devuelve el stock de cada producto. */
    public void cancelarPedido(Pedido pedido) {
        String estado = pedido.getEstado();
        if (estado.equals("ENTREGADO") || estado.equals("CANCELADO")) {
            System.out.println("No se puede cancelar un pedido " + estado + ".");
            return;
        }
        for (DetallePedido d : pedido.getDetalles()) {
            d.getProducto().aumentarStock(d.getCantidad()); // ACTUALIZACION AUTOMATICA DE EXISTENCIAS
        }
        pedido.cambiarEstado("CANCELADO");
        System.out.println("Existencias devueltas al inventario.");
    }

    public void mostrarFacturas() {
        System.out.println("\nFACTURAS EMITIDAS");
        if (facturas.isEmpty()) {
            System.out.println("No hay facturas.");
            return;
        }
        for (Factura f : facturas) {
            System.out.printf("Factura #%d | Pedido #%d | Cliente: %s | Total: Q%.2f%n",
                    f.getId(), f.getPedido().getId(), f.getPedido().getCliente().getNombre(), f.getTotal());
        }
    }
}
