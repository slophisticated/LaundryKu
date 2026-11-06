package laundryku.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import laundryku.model.Admin;
import laundryku.service.LaundryService;

/** Kelola akun admin lain. Admin tidak bisa menghapus dirinya sendiri atau admin terakhir. */
class AkunAdminPanel extends JPanel implements AdminFrame.Muat {
    private final LaundryService laundry;
    private final Admin login;
    private final DefaultTableModel model = Ui.model("ID", "Nama", "Username");
    private final JTable tabel = Ui.tabel(model);

    AkunAdminPanel(LaundryService laundry, Admin login) {
        super(new BorderLayout());
        this.laundry = laundry;
        this.login = login;
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        tombol.add(Ui.tombol("Tambah Admin", this::tambah));
        tombol.add(Ui.tombol("Hapus Admin", this::hapus));
        add(tombol, BorderLayout.NORTH);
        add(new JScrollPane(tabel), BorderLayout.CENTER);
    }

    @Override
    public void muat() {
        Ui.coba(this, () -> {
            model.setRowCount(0);
            for (Admin a : laundry.getDaftarAdmin()) {
                model.addRow(new Object[]{a.getId(), a.getNama(), a.getUsername()});
            }
        });
    }

    private void tambah() {
        JTextField nama = new JTextField(20);
        JTextField username = new JTextField(20);
        JPasswordField password = new JPasswordField(20);
        if (Ui.form(this, "Tambah Admin", new String[]{"Nama", "Username", "Password (min. 6)"},
                new JComponent[]{nama, username, password})
                && Ui.coba(this, () -> laundry.tambahAdmin(nama.getText(), username.getText(),
                        new String(password.getPassword())))) {
            muat();
        }
    }

    private void hapus() {
        String id = Ui.terpilih(tabel);
        if (id == null) { Ui.gagal(this, "Pilih admin pada tabel terlebih dahulu."); return; }
        if (Ui.yakin(this, "Hapus admin " + id + "?")
                && Ui.coba(this, () -> laundry.hapusAdmin(id, login.getId()))) {
            muat();
        }
    }
}
