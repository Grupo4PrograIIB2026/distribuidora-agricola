package distribuidoraagricola.vista;

import distribuidoraagricola.modelo.Usuario;
import distribuidoraagricola.servicio.LoginService;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Pantalla de inicio de sesion. Se muestra antes del menu principal;
 * no se puede pasar sin una cuenta valida y activa.
 *
 * Nota: la contrasenia se lee con Scanner (no con System.console())
 * porque System.console() devuelve null al ejecutar desde la consola
 * de salida de NetBeans, lo que rompería el programa con un
 * NullPointerException apenas se pidiera la contrasenia.
 */
public class Login {

    private static final int INTENTOS_MAXIMOS = 3;

    private final Scanner sc = new Scanner(System.in, StandardCharsets.UTF_8);
    private final LoginService loginService = new LoginService();

    /**
     * @return el Usuario autenticado, o null si se agotaron los intentos.
     */
    public Usuario iniciarSesion() {
        System.out.println("===== SISTEMA DISTRIBUIDORA AGRICOLA =====");
        for (int intento = 1; intento <= INTENTOS_MAXIMOS; intento++) {
            System.out.print("Usuario: ");
            String nombreUsuario = sc.nextLine().trim();
            System.out.print("Contrasenia: ");
            String contrasenia = sc.nextLine();

            Usuario usuario = loginService.autenticar(nombreUsuario, contrasenia);
            if (usuario != null) {
                System.out.println("\nBienvenido, " + usuario.getNombreUsuario()
                        + " (" + usuario.getNombreRol() + ")\n");
                return usuario;
            }
            System.out.println("Usuario o contrasenia incorrectos, o la cuenta esta inactiva. "
                    + "Intento " + intento + " de " + INTENTOS_MAXIMOS + ".\n");
        }
        System.out.println("Se agotaron los intentos. Cerrando el sistema.");
        return null;
    }
}
