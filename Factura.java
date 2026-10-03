package distribuidoraagricola;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Factura generada cuando un pedido se completa (estado ENTREGADO). */
public class Factura {
    private final int id;
    private final Pedido pedido;
    private final LocalDateTime fecha;
    private final double total;

    public Factura(int id, Pedido pedido) {
        this.id = id;
        this.pedido = pedido;
        this.fecha = LocalDateTime.now();
        this.total = pedido.calcularTotal();
    }

    public int getId() { return id; }
    public Pedido getPedido() { return pedido; }
    public double getTotal() { return total; }

    public void mostrarFactura() {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        System.out.println("\n==============================================");
        System.out.println("      DISTRIBUIDORA AGRICOLA - FACTURA");
        System.out.println("==============================================");
        System.out.println("Factura No.: " + id + "    Fecha: " + fecha.format(f));
        System.out.println("Pedido No. : " + pedido.getId());
        System.out.println("Cliente    : " + pedido.getCliente().getNombre());
        System.out.println("----------------------------------------------");
        for (DetallePedido d : pedido.getDetalles()) {
            System.out.println(d);
        }
        System.out.println("----------------------------------------------");
        System.out.printf("TOTAL: Q%.2f%n", total);
        System.out.println("==============================================");
    }
}
