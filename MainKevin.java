package distribuidoraagricola;

/** Demostracion de: existencias automaticas, registro de pedidos y factura. */
public class MainKevin {
    public static void main(String[] args) {
        Producto p1 = new Producto(1, "Fertilizante 20-20-20", 150.00, 50);
        Producto p2 = new Producto(2, "Semilla de Maiz", 85.00, 100);
        Cliente cliente = new Cliente(1, "Finca El Progreso", "5555-1234", "Amatitlan, Guatemala");
        Proveedor proveedor = new Proveedor(1, "Carlos Lopez", "4444-5678", "AgroProveedores Guatemala");
        GestorPedidos gestor = new GestorPedidos();

        System.out.println("\n===== 1. COMPRA A PROVEEDOR (sube el stock) =====");
        System.out.println("Stock inicial " + p1.getNombre() + ": " + p1.getStock());
        Compra compra = new Compra(1, proveedor);
        compra.agregarProducto(p1, 20, 120.00);
        compra.agregarProducto(p2, 30, 60.00);
        compra.mostrarCompra();

        System.out.println("\n===== 2. REGISTRO DE PEDIDO (baja el stock) =====");
        Pedido pedido = new Pedido(1, cliente);
        pedido.agregarProducto(p1, 4);
        pedido.agregarProducto(p2, 10);
        pedido.mostrarPedido();
        System.out.println("Stock " + p1.getNombre() + ": " + p1.getStock());

        System.out.println("\n===== 3. CONFIRMAR Y COMPLETAR (genera factura) =====");
        gestor.confirmarPedido(pedido);
        pedido.cambiarEstado("PREPARANDO");
        pedido.cambiarEstado("ENVIADO");
        Factura factura = gestor.completarPedido(pedido);
        if (factura != null) factura.mostrarFactura();

        System.out.println("\n===== 4. PEDIDO CANCELADO (devuelve el stock) =====");
        Pedido pedido2 = new Pedido(2, cliente);
        pedido2.agregarProducto(p1, 5);
        System.out.println("Stock antes de cancelar: " + p1.getStock());
        gestor.cancelarPedido(pedido2);
        System.out.println("Stock despues de cancelar: " + p1.getStock());

        gestor.mostrarFacturas();

        System.out.println("\n STOCK ACTUAL");
        System.out.println(p1);
        System.out.println(p2);
    }
}
