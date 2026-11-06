package laundryku;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Properties;
import laundryku.db.HibernateUtil;
import laundryku.model.*;
import laundryku.service.LaundryService;
import org.hibernate.SessionFactory;

/**
 * Pemeriksaan logika bisnis dan penyimpanan Hibernate tanpa JUnit.
 * Memakai database H2 di memori sehingga data MySQL tidak tersentuh. Jalankan melalui test.bat.
 */
public class SelfCheck {
    private static int jumlah;

    private static void cek(boolean kondisi, String pesan) {
        jumlah++;
        if (!kondisi) { throw new AssertionError(pesan); }
    }

    private static void ditolak(Runnable aksi, String pesan) {
        try { aksi.run(); }
        catch (IllegalArgumentException | IllegalStateException e) { jumlah++; return; }
        throw new AssertionError(pesan);
    }

    private static SessionFactory databaseUji() {
        Properties p = new Properties();
        p.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
        p.setProperty("hibernate.connection.url", "jdbc:h2:mem:laundryku;DB_CLOSE_DELAY=-1");
        p.setProperty("hibernate.connection.username", "sa");
        p.setProperty("hibernate.connection.password", "");
        p.setProperty("hibernate.hbm2ddl.auto", "create-drop");
        return HibernateUtil.buat(p);
    }

    public static void main(String[] args) {
        try (SessionFactory sf = databaseUji()) {
            LaundryService s = new LaundryService(sf);
            LocalDate hari = LocalDate.of(2026, 10, 23);

            // Data awal dan login
            cek(s.getDaftarLayanan().size() == 4, "Layanan contoh dibuat otomatis");
            Pengguna admin = s.login("admin", "admin123");
            cek(admin instanceof Admin, "Admin default bisa login");
            ditolak(() -> s.login("admin", "salah123"), "Password salah ditolak");
            ditolak(() -> s.login("", ""), "Login kosong ditolak");
            new LaundryService(sf);
            cek(s.getDaftarAdmin().size() == 1 && s.getDaftarLayanan().size() == 4, "Data awal tidak dobel");

            // Pelanggan
            Pelanggan p = s.tambahPelanggan("Rafael", "081234567890");
            cek(p.getId().equals("P001"), "ID pelanggan pertama");
            Pengguna[] pengguna = {admin, p};
            cek(pengguna[0].getPeran().equals("Admin") && pengguna[1].getPeran().equals("Pelanggan"), "Polymorphism");
            ditolak(() -> s.tambahPelanggan(" ", "081234567890"), "Nama kosong harus ditolak");
            ditolak(() -> s.tambahPelanggan("A", "------"), "Telepon tanpa angka harus ditolak");
            cek(s.getDaftarPelanggan().size() == 1, "Input gagal tidak menambah pelanggan");

            Pelanggan budi = s.tambahPelanggan("Budi", "0812000111", "budi", "rahasia1");
            cek(s.login("BUDI", "rahasia1") instanceof Pelanggan, "Pelanggan bisa login, username tidak peka huruf");
            ditolak(() -> s.tambahPelanggan("Lain", "0812000222", "budi", "rahasia2"), "Username dobel ditolak");
            ditolak(() -> s.tambahPelanggan("Lain", "0812000222", "lain", "123"), "Password pendek ditolak");
            s.ubahPelanggan(budi.getId(), "Budi S", "0812999", "budi", "");
            cek(s.login("budi", "rahasia1").getNama().equals("Budi S"), "Edit tanpa password tetap pakai password lama");
            s.gantiPassword(budi.getId(), "rahasia1", "rahasia9");
            ditolak(() -> s.login("budi", "rahasia1"), "Password lama tidak berlaku");
            ditolak(() -> s.gantiPassword(budi.getId(), "salah", "abcdef"), "Ganti password perlu password lama");
            s.ubahPelanggan(budi.getId(), "Budi S", "0812999", "", "");
            ditolak(() -> s.login("budi", "rahasia9"), "Akun dihapus tidak bisa login");
            s.hapusPelanggan(budi.getId());
            cek(s.getDaftarPelanggan().size() == 1, "Pelanggan tanpa transaksi bisa dihapus");

            // Transaksi
            ditolak(() -> s.buatTransaksi("P999", "L02", new BigDecimal("3"), hari), "Pelanggan tidak ada");
            ditolak(() -> s.buatTransaksi("P001", "L02", BigDecimal.ZERO, hari), "Berat nol");
            ditolak(() -> s.buatTransaksi("P001", "L02", new BigDecimal("-1"), hari), "Berat negatif");
            ditolak(() -> s.buatTransaksi("P001", "L02", new BigDecimal("1.0001"), hari), "Presisi berat");
            TransaksiLaundry t = s.buatTransaksi("p001", "l02", new BigDecimal("3"), hari);
            cek(t.getKode().equals("TRX0001"), "Input gagal tidak menghabiskan ID transaksi");
            cek(t.getTotalBiaya().compareTo(new BigDecimal("21000")) == 0, "3 kg x 7000 = 21000");
            ditolak(() -> s.hapusPelanggan("P001"), "Pelanggan dengan transaksi tidak bisa dihapus");
            ditolak(() -> s.hapusLayanan("L02"), "Layanan yang dipakai tidak bisa dihapus");

            Invoice invoice = new Invoice(s.cariTransaksi("TRX0001"));
            cek(invoice.getNomor().equals("INV-TRX0001"), "Nomor invoice mengacu pada transaksi");
            String belumLunas = invoice.cetak();
            cek(belumLunas.contains("BELUM LUNAS") && belumLunas.contains("belum menjadi bukti pelunasan"),
                    "Invoice belum lunas tidak mengaku sebagai bukti pembayaran");
            cek(belumLunas.contains("Rafael") && belumLunas.contains("081234567890")
                    && belumLunas.contains("Cuci Setrika"), "Identitas pelanggan dan layanan di invoice");
            cek(belumLunas.contains("3 kg") && belumLunas.contains("Rp7.000")
                    && belumLunas.contains("Rp21.000"), "Berat, tarif, dan total invoice dari database");
            cek(t.getEstimasiSelesai().equals(hari.plusDays(3)), "Estimasi layanan 3 hari");

            // Ubah tarif
            s.ubahLayanan("L02", "Cuci Setrika", new BigDecimal("8000"), 3, true, true);
            TransaksiLaundry lama = s.cariTransaksi("TRX0001");
            cek(lama.getTotalBiaya().compareTo(new BigDecimal("21000")) == 0, "Tarif transaksi lama tetap");
            cek(new Invoice(lama).cetak().contains("Rp7.000"), "Invoice tetap menggunakan tarif saat transaksi");
            TransaksiLaundry baru = s.buatTransaksi("P001", "L02", new BigDecimal("1.25"), hari);
            cek(baru.getKode().equals("TRX0002"), "Kode transaksi berurutan");
            cek(baru.getTotalBiaya().compareTo(new BigDecimal("10000")) == 0, "Tarif baru dan berat desimal");
            cek(s.hitungPendapatan(hari, hari).signum() == 0, "Belum lunas bukan pendapatan");

            // Pembayaran dan status
            ditolak(() -> s.bayar("TRX0001", new BigDecimal("20000"), hari), "Pembayaran kurang");
            ditolak(() -> s.bayar("TRX0001", new BigDecimal("21000.5"), hari), "Pembayaran pecahan rupiah");
            ditolak(() -> s.bayar("TRX0001", new BigDecimal("25000"), hari.minusDays(1)), "Tanggal sebelum masuk");
            cek(!s.cariTransaksi("TRX0001").isLunas(), "Pembayaran gagal tidak tersimpan");
            cek(s.lanjutkanStatus("TRX0001").getStatus() == StatusCucian.DICUCI, "Proses cuci");
            cek(s.lanjutkanStatus("TRX0001").getStatus() == StatusCucian.DISETRIKA, "Proses setrika");
            cek(s.lanjutkanStatus("TRX0001").getStatus() == StatusCucian.SIAP_DIAMBIL, "Siap diambil");
            ditolak(() -> s.lanjutkanStatus("TRX0001"), "Belum lunas tidak boleh diserahkan");
            cek(s.cariTransaksi("TRX0001").getStatus() == StatusCucian.SIAP_DIAMBIL, "Status tersimpan di database");
            Pembayaran bayar = s.bayar("TRX0001", new BigDecimal("25000"), hari.plusDays(3));
            cek(bayar.getKembalian().compareTo(new BigDecimal("4000")) == 0, "Kembalian 4000");
            String lunas = new Invoice(s.cariTransaksi("TRX0001")).cetak();
            cek(lunas.contains("LUNAS") && !lunas.contains("BELUM LUNAS")
                    && lunas.contains("Pembayaran sudah diterima"), "Invoice diperbarui setelah pelunasan");
            cek(lunas.contains("Rp25.000") && lunas.contains("Rp4.000")
                    && lunas.contains(hari.plusDays(3).toString()), "Uang tunai, kembalian dan tanggal bayar di invoice");
            cek(lunas.contains("Sisa tagihan        : Rp0"), "Sisa tagihan nol setelah lunas");
            ditolak(() -> s.bayar("TRX0001", new BigDecimal("25000"), hari.plusDays(3)), "Pembayaran ganda");
            cek(s.lanjutkanStatus("TRX0001").getStatus() == StatusCucian.SELESAI, "Selesai setelah lunas");
            ditolak(() -> s.lanjutkanStatus("TRX0001"), "Tidak dapat melanjutkan transaksi selesai");

            // Laporan
            cek(s.hitungPendapatan(hari, hari).signum() == 0, "Laporan menggunakan tanggal pembayaran");
            cek(s.hitungPendapatan(hari.plusDays(3), hari.plusDays(3)).compareTo(new BigDecimal("21000")) == 0,
                    "Laporan mencatat tagihan, bukan uang sebelum kembalian");
            cek(s.getTransaksiLunas(hari, hari.plusDays(3)).size() == 1, "Daftar transaksi lunas di laporan");
            ditolak(() -> s.hitungPendapatan(hari.plusDays(1), hari), "Rentang laporan terbalik");

            // Alur layanan
            TransaksiLaundry kering = s.buatTransaksi("P001", "L01", BigDecimal.ONE, hari);
            s.lanjutkanStatus(kering.getKode());
            cek(s.lanjutkanStatus(kering.getKode()).getStatus() == StatusCucian.SIAP_DIAMBIL, "Cuci kering melewati setrika");
            TransaksiLaundry setrika = s.buatTransaksi("P001", "L03", BigDecimal.ONE, hari);
            cek(s.lanjutkanStatus(setrika.getKode()).getStatus() == StatusCucian.DISETRIKA, "Setrika saja melewati cuci");
            cek(s.getTransaksiPelanggan("P001").size() == 4, "Riwayat transaksi pelanggan");

            // CRUD layanan dan admin
            s.tambahLayanan("l05", "Selimut", new BigDecimal("15000"), 4, true, false);
            ditolak(() -> s.tambahLayanan("L05", "Dobel", new BigDecimal("1000"), 1, true, true), "Kode layanan dobel");
            ditolak(() -> s.tambahLayanan("L06", "Tanpa proses", new BigDecimal("1000"), 1, false, false), "Layanan tanpa proses");
            s.hapusLayanan("L05");
            cek(s.getDaftarLayanan().size() == 4, "Layanan tanpa transaksi bisa dihapus");
            Admin kedua = s.tambahAdmin("Admin Dua", "admin2", "admin456");
            cek(kedua.getId().equals("A002"), "ID admin berurutan");
            ditolak(() -> s.hapusAdmin(admin.getId(), admin.getId()), "Admin tidak bisa menghapus diri sendiri");
            s.hapusAdmin(kedua.getId(), admin.getId());
            ditolak(() -> s.hapusAdmin(admin.getId(), "X"), "Admin terakhir tidak bisa dihapus");
        }
        System.out.println("LULUS: " + jumlah + " pemeriksaan logika bisnis dan Hibernate.");
    }
}
