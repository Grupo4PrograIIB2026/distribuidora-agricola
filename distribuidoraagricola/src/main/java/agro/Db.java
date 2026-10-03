package agro;

import java.sql.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Conexión, sesión y utilidades JDBC. */
public class Db {
    // >>> CAMBIAR usuario/clave/servidor según su SQL Server <<<
    static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=DistribuidoraAgricola;encrypt=true;trustServerCertificate=true";
    static final String USER = "sa", PASS = "CAMBIAR_CLAVE";

    // Sesión actual
    static int uid; static String usuario, rol;

    static Connection get() throws SQLException { return DriverManager.getConnection(URL, USER, PASS); }

    static String sha256(String s) {
        try {
            StringBuilder sb = new StringBuilder();
            for (byte b : java.security.MessageDigest.getInstance("SHA-256").digest(s.getBytes("UTF-8")))
                sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    static void bind(PreparedStatement ps, Object... p) throws SQLException {
        for (int i = 0; i < p.length; i++) ps.setObject(i + 1, p[i]);
    }

    static DefaultTableModel model(String sql, Object... p) throws SQLException {
        try (Connection c = get(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, p);
            ResultSet rs = ps.executeQuery(); int n = rs.getMetaData().getColumnCount();
            DefaultTableModel m = new DefaultTableModel() { public boolean isCellEditable(int r, int c) { return false; } };
            for (int i = 1; i <= n; i++) m.addColumn(rs.getMetaData().getColumnLabel(i));
            while (rs.next()) { Object[] row = new Object[n]; for (int i = 0; i < n; i++) row[i] = rs.getObject(i + 1); m.addRow(row); }
            return m;
        }
    }

    static void exec(String sql, Object... p) throws SQLException {
        try (Connection c = get(); PreparedStatement ps = c.prepareStatement(sql)) { bind(ps, p); ps.executeUpdate(); }
    }

    static void fill(JComboBox<Item> cb, String sql) throws SQLException {
        cb.removeAllItems();
        try (Connection c = get(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) cb.addItem(new Item(rs.getInt(1), rs.getString(2)));
        }
    }

    /** Bitácora de acceso al sistema. */
    static void log(Integer id, String u, String accion, String res) {
        try { exec("INSERT INTO BitacoraAcceso(id_usuario,usuario,accion,resultado) VALUES(?,?,?,?)", id, u, accion, res); }
        catch (Exception e) { e.printStackTrace(); }
    }

    static void err(java.awt.Component c, Exception e) {
        JOptionPane.showMessageDialog(c, e.getMessage(), "Aviso", JOptionPane.WARNING_MESSAGE);
    }

    static JButton btn(String t, java.awt.event.ActionListener l) { JButton b = new JButton(t); b.addActionListener(l); return b; }
}

record Item(int id, String nombre) { public String toString() { return nombre; } }

interface Refrescable { void refrescar(); }
