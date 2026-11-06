package laundryku.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.math.BigDecimal;
import java.time.LocalDate;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import laundryku.model.Invoice;
import laundryku.model.LayananLaundry;
import laundryku.model.Pelanggan;
import laundryku.model.Pembayaran;
import laundryku.model.StatusCucian;
import laundryku.model.TransaksiLaundry;
import laundryku.service.LaundryService;

/** Buat transaksi, lanjutkan status, pembayaran tunai, dan cetak invoice. */
class TransaksiPanel extends JPanel implements AdminFrame.Muat {
    private final LaundryService laundry;
    private final DefaultTableModel model = Ui.model("Kode", "Pelanggan", "Layanan", "Berat",
            "Total", "Masuk", "Estimasi", "Status", "Pembayaran");
    private final JTable tabel = Ui.tabel(model);

    TransaksiPanel(LaundryService laundry) {
        super(new BorderLayout());
        this.laundry = laundry;
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        tombol.add(Ui.tombol("Transaksi Baru", this::baru));
        tombol.add(Ui.tombol("Lanjutkan Status", this::lanjutkan));
        tombol.add(Ui.tombol("Bayar Tunai", this::bayar));
        tombol.add(Ui.tombol("Lihat Invoice", this::invoice));
        tombol.add(Ui.tombol("Muat Ulang", this::muat));
        add(tombol, BorderLayout.NORTH);
        add(new JScrollPane(tabel), BorderLayout.CENTER);
    }

    @Override
    public void muat() {
        Ui.coba(this, () -> {
            model.setRowCount(0);
            for (TransaksiLaundry t : laundry.getDaftarTransaksi()) {
                model.addRow(new Object[]{t.getKode(), t.getPelanggan().getNama(), t.getLayanan().getNama(),
                        Ui.beratTeks(t.getBeratKg()), Ui.uang(t.getTotalBiaya()), t.getTanggalMasuk(),
                        t.getEstimasiSelesai(), t.getStatus(), t.isLunas() ? "LUNAS" : "BELUM LUNAS"});
            }
        });
    }

    private void baru() {
        JComboBox<Pelanggan> pelanggan = new JComboBox<>();
        JComboBox<LayananLaundry> layanan = new JComboBox<>();
        if (!Ui.coba(this, () -> {
            laundry.getDaftarPelanggan().forEach(pelanggan::addItem);
            laundry.getDaftarLayanan().forEach(layanan::addItem);
        })) {
            return;
        }
        if (pelanggan.getItemCount() == 0) {
            Ui.gagal(this, "Belum ada pelanggan. Tambahkan pelanggan di tab Pelanggan terlebih dahulu.");
            return;
        }
        JTextField berat = new JTextField(12);
        JLabel perkiraan = new JLabel("-");
        Runnable hitung = () -> {
            LayananLaundry l = (LayananLaundry) layanan.getSelectedItem();
            try {
                BigDecimal b = Ui.angka(berat.getText(), "Berat");
                perkiraan.setText(Ui.uang(TransaksiLaundry.hitungTotal(b, l.getHargaPerKg()))
                        + "  (estimasi " + l.getDurasiHari() + " hari)");
            } catch (IllegalArgumentException | NullPointerException e) {
                perkiraan.setText(l == null ? "-" : Ui.uang(l.getHargaPerKg()) + "/kg");
            }
        };
        layanan.addActionListener(e -> hitung.run());
        berat.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { hitung.run(); }
            @Override public void removeUpdate(DocumentEvent e) { hitung.run(); }
            @Override public void changedUpdate(DocumentEvent e) { hitung.run(); }
        });
        hitung.run();

        if (!Ui.form(this, "Transaksi Baru", new String[]{"Pelanggan", "Layanan", "Berat (kg)", "Perkiraan biaya"},
                new JComponent[]{pelanggan, layanan, berat, perkiraan})) {
            return;
        }
        Ui.coba(this, () -> {
            TransaksiLaundry t = laundry.buatTransaksi(((Pelanggan) pelanggan.getSelectedItem()).getId(),
                    ((LayananLaundry) layanan.getSelectedItem()).getKode(),
                    Ui.angka(berat.getText(), "Berat"), LocalDate.now());
            muat();
            Ui.tampilkanTeks(this, "Transaksi berhasil dibuat", new Invoice(t).cetak());
        });
    }

    private TransaksiLaundry pilih() {
        String kode = Ui.terpilih(tabel);
        if (kode == null) {
            Ui.gagal(this, "Pilih transaksi pada tabel terlebih dahulu.");
            return null;
        }
        TransaksiLaundry[] t = new TransaksiLaundry[1];
        return Ui.coba(this, () -> t[0] = laundry.cariTransaksi(kode)) ? t[0] : null;
    }

    private void lanjutkan() {
        TransaksiLaundry t = pilih();
        if (t == null) { return; }
        if (t.getStatus() == StatusCucian.SELESAI) {
            Ui.info(this, "Transaksi " + t.getKode() + " sudah selesai.");
            return;
        }
        if (Ui.yakin(this, t.getKode() + ": " + t.getStatus() + " -> " + t.getStatusBerikutnya() + "\nLanjutkan?")
                && Ui.coba(this, () -> laundry.lanjutkanStatus(t.getKode()))) {
            muat();
        }
    }

    private void bayar() {
        TransaksiLaundry t = pilih();
        if (t == null) { return; }
        if (t.isLunas()) {
            Ui.info(this, "Transaksi " + t.getKode() + " sudah lunas.");
            return;
        }
        JTextField uang = new JTextField(12);
        if (!Ui.form(this, "Pembayaran Tunai " + t.getKode(), new String[]{"Total tagihan", "Uang diterima (Rp)"},
                new JComponent[]{new JLabel(Ui.uang(t.getTotalBiaya())), uang})) {
            return;
        }
        Ui.coba(this, () -> {
            Pembayaran p = laundry.bayar(t.getKode(), Ui.angka(uang.getText(), "Uang diterima"), LocalDate.now());
            muat();
            Ui.tampilkanTeks(this, "Pembayaran berhasil - Kembalian " + Ui.uang(p.getKembalian()),
                    new Invoice(laundry.cariTransaksi(t.getKode())).cetak());
        });
    }

    private void invoice() {
        TransaksiLaundry t = pilih();
        if (t != null) {
            Ui.tampilkanTeks(this, "Invoice " + t.getKode(), new Invoice(t).cetak());
        }
    }
}
