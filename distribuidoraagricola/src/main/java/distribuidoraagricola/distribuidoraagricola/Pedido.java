package distribuidoraagricola;

import java.util.ArrayList;

public class Pedido {

    private final int id;
    private final Cliente cliente;
    private final ArrayList<DetallePedido> detalles;
    private String estado;

    public Pedido(int id, Cliente cliente) {
        this.id = id;
        this.cliente = cliente;
        this.detalles = new ArrayList<>();
        this.estado = "PENDIENTE";
    }

    public int getId() {
        return id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public String getEstado() {
        return estado;
    }

    public ArrayList<DetallePedido> getDetalles() {
        return detalles;
    }

    public void agregarProducto(Producto producto, int cantidad) {

        if (cantidad <= 0) {
            System.out.println("La cantidad debe ser mayor que cero.");
            return;
        }

        if (cantidad > producto.getStock()) {
            System.out.println("No hay suficiente stock de "
                    + producto.getNombre());
            return;
        }

        for (DetallePedido detalle : detalles) {

            if (detalle.getProducto().getId() == producto.getId()) {

                detalle.aumentarCantidad(cantidad);
                producto.reducirStock(cantidad);

                System.out.println("Cantidad actualizada correctamente.");
                return;
            }
        }

        DetallePedido nuevoDetalle =
                new DetallePedido(producto, cantidad);

        detalles.add(nuevoDetalle);
        producto.reducirStock(cantidad);

        System.out.println("Producto agregado correctamente.");
    }

    public double calcularTotal() {

        double total = 0;

        for (DetallePedido detalle : detalles) {
            total += detalle.calcularSubtotal();
        }

        return total;
    }

    public void cambiarEstado(String nuevoEstado) {

        nuevoEstado = nuevoEstado.toUpperCase();

        if (nuevoEstado.equals("PENDIENTE")
                || nuevoEstado.equals("CONFIRMADO")
                || nuevoEstado.equals("PREPARANDO")
                || nuevoEstado.equals("ENVIADO")
                || nuevoEstado.equals("ENTREGADO")
                || nuevoEstado.equals("CANCELADO")) {

            estado = nuevoEstado;

            System.out.println("Estado actualizado a: " + estado);

        } else {

            System.out.println("Estado no válido.");
        }
    }

    public void mostrarPedido() {

        System.out.println("             PEDIDO #" + id);

        System.out.println("Cliente: " + cliente.getNombre());
        System.out.println("Estado: " + estado);

        System.out.println("\nProductos:");

        if (detalles.isEmpty()) {

            System.out.println("El pedido no tiene productos.");

        } else {

            for (DetallePedido detalle : detalles) {
                System.out.println(detalle);
            }
        }
        System.out.printf("TOTAL: Q%.2f%n", calcularTotal());
        
    }
}