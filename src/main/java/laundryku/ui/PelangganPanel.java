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
import laundryku.model.Pelanggan;
import laundryku.service.LaundryService;

/** CRUD pelanggan, termasuk akun login opsional untuk pelanggan. */
class PelangganPanel extends JPanel implements AdminFrame.Muat {
    private final LaundryService laundry;
    private final DefaultTableModel model = Ui.model("ID", "Nama", "Telepon", "Username Login");
    private final JTable tabel = Ui.tabel(model);

    PelangganPanel(LaundryService laundry) {
        super(new BorderLayout());
        this.laundry = laundry;
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        tombol.add(Ui.tombol("Tambah", this::tambah));
        tombol.add(Ui.tombol("Edit", this::edit));
        tombol.add(Ui.tombol("Hapus", this::hapus));
        tombol.add(Ui.tombol("Muat Ulang", this::muat));
        add(tombol, BorderLayout.NORTH);
        add(new JScrollPane(tabel), BorderLayout.CENTER);
    }

    @Override
    public void muat() {
        Ui.coba(this, () -> {
            model.setRowCount(0);
            for (Pelanggan p : laundry.getDaftarPelanggan()) {
                model.addRow(new Object[]{p.getId(), p.getNama(), p.getNomorTelepon(),
                        p.punyaAkun() ? p.getUsername() : "-"});
            }
        });
    }

    private void tambah() {
        JTextField nama = new JTextField(20);
        JTextField telepon = new JTextField(20);
        JTextField username = new JTextField(20);
        JPasswordField password = new JPasswordField(20);
        if (Ui.form(this, "Tambah Pelanggan",
                new String[]{"Nama", "Nomor telepon", "Username (opsional)", "Password"},
                new JComponent[]{nama, telepon, username, password})
                && Ui.coba(this, () -> {
                    Pelanggan p = laundry.tambahPelanggan(nama.getText(), telepon.getText(),
                            username.getText(), new String(password.getPassword()));
                    Ui.info(this, "Pelanggan tersimpan dengan ID " + p.getId() + ".");
                })) {
            muat();
        }
    }

    private void edit() {
        String id = Ui.terpilih(tabel);
        if (id == null) { Ui.gagal(this, "Pilih pelanggan pada tabel terlebih dahulu."); return; }
        Pelanggan[] p = new Pelanggan[1];
        if (!Ui.coba(this, () -> p[0] = laundry.cariPelanggan(id))) { return; }
        JTextField nama = new JTextField(p[0].getNama(), 20);
        JTextField telepon = new JTextField(p[0].getNomorTelepon(), 20);
        JTextField username = new JTextField(p[0].punyaAkun() ? p[0].getUsername() : "", 20);
        JPasswordField password = new JPasswordField(20);
        if (Ui.form(this, "Edit Pelanggan " + id,
                new String[]{"Nama", "Nomor telepon", "Username (kosong = tanpa login)",
                        "Password baru (kosong = tetap)"},
                new JComponent[]{nama, telepon, username, password})
                && Ui.coba(this, () -> laundry.ubahPelanggan(id, nama.getText(), telepon.getText(),
                        username.getText(), new String(password.getPassword())))) {
            muat();
        }
    }

    private void hapus() {
        String id = Ui.terpilih(tabel);
        if (id == null) { Ui.gagal(this, "Pilih pelanggan pada tabel terlebih dahulu."); return; }
        if (Ui.yakin(this, "Hapus pelanggan " + id + "?") && Ui.coba(this, () -> laundry.hapusPelanggan(id))) {
            muat();
        }
    }
}
