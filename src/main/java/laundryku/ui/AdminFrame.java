package laundryku.ui;

import java.awt.BorderLayout;
import javax.swing.JTabbedPane;
import laundryku.model.Admin;
import laundryku.service.LaundryService;

/** Menu admin: semua fitur operasional laundry dalam bentuk tab. */
public class AdminFrame extends MenuFrame {
    public AdminFrame(LaundryService laundry, Admin admin) {
        super(laundry, admin, "Admin");
        TransaksiPanel transaksi = new TransaksiPanel(laundry);
        PelangganPanel pelanggan = new PelangganPanel(laundry);
        LayananPanel layanan = new LayananPanel(laundry);
        LaporanPanel laporan = new LaporanPanel(laundry);
        AkunAdminPanel akun = new AkunAdminPanel(laundry, admin);

        JTabbedPane tab = new JTabbedPane();
        tab.addTab("Transaksi", transaksi);
        tab.addTab("Pelanggan", pelanggan);
        tab.addTab("Layanan", layanan);
        tab.addTab("Laporan Pendapatan", laporan);
        tab.addTab("Akun Admin", akun);
        // Data dimuat ulang dari database setiap pindah tab agar selalu terbaru.
        tab.addChangeListener(e -> ((Muat) tab.getSelectedComponent()).muat());
        add(tab, BorderLayout.CENTER);
        transaksi.muat();
        tampil();
    }

    /** Panel yang bisa memuat ulang datanya dari database. */
    interface Muat {
        void muat();
    }
}
