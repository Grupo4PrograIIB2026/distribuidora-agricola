package agro;

import java.awt.event.*;
import javax.swing.*;

public class MainFrame extends JFrame {
    public MainFrame() {
        super("Distribuidora Agrícola - " + Db.usuario + " (" + Db.rol + ")");
        boolean admin = Db.rol.equals("Administrador");
        boolean ven = admin || Db.rol.equals("Ventas"), bod = admin || Db.rol.equals("Bodega");
        String[] dir = {"nombre", "nit", "telefono", "direccion", "correo"};
        String[] dirL = {"Nombre", "NIT", "Teléfono", "Dirección", "Correo"};
        JTabbedPane t = new JTabbedPane();
        if (ven) t.add("Clientes", new CrudPanel("Cliente", "id_cliente", dir, dirL));
        if (bod) t.add("Proveedores", new CrudPanel("Proveedor", "id_proveedor", dir, dirL));
        if (bod) t.add("Productos", new CrudPanel("Producto", "id_producto",
                new String[]{"nombre", "categoria", "unidad", "precio_venta", "stock_minimo"},
                new String[]{"Nombre", "Categoría", "Unidad", "Precio venta", "Stock mínimo"}));
        if (bod) t.add("Compras", new ComprasPanel());
        if (ven || bod) t.add("Inventario", new ReadPanel("SELECT id_producto,nombre,categoria,existencia,stock_minimo,"
                + "CASE WHEN existencia<=stock_minimo THEN 'BAJO' ELSE 'OK' END AS estado FROM Producto ORDER BY nombre"));
        if (ven) t.add("Pedidos", new PedidosPanel());
        if (ven) t.add("Facturas", new ReadPanel("SELECT f.id_factura,f.id_pedido,c.nombre AS cliente,f.fecha,f.total "
                + "FROM Factura f JOIN Pedido p ON p.id_pedido=f.id_pedido JOIN Cliente c ON c.id_cliente=p.id_cliente ORDER BY 1 DESC"));
        if (admin) t.add("Usuarios", new UsuariosPanel());
        if (admin) t.add("Bitácora", new ReadPanel("SELECT id_bitacora,usuario,accion,resultado,fecha FROM BitacoraAcceso ORDER BY id_bitacora DESC"));
        t.addChangeListener(e -> { if (t.getSelectedComponent() instanceof Refrescable r) r.refrescar(); });
        add(t);
        setSize(1000, 600); setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { Db.log(Db.uid, Db.usuario, "LOGOUT", "Cierre de sesión"); System.exit(0); }
        });
    }
}
