package laundryku;

import laundryku.model.*;
import laundryku.service.LaundryService;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Pemeriksaan logika bisnis tanpa library tambahan. Jalankan melalui test.bat. */
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

    public static void main(String[] args) {
        LaundryService s = new LaundryService();
        LocalDate hari = LocalDate.of(2026, 10, 23);
        Pelanggan p = s.tambahPelanggan("Rafael", "081234567890");
        cek(p.getId().equals("P001"), "ID pelanggan pertama");
        Pengguna[] pengguna = {new Admin("A001", "Admin"), p};
        cek(pengguna[0].getPeran().equals("Admin") && pengguna[1].getPeran().equals("Pelanggan"), "Polymorphism");
        ditolak(() -> s.tambahPelanggan(" ", "081234567890"), "Nama kosong harus ditolak");
        ditolak(() -> s.tambahPelanggan("A", "------"), "Telepon tanpa angka harus ditolak");
        cek(s.getDaftarPelanggan().size() == 1, "Input gagal tidak menambah pelanggan");
        ditolak(() -> s.buatTransaksi("P999", "L02", new BigDecimal("3"), hari), "Pelanggan tidak ada");
        ditolak(() -> s.buatTransaksi("P001", "L02", BigDecimal.ZERO, hari), "Berat nol");
        ditolak(() -> s.buatTransaksi("P001", "L02", new BigDecimal("-1"), hari), "Berat negatif");
        ditolak(() -> s.buatTransaksi("P001", "L02", new BigDecimal("1.0001"), hari), "Presisi berat");
        TransaksiLaundry t = s.buatTransaksi("P001", "L02", new BigDecimal("3"), hari);
        cek(t.getKode().equals("TRX0001"), "Input gagal tidak menghabiskan ID transaksi");
        cek(t.getTotalBiaya().compareTo(new BigDecimal("21000")) == 0, "3 kg x 7000 = 21000");
        Invoice invoice = new Invoice(t);
        cek(invoice.getNomor().equals("INV-TRX0001"), "Nomor invoice mengacu pada transaksi");
        String belumLunas = invoice.cetak();
        cek(belumLunas.contains("BELUM LUNAS") && belumLunas.contains("belum menjadi bukti pelunasan"),
                "Invoice belum lunas tidak mengaku sebagai bukti pembayaran");
        cek(belumLunas.contains("Rafael") && belumLunas.contains("081234567890")
                && belumLunas.contains("Cuci Setrika"), "Identitas pelanggan dan layanan di invoice");
        cek(belumLunas.contains("3 kg") && belumLunas.contains("Rp7.000")
                && belumLunas.contains("Rp21.000"), "Berat, tarif, dan total invoice");
        cek(t.getEstimasiSelesai().equals(hari.plusDays(3)), "Estimasi layanan 3 hari");
        s.cariLayanan("L02").setHargaPerKg(new BigDecimal("8000"));
        cek(t.getTotalBiaya().compareTo(new BigDecimal("21000")) == 0, "Tarif transaksi lama tetap");
        cek(invoice.cetak().contains("Rp7.000"), "Invoice tetap menggunakan tarif saat transaksi");
        TransaksiLaundry baru = s.buatTransaksi("P001", "L02", new BigDecimal("1.25"), hari);
        cek(baru.getTotalBiaya().compareTo(new BigDecimal("10000")) == 0, "Tarif baru dan berat desimal");
        cek(s.hitungPendapatan(hari, hari).signum() == 0, "Belum lunas bukan pendapatan");
        ditolak(() -> t.bayar(new BigDecimal("20000"), hari), "Pembayaran kurang");
        ditolak(() -> t.bayar(new BigDecimal("21000.5"), hari), "Pembayaran pecahan rupiah");
        ditolak(() -> t.bayar(new BigDecimal("25000"), hari.minusDays(1)), "Tanggal pembayaran sebelum masuk");
        cek(!t.isLunas(), "Pembayaran gagal tidak mengubah status lunas");
        t.lanjutkanStatus(); cek(t.getStatus() == StatusCucian.DICUCI, "Proses cuci");
        t.lanjutkanStatus(); cek(t.getStatus() == StatusCucian.DISETRIKA, "Proses setrika");
        t.lanjutkanStatus(); cek(t.getStatus() == StatusCucian.SIAP_DIAMBIL, "Siap diambil");
        ditolak(t::lanjutkanStatus, "Belum lunas tidak boleh diserahkan");
        Pembayaran bayar = t.bayar(new BigDecimal("25000"), hari.plusDays(3));
        cek(bayar.getKembalian().compareTo(new BigDecimal("4000")) == 0, "Kembalian 4000");
        String lunas = invoice.cetak();
        cek(lunas.contains("LUNAS") && !lunas.contains("BELUM LUNAS")
                && lunas.contains("Pembayaran sudah diterima"), "Invoice diperbarui setelah pelunasan");
        cek(lunas.contains("Rp25.000") && lunas.contains("Rp4.000")
                && lunas.contains(hari.plusDays(3).toString()), "Uang tunai, kembalian dan tanggal bayar di invoice");
        cek(lunas.contains("Sisa tagihan        : Rp0"), "Sisa tagihan nol setelah lunas");
        ditolak(() -> t.bayar(new BigDecimal("25000"), hari.plusDays(3)), "Pembayaran ganda");
        t.lanjutkanStatus(); cek(t.getStatus() == StatusCucian.SELESAI, "Selesai setelah lunas");
        ditolak(t::lanjutkanStatus, "Tidak dapat melanjutkan transaksi selesai");
        cek(s.hitungPendapatan(hari, hari).signum() == 0, "Laporan menggunakan tanggal pembayaran");
        cek(s.hitungPendapatan(hari.plusDays(3), hari.plusDays(3)).compareTo(new BigDecimal("21000")) == 0,
                "Laporan mencatat tagihan, bukan uang sebelum kembalian");
        ditolak(() -> s.hitungPendapatan(hari.plusDays(1), hari), "Rentang laporan terbalik");
        TransaksiLaundry kering = s.buatTransaksi("P001", "L01", BigDecimal.ONE, hari);
        kering.lanjutkanStatus(); kering.lanjutkanStatus();
        cek(kering.getStatus() == StatusCucian.SIAP_DIAMBIL, "Cuci kering melewati setrika");
        TransaksiLaundry setrika = s.buatTransaksi("P001", "L03", BigDecimal.ONE, hari);
        setrika.lanjutkanStatus();
        cek(setrika.getStatus() == StatusCucian.DISETRIKA, "Setrika saja melewati cuci");
        System.out.println("LULUS: " + jumlah + " pemeriksaan logika bisnis.");
    }
}
