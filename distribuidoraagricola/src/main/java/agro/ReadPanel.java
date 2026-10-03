package agro;

import java.awt.*;
import javax.swing.*;

class ReadPanel extends JPanel implements Refrescable {
    final String sql; final JTable t = new JTable();
    ReadPanel(String sql) {
        this.sql = sql; setLayout(new BorderLayout());
        add(new JScrollPane(t), BorderLayout.CENTER);
        JPanel b = new JPanel(); b.add(Db.btn("Actualizar", e -> refrescar())); add(b, BorderLayout.SOUTH);
        refrescar();
    }
    public void refrescar() { try { t.setModel(Db.model(sql)); } catch (Exception e) { Db.err(this, e); } }
}
