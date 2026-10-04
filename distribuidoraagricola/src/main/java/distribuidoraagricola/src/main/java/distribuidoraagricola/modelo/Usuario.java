package distribuidoraagricola.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * Representa una cuenta de acceso al sistema. idCliente solo tiene valor
 * cuando el rol es Cliente; para Administrador/Trabajador queda null.
 * idRol se guarda como columna simple (no @ManyToOne); nombreRol no es
 * columna real (viene de un JOIN con Roles) por lo que va @Transient.
 */
@Entity
@Table(name = "Usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdUsuario")
    private int idUsuario;

    @Column(name = "NombreUsuario", nullable = false, unique = true, length = 50)
    private String nombreUsuario;

    @Column(name = "Salt", nullable = false, length = 64)
    private String salt;

    @Column(name = "HashContrasenia", nullable = false, length = 64)
    private String hashContrasenia;

    @Column(name = "IdRol", nullable = false)
    private int idRol;

    @Transient
    private String nombreRol;

    @Column(name = "IdCliente")
    private Integer idCliente;

    @Column(name = "Estado", nullable = false)
    private boolean estado;

    public Usuario() {
    }

    public Usuario(String nombreUsuario, String salt, String hashContrasenia, int idRol, Integer idCliente) {
        this.nombreUsuario = nombreUsuario;
        this.salt = salt;
        this.hashContrasenia = hashContrasenia;
        this.idRol = idRol;
        this.idCliente = idCliente;
        this.estado = true;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getHashContrasenia() {
        return hashContrasenia;
    }

    public void setHashContrasenia(String hashContrasenia) {
        this.hashContrasenia = hashContrasenia;
    }

    public int getIdRol() {
        return idRol;
    }

    public void setIdRol(int idRol) {
        this.idRol = idRol;
    }

    public String getNombreRol() {
        return nombreRol;
    }

    public void setNombreRol(String nombreRol) {
        this.nombreRol = nombreRol;
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }
}
