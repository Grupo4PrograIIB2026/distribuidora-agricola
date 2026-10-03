package distribuidoraagricola;

public class Main {

    public static void main(String[] args) {

        Producto fertilizante = new Producto(
                1,
                "Fertilizante 20-20-20",
                150.00,
                50
        );

        Producto maiz = new Producto(
                2,
                "Semilla de Maíz",
                85.00,
                100
        );

        Producto insecticida = new Producto(
                3,
                "Insecticida Agrícola",
                125.00,
                30
        );

        Cliente cliente = new Cliente(
                1,
                "Finca El Progreso",
                "5555-1234",
                "Amatitlán, Guatemala"
        );

        Proveedor proveedor = new Proveedor(
                1,
                "Carlos López",
                "4444-5678",
                "AgroProveedores Guatemala"
        );

        System.out.println(" PRODUCTOS ");

        System.out.println(fertilizante);
        System.out.println(maiz);
        System.out.println(insecticida);

        System.out.println("\n CLIENTE ");

        System.out.println(cliente);

        System.out.println("\n PROVEEDOR ");

        System.out.println(proveedor);

        Pedido pedido = new Pedido(1001, cliente);

        pedido.agregarProducto(fertilizante, 2);
        pedido.agregarProducto(maiz, 5);
        pedido.agregarProducto(insecticida, 1);

        pedido.mostrarPedido();

        pedido.cambiarEstado("CONFIRMADO");
        pedido.cambiarEstado("PREPARANDO");
        pedido.cambiarEstado("ENVIADO");

        pedido.mostrarPedido();

        System.out.println("\n STOCK ACTUAL ");

        System.out.println(fertilizante);
        System.out.println(maiz);
        System.out.println(insecticida);
    }
}