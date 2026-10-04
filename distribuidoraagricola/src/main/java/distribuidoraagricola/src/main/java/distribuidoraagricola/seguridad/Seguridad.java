package distribuidoraagricola.seguridad;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Utilidades para no guardar contrasenias en texto plano.
 *
 * Cada usuario tiene un salt aleatorio propio (guardado junto con el
 * hash en la tabla Usuarios). La contrasenia nunca se guarda tal cual:
 * se guarda unicamente SHA-256(salt + contrasenia).
 *
 * Nota: esto es razonable para un proyecto universitario. En un sistema
 * en produccion real se usaria un algoritmo pensado para contrasenias
 * (BCrypt, Argon2), que son intencionalmente lentos para dificultar
 * ataques de fuerza bruta; SHA-256 no lo es.
 */
public class Seguridad {

    private Seguridad() {
        // clase de utilidad: no se instancia
    }

    public static String generarSalt() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        return bytesToHex(bytes);
    }

    public static String hashPassword(String contrasenia, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] hash = md.digest(contrasenia.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("No se pudo calcular el hash de la contrasenia.", e);
        }
    }

    public static boolean verificar(String contrasenia, String salt, String hashEsperado) {
        return hashPassword(contrasenia, salt).equalsIgnoreCase(hashEsperado);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
