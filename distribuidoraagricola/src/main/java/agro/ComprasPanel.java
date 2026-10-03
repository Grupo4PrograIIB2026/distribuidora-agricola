package agro;

import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Registro de compras a proveedores; suma automáticamente a las existencias. */
class ComprasPanel extends JPanel implements Refrescable {
    final JComboBox<Item> prov = new JComboBox<>(), pro = new JComboBox<>();
    final JSpinner qty = new JSpinner(new SpinnerNumberModel(1, 1, 100000, 1));
    final JTextField costo = new JTextField("0.00", 6);
    final DefaultTableModel lines = new DefaultTableModel(new String[]{"id", "Producto", "Cantidad", "Costo", "Subtotal"}, 0) {
        public boolean isCellEditable(int r, int c) { return false; } };
    final JTable tl = new JTable(lines), th = new JTable();
    final JLabel total = new JLabel("Total: 0.00");

    ComprasPanel() {
        setLayout(new BorderLayout(5, 5));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Proveedor:")); top.add(prov); top.add(new JLabel("Producto:")); top.add(pro);
        top.add(new JLabel("Cant.:")); top.add(qty); top.add(new JLabel("Costo:")); top.add(costo);
        top.add(Db.btn("Agregar línea", e -> agregar())); top.add(Db.btn("Quitar línea", e -> quitar()));
        top.add(Db.btn("Registrar compra", e -> registrar())); top.add(total);
        JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(tl), new JScrollPane(th)); sp.setResizeWeight(0.4);
        add(top, BorderLayout.NORTH); add(sp, BorderLayout.CENTER);
        refrescar();
    }

    public void refrescar() {
        try {
            Db.fill(prov, "SELECT id_proveedor,nombre FROM Proveedor ORDER BY nombre");
            Db.fill(pro, "SELECT id_producto,nombre FROM Producto ORDER BY nombre");
            th.setModel(Db.model("SELECT c.id_compra,p.nombre AS proveedor,c.fecha,c.total FROM Compra c JOIN Proveedor p ON p.id_proveedor=c.id_proveedor ORDER BY 1 DESC"));
        } catch (Exception e) { Db.err(this, e); }
    }

    void agregar() {
        Item p = (Item) pro.getSelectedItem(); if (p == null) return;
        try {
            BigDecimal c = new BigDecimal(costo.getText().trim()); int q = (Integer) qty.getValue();
            lines.addRow(new Object[]{p.id(), p.nombre(), q, c, c.multiply(BigDecimal.valueOf(q))}); upd();
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Costo inválido"); }
    }

    void quitar() { int r = tl.getSelectedRow(); if (r >= 0) { lines.removeRow(r); upd(); } }

    BigDecimal suma() {
        BigDecimal s = BigDecimal.ZERO;
        for (int i = 0; i < lines.getRowCount(); i++) s = s.add((BigDecimal) lines.getValueAt(i, 4));
        return s;
    }

    void upd() { total.setText("Total: " + suma()); }

    void registrar() {
        if (lines.getRowCount() == 0 || prov.getSelectedItem() == null) { JOptionPane.showMessageDialog(this, "Seleccione proveedor y agregue productos"); return; }
        try (Connection c = Db.get()) {
            c.setAutoCommit(false);
            try {
                int id;
                try (PreparedStatement ps = c.prepareStatement("INSERT INTO Compra(id_proveedor,total,id_usuario) VALUES(?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, ((Item) prov.getSelectedItem()).id()); ps.setBigDecimal(2, suma()); ps.setInt(3, Db.uid);
                    ps.executeUpdate(); ResultSet k = ps.getGeneratedKeys(); k.next(); id = k.getInt(1);
                }
                for (int i = 0; i < lines.getRowCount(); i++) {
                    int pid = (Integer) lines.getValueAt(i, 0), q = (Integer) lines.getValueAt(i, 2);
                    PedidosPanel.run(c, "INSERT INTO DetalleCompra(id_compra,id_producto,cantidad,costo_unitario) VALUES(?,?,?,?)", id, pid, q, lines.getValueAt(i, 3));
                    // Actualización automática de existencias
                    PedidosPanel.run(c, "UPDATE Producto SET existencia=existencia+? WHERE id_producto=?", q, pid);
                }
                c.commit();
            } catch (Exception e) { c.rollback(); throw e; }
            lines.setRowCount(0); upd(); refrescar();
        } catch (Exception e) { Db.err(this, e); }
    }
}
