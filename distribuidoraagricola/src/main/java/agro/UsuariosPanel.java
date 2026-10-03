package agro;

import java.awt.*;
import javax.swing.*;

/** Manejo de usuarios y roles (solo Administrador). Los usuarios no se borran, se desactivan (queda historial en bitácora). */
class UsuariosPanel extends JPanel implements Refrescable {
    final JTextField us = new JTextField(), nom = new JTextField();
    final JPasswordField pw = new JPasswordField();
    final JComboBox<Item> rol = new JComboBox<>();
    final JCheckBox act = new JCheckBox("Activo", true);
    final JTable t = new JTable(); Integer sel;

    UsuariosPanel() {
        setLayout(new BorderLayout(8, 8));
        JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
        form.add(new JLabel("Usuario")); form.add(us);
        form.add(new JLabel("Nombre completo")); form.add(nom);
        form.add(new JLabel("Contraseña (vacío = no cambiar)")); form.add(pw);
        form.add(new JLabel("Rol")); form.add(rol);
        form.add(new JLabel()); form.add(act);
        JPanel bt = new JPanel(); bt.add(Db.btn("Nuevo", e -> clear())); bt.add(Db.btn("Guardar", e -> save()));
        JPanel left = new JPanel(new BorderLayout()); left.add(form, BorderLayout.NORTH); left.add(bt, BorderLayout.CENTER);
        left.setPreferredSize(new Dimension(380, 0)); left.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 0));
        add(left, BorderLayout.WEST); add(new JScrollPane(t), BorderLayout.CENTER);
        t.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) pick(); });
        refrescar();
    }

    public void refrescar() {
        try {
            Db.fill(rol, "SELECT id_rol,nombre FROM Rol ORDER BY nombre");
            t.setModel(Db.model("SELECT u.id_usuario,u.usuario,u.nombre_completo,r.nombre AS rol,u.activo FROM Usuario u JOIN Rol r ON r.id_rol=u.id_rol ORDER BY 1"));
            sel = null;
        } catch (Exception e) { Db.err(this, e); }
    }

    void pick() {
        int r = t.getSelectedRow(); if (r < 0) return;
        sel = (Integer) t.getValueAt(r, 0);
        us.setText("" + t.getValueAt(r, 1)); nom.setText("" + t.getValueAt(r, 2)); pw.setText("");
        for (int i = 0; i < rol.getItemCount(); i++) if (rol.getItemAt(i).nombre().equals(t.getValueAt(r, 3))) rol.setSelectedIndex(i);
        act.setSelected(Boolean.TRUE.equals(t.getValueAt(r, 4)));
    }

    void clear() { sel = null; us.setText(""); nom.setText(""); pw.setText(""); act.setSelected(true); t.clearSelection(); }

    void save() {
        String clave = new String(pw.getPassword()); Item r = (Item) rol.getSelectedItem();
        if (us.getText().isBlank() || nom.getText().isBlank() || r == null) { JOptionPane.showMessageDialog(this, "Complete usuario, nombre y rol"); return; }
        try {
            if (sel == null) {
                if (clave.isEmpty()) { JOptionPane.showMessageDialog(this, "La contraseña es obligatoria"); return; }
                Db.exec("INSERT INTO Usuario(usuario,nombre_completo,clave_hash,id_rol,activo) VALUES(?,?,?,?,?)",
                        us.getText().trim(), nom.getText().trim(), Db.sha256(clave), r.id(), act.isSelected());
            } else {
                Db.exec("UPDATE Usuario SET usuario=?,nombre_completo=?,id_rol=?,activo=? WHERE id_usuario=?",
                        us.getText().trim(), nom.getText().trim(), r.id(), act.isSelected(), sel);
                if (!clave.isEmpty()) Db.exec("UPDATE Usuario SET clave_hash=? WHERE id_usuario=?", Db.sha256(clave), sel);
            }
            Db.log(Db.uid, Db.usuario, "ADMIN_USUARIOS", "Guardó usuario " + us.getText().trim());
            refrescar(); clear();
        } catch (Exception e) { Db.err(this, e); }
    }
}
