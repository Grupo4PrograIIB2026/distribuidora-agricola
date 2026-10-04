package distribuidoraagricola.modelo;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Traduce entre el enum EstadoPedido y el texto que se guarda en la
 * columna 'estado' de Pedidos ('Registrado','Confirmado','Despachado',
 * 'Cancelado'). Es necesario porque @Enumerated(STRING) por si solo
 * guardaria el nombre literal de la constante de Java (REGISTRADO en
 * mayusculas), que no coincide con el CHECK constraint de la tabla.
 * Con autoApply=true se usa automaticamente en cualquier campo de
 * tipo EstadoPedido, sin anotar cada campo con @Convert.
 */
@Converter(autoApply = true)
public class EstadoPedidoConverter implements AttributeConverter<EstadoPedido, String> {

    @Override
    public String convertToDatabaseColumn(EstadoPedido estado) {
        return estado == null ? null : estado.getValor();
    }

    @Override
    public EstadoPedido convertToEntityAttribute(String valor) {
        return valor == null ? null : EstadoPedido.desdeTexto(valor);
    }
}
