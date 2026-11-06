package laundryku.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import laundryku.model.LayananLaundry;
import laundryku.service.LaundryService;

/** CRUD layanan dan tarif. Perubahan tarif tidak mengubah transaksi lama. */
class LayananPanel extends JPanel implements AdminFrame.Muat {
    private final LaundryService laundry;
    private final DefaultTableModel model = Ui.model("Kode", "Layanan", "Harga/kg", "Estimasi", "Proses");
    private final JTable tabel = Ui.tabel(model);

    LayananPanel(LaundryService laundry) {
        super(new BorderLayout());
        this.laundry = laundry;
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        tombol.add(Ui.tombol("Tambah", this::tambah));
        tombol.add(Ui.tombol("Edit", this::edit));
        tombol.add(Ui.tombol("Hapus", this::hapus));
        tombol.add(Ui.tombol("Muat Ulang", this::muat));
        tombol.add(new JLabel("   Perubahan tarif hanya berlaku untuk transaksi baru."));
        add(tombol, BorderLayout.NORTH);
        add(new JScrollPane(tabel), BorderLayout.CENTER);
    }

    @Override
    public void muat() {
        Ui.coba(this, () -> {
            model.setRowCount(0);
            for (LayananLaundry l : laundry.getDaftarLayanan()) {
                model.addRow(new Object[]{l.getKode(), l.getNama(), Ui.uang(l.getHargaPerKg()),
                        l.getDurasiHari() + " hari", l.getProses()});
            }
        });
    }

    private void tambah() {
        JTextField kode = new JTextField(10);
        Isian f = new Isian(null);
        if (Ui.form(this, "Tambah Layanan", f.label(true), f.komponen(kode))
                && Ui.coba(this, () -> laundry.tambahLayanan(kode.getText(), f.nama.getText(),
                        Ui.angka(f.harga.getText(), "Harga"), Ui.bulat(f.durasi.getText(), "Durasi"),
                        f.cuci.isSelected(), f.setrika.isSelected()))) {
            muat();
        }
    }

    private void edit() {
        String kode = Ui.terpilih(tabel);
        if (kode == null) { Ui.gagal(this, "Pilih layanan pada tabel terlebih dahulu."); return; }
        LayananLaundry[] l = new LayananLaundry[1];
        if (!Ui.coba(this, () -> l[0] = laundry.cariLayanan(kode))) { return; }
        Isian f = new Isian(l[0]);
        if (Ui.form(this, "Edit Layanan " + kode, f.label(false), f.komponen(null))
                && Ui.coba(this, () -> laundry.ubahLayanan(kode, f.nama.getText(),
                        Ui.angka(f.harga.getText(), "Harga"), Ui.bulat(f.durasi.getText(), "Durasi"),
                        f.cuci.isSelected(), f.setrika.isSelected()))) {
            muat();
        }
    }

    private void hapus() {
        String kode = Ui.terpilih(tabel);
        if (kode == null) { Ui.gagal(this, "Pilih layanan pada tabel terlebih dahulu."); return; }
        if (Ui.yakin(this, "Hapus layanan " + kode + "?") && Ui.coba(this, () -> laundry.hapusLayanan(kode))) {
            muat();
        }
    }

    /** Isian form layanan yang dipakai bersama oleh tambah dan edit. */
    private static class Isian {
        final JTextField nama = new JTextField(20);
        final JTextField harga = new JTextField(20);
        final JTextField durasi = new JTextField(20);
        final JCheckBox cuci = new JCheckBox("Perlu dicuci", true);
        final JCheckBox setrika = new JCheckBox("Perlu disetrika", true);

        Isian(LayananLaundry l) {
            if (l != null) {
                nama.setText(l.getNama());
                harga.setText(l.getHargaPerKg().toPlainString());
                durasi.setText(String.valueOf(l.getDurasiHari()));
                cuci.setSelected(l.isPerluCuci());
                setrika.setSelected(l.isPerluSetrika());
            }
        }

        String[] label(boolean denganKode) {
            String[] dasar = {"Nama layanan", "Harga per kg (Rp)", "Estimasi (hari)", "Proses", ""};
            if (!denganKode) { return dasar; }
            return new String[]{"Kode (mis. L05)", dasar[0], dasar[1], dasar[2], dasar[3], dasar[4]};
        }

        JComponent[] komponen(JTextField kode) {
            JComponent[] dasar = {nama, harga, durasi, cuci, setrika};
            if (kode == null) { return dasar; }
            return new JComponent[]{kode, nama, harga, durasi, cuci, setrika};
        }
    }
}
