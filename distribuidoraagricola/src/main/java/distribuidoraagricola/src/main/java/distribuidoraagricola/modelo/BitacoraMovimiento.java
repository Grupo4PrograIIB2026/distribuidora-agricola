package distribuidoraagricola.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

/**
 * Un registro de la bitacora de movimientos: que usuario agrego, modifico
 * o elimino algo en el sistema, cuando lo hizo y un resumen de lo que paso.
 * Se guarda en la tabla BitacoraMovimientos.
 */
@Entity
@Table(name = "BitacoraMovimientos")
public class BitacoraMovimiento {

    /* Tipo de movimiento */
    public static final String AGREGAR = "AGREGAR";
    public static final String MODIFICAR = "MODIFICAR";
    public static final String ELIMINAR = "ELIMINAR";

    /* Modulo del sistema donde se hizo el movimiento */
    public static final String CLIENTES = "Clientes";
    public static final String PROVEEDORES = "Proveedores";
    public static final String PRODUCTOS = "Productos";
    public static final String COMPRAS = "Compras";
    public static final String PEDIDOS = "Pedidos";
    public static final String USUARIOS = "Usuarios";

    private static final int MAX_NOMBRE_USUARIO = 50;
    private static final int MAX_DETALLE = 500;
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdMovimiento")
    private int idMovimiento;

    @Column(name = "IdUsuario", nullable = false)
    private int idUsuario;

    /** Se guarda tambien el nombre para que el registro se entienda solo. */
    @Column(name = "NombreUsuario", nullable = false, length = 50)
    private String nombreUsuario;

    @Column(name = "Accion", nullable = false, length = 20)
    private String accion;

    @Column(name = "Modulo", nullable = false, length = 30)
    private String modulo;

    @Column(name = "Detalle", nullable = false, length = 500)
    private String detalle;

    @Column(name = "Fecha", nullable = false)
    private Timestamp fecha;

    public BitacoraMovimiento() {
        // requerido por JPA
    }

    public BitacoraMovimiento(int idUsuario, String nombreUsuario, String accion, String modulo, String detalle) {
        this.idUsuario = idUsuario;
        this.nombreUsuario = recortar(nombreUsuario, MAX_NOMBRE_USUARIO);
        this.accion = accion;
        this.modulo = modulo;
        this.detalle = recortar(detalle, MAX_DETALLE);
        this.fecha = new Timestamp(System.currentTimeMillis());
    }

    /** Evita que un texto largo (ej. una compra con muchos productos) rompa el INSERT. */
    private static String recortar(String texto, int maximo) {
        if (texto == null) {
            return "";
        }
        return texto.length() <= maximo ? texto : texto.substring(0, maximo - 3) + "...";
    }

    public int getIdMovimiento() { return idMovimiento; }
    public int getIdUsuario() { return idUsuario; }
    public String getNombreUsuario() { return nombreUsuario; }
    public String getAccion() { return accion; }
    public String getModulo() { return modulo; }
    public String getDetalle() { return detalle; }
    public Timestamp getFecha() { return fecha; }

    public String getFechaTexto() {
        return fecha.toLocalDateTime().format(FORMATO_FECHA);
    }

    @Override
    public String toString() {
        return String.format("%-4d | %-19s | %-14s | %-10s | %-11s | %s",
                idMovimiento, getFechaTexto(), nombreUsuario, accion, modulo, detalle);
    }
}
