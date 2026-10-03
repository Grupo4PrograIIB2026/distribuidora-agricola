package distribuidoraagricola.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.sql.Timestamp;

/**
 * Un registro de la bitacora de acceso al sistema: quien intento entrar
 * (o salir), cuando, y que paso. Se guarda en la tabla BitacoraAcceso.
 */
@Entity
@Table(name = "BitacoraAcceso")
public class BitacoraAcceso {

    public static final String LOGIN = "LOGIN";
    public static final String LOGIN_FALLIDO = "LOGIN_FALLIDO";
    public static final String LOGOUT = "LOGOUT";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdBitacora")
    private int idBitacora;

    /** Es null cuando el usuario escrito no existe en el sistema. */
    @Column(name = "IdUsuario")
    private Integer idUsuario;

    @Column(name = "NombreUsuario", nullable = false, length = 50)
    private String nombreUsuario;

    @Column(name = "Accion", nullable = false, length = 20)
    private String accion;

    @Column(name = "Detalle", length = 200)
    private String detalle;

    @Column(name = "Fecha", nullable = false)
    private Timestamp fecha;

    public BitacoraAcceso() {
        // requerido por JPA
    }

    public BitacoraAcceso(Integer idUsuario, String nombreUsuario, String accion, String detalle) {
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.accion = accion;
        this.detalle = detalle;
        this.fecha = new Timestamp(System.currentTimeMillis());
    }

    public int getIdBitacora() { return idBitacora; }
    public Integer getIdUsuario() { return idUsuario; }
    public String getNombreUsuario() { return nombreUsuario; }
    public String getAccion() { return accion; }
    public String getDetalle() { return detalle; }
    public Timestamp getFecha() { return fecha; }

    @Override
    public String toString() {
        return String.format("%-4d | %s | %-14s | %-13s | %s",
                idBitacora, fecha.toString().substring(0, 19), nombreUsuario, accion,
                detalle == null ? "" : detalle);
    }
}
