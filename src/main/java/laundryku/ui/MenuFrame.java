package laundryku.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import laundryku.model.Pengguna;
import laundryku.service.LaundryService;

/** Jendela utama setelah login: header berisi nama pengguna, ganti password, dan logout. */
abstract class MenuFrame extends JFrame {
    protected final LaundryService laundry;
    protected final Pengguna pengguna;

    protected MenuFrame(LaundryService laundry, Pengguna pengguna, String judul) {
        super("LaundryKu - " + judul);
        this.laundry = laundry;
        this.pengguna = pengguna;
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        header.add(Ui.judul("LaundryKu  |  " + judul), BorderLayout.WEST);
        JPanel kanan = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        kanan.add(new JLabel(pengguna.getNama() + " (" + pengguna.getPeran() + ")"));
        kanan.add(Ui.tombol("Ganti Password", this::gantiPassword));
        kanan.add(Ui.tombol("Logout", this::logout));
        header.add(kanan, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);
    }

    protected void tampil() {
        setSize(1000, 640);
        setLocationRelativeTo(null);
    }

    private void gantiPassword() {
        JPasswordField lama = new JPasswordField(16);
        JPasswordField baru = new JPasswordField(16);
        JPasswordField ulang = new JPasswordField(16);
        if (!Ui.form(this, "Ganti Password", new String[]{"Password lama", "Password baru", "Ulangi password baru"},
                new JPasswordField[]{lama, baru, ulang})) {
            return;
        }
        String b = new String(baru.getPassword());
        if (!b.equals(new String(ulang.getPassword()))) {
            Ui.gagal(this, "Ulangi password baru tidak sama.");
            return;
        }
        if (Ui.coba(this, () -> laundry.gantiPassword(pengguna.getId(), new String(lama.getPassword()), b))) {
            Ui.info(this, "Password berhasil diganti.");
        }
    }

    private void logout() {
        if (Ui.yakin(this, "Keluar dari akun ini?")) {
            new LoginFrame(laundry).setVisible(true);
            dispose();
        }
    }
}
