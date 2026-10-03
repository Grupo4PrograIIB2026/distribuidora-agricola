package distribuidoraagricola;

public class DetallePedido {

    private final Producto producto;
    private int cantidad;
    private final double precioUnitario;

    public DetallePedido(Producto producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = producto.getPrecio();
    }

    public Producto getProducto() {
        return producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public double calcularSubtotal() {
        return cantidad * precioUnitario;
    }

    public void aumentarCantidad(int cantidad) {
        this.cantidad += cantidad;
    }

    @Override
    public String toString() {
        return producto.getNombre()
                + " | Cantidad: " + cantidad
                + " | Precio: Q" + precioUnitario
                + " | Subtotal: Q" + calcularSubtotal();
    }
}