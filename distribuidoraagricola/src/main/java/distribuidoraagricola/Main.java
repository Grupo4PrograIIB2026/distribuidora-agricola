package distribuidoraagricola;

import distribuidoraagricola.modelo.Usuario;
import distribuidoraagricola.persistencia.PersistenciaUtil;
import distribuidoraagricola.vista.Login;
import distribuidoraagricola.vista.MenuPrincipal;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Punto de entrada del sistema.
 */
public class Main {
    public static void main(String[] args) {
        
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));
        try {
            Usuario usuario = new Login().iniciarSesion();
            if (usuario != null) {
                new MenuPrincipal(usuario).iniciar();
            }
        } catch (RuntimeException e) {
            System.out.println("\n=====================================================");
            System.out.println("No se pudo iniciar el sistema.");
            System.out.println(e.getMessage());
            System.out.println("=====================================================");
        } finally {
            PersistenciaUtil.cerrar();
        }
    }
}
