package distribuidoraagricola.servicio;

import distribuidoraagricola.dao.BitacoraDb;
import distribuidoraagricola.modelo.BitacoraMovimiento;
import distribuidoraagricola.modelo.Usuario;

import java.util.List;
import java.util.Locale;

/**
 * Regla de negocio de la bitacora de movimientos: deja constancia de
 * cada cosa que un usuario agrega, modifica o elimina en el sistema
 * (quien, cuando, en que modulo y un resumen de lo que paso), y permite
 * que el Administrador la consulte.
 *
 * Las contrasenias NUNCA se escriben en la bitacora.
 */
public class BitacoraService {

    private final BitacoraDb bitacoraDb = new BitacoraDb();

    /**
     * Registra un movimiento hecho por 'usuario'. No lanza excepcion: si la
     * bitacora falla, la operacion que ya se hizo no se ve afectada.
     */
    public void registrarMovimiento(Usuario usuario, String accion, String modulo, String detalle) {
        if (usuario == null) {
            return;
        }
        bitacoraDb.insertar(new BitacoraMovimiento(
                usuario.getIdUsuario(), usuario.getNombreUsuario(), accion, modulo, detalle));
    }

    /**
     * Registra una modificacion con la lista de campos que cambiaron (ver
     * agregarCambio). Si no cambio nada, no se registra nada.
     *
     * @param objeto lo que se modifico, ej. "Cliente #2 'Finca El Rosario'"
     */
    public void registrarModificacion(Usuario usuario, String modulo, String objeto, List<String> cambios) {
        if (cambios == null || cambios.isEmpty()) {
            return;
        }
        registrarMovimiento(usuario, BitacoraMovimiento.MODIFICAR, modulo,
                objeto + ": " + String.join("; ", cambios));
    }

    /**
     * Si el texto cambio, agrega a la lista "campo: 'antes' -> 'despues'".
     * Vacio o null se muestra como (vacio).
     */
    public static void agregarCambio(List<String> cambios, String campo, String antes, String despues) {
        String a = antes == null ? "" : antes;
        String d = despues == null ? "" : despues;
        if (!a.equals(d)) {
            cambios.add(campo + ": " + entreComillas(a) + " -> " + entreComillas(d));
        }
    }

    /** Igual que el anterior, pero para dinero (se muestra como Q0.00). */
    public static void agregarCambio(List<String> cambios, String campo, double antes, double despues) {
        String a = quetzales(antes);
        String d = quetzales(despues);
        if (!a.equals(d)) {
            cambios.add(campo + ": " + a + " -> " + d);
        }
    }

    /** Formato fijo (punto decimal) sin importar la configuracion regional de la PC. */
    public static String quetzales(double monto) {
        return String.format(Locale.US, "Q%.2f", monto);
    }

    private static String entreComillas(String texto) {
        return texto.isEmpty() ? "(vacio)" : "'" + texto + "'";
    }

    public List<BitacoraMovimiento> listarTodos() {
        return bitacoraDb.listarTodos();
    }

    public List<BitacoraMovimiento> listarPorUsuario(String nombreUsuario) {
        return bitacoraDb.listarPorUsuario(nombreUsuario);
    }

    /** Imprime la bitacora en consola, del movimiento mas reciente al mas antiguo (opcion 9 del Administrador). */
    public void mostrarBitacora() {
        List<BitacoraMovimiento> lista = bitacoraDb.listarTodos();
        System.out.println("\n===== BITACORA DE MOVIMIENTOS DEL SISTEMA =====");
        if (lista.isEmpty()) {
            System.out.println("No hay movimientos registrados.");
            return;
        }
        System.out.println(String.format("%-4s | %-19s | %-14s | %-10s | %-11s | %s",
                "ID", "FECHA Y HORA", "USUARIO", "MOVIMIENTO", "MODULO", "RESUMEN"));
        System.out.println("-".repeat(110));

        long agregados = 0;
        long modificados = 0;
        long eliminados = 0;
        for (BitacoraMovimiento m : lista) {
            System.out.println(m);
            switch (m.getAccion()) {
                case BitacoraMovimiento.AGREGAR: agregados++; break;
                case BitacoraMovimiento.MODIFICAR: modificados++; break;
                case BitacoraMovimiento.ELIMINAR: eliminados++; break;
                default: break;
            }
        }
        System.out.println("-".repeat(110));
        System.out.println("Total: " + lista.size() + " movimientos (agregados: " + agregados
                + ", modificados: " + modificados + ", eliminados: " + eliminados + ")");
    }
}
