package distribuidoraagricola.vista;

import distribuidoraagricola.dao.ClienteDb;
import distribuidoraagricola.dao.CompraDb;
import distribuidoraagricola.dao.FacturaDb;
import distribuidoraagricola.dao.PedidoDb;
import distribuidoraagricola.dao.ProductoDb;
import distribuidoraagricola.dao.ProveedorDb;
import distribuidoraagricola.dao.RolDb;
import distribuidoraagricola.dao.UsuarioDb;
import distribuidoraagricola.modelo.BitacoraMovimiento;
import distribuidoraagricola.modelo.Cliente;
import distribuidoraagricola.modelo.Compra;
import distribuidoraagricola.modelo.DetalleCompra;
import distribuidoraagricola.modelo.DetallePedido;
import distribuidoraagricola.modelo.Factura;
import distribuidoraagricola.modelo.Pedido;
import distribuidoraagricola.modelo.ProductoAgricola;
import distribuidoraagricola.modelo.Proveedor;
import distribuidoraagricola.modelo.Rol;
import distribuidoraagricola.modelo.Usuario;
import distribuidoraagricola.servicio.BitacoraService;
import distribuidoraagricola.servicio.CompraService;
import distribuidoraagricola.servicio.PedidoService;
import distribuidoraagricola.servicio.UsuarioService;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Menu de consola que conecta al usuario con toda la funcionalidad del
 * sistema: clientes, proveedores, productos, compras, pedidos e
 * inventario.
 */
public class MenuPrincipal {

    private final Scanner sc = new Scanner(System.in, StandardCharsets.UTF_8);
    private final Usuario usuarioActual;

    private final ClienteDb clienteDb = new ClienteDb();
    private final ProveedorDb proveedorDb = new ProveedorDb();
    private final ProductoDb productoDb = new ProductoDb();
    private final CompraDb compraDb = new CompraDb();
    private final PedidoDb pedidoDb = new PedidoDb();
    private final FacturaDb facturaDb = new FacturaDb();
    private final RolDb rolDb = new RolDb();
    private final UsuarioDb usuarioDb = new UsuarioDb();

    private final CompraService compraService = new CompraService();
    private final PedidoService pedidoService = new PedidoService();
    private final UsuarioService usuarioService = new UsuarioService();
    private final BitacoraService bitacoraService = new BitacoraService();

    public MenuPrincipal(Usuario usuarioActual) {
        this.usuarioActual = usuarioActual;
    }

    public void iniciar() {
        if (esCliente()) {
            menuCliente();
        } else {
            menuStaff();
        }
    }

    private boolean esCliente() {
        return "Cliente".equalsIgnoreCase(usuarioActual.getNombreRol());
    }

    private boolean esAdministrador() {
        return "Administrador".equalsIgnoreCase(usuarioActual.getNombreRol());
    }

    /* =========================================================
       MENU DE STAFF (Administrador / Trabajador)
       ========================================================= */

    private void menuStaff() {
        int opcion;
        do {
            System.out.println("\n===== SISTEMA DISTRIBUIDORA AGRICOLA ("
                    + usuarioActual.getNombreUsuario() + " - " + usuarioActual.getNombreRol() + ") =====");
            System.out.println("1. Clientes");
            System.out.println("2. Proveedores");
            System.out.println("3. Productos agricolas");
            System.out.println("4. Compras a proveedores");
            System.out.println("5. Pedidos de clientes");
            System.out.println("6. Inventario");
            System.out.println("7. Facturas");
            if (esAdministrador()) {
                System.out.println("8. Gestion de usuarios");
                System.out.println("9. Bitacora de movimientos");
            }
            System.out.println("0. Salir");
            opcion = leerEntero("Seleccione una opcion: ");

            switch (opcion) {
                case 1:
                    menuClientes();
                    break;
                case 2:
                    menuProveedores();
                    break;
                case 3:
                    menuProductos();
                    break;
                case 4:
                    menuCompras();
                    break;
                case 5:
                    menuPedidos();
                    break;
                case 6:
                    menuInventario();
                    break;
                case 7:
                    menuFacturas();
                    break;
                case 8:
                    if (esAdministrador()) {
                        menuUsuarios();
                    } else {
                        System.out.println("Opcion invalida.");
                    }
                    break;
                case 9:
                    if (esAdministrador()) {
                        bitacoraService.mostrarBitacora();
                    } else {
                        System.out.println("Opcion invalida.");
                    }
                    break;
                case 0:
                    System.out.println("Saliendo del sistema...");
                    break;
                default:
                    System.out.println("Opcion invalida.");
            }
        } while (opcion != 0);
    }

    /* =========================================================
       CLIENTES
       ========================================================= */

    private void menuClientes() {
        int opcion;
        do {
            System.out.println("\n--- CLIENTES ---");
            System.out.println("1. Registrar cliente");
            System.out.println("2. Listar clientes");
            System.out.println("3. Actualizar cliente");
            System.out.println("4. Eliminar cliente");
            System.out.println("0. Volver al menu principal");
            opcion = leerEntero("Seleccione una opcion: ");

            switch (opcion) {
                case 1: registrarCliente(); break;
                case 2: listarClientes(); break;
                case 3: actualizarCliente(); break;
                case 4: eliminarCliente(); break;
                case 0: break;
                default: System.out.println("Opcion invalida.");
            }
        } while (opcion != 0);
    }

    private void registrarCliente() {
        System.out.println("\n-- Registrar cliente --");
        String nombre = leerTexto("Nombre: ");
        String direccion = leerTexto("Direccion: ");
        String telefono = leerTexto("Telefono: ");
        String correo = leerTexto("Correo: ");
        String nit = leerTexto("NIT: ");

        Cliente c = new Cliente(nombre, direccion, telefono, correo, nit);
        if (clienteDb.insertar(c)) {
            System.out.println("Cliente registrado correctamente.");
            bitacoraService.registrarMovimiento(usuarioActual, BitacoraMovimiento.AGREGAR,
                    BitacoraMovimiento.CLIENTES,
                    "Cliente #" + c.getIdCliente() + " '" + c.getNombre() + "' registrado");
        } else {
            System.out.println("No se pudo registrar el cliente.");
        }
    }

    private void listarClientes() {
        System.out.println("\n-- Lista de clientes --");
        List<Cliente> lista = clienteDb.listarTodos();
        if (lista.isEmpty()) {
            System.out.println("No hay clientes registrados.");
            return;
        }
        System.out.printf("%-4s %-28s %-14s %-25s%n", "ID", "NOMBRE", "TELEFONO", "CORREO");
        for (Cliente c : lista) {
            System.out.println(c);
        }
    }

    private void actualizarCliente() {
        System.out.println("\n-- Actualizar cliente --");
        listarClientes();
        int id = leerEntero("\nID del cliente a actualizar: ");
        Cliente c = clienteDb.buscarPorId(id);
        if (c == null) {
            System.out.println("No existe un cliente con ese ID.");
            return;
        }
        String etiqueta = "Cliente #" + id + " '" + c.getNombre() + "'";
        List<String> cambios = new ArrayList<>();
        System.out.println("Deje el campo en blanco para mantener el valor actual.");
        String nombre = leerTexto("Nombre [" + c.getNombre() + "]: ");
        if (!nombre.isBlank()) {
            BitacoraService.agregarCambio(cambios, "nombre", c.getNombre(), nombre);
            c.setNombre(nombre);
        }
        String direccion = leerTexto("Direccion [" + c.getDireccion() + "]: ");
        if (!direccion.isBlank()) {
            BitacoraService.agregarCambio(cambios, "direccion", c.getDireccion(), direccion);
            c.setDireccion(direccion);
        }
        String telefono = leerTexto("Telefono [" + c.getTelefono() + "]: ");
        if (!telefono.isBlank()) {
            BitacoraService.agregarCambio(cambios, "telefono", c.getTelefono(), telefono);
            c.setTelefono(telefono);
        }
        String correo = leerTexto("Correo [" + c.getCorreo() + "]: ");
        if (!correo.isBlank()) {
            BitacoraService.agregarCambio(cambios, "correo", c.getCorreo(), correo);
            c.setCorreo(correo);
        }
        String nit = leerTexto("NIT [" + c.getNit() + "]: ");
        if (!nit.isBlank()) {
            BitacoraService.agregarCambio(cambios, "nit", c.getNit(), nit);
            c.setNit(nit);
        }

        if (clienteDb.actualizar(c)) {
            System.out.println("Cliente actualizado correctamente.");
            bitacoraService.registrarModificacion(usuarioActual, BitacoraMovimiento.CLIENTES, etiqueta, cambios);
        } else {
            System.out.println("No se pudo actualizar el cliente.");
        }
    }

    private void eliminarCliente() {
        System.out.println("\n-- Eliminar cliente --");
        listarClientes();
        int id = leerEntero("\nID del cliente a eliminar: ");
        Cliente c = clienteDb.buscarPorId(id);
        if (c == null) {
            System.out.println("No existe un cliente con ese ID.");
            return;
        }
        String confirmacion = leerTexto("Seguro que desea eliminar el cliente " + id + "? (S/N): ");
        if (confirmacion.equalsIgnoreCase("S")) {
            if (clienteDb.eliminar(id)) {
                System.out.println("Cliente eliminado.");
                bitacoraService.registrarMovimiento(usuarioActual, BitacoraMovimiento.ELIMINAR,
                        BitacoraMovimiento.CLIENTES,
                        "Cliente #" + id + " '" + c.getNombre() + "' eliminado");
            } else {
                System.out.println("No se pudo eliminar el cliente.");
            }
        } else {
            System.out.println("Operacion cancelada.");
        }
    }

    /* =========================================================
       PROVEEDORES
       ========================================================= */

    private void menuProveedores() {
        int opcion;
        do {
            System.out.println("\n--- PROVEEDORES ---");
            System.out.println("1. Registrar proveedor");
            System.out.println("2. Listar proveedores");
            System.out.println("3. Actualizar proveedor");
            System.out.println("4. Eliminar proveedor");
            System.out.println("0. Volver al menu principal");
            opcion = leerEntero("Seleccione una opcion: ");

            switch (opcion) {
                case 1: registrarProveedor(); break;
                case 2: listarProveedores(); break;
                case 3: actualizarProveedor(); break;
                case 4: eliminarProveedor(); break;
                case 0: break;
                default: System.out.println("Opcion invalida.");
            }
        } while (opcion != 0);
    }

    private void registrarProveedor() {
        System.out.println("\n-- Registrar proveedor --");
        String nombre = leerTexto("Nombre: ");
        String direccion = leerTexto("Direccion: ");
        String telefono = leerTexto("Telefono: ");
        String correo = leerTexto("Correo: ");

        Proveedor p = new Proveedor(nombre, direccion, telefono, correo);
        if (proveedorDb.insertar(p)) {
            System.out.println("Proveedor registrado correctamente.");
            bitacoraService.registrarMovimiento(usuarioActual, BitacoraMovimiento.AGREGAR,
                    BitacoraMovimiento.PROVEEDORES,
                    "Proveedor #" + p.getIdProveedor() + " '" + p.getNombre() + "' registrado");
        } else {
            System.out.println("No se pudo registrar el proveedor.");
        }
    }

    private void listarProveedores() {
        System.out.println("\n-- Lista de proveedores --");
        List<Proveedor> lista = proveedorDb.listarTodos();
        if (lista.isEmpty()) {
            System.out.println("No hay proveedores registrados.");
            return;
        }
        System.out.printf("%-4s %-28s %-14s %-25s%n", "ID", "NOMBRE", "TELEFONO", "CORREO");
        for (Proveedor p : lista) {
            System.out.println(p);
        }
    }

    private void actualizarProveedor() {
        System.out.println("\n-- Actualizar proveedor --");
        listarProveedores();
        int id = leerEntero("\nID del proveedor a actualizar: ");
        Proveedor p = proveedorDb.buscarPorId(id);
        if (p == null) {
            System.out.println("No existe un proveedor con ese ID.");
            return;
        }
        String etiqueta = "Proveedor #" + id + " '" + p.getNombre() + "'";
        List<String> cambios = new ArrayList<>();
        System.out.println("Deje el campo en blanco para mantener el valor actual.");
        String nombre = leerTexto("Nombre [" + p.getNombre() + "]: ");
        if (!nombre.isBlank()) {
            BitacoraService.agregarCambio(cambios, "nombre", p.getNombre(), nombre);
            p.setNombre(nombre);
        }
        String direccion = leerTexto("Direccion [" + p.getDireccion() + "]: ");
        if (!direccion.isBlank()) {
            BitacoraService.agregarCambio(cambios, "direccion", p.getDireccion(), direccion);
            p.setDireccion(direccion);
        }
        String telefono = leerTexto("Telefono [" + p.getTelefono() + "]: ");
        if (!telefono.isBlank()) {
            BitacoraService.agregarCambio(cambios, "telefono", p.getTelefono(), telefono);
            p.setTelefono(telefono);
        }
        String correo = leerTexto("Correo [" + p.getCorreo() + "]: ");
        if (!correo.isBlank()) {
            BitacoraService.agregarCambio(cambios, "correo", p.getCorreo(), correo);
            p.setCorreo(correo);
        }

        if (proveedorDb.actualizar(p)) {
            System.out.println("Proveedor actualizado correctamente.");
            bitacoraService.registrarModificacion(usuarioActual, BitacoraMovimiento.PROVEEDORES, etiqueta, cambios);
        } else {
            System.out.println("No se pudo actualizar el proveedor.");
        }
    }

    private void eliminarProveedor() {
        System.out.println("\n-- Eliminar proveedor --");
        listarProveedores();
        int id = leerEntero("\nID del proveedor a eliminar: ");
        Proveedor p = proveedorDb.buscarPorId(id);
        if (p == null) {
            System.out.println("No existe un proveedor con ese ID.");
            return;
        }
        String confirmacion = leerTexto("Seguro que desea eliminar el proveedor " + id + "? (S/N): ");
        if (confirmacion.equalsIgnoreCase("S")) {
            if (proveedorDb.eliminar(id)) {
                System.out.println("Proveedor eliminado.");
                bitacoraService.registrarMovimiento(usuarioActual, BitacoraMovimiento.ELIMINAR,
                        BitacoraMovimiento.PROVEEDORES,
                        "Proveedor #" + id + " '" + p.getNombre() + "' eliminado");
            } else {
                System.out.println("No se pudo eliminar el proveedor.");
            }
        } else {
            System.out.println("Operacion cancelada.");
        }
    }

    /* =========================================================
       PRODUCTOS AGRICOLAS
       ========================================================= */

    private void menuProductos() {
        int opcion;
        do {
            System.out.println("\n--- PRODUCTOS AGRICOLAS ---");
            System.out.println("1. Registrar producto");
            System.out.println("2. Listar productos");
            System.out.println("3. Actualizar producto");
            System.out.println("4. Eliminar producto");
            System.out.println("0. Volver al menu principal");
            opcion = leerEntero("Seleccione una opcion: ");

            switch (opcion) {
                case 1: registrarProducto(); break;
                case 2: listarProductos(); break;
                case 3: actualizarProducto(); break;
                case 4: eliminarProducto(); break;
                case 0: break;
                default: System.out.println("Opcion invalida.");
            }
        } while (opcion != 0);
    }

    private void registrarProducto() {
        System.out.println("\n-- Registrar producto agricola --");
        String nombre = leerTexto("Nombre: ");
        String descripcion = leerTexto("Descripcion: ");
        String unidadMedida = leerTexto("Unidad de medida (saco, libra, litro, etc.): ");
        double precio = leerDouble("Precio unitario (Q): ");
        int existencia = leerEntero("Existencia inicial: ");

        ProductoAgricola p = new ProductoAgricola(nombre, descripcion, unidadMedida, precio, existencia);
        if (productoDb.insertar(p)) {
            System.out.println("Producto registrado correctamente.");
            bitacoraService.registrarMovimiento(usuarioActual, BitacoraMovimiento.AGREGAR,
                    BitacoraMovimiento.PRODUCTOS,
                    "Producto #" + p.getIdProducto() + " '" + p.getNombre() + "' registrado (precio "
                    + BitacoraService.quetzales(precio) + ", existencia inicial " + existencia + ")");
        } else {
            System.out.println("No se pudo registrar el producto.");
        }
    }

    private void listarProductos() {
        System.out.println("\n-- Lista de productos agricolas --");
        List<ProductoAgricola> lista = productoDb.listarTodos();
        if (lista.isEmpty()) {
            System.out.println("No hay productos registrados.");
            return;
        }
        System.out.printf("%-4s %-28s %-8s %-12s %s%n", "ID", "NOMBRE", "UNIDAD", "PRECIO", "EXISTENCIA");
        for (ProductoAgricola p : lista) {
            System.out.println(p);
        }
    }

    private void actualizarProducto() {
        System.out.println("\n-- Actualizar producto --");
        listarProductos();
        int id = leerEntero("\nID del producto a actualizar: ");
        ProductoAgricola p = productoDb.buscarPorId(id);
        if (p == null) {
            System.out.println("No existe un producto con ese ID.");
            return;
        }
        String etiqueta = "Producto #" + id + " '" + p.getNombre() + "'";
        List<String> cambios = new ArrayList<>();
        System.out.println("Deje el campo en blanco (o -1 en numeros) para mantener el valor actual.");
        String nombre = leerTexto("Nombre [" + p.getNombre() + "]: ");
        if (!nombre.isBlank()) {
            BitacoraService.agregarCambio(cambios, "nombre", p.getNombre(), nombre);
            p.setNombre(nombre);
        }
        String descripcion = leerTexto("Descripcion [" + p.getDescripcion() + "]: ");
        if (!descripcion.isBlank()) {
            BitacoraService.agregarCambio(cambios, "descripcion", p.getDescripcion(), descripcion);
            p.setDescripcion(descripcion);
        }
        String unidad = leerTexto("Unidad de medida [" + p.getUnidadMedida() + "]: ");
        if (!unidad.isBlank()) {
            BitacoraService.agregarCambio(cambios, "unidad de medida", p.getUnidadMedida(), unidad);
            p.setUnidadMedida(unidad);
        }
        double precio = leerDouble("Precio unitario [" + p.getPrecioUnitario() + "] (-1 para no cambiar): ");
        if (precio >= 0) {
            BitacoraService.agregarCambio(cambios, "precio", p.getPrecioUnitario(), precio);
            p.setPrecioUnitario(precio);
        }

        if (productoDb.actualizar(p)) {
            System.out.println("Producto actualizado correctamente. (Para cambiar existencia use Compras o Inventario)");
            bitacoraService.registrarModificacion(usuarioActual, BitacoraMovimiento.PRODUCTOS, etiqueta, cambios);
        } else {
            System.out.println("No se pudo actualizar el producto.");
        }
    }

    private void eliminarProducto() {
        System.out.println("\n-- Eliminar producto --");
        listarProductos();
        int id = leerEntero("\nID del producto a eliminar: ");
        ProductoAgricola p = productoDb.buscarPorId(id);
        if (p == null) {
            System.out.println("No existe un producto con ese ID.");
            return;
        }
        String confirmacion = leerTexto("Seguro que desea eliminar el producto " + id + "? (S/N): ");
        if (confirmacion.equalsIgnoreCase("S")) {
            if (productoDb.eliminar(id)) {
                System.out.println("Producto eliminado.");
                bitacoraService.registrarMovimiento(usuarioActual, BitacoraMovimiento.ELIMINAR,
                        BitacoraMovimiento.PRODUCTOS,
                        "Producto #" + id + " '" + p.getNombre() + "' eliminado (existencia al eliminar: "
                        + p.getExistencia() + ")");
            } else {
                System.out.println("No se pudo eliminar el producto.");
            }
        } else {
            System.out.println("Operacion cancelada.");
        }
    }

    /* =========================================================
       COMPRAS A PROVEEDORES
       ========================================================= */

    private void menuCompras() {
        int opcion;
        do {
            System.out.println("\n--- COMPRAS A PROVEEDORES ---");
            System.out.println("1. Registrar compra");
            System.out.println("2. Listar compras");
            System.out.println("0. Volver al menu principal");
            opcion = leerEntero("Seleccione una opcion: ");

            switch (opcion) {
                case 1: registrarCompra(); break;
                case 2: listarCompras(); break;
                case 0: break;
                default: System.out.println("Opcion invalida.");
            }
        } while (opcion != 0);
    }

    private void registrarCompra() {
        System.out.println("\n-- Registrar compra --");
        listarProveedores();
        int idProveedor = leerEntero("\nID del proveedor: ");
        if (!proveedorDb.existeId(idProveedor)) {
            System.out.println("No existe un proveedor con ese ID. Operacion cancelada.");
            return;
        }

        Compra compra = new Compra(idProveedor);
        boolean agregarMas = true;
        while (agregarMas) {
            listarProductos();
            int idProducto = leerEntero("\nID del producto a comprar: ");
            ProductoAgricola producto = productoDb.buscarPorId(idProducto);
            if (producto == null) {
                System.out.println("No existe un producto con ese ID.");
            } else {
                int cantidad = leerEntero("Cantidad: ");
                double precioUnitario = leerDouble("Precio unitario pagado al proveedor (Q): ");
                if (cantidad > 0 && precioUnitario >= 0) {
                    DetalleCompra d = new DetalleCompra(idProducto, producto.getNombre(), cantidad, precioUnitario);
                    compra.agregarDetalle(d);
                    System.out.println("Agregado: " + d);
                } else {
                    System.out.println("Cantidad y precio deben ser mayores o iguales a cero.");
                }
            }
            String resp = leerTexto("Agregar otro producto a la compra? (S/N): ");
            agregarMas = resp.equalsIgnoreCase("S");
        }

        if (compra.getDetalle().isEmpty()) {
            System.out.println("La compra no tiene productos, se cancela.");
            return;
        }

        System.out.println(compraService.registrarCompra(compra, usuarioActual));
    }

    private void listarCompras() {
        System.out.println("\n-- Lista de compras --");
        List<Compra> lista = compraDb.listarTodas();
        if (lista.isEmpty()) {
            System.out.println("No hay compras registradas.");
            return;
        }
        for (Compra c : lista) {
            System.out.printf("Compra #%d | Proveedor: %s | Fecha: %s | Total: Q%.2f%n",
                    c.getIdCompra(), c.getNombreProveedor(), c.getFechaCompra(), c.getTotal());
            for (DetalleCompra d : compraDb.listarDetalle(c.getIdCompra())) {
                System.out.println("    " + d);
            }
        }
    }

    /* =========================================================
       PEDIDOS DE CLIENTES
       ========================================================= */

    private void menuPedidos() {
        int opcion;
        do {
            System.out.println("\n--- PEDIDOS DE CLIENTES ---");
            System.out.println("1. Registrar pedido");
            System.out.println("2. Confirmar pedido");
            System.out.println("3. Despachar pedido");
            System.out.println("4. Cancelar pedido");
            System.out.println("5. Listar pedidos");
            System.out.println("6. Ver detalle de un pedido");
            System.out.println("0. Volver al menu principal");
            opcion = leerEntero("Seleccione una opcion: ");

            switch (opcion) {
                case 1: registrarPedido(); break;
                case 2: cambiarEstadoPedido(1); break;
                case 3: cambiarEstadoPedido(2); break;
                case 4: cambiarEstadoPedido(3); break;
                case 5: listarPedidos(); break;
                case 6: verDetallePedido(); break;
                case 0: break;
                default: System.out.println("Opcion invalida.");
            }
        } while (opcion != 0);
    }

    private void registrarPedido() {
        System.out.println("\n-- Registrar pedido --");
        listarClientes();
        int idCliente = leerEntero("\nID del cliente: ");
        if (!clienteDb.existeId(idCliente)) {
            System.out.println("No existe un cliente con ese ID. Operacion cancelada.");
            return;
        }

        Pedido pedido = new Pedido(idCliente);
        boolean agregarMas = true;
        while (agregarMas) {
            listarProductos();
            int idProducto = leerEntero("\nID del producto a pedir: ");
            ProductoAgricola producto = productoDb.buscarPorId(idProducto);
            if (producto == null) {
                System.out.println("No existe un producto con ese ID.");
            } else {
                int cantidad = leerEntero("Cantidad solicitada: ");
                if (cantidad > 0) {
                    DetallePedido d = new DetallePedido(idProducto, producto.getNombre(),
                            cantidad, producto.getPrecioUnitario());
                    pedido.agregarDetalle(d);
                    System.out.println("Agregado: " + d
                            + " (existencia actual: " + producto.getExistencia() + ")");
                } else {
                    System.out.println("La cantidad debe ser mayor a cero.");
                }
            }
            String resp = leerTexto("Agregar otro producto al pedido? (S/N): ");
            agregarMas = resp.equalsIgnoreCase("S");
        }

        if (pedido.getDetalle().isEmpty()) {
            System.out.println("El pedido no tiene productos, se cancela.");
            return;
        }

        int idPedido = pedidoService.registrarPedido(pedido, usuarioActual);
        if (idPedido > 0) {
            System.out.println("Pedido #" + idPedido + " registrado con estado Registrado.");
            System.out.println("Recuerda confirmarlo y despacharlo desde este menu.");
        } else {
            System.out.println("No se pudo registrar el pedido.");
        }
    }

    /**
     * accion: 1 = confirmar, 2 = despachar, 3 = cancelar.
     * Se centraliza aqui porque las tres operaciones piden lo mismo
     * (el id del pedido) y solo delegan a un metodo distinto del servicio.
     */
    private void cambiarEstadoPedido(int accion) {
        listarPedidos();
        int idPedido = leerEntero("\nID del pedido: ");
        String resultado;
        switch (accion) {
            case 1:
                resultado = pedidoService.confirmarPedido(idPedido, usuarioActual);
                break;
            case 2:
                resultado = pedidoService.despacharPedido(idPedido, usuarioActual);
                break;
            case 3:
                resultado = pedidoService.cancelarPedido(idPedido, usuarioActual);
                break;
            default:
                resultado = "Accion no reconocida.";
        }
        System.out.println(resultado);
    }

    private void listarPedidos() {
        System.out.println("\n-- Lista de pedidos --");
        List<Pedido> lista = pedidoDb.listarTodos();
        if (lista.isEmpty()) {
            System.out.println("No hay pedidos registrados.");
            return;
        }
        System.out.printf("%-4s %-25s %-20s %-12s %s%n", "ID", "CLIENTE", "FECHA", "ESTADO", "TOTAL");
        for (Pedido p : lista) {
            System.out.printf("%-4d %-25s %-20s %-12s Q%.2f%n",
                    p.getIdPedido(), p.getNombreCliente(), p.getFechaPedido(), p.getEstado(), p.getTotal());
        }
    }

    private void verDetallePedido() {
        listarPedidos();
        int idPedido = leerEntero("\nID del pedido a consultar: ");
        Pedido p = pedidoDb.buscarPorId(idPedido);
        if (p == null) {
            System.out.println("No existe un pedido con ese ID.");
            return;
        }
        System.out.println("\nPedido #" + p.getIdPedido());
        System.out.println("Cliente: " + p.getNombreCliente());
        System.out.println("Fecha: " + p.getFechaPedido());
        System.out.println("Estado: " + p.getEstado());
        System.out.println("Detalle:");
        for (DetallePedido d : p.getDetalle()) {
            System.out.println("    " + d);
        }
        System.out.printf("Total: Q%.2f%n", p.getTotal());

        if (p.getEstado().name().equals("DESPACHADO")) {
            Factura f = facturaDb.buscarPorPedido(idPedido);
            if (f != null) {
                System.out.println("\n--- FACTURA #" + f.getIdFactura() + " ---");
                System.out.println("Fecha: " + f.getFechaFactura());
                System.out.printf("Total: Q%.2f%n", f.getTotal());
            }
        }
    }

    /* =========================================================
       INVENTARIO
       ========================================================= */

    private void menuInventario() {
        int opcion;
        do {
            System.out.println("\n--- INVENTARIO ---");
            System.out.println("1. Ver existencias de todos los productos");
            System.out.println("2. Consultar existencia de un producto");
            System.out.println("0. Volver al menu principal");
            opcion = leerEntero("Seleccione una opcion: ");

            switch (opcion) {
                case 1: listarProductos(); break;
                case 2: consultarExistenciaProducto(); break;
                case 0: break;
                default: System.out.println("Opcion invalida.");
            }
        } while (opcion != 0);
    }

    private void consultarExistenciaProducto() {
        listarProductos();
        int idProducto = leerEntero("\nID del producto a consultar: ");
        int existencia = productoDb.consultarExistencia(idProducto);
        if (existencia < 0) {
            System.out.println("No existe un producto con ese ID.");
        } else {
            System.out.println("Existencia actual: " + existencia);
        }
    }

    /* =========================================================
       FACTURAS
       ========================================================= */

    private void menuFacturas() {
        int opcion;
        do {
            System.out.println("\n--- FACTURAS ---");
            System.out.println("1. Listar todas las facturas");
            System.out.println("2. Consultar factura por numero de pedido");
            System.out.println("0. Volver al menu principal");
            opcion = leerEntero("Seleccione una opcion: ");

            switch (opcion) {
                case 1: listarFacturas(); break;
                case 2: consultarFacturaPorPedido(); break;
                case 0: break;
                default: System.out.println("Opcion invalida.");
            }
        } while (opcion != 0);
    }

    private void listarFacturas() {
        System.out.println("\n-- Lista de facturas --");
        List<Factura> lista = facturaDb.listarTodas();
        if (lista.isEmpty()) {
            System.out.println("No hay facturas generadas todavia (se generan al despachar un pedido).");
            return;
        }
        System.out.printf("%-4s %-10s %-25s %-20s %s%n", "ID", "PEDIDO", "CLIENTE", "FECHA", "TOTAL");
        for (Factura f : lista) {
            System.out.printf("%-4d %-10d %-25s %-20s Q%.2f%n",
                    f.getIdFactura(), f.getIdPedido(), f.getNombreCliente(), f.getFechaFactura(), f.getTotal());
        }
    }

    private void consultarFacturaPorPedido() {
        int idPedido = leerEntero("\nNumero de pedido: ");
        Factura f = facturaDb.buscarPorPedido(idPedido);
        if (f == null) {
            System.out.println("Ese pedido no tiene factura (verifica el numero o si ya fue despachado).");
            return;
        }
        System.out.println("\n--- FACTURA #" + f.getIdFactura() + " ---");
        System.out.println("Pedido: #" + f.getIdPedido());
        System.out.println("Cliente: " + f.getNombreCliente());
        System.out.println("Fecha: " + f.getFechaFactura());
        System.out.printf("Total: Q%.2f%n", f.getTotal());
    }

    /* =========================================================
       PORTAL DEL CLIENTE (rol Cliente: acceso restringido a
       sus propios pedidos y facturas)
       ========================================================= */

    private void menuCliente() {
        int opcion;
        do {
            System.out.println("\n===== PORTAL DEL CLIENTE (" + usuarioActual.getNombreUsuario() + ") =====");
            System.out.println("1. Ver catalogo de productos");
            System.out.println("2. Registrar un pedido");
            System.out.println("3. Ver mis pedidos");
            System.out.println("4. Ver detalle de uno de mis pedidos");
            System.out.println("5. Ver mis facturas");
            System.out.println("0. Salir");
            opcion = leerEntero("Seleccione una opcion: ");

            switch (opcion) {
                case 1: listarProductos(); break;
                case 2: registrarPedidoPropio(); break;
                case 3: listarMisPedidos(); break;
                case 4: verDetalleMiPedido(); break;
                case 5: listarMisFacturas(); break;
                case 0: System.out.println("Saliendo del sistema..."); break;
                default: System.out.println("Opcion invalida.");
            }
        } while (opcion != 0);
    }

    private void registrarPedidoPropio() {
        System.out.println("\n-- Registrar pedido --");
        int idCliente = usuarioActual.getIdCliente();

        Pedido pedido = new Pedido(idCliente);
        boolean agregarMas = true;
        while (agregarMas) {
            listarProductos();
            int idProducto = leerEntero("\nID del producto a pedir: ");
            ProductoAgricola producto = productoDb.buscarPorId(idProducto);
            if (producto == null) {
                System.out.println("No existe un producto con ese ID.");
            } else {
                int cantidad = leerEntero("Cantidad solicitada: ");
                if (cantidad > 0) {
                    DetallePedido d = new DetallePedido(idProducto, producto.getNombre(),
                            cantidad, producto.getPrecioUnitario());
                    pedido.agregarDetalle(d);
                    System.out.println("Agregado: " + d);
                } else {
                    System.out.println("La cantidad debe ser mayor a cero.");
                }
            }
            String resp = leerTexto("Agregar otro producto al pedido? (S/N): ");
            agregarMas = resp.equalsIgnoreCase("S");
        }

        if (pedido.getDetalle().isEmpty()) {
            System.out.println("El pedido no tiene productos, se cancela.");
            return;
        }

        int idPedido = pedidoService.registrarPedido(pedido, usuarioActual);
        if (idPedido > 0) {
            System.out.println("Pedido #" + idPedido + " registrado. "
                    + "Queda pendiente de confirmacion por parte de la distribuidora.");
        } else {
            System.out.println("No se pudo registrar el pedido.");
        }
    }

    private void listarMisPedidos() {
        System.out.println("\n-- Mis pedidos --");
        List<Pedido> lista = pedidoDb.listarPorCliente(usuarioActual.getIdCliente());
        if (lista.isEmpty()) {
            System.out.println("Todavia no tienes pedidos registrados.");
            return;
        }
        System.out.printf("%-4s %-20s %-12s %s%n", "ID", "FECHA", "ESTADO", "TOTAL");
        for (Pedido p : lista) {
            System.out.printf("%-4d %-20s %-12s Q%.2f%n",
                    p.getIdPedido(), p.getFechaPedido(), p.getEstado(), p.getTotal());
        }
    }

    private void verDetalleMiPedido() {
        listarMisPedidos();
        int idPedido = leerEntero("\nID del pedido a consultar: ");
        Pedido p = pedidoDb.buscarPorId(idPedido);
        if (p == null || p.getIdCliente() != usuarioActual.getIdCliente()) {
            System.out.println("No existe un pedido con ese ID a tu nombre.");
            return;
        }
        System.out.println("\nPedido #" + p.getIdPedido());
        System.out.println("Fecha: " + p.getFechaPedido());
        System.out.println("Estado: " + p.getEstado());
        System.out.println("Detalle:");
        for (DetallePedido d : p.getDetalle()) {
            System.out.println("    " + d);
        }
        System.out.printf("Total: Q%.2f%n", p.getTotal());
    }

    private void listarMisFacturas() {
        System.out.println("\n-- Mis facturas --");
        List<Factura> lista = facturaDb.listarPorCliente(usuarioActual.getIdCliente());
        if (lista.isEmpty()) {
            System.out.println("Todavia no tienes facturas generadas.");
            return;
        }
        System.out.printf("%-4s %-10s %-20s %s%n", "ID", "PEDIDO", "FECHA", "TOTAL");
        for (Factura f : lista) {
            System.out.printf("%-4d %-10d %-20s Q%.2f%n",
                    f.getIdFactura(), f.getIdPedido(), f.getFechaFactura(), f.getTotal());
        }
    }

    /* =========================================================
       GESTION DE USUARIOS (solo Administrador)
       ========================================================= */

    private void menuUsuarios() {
        int opcion;
        do {
            System.out.println("\n--- GESTION DE USUARIOS ---");
            System.out.println("1. Crear usuario");
            System.out.println("2. Listar usuarios");
            System.out.println("3. Cambiar contrasenia de un usuario");
            System.out.println("4. Activar/Desactivar usuario");
            System.out.println("0. Volver al menu principal");
            opcion = leerEntero("Seleccione una opcion: ");

            switch (opcion) {
                case 1: crearUsuario(); break;
                case 2: listarUsuarios(); break;
                case 3: cambiarPasswordUsuario(); break;
                case 4: cambiarEstadoUsuario(); break;
                case 0: break;
                default: System.out.println("Opcion invalida.");
            }
        } while (opcion != 0);
    }

    private void crearUsuario() {
        System.out.println("\n-- Crear usuario --");
        String nombreUsuario = leerTexto("Nombre de usuario: ");
        String contrasenia = leerTexto("Contrasenia (minimo 4 caracteres): ");

        List<Rol> roles = rolDb.listarTodos();
        System.out.println("Roles disponibles:");
        for (Rol r : roles) {
            System.out.println("  " + r.getIdRol() + ". " + r.getNombreRol() + " - " + r.getDescripcion());
        }
        int idRol = leerEntero("ID del rol: ");
        Rol rolElegido = null;
        for (Rol r : roles) {
            if (r.getIdRol() == idRol) {
                rolElegido = r;
                break;
            }
        }
        if (rolElegido == null) {
            System.out.println("No existe un rol con ese ID.");
            return;
        }

        Integer idCliente = null;
        if ("Cliente".equalsIgnoreCase(rolElegido.getNombreRol())) {
            listarClientes();
            int idClienteIngresado = leerEntero("ID del cliente al que pertenece esta cuenta: ");
            if (!clienteDb.existeId(idClienteIngresado)) {
                System.out.println("No existe un cliente con ese ID. Operacion cancelada.");
                return;
            }
            idCliente = idClienteIngresado;
        }

        String resultado = usuarioService.crearUsuario(
                nombreUsuario, contrasenia, idRol, idCliente, rolElegido.getNombreRol(), usuarioActual);
        System.out.println(resultado);
    }

    private void listarUsuarios() {
        System.out.println("\n-- Lista de usuarios --");
        List<Usuario> lista = usuarioDb.listarTodos();
        if (lista.isEmpty()) {
            System.out.println("No hay usuarios registrados.");
            return;
        }
        System.out.printf("%-4s %-20s %-15s %-10s %s%n", "ID", "USUARIO", "ROL", "ID CLIENTE", "ESTADO");
        for (Usuario u : lista) {
            System.out.printf("%-4d %-20s %-15s %-10s %s%n",
                    u.getIdUsuario(), u.getNombreUsuario(), u.getNombreRol(),
                    u.getIdCliente() == null ? "-" : u.getIdCliente().toString(),
                    u.isEstado() ? "Activo" : "Inactivo");
        }
    }

    private void cambiarPasswordUsuario() {
        listarUsuarios();
        int idUsuario = leerEntero("\nID del usuario: ");
        String nuevaContrasenia = leerTexto("Nueva contrasenia (minimo 4 caracteres): ");
        System.out.println(usuarioService.cambiarPassword(idUsuario, nuevaContrasenia, usuarioActual));
    }

    private void cambiarEstadoUsuario() {
        listarUsuarios();
        int idUsuario = leerEntero("\nID del usuario: ");
        String resp = leerTexto("Activar (A) o Desactivar (D)? ");
        boolean nuevoEstado = resp.equalsIgnoreCase("A");
        System.out.println(usuarioService.cambiarEstado(idUsuario, nuevoEstado, usuarioActual));
    }

    /* =========================================================
       UTILIDADES DE LECTURA DE DATOS
       ========================================================= */

    private int leerEntero(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String texto = sc.nextLine().trim();
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                System.out.println("Debe ingresar un numero entero valido.");
            }
        }
    }

    private double leerDouble(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String texto = sc.nextLine().trim();
            try {
                return Double.parseDouble(texto);
            } catch (NumberFormatException e) {
                System.out.println("Debe ingresar un numero valido.");
            }
        }
    }

    private String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return sc.nextLine().trim();
    }
}
