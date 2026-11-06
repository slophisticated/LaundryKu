package laundryku.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import laundryku.model.Invoice;
import laundryku.model.Pelanggan;
import laundryku.model.TransaksiLaundry;
import laundryku.service.LaundryService;

/** Menu pelanggan: hanya dapat melihat cucian dan invoice miliknya sendiri. */
public class PelangganFrame extends MenuFrame {
    private final DefaultTableModel model = Ui.model("Kode", "Layanan", "Berat", "Total",
            "Masuk", "Estimasi Selesai", "Status", "Pembayaran");
    private final JTable tabel = Ui.tabel(model);

    public PelangganFrame(LaundryService laundry, Pelanggan pelanggan) {
        super(laundry, pelanggan, "Cucian Saya");
        JPanel isi = new JPanel(new BorderLayout());
        isi.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        tombol.add(Ui.tombol("Lihat Invoice", this::invoice));
        tombol.add(Ui.tombol("Muat Ulang", this::muat));
        isi.add(tombol, BorderLayout.NORTH);
        isi.add(new JScrollPane(tabel), BorderLayout.CENTER);
        add(isi, BorderLayout.CENTER);
        muat();
        tampil();
    }

    private void muat() {
        Ui.coba(this, () -> {
            model.setRowCount(0);
            for (TransaksiLaundry t : laundry.getTransaksiPelanggan(pengguna.getId())) {
                model.addRow(new Object[]{t.getKode(), t.getLayanan().getNama(), Ui.beratTeks(t.getBeratKg()),
                        Ui.uang(t.getTotalBiaya()), t.getTanggalMasuk(), t.getEstimasiSelesai(),
                        t.getStatus(), t.isLunas() ? "LUNAS" : "BELUM LUNAS"});
            }
        });
    }

    private void invoice() {
        String kode = Ui.terpilih(tabel);
        if (kode == null) { Ui.gagal(this, "Pilih transaksi pada tabel terlebih dahulu."); return; }
        Ui.coba(this, () -> {
            TransaksiLaundry t = laundry.cariTransaksi(kode);
            // Pengaman tambahan agar pelanggan tidak bisa membuka invoice milik orang lain.
            if (!t.getPelanggan().getId().equals(pengguna.getId())) {
                throw new IllegalStateException("Transaksi ini bukan milik kamu.");
            }
            Ui.tampilkanTeks(this, "Invoice " + kode, new Invoice(t).cetak());
        });
    }
}
