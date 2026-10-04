package distribuidoraagricola.persistencia;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Administra el EntityManagerFactory de JPA/Hibernate para toda la
 * aplicacion (se crea una sola vez) y entrega un EntityManager nuevo
 * cada vez que se necesita.
 *
 * IMPORTANTE: antes de ejecutar el sistema hay que:
 *   1) Ejecutar el script sql/DistribuidoraAgricola.sql en SQL Server
 *      (las tablas ya deben existir; Hibernate no las crea ni las
 *      modifica, ver hibernate.hbm2ddl.auto=none en persistence.xml).
 *   2) Editar src/main/resources/META-INF/persistence.xml con los
 *      datos de tu propio SQL Server (servidor, puerto, contrasenia).
 */
public class PersistenciaUtil {

    private static final String UNIDAD_PERSISTENCIA = "distribuidoraAgricolaPU";
    private static EntityManagerFactory factory;

    private PersistenciaUtil() {
        // clase de utilidad: no se instancia
    }

    public static synchronized EntityManagerFactory getFactory() {
        if (factory == null || !factory.isOpen()) {
            try {
                factory = Persistence.createEntityManagerFactory(UNIDAD_PERSISTENCIA);
            } catch (RuntimeException e) {
                throw new RuntimeException(
                        "No se pudo inicializar la base de datos (JPA/Hibernate). Puede ser: "
                        + "1) la conexion (servicio de SQL Server apagado, TCP/IP deshabilitado, "
                        + "o datos incorrectos en persistence.xml), o "
                        + "2) un problema en el mapeo de las entidades (anotaciones @Entity/@Column). "
                        + "El detalle tecnico de abajo suele decir cual de los dos es. "
                        + "Detalle tecnico: " + e.getMessage(), e);
            }
        }
        return factory;
    }

    /** Cada operacion de los DAO pide su propio EntityManager y lo cierra al terminar. */
    public static EntityManager crearEntityManager() {
        return getFactory().createEntityManager();
    }

    /** Se llama unicamente al salir del programa. */
    public static void cerrar() {
        if (factory != null && factory.isOpen()) {
            factory.close();
        }
    }
}
