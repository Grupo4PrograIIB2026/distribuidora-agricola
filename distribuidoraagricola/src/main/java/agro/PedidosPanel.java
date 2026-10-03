package agro;

import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Pedidos: registro, detalle, validación de existencias, estados y facturación. */
class PedidosPanel extends JPanel implements Refrescable {
    final JComboBox<Item> cli = new JComboBox<>(), pro = new JComboBox<>();
    final JSpinner qty = new JSpinner(new SpinnerNumberModel(1, 1, 100000, 1));
    final DefaultTableModel lines = new DefaultTableModel(new String[]{"id", "Producto", "Cantidad", "Precio", "Subtotal"}, 0) {
        public boolean isCellEditable(int r, int c) { return false; } };
    final JTable tl = new JTable(lines), tp = new JTable();
    final JLabel total = new JLabel("Total: 0.00");

    PedidosPanel() {
        setLayout(new BorderLayout(5, 5));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Cliente:")); top.add(cli); top.add(new JLabel("Producto:")); top.add(pro);
        top.add(new JLabel("Cant.:")); top.add(qty);
        top.add(Db.btn("Agregar línea", e -> agregar())); top.add(Db.btn("Quitar línea", e -> quitar()));
        top.add(Db.btn("Registrar pedido", e -> registrar())); top.add(total);
        JPanel bot = new JPanel();
        bot.add(Db.btn("Confirmar", e -> accion("CONFIRMAR")));
        bot.add(Db.btn("Despachar (genera factura)", e -> accion("DESPACHAR")));
        bot.add(Db.btn("Cancelar pedido", e -> accion("CANCELAR")));
        bot.add(Db.btn("Ver detalle", e -> detalle()));
        bot.add(Db.btn("Actualizar", e -> refrescar()));
        JPanel south = new JPanel(new BorderLayout());
        south.add(new JScrollPane(tp), BorderLayout.CENTER); south.add(bot, BorderLayout.SOUTH);
        JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(tl), south); sp.setResizeWeight(0.35);
        add(top, BorderLayout.NORTH); add(sp, BorderLayout.CENTER);
        refrescar();
    }

    public void refrescar() {
        try {
            Db.fill(cli, "SELECT id_cliente,nombre FROM Cliente ORDER BY nombre");
            Db.fill(pro, "SELECT id_producto,nombre FROM Producto ORDER BY nombre");
            tp.setModel(Db.model("SELECT p.id_pedido,c.nombre AS cliente,p.fecha,p.estado,p.total FROM Pedido p JOIN Cliente c ON c.id_cliente=p.id_cliente ORDER BY 1 DESC"));
        } catch (Exception e) { Db.err(this, e); }
    }

    void agregar() {
        Item p = (Item) pro.getSelectedItem(); if (p == null) return;
        int q = (Integer) qty.getValue();
        try {
            BigDecimal pr = (BigDecimal) Db.model("SELECT precio_venta FROM Producto WHERE id_producto=?", p.id()).getValueAt(0, 0);
            for (int i = 0; i < lines.getRowCount(); i++)
                if ((Integer) lines.getValueAt(i, 0) == p.id()) { q += (Integer) lines.getValueAt(i, 2); lines.removeRow(i); break; }
            lines.addRow(new Object[]{p.id(), p.nombre(), q, pr, pr.multiply(BigDecimal.valueOf(q))});
            upd();
        } catch (Exception e) { Db.err(this, e); }
    }

    void quitar() { int r = tl.getSelectedRow(); if (r >= 0) { lines.removeRow(r); upd(); } }

    void upd() {
        BigDecimal s = BigDecimal.ZERO;
        for (int i = 0; i < lines.getRowCount(); i++) s = s.add((BigDecimal) lines.getValueAt(i, 4));
        total.setText("Total: " + s);
    }

    void registrar() {
        if (lines.getRowCount() == 0 || cli.getSelectedItem() == null) { JOptionPane.showMessageDialog(this, "Seleccione cliente y agregue productos"); return; }
        try (Connection c = Db.get()) {
            c.setAutoCommit(false);
            try {
                BigDecimal tot = BigDecimal.ZERO; int idp;
                for (int i = 0; i < lines.getRowCount(); i++) tot = tot.add((BigDecimal) lines.getValueAt(i, 4));
                try (PreparedStatement ps = c.prepareStatement("INSERT INTO Pedido(id_cliente,estado,total,id_usuario) VALUES(?,'REGISTRADO',?,?)", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, ((Item) cli.getSelectedItem()).id()); ps.setBigDecimal(2, tot); ps.setInt(3, Db.uid);
                    ps.executeUpdate(); ResultSet k = ps.getGeneratedKeys(); k.next(); idp = k.getInt(1);
                }
                try (PreparedStatement ps = c.prepareStatement("INSERT INTO DetallePedido(id_pedido,id_producto,cantidad,precio_unitario) VALUES(?,?,?,?)")) {
                    for (int i = 0; i < lines.getRowCount(); i++) {
                        ps.setInt(1, idp); ps.setInt(2, (Integer) lines.getValueAt(i, 0)); ps.setInt(3, (Integer) lines.getValueAt(i, 2));
                        ps.setBigDecimal(4, (BigDecimal) lines.getValueAt(i, 3)); ps.addBatch();
                    }
                    ps.executeBatch();
                }
                c.commit();
            } catch (Exception e) { c.rollback(); throw e; }
            lines.setRowCount(0); upd(); refrescar();
        } catch (Exception e) { Db.err(this, e); }
    }

    void accion(String a) {
        int r = tp.getSelectedRow(); if (r < 0) { JOptionPane.showMessageDialog(this, "Seleccione un pedido"); return; }
        int id = (Integer) tp.getValueAt(r, 0); String est = (String) tp.getValueAt(r, 3);
        try (Connection c = Db.get()) {
            c.setAutoCommit(false);
            try {
                switch (a) {
                    case "CONFIRMAR" -> { exigir(est, "REGISTRADO"); faltante(c, id); run(c, "UPDATE Pedido SET estado='CONFIRMADO' WHERE id_pedido=?", id); }
                    case "DESPACHAR" -> {
                        exigir(est, "CONFIRMADO"); faltante(c, id);
                        // Descuenta existencias automáticamente
                        run(c, "UPDATE p SET p.existencia=p.existencia-d.cantidad FROM Producto p JOIN DetallePedido d ON d.id_producto=p.id_producto WHERE d.id_pedido=?", id);
                        run(c, "INSERT INTO Factura(id_pedido,total) SELECT id_pedido,total FROM Pedido WHERE id_pedido=?", id);
                        run(c, "UPDATE Pedido SET estado='DESPACHADO' WHERE id_pedido=?", id);
                    }
                    default -> {
                        if (est.equals("DESPACHADO") || est.equals("CANCELADO")) throw new Exception("No se puede cancelar un pedido " + est);
                        run(c, "UPDATE Pedido SET estado='CANCELADO' WHERE id_pedido=?", id);
                    }
                }
                c.commit();
            } catch (Exception e) { c.rollback(); throw e; }
        } catch (Exception e) { Db.err(this, e); }
        refrescar();
    }

    void exigir(String actual, String esperado) throws Exception {
        if (!actual.equals(esperado)) throw new Exception("El pedido debe estar en estado " + esperado + " (actual: " + actual + ")");
    }

    /** Validación de existencia antes de confirmar/despachar. */
    void faltante(Connection c, int id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT TOP 1 p.nombre,p.existencia FROM DetallePedido d JOIN Producto p ON p.id_producto=d.id_producto WHERE d.id_pedido=? AND p.existencia<d.cantidad")) {
            ps.setInt(1, id); ResultSet rs = ps.executeQuery();
            if (rs.next()) throw new SQLException("Existencia insuficiente de " + rs.getString(1) + " (disponible: " + rs.getInt(2) + ")");
        }
    }

    static void run(Connection c, String sql, Object... p) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) { Db.bind(ps, p); ps.executeUpdate(); }
    }

    void detalle() {
        int r = tp.getSelectedRow(); if (r < 0) return;
        try {
            JTable d = new JTable(Db.model("SELECT pr.nombre AS producto,d.cantidad,d.precio_unitario,d.cantidad*d.precio_unitario AS subtotal FROM DetallePedido d JOIN Producto pr ON pr.id_producto=d.id_producto WHERE d.id_pedido=?", tp.getValueAt(r, 0)));
            JScrollPane sc = new JScrollPane(d); sc.setPreferredSize(new Dimension(500, 200));
            JOptionPane.showMessageDialog(this, sc, "Detalle del pedido " + tp.getValueAt(r, 0), JOptionPane.PLAIN_MESSAGE);
        } catch (Exception e) { Db.err(this, e); }
    }
}
