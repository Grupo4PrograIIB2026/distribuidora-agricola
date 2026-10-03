package distribuidoraagricola.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa una compra de productos a un proveedor. idProveedor se
 * guarda como columna simple (no @ManyToOne). nombreProveedor y el
 * detalle no son columnas de esta tabla, van @Transient y los llena
 * el DAO/servicio.
 */
@Entity
@Table(name = "Compras")
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCompra")
    private int idCompra;

    @Column(name = "idProveedor", nullable = false)
    private int idProveedor;

    @Transient
    private String nombreProveedor;

    @Column(name = "fechaCompra")
    private Timestamp fechaCompra;

    @Column(name = "total", nullable = false)
    private double total;

    @Transient
    private List<DetalleCompra> detalle = new ArrayList<>();

    public Compra() {
    }

    public Compra(int idProveedor) {
        this.idProveedor = idProveedor;
    }

    public int getIdCompra() {
        return idCompra;
    }

    public void setIdCompra(int idCompra) {
        this.idCompra = idCompra;
    }

    public int getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(int idProveedor) {
        this.idProveedor = idProveedor;
    }

    public String getNombreProveedor() {
        return nombreProveedor;
    }

    public void setNombreProveedor(String nombreProveedor) {
        this.nombreProveedor = nombreProveedor;
    }

    public Timestamp getFechaCompra() {
        return fechaCompra;
    }

    public void setFechaCompra(Timestamp fechaCompra) {
        this.fechaCompra = fechaCompra;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public List<DetalleCompra> getDetalle() {
        return detalle;
    }

    public void setDetalle(List<DetalleCompra> detalle) {
        this.detalle = detalle;
    }

    public void agregarDetalle(DetalleCompra d) {
        this.detalle.add(d);
    }
}
