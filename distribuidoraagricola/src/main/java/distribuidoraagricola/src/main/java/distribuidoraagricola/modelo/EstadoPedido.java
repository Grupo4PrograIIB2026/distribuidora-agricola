package distribuidoraagricola.modelo;

/**
 * Estados posibles de un pedido. El flujo normal es:
 *
 *   REGISTRADO -> CONFIRMADO -> DESPACHADO
 *        \-> CANCELADO <-/
 *
 * - Un pedido nace en REGISTRADO (se guardo el pedido y su detalle,
 *   todavia no afecta el inventario).
 * - Pasa a CONFIRMADO solo si hay existencia suficiente de todos los
 *   productos solicitados.
 * - Pasa a DESPACHADO cuando se descuenta el inventario y se genera
 *   la factura.
 * - Puede CANCELARSE mientras este en REGISTRADO o CONFIRMADO.
 */
public enum EstadoPedido {

    REGISTRADO("Registrado"),
    CONFIRMADO("Confirmado"),
    DESPACHADO("Despachado"),
    CANCELADO("Cancelado");

    private final String valor;

    EstadoPedido(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    public static EstadoPedido desdeTexto(String texto) {
        for (EstadoPedido estado : values()) {
            if (estado.valor.equalsIgnoreCase(texto)) {
                return estado;
            }
        }
        throw new IllegalArgumentException("Estado de pedido no reconocido: " + texto);
    }

    @Override
    public String toString() {
        return valor;
    }
}
