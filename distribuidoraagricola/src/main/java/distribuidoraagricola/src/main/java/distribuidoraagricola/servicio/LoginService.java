package distribuidoraagricola.servicio;

import distribuidoraagricola.dao.UsuarioDb;
import distribuidoraagricola.modelo.Usuario;
import distribuidoraagricola.seguridad.Seguridad;

/**
 * Regla de negocio de autenticacion: un login es valido solo si el
 * usuario existe, esta activo y la contrasenia coincide con el hash
 * guardado. No se distingue el motivo del rechazo hacia afuera (por
 * seguridad no conviene revelar si el usuario existe o no).
 */
public class LoginService {

    private final UsuarioDb usuarioDb = new UsuarioDb();

    /**
     * @return el Usuario autenticado, o null si el usuario no existe,
     *         esta desactivado, o la contrasenia no coincide.
     */
    public Usuario autenticar(String nombreUsuario, String contrasenia) {
        Usuario u = usuarioDb.buscarPorNombreUsuario(nombreUsuario);
        if (u == null) {
            return null;
        }
        if (!u.isEstado()) {
            return null;
        }
        if (!Seguridad.verificar(contrasenia, u.getSalt(), u.getHashContrasenia())) {
            return null;
        }
        return u;
    }
}
