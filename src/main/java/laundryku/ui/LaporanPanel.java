package laundryku.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.math.BigDecimal;
import java.time.LocalDate;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import laundryku.model.TransaksiLaundry;
import laundryku.service.LaundryService;

/** Rekap pendapatan berdasarkan tanggal pembayaran. */
class LaporanPanel extends JPanel implements AdminFrame.Muat {
    private final LaundryService laundry;
    private final JTextField mulai = new JTextField(LocalDate.now().withDayOfMonth(1).toString(), 10);
    private final JTextField akhir = new JTextField(LocalDate.now().toString(), 10);
    private final DefaultTableModel model = Ui.model("Kode", "Tanggal Bayar", "Pelanggan", "Layanan", "Tagihan");
    private final JLabel total = new JLabel(" ");

    LaporanPanel(LaundryService laundry) {
        super(new BorderLayout());
        this.laundry = laundry;
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        filter.add(new JLabel("Dari (YYYY-MM-DD)"));
        filter.add(mulai);
        filter.add(new JLabel("Sampai"));
        filter.add(akhir);
        filter.add(Ui.tombol("Tampilkan", this::muat));
        add(filter, BorderLayout.NORTH);
        add(new JScrollPane(Ui.tabel(model)), BorderLayout.CENTER);
        total.setFont(total.getFont().deriveFont(Font.BOLD, 16f));
        total.setBorder(BorderFactory.createEmptyBorder(8, 4, 0, 4));
        add(total, BorderLayout.SOUTH);
    }

    @Override
    public void muat() {
        Ui.coba(this, () -> {
            LocalDate a = Ui.tanggal(mulai.getText(), "Tanggal awal");
            LocalDate b = Ui.tanggal(akhir.getText(), "Tanggal akhir");
            BigDecimal pendapatan = laundry.hitungPendapatan(a, b);
            model.setRowCount(0);
            for (TransaksiLaundry t : laundry.getTransaksiLunas(a, b)) {
                model.addRow(new Object[]{t.getKode(), t.getPembayaran().getTanggal(), t.getPelanggan().getNama(),
                        t.getLayanan().getNama(), Ui.uang(t.getPembayaran().getTagihan())});
            }
            total.setText("Total pendapatan " + a + " s.d. " + b + ": " + Ui.uang(pendapatan)
                    + "   (kembalian tidak dihitung)");
        });
    }
}
