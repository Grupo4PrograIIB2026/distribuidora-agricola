package distribuidoraagricola.servicio;

import distribuidoraagricola.dao.UsuarioDb;
import distribuidoraagricola.modelo.BitacoraMovimiento;
import distribuidoraagricola.modelo.Usuario;
import distribuidoraagricola.seguridad.Seguridad;

/**
 * Reglas de negocio para administrar cuentas de usuario. La lectura y
 * escritura pura de la tabla vive en UsuarioDb; aqui se valida antes
 * de llamarlo (nombre repetido, contrasenia minima, y que solo las
 * cuentas con rol Cliente tengan un idCliente asociado).
 *
 * Cada cambio exitoso queda en la bitacora de movimientos a nombre del
 * usuario que lo hizo ('realizadoPor'). La contrasenia nunca se escribe ahi.
 */
public class UsuarioService {

    private final UsuarioDb usuarioDb = new UsuarioDb();
    private final BitacoraService bitacora = new BitacoraService();

    public String crearUsuario(String nombreUsuario, String contrasenia, int idRol,
                                Integer idCliente, String nombreRol, Usuario realizadoPor) {
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
        if (id <= 0) {
            return "No se pudo crear el usuario.";
        }

        String detalle = "Usuario '" + nombreUsuario + "' creado con rol " + nombreRol
                + (idCliente != null ? " (vinculado al cliente #" + idCliente + ")" : "");
        bitacora.registrarMovimiento(realizadoPor, BitacoraMovimiento.AGREGAR,
                BitacoraMovimiento.USUARIOS, detalle);
        return "Usuario '" + nombreUsuario + "' creado con id " + id + ".";
    }

    public String cambiarPassword(int idUsuario, String nuevaContrasenia, Usuario realizadoPor) {
        if (nuevaContrasenia == null || nuevaContrasenia.length() < 4) {
            return "La contrasenia debe tener al menos 4 caracteres.";
        }
        String salt = Seguridad.generarSalt();
        String hash = Seguridad.hashPassword(nuevaContrasenia, salt);
        boolean ok = usuarioDb.cambiarPassword(idUsuario, salt, hash);
        if (!ok) {
            return "No se pudo actualizar la contrasenia (verifica el ID).";
        }

        Usuario afectado = usuarioDb.buscarPorId(idUsuario);
        String nombre = afectado != null ? afectado.getNombreUsuario() : "#" + idUsuario;
        bitacora.registrarMovimiento(realizadoPor, BitacoraMovimiento.MODIFICAR,
                BitacoraMovimiento.USUARIOS, "Cambio la contrasenia del usuario '" + nombre + "'");
        return "Contrasenia actualizada.";
    }

    public String cambiarEstado(int idUsuario, boolean nuevoEstado, Usuario realizadoPor) {
        Usuario antes = usuarioDb.buscarPorId(idUsuario);
        boolean ok = usuarioDb.cambiarEstado(idUsuario, nuevoEstado);
        if (!ok) {
            return "No se pudo cambiar el estado (verifica el ID).";
        }

        // Solo se registra si el estado realmente cambio (no si ya estaba asi).
        if (antes != null && antes.isEstado() != nuevoEstado) {
            bitacora.registrarMovimiento(realizadoPor, BitacoraMovimiento.MODIFICAR,
                    BitacoraMovimiento.USUARIOS,
                    "Usuario '" + antes.getNombreUsuario() + "': estado "
                    + (antes.isEstado() ? "Activo" : "Inactivo") + " -> "
                    + (nuevoEstado ? "Activo" : "Inactivo"));
        }
        return "Cuenta " + (nuevoEstado ? "activada" : "desactivada") + ".";
    }
}
