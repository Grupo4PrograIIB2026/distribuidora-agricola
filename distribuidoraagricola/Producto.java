package distribuidoraagricola;

public class Producto {

    private final int id;
    private String nombre;
    private double precio;
    private int stock;

    public Producto(int id, String nombre, double precio, int stock) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public double getPrecio() {
        return precio;
    }

    public int getStock() {
        return stock;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public void reducirStock(int cantidad) {
        if (cantidad <= stock) {
            stock -= cantidad;
        }
    }

    public void aumentarStock(int cantidad) {
        stock += cantidad;
    }

    @Override
    public String toString() {
        return "ID: " + id
                + " | Producto: " + nombre
                + " | Precio: Q" + precio
                + " | Stock: " + stock;
    }
}