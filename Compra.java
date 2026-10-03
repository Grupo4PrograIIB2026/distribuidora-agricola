package distribuidoraagricola;

import java.util.ArrayList;

/**
 * Compra a un proveedor.
 * Al agregar un producto, el stock se actualiza automaticamente (aumenta).
 */
public class Compra {
    private final int id;
    private final Proveedor proveedor;
    private final ArrayList<DetalleCompra> detalles;

    public Compra(int id, Proveedor proveedor) {
        this.id = id;
        this.proveedor = proveedor;
        this.detalles = new ArrayList<>();
    }

    public int getId() { return id; }
    public Proveedor getProveedor() { return proveedor; }
    public ArrayList<DetalleCompra> getDetalles() { return detalles; }

    public void agregarProducto(Producto producto, int cantidad, double costoUnitario) {
        if (cantidad <= 0) {
            System.out.println("La cantidad debe ser mayor que cero.");
            return;
        }
        if (costoUnitario < 0) {
            System.out.println("El costo no puede ser negativo.");
            return;
        }
        detalles.add(new DetalleCompra(producto, cantidad, costoUnitario));
        producto.aumentarStock(cantidad); // ACTUALIZACION AUTOMATICA DE EXISTENCIAS
        System.out.println("Compra registrada: +" + cantidad + " de " + producto.getNombre()
                + ". Stock actual: " + producto.getStock());
    }

    public double calcularTotal() {
        double total = 0;
        for (DetalleCompra d : detalles) {
            total += d.calcularSubtotal();
        }
        return total;
    }

    public void mostrarCompra() {
        System.out.println("\nCOMPRA #" + id);
        System.out.println("Proveedor: " + proveedor);
        System.out.println("\nProductos:");
        if (detalles.isEmpty()) {
            System.out.println("La compra no tiene productos.");
        } else {
            for (DetalleCompra d : detalles) {
                System.out.println(d);
            }
        }
        System.out.printf("TOTAL: Q%.2f%n", calcularTotal());
    }
}
