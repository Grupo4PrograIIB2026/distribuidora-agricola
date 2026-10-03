package agro;

import java.awt.*;
import java.sql.*;
import javax.swing.*;

public class LoginFrame extends JFrame {
    final JTextField u = new JTextField(15);
    final JPasswordField p = new JPasswordField(15);

    public LoginFrame() {
        super("Distribuidora Agrícola - Inicio de sesión");
        JPanel f = new JPanel(new GridLayout(3, 2, 8, 8));
        f.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        f.add(new JLabel("Usuario:")); f.add(u);
        f.add(new JLabel("Contraseña:")); f.add(p);
        JButton b = Db.btn("Ingresar", e -> login());
        f.add(new JLabel()); f.add(b);
        add(f); getRootPane().setDefaultButton(b);
        pack(); setLocationRelativeTo(null); setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    void login() {
        String us = u.getText().trim(), pw = new String(p.getPassword());
        String sql = "SELECT u.id_usuario,u.clave_hash,u.activo,r.nombre FROM Usuario u JOIN Rol r ON r.id_rol=u.id_rol WHERE u.usuario=?";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, us);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) { Db.log(null, us, "LOGIN_FALLIDO", "Usuario inexistente"); aviso("Usuario o contraseña incorrectos"); return; }
            int id = rs.getInt(1);
            if (!rs.getBoolean(3)) { Db.log(id, us, "LOGIN_FALLIDO", "Usuario inactivo"); aviso("Usuario inactivo"); return; }
            if (!rs.getString(2).equalsIgnoreCase(Db.sha256(pw))) { Db.log(id, us, "LOGIN_FALLIDO", "Clave incorrecta"); aviso("Usuario o contraseña incorrectos"); return; }
            Db.uid = id; Db.usuario = us; Db.rol = rs.getString(4);
            Db.log(id, us, "LOGIN", "Acceso correcto");
            new MainFrame().setVisible(true); dispose();
        } catch (Exception ex) { Db.err(this, ex); }
    }

    void aviso(String m) { JOptionPane.showMessageDialog(this, m, "Login", JOptionPane.ERROR_MESSAGE); }

    public static void main(String[] a) { SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true)); }
}
