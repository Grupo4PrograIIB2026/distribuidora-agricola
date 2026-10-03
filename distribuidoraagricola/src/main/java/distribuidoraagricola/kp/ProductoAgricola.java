package distribuidoraagricola.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ProductosAgricolas")
public class ProductoAgricola {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idProducto")
    private int idProducto;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "unidadMedida", nullable = false, length = 20)
    private String unidadMedida;

    @Column(name = "precioUnitario", nullable = false)
    private double precioUnitario;

    @Column(name = "existencia", nullable = false)
    private int existencia;

    public ProductoAgricola() {
    }

    public ProductoAgricola(String nombre, String descripcion, String unidadMedida,
                             double precioUnitario, int existencia) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.unidadMedida = unidadMedida;
        this.precioUnitario = precioUnitario;
        this.existencia = existencia;
    }

    public ProductoAgricola(int idProducto, String nombre, String descripcion, String unidadMedida,
                             double precioUnitario, int existencia) {
        this.idProducto = idProducto;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.unidadMedida = unidadMedida;
        this.precioUnitario = precioUnitario;
        this.existencia = existencia;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public int getExistencia() {
        return existencia;
    }

    public void setExistencia(int existencia) {
        this.existencia = existencia;
    }

    @Override
    public String toString() {
        return String.format("%-4d %-28s %-8s Q%-10.2f existencia: %d",
                idProducto, nombre, unidadMedida, precioUnitario, existencia);
    }
}
