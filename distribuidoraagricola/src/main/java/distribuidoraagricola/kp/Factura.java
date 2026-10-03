package distribuidoraagricola.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.sql.Timestamp;

/**
 * Factura generada automaticamente al despachar un pedido.
 * nombreCliente no es columna de esta tabla (viene de un JOIN via
 * Pedidos -> Clientes), por lo que va @Transient.
 */
@Entity
@Table(name = "Facturas")
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idFactura")
    private int idFactura;

    @Column(name = "idPedido", nullable = false, unique = true)
    private int idPedido;

    @Column(name = "fechaFactura")
    private Timestamp fechaFactura;

    @Column(name = "total", nullable = false)
    private double total;

    @Transient
    private String nombreCliente;

    public Factura() {
    }

    public Factura(int idFactura, int idPedido, Timestamp fechaFactura, double total) {
        this.idFactura = idFactura;
        this.idPedido = idPedido;
        this.fechaFactura = fechaFactura;
        this.total = total;
    }

    public int getIdFactura() {
        return idFactura;
    }

    public void setIdFactura(int idFactura) {
        this.idFactura = idFactura;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public Timestamp getFechaFactura() {
        return fechaFactura;
    }

    public void setFechaFactura(Timestamp fechaFactura) {
        this.fechaFactura = fechaFactura;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }
}
