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
 * Representa un pedido realizado por un cliente, junto con su detalle
 * y su estado dentro del flujo del negocio. idCliente se guarda como
 * columna simple (no @ManyToOne). nombreCliente y el detalle no son
 * columnas de esta tabla, por lo que van @Transient y los llena el
 * DAO/servicio.
 */
@Entity
@Table(name = "Pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPedido")
    private int idPedido;

    @Column(name = "idCliente", nullable = false)
    private int idCliente;

    @Transient
    private String nombreCliente;

    @Column(name = "fechaPedido")
    private Timestamp fechaPedido;

    @Column(name = "estado", nullable = false, length = 20)
    private EstadoPedido estado;

    @Column(name = "total", nullable = false)
    private double total;

    @Transient
    private List<DetallePedido> detalle = new ArrayList<>();

    public Pedido() {
        this.estado = EstadoPedido.REGISTRADO;
    }

    public Pedido(int idCliente) {
        this.idCliente = idCliente;
        this.estado = EstadoPedido.REGISTRADO;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public Timestamp getFechaPedido() {
        return fechaPedido;
    }

    public void setFechaPedido(Timestamp fechaPedido) {
        this.fechaPedido = fechaPedido;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public void setEstado(EstadoPedido estado) {
        this.estado = estado;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public List<DetallePedido> getDetalle() {
        return detalle;
    }

    public void setDetalle(List<DetallePedido> detalle) {
        this.detalle = detalle;
    }

    public void agregarDetalle(DetallePedido d) {
        this.detalle.add(d);
    }
}
