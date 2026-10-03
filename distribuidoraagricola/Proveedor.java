package distribuidoraagricola;

public class Proveedor {

    private final int id;
    private String nombre;
    private String telefono;
    private String empresa;

    public Proveedor(int id, String nombre, String telefono, String empresa) {
        this.id = id;
        this.nombre = nombre;
        this.telefono = telefono;
        this.empresa = empresa;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    @Override
    public String toString() {
        return "ID: " + id
                + " | Nombre: " + nombre
                + " | Teléfono: " + telefono
                + " | Empresa: " + empresa;
    }
}