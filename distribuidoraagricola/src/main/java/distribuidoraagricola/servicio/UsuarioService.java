package distribuidoraagricola.servicio;

import distribuidoraagricola.dao.UsuarioDb;
import distribuidoraagricola.modelo.Usuario;
import distribuidoraagricola.seguridad.Seguridad;

/**
 * Reglas de negocio para administrar cuentas de usuario. La lectura y
 * escritura pura de la tabla vive en UsuarioDb; aqui se valida antes
 * de llamarlo (nombre repetido, contrasenia minima, y que solo las
 * cuentas con rol Cliente tengan un idCliente asociado).
 */
public class UsuarioService {

    private final UsuarioDb usuarioDb = new UsuarioDb();

    public String crearUsuario(String nombreUsuario, String contrasenia, int idRol,
                                Integer idCliente, String nombreRol) {
        if (nombreUsuario == null || nombreUsuario.isBlank()) {
            return "El nombre de usuario no puede estar vacio.";
        }
        if (contrasenia == null || contrasenia.length() < 4) {
            return "La contrasenia debe tener al menos 4 caracteres.";
        }
        if (usuarioDb.existeNombreUsuario(nombreUsuario)) {
            return "Ya existe un usuario con ese nombre.";
        }
        boolean esRolCliente = "Cliente".equalsIgnoreCase(nombreRol);
        if (esRolCliente && idCliente == null) {
            return "Una cuenta con rol Cliente debe estar vinculada a un cliente existente.";
        }
        if (!esRolCliente && idCliente != null) {
            return "Solo las cuentas con rol Cliente se vinculan a un cliente.";
        }

        String salt = Seguridad.generarSalt();
        String hash = Seguridad.hashPassword(contrasenia, salt);
        Usuario u = new Usuario(nombreUsuario, salt, hash, idRol, idCliente);
        int id = usuarioDb.insertar(u);
        return id > 0
                ? "Usuario '" + nombreUsuario + "' creado con id " + id + "."
                : "No se pudo crear el usuario.";
    }

    public String cambiarPassword(int idUsuario, String nuevaContrasenia) {
        if (nuevaContrasenia == null || nuevaContrasenia.length() < 4) {
            return "La contrasenia debe tener al menos 4 caracteres.";
        }
        String salt = Seguridad.generarSalt();
        String hash = Seguridad.hashPassword(nuevaContrasenia, salt);
        boolean ok = usuarioDb.cambiarPassword(idUsuario, salt, hash);
        return ok ? "Contrasenia actualizada." : "No se pudo actualizar la contrasenia (verifica el ID).";
    }

    public String cambiarEstado(int idUsuario, boolean nuevoEstado) {
        boolean ok = usuarioDb.cambiarEstado(idUsuario, nuevoEstado);
        String accion = nuevoEstado ? "activada" : "desactivada";
        return ok ? "Cuenta " + accion + "." : "No se pudo cambiar el estado (verifica el ID).";
    }
}
