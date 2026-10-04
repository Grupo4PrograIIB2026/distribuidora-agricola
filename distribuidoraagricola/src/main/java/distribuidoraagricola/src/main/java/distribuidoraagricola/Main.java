package distribuidoraagricola;

import distribuidoraagricola.modelo.Usuario;
import distribuidoraagricola.persistencia.PersistenciaUtil;
import distribuidoraagricola.vista.Login;
import distribuidoraagricola.vista.MenuPrincipal;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Punto de entrada del sistema.
 */
public class Main {
    public static void main(String[] args) {
        // Fuerza la salida a UTF-8 sin importar que la consola de Windows
        // o la ventana Output de NetBeans esten en otra codificacion por
        // defecto (Cp1252/CP850). Sin esto, tildes y enies pueden salir
        // como simbolos raros aunque el codigo fuente este bien escrito.
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));

        // Apaga el logging interno de Hibernate (las lineas "HHH..." que
        // imprime al arrancar, con fecha y nombre de clase). Tiene que ir
        // ANTES de la primera operacion de base de datos (el login), que
        // es cuando Hibernate se inicializa y empieza a loguear.
        Logger.getLogger("org.hibernate").setLevel(Level.SEVERE);

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
