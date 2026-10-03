package distribuidoraagricola;

public class DetalleCompra {
    private final Producto producto;
    private final int cantidad;
    private final double costoUnitario;

    public DetalleCompra(Producto producto, int cantidad, double costoUnitario) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.costoUnitario = costoUnitario;
    }

    public Producto getProducto() { return producto; }
    public int getCantidad() { return cantidad; }
    public double getCostoUnitario() { return costoUnitario; }

    public double calcularSubtotal() {
        return cantidad * costoUnitario;
    }

    @Override
    public String toString() {
        return String.format("%s | Cantidad: %d | Costo: Q%.2f | Subtotal: Q%.2f",
                producto.getNombre(), cantidad, costoUnitario, calcularSubtotal());
    }
}
