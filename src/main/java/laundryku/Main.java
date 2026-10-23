package laundryku;

import laundryku.model.*;
import laundryku.service.LaundryService;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Scanner;

public class Main {
    private final Scanner input = new Scanner(System.in);
    private final LaundryService laundry = new LaundryService();
    private final NumberFormat rupiah = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"));

    public static void main(String[] args) { new Main().jalankan(); }

    private void jalankan() {
        System.out.println("LAUNDRYKU - Praktikum OOP Rafael Ramdani / 51423176");
        System.out.println("Mode admin console. Data hanya tersimpan selama program berjalan.");
        cetakProfil(new Admin("A001", "Rafael Ramdani"));
        Pelanggan contoh = laundry.tambahPelanggan("Pelanggan Contoh", "081234567890");
        cetakProfil(contoh);
        while (true) {
            System.out.println("\n=== MENU LAUNDRYKU ===");
            System.out.println("1. Tambah pelanggan\n2. Lihat pelanggan\n3. Lihat layanan dan harga");
            System.out.println("4. Buat transaksi cucian\n5. Lihat transaksi dan detail");
            System.out.println("6. Lanjutkan status cucian\n7. Pembayaran tunai\n8. Rekap pendapatan\n9. Cetak invoice transaksi\n0. Keluar");
            String menu = baca("Pilih menu: ");
            if (menu == null || menu.equals("0")) {
                System.out.println("Program selesai. Data sesi ini tidak disimpan.");
                return;
            }
            try {
                switch (menu) {
                    case "1": tambahPelanggan(); break;
                    case "2": lihatPelanggan(); break;
                    case "3": lihatLayanan(); break;
                    case "4": buatTransaksi(); break;
                    case "5": lihatTransaksi(); break;
                    case "6": lanjutkanStatus(); break;
                    case "7": bayar(); break;
                    case "8": laporan(); break;
                    case "9": cetakInvoice(); break;
                    default: System.out.println("Pilih angka menu yang tersedia.");
                }
            } catch (DateTimeParseException e) {
                System.out.println("Tanggal tidak valid. Gunakan format YYYY-MM-DD.");
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("Tidak berhasil: " + e.getMessage());
            }
        }
    }

    // Satu tipe induk menerima Admin maupun Pelanggan (polymorphism).
    private void cetakProfil(Pengguna pengguna) { System.out.println(pengguna.getProfil()); }

    private String baca(String label) {
        System.out.print(label);
        return input.hasNextLine() ? input.nextLine().trim() : null;
    }

    private String wajib(String label) {
        String teks = baca(label);
        if (teks == null) { throw new IllegalStateException("Input berakhir."); }
        if (teks.isBlank()) { throw new IllegalArgumentException("Input tidak boleh kosong."); }
        return teks;
    }

    private BigDecimal angka(String label) {
        String teks = wajib(label);
        if (!teks.matches("[0-9]+([.,][0-9]+)?")) {
            throw new IllegalArgumentException("Masukkan angka tanpa pemisah ribuan, misalnya 3,5 atau 21000.");
        }
        return new BigDecimal(teks.replace(',', '.'));
    }

    private String uang(BigDecimal nilai) { return rupiah.format(nilai); }

    private void tambahPelanggan() {
        String nama = wajib("Nama: ");
        String telepon = wajib("Nomor telepon: ");
        Pelanggan p = laundry.tambahPelanggan(nama, telepon);
        System.out.println("Pelanggan tersimpan: " + p.getId());
    }

    private void lihatPelanggan() {
        System.out.println("\nID | NAMA | TELEPON");
        for (Pelanggan p : laundry.getDaftarPelanggan()) {
            System.out.println(p.getId() + " | " + p.getNama() + " | " + p.getNomorTelepon());
        }
    }

    private void lihatLayanan() {
        System.out.println("\nKODE | LAYANAN | HARGA/KG | ESTIMASI");
        for (LayananLaundry l : laundry.getDaftarLayanan()) {
            System.out.println(l.getKode() + " | " + l.getNama() + " | "
                    + uang(l.getHargaPerKg()) + " | " + l.getDurasiHari() + " hari");
        }
        System.out.println("Tarif di atas adalah contoh untuk praktikum.");
    }

    private void buatTransaksi() {
        lihatPelanggan();
        String pelanggan = wajib("ID pelanggan: ");
        lihatLayanan();
        String layanan = wajib("Kode layanan: ");
        BigDecimal berat = angka("Berat (kg, maksimal 3 desimal): ");
        TransaksiLaundry t = laundry.buatTransaksi(pelanggan, layanan, berat, LocalDate.now());
        System.out.println("Transaksi berhasil dibuat.");
        System.out.println(new Invoice(t).cetak());
    }

    private void lihatTransaksi() {
        if (laundry.getDaftarTransaksi().isEmpty()) {
            System.out.println("Belum ada transaksi."); return;
        }
        for (TransaksiLaundry t : laundry.getDaftarTransaksi()) {
            System.out.println(t.getKode() + " | " + t.getPelanggan().getNama() + " | "
                    + t.getStatus() + " | " + uang(t.getTotalBiaya()) + " | "
                    + (t.isLunas() ? "LUNAS" : "BELUM LUNAS"));
        }
        String kode = baca("Kode untuk detail (Enter untuk kembali): ");
        if (kode != null && !kode.isBlank()) { detail(laundry.cariTransaksi(kode)); }
    }

    private void detail(TransaksiLaundry t) {
        System.out.println("\nKode             : " + t.getKode());
        System.out.println("Pelanggan        : " + t.getPelanggan().getNama());
        System.out.println("Layanan          : " + t.getLayanan().getNama());
        System.out.println("Berat            : " + t.getBeratKg().toPlainString() + " kg");
        System.out.println("Tarif saat masuk : " + uang(t.getTarifSaatTransaksi()) + "/kg");
        System.out.println("Total biaya      : " + uang(t.getTotalBiaya()));
        System.out.println("Tanggal masuk    : " + t.getTanggalMasuk());
        System.out.println("Estimasi selesai : " + t.getEstimasiSelesai());
        System.out.println("Status cucian    : " + t.getStatus());
        System.out.println("Pembayaran       : " + (t.isLunas() ? "LUNAS" : "BELUM LUNAS"));
    }

    private void lanjutkanStatus() {
        TransaksiLaundry t = laundry.cariTransaksi(wajib("Kode transaksi: "));
        System.out.println(t.getStatus() + " -> " + t.getStatusBerikutnya());
        if ("y".equalsIgnoreCase(baca("Lanjutkan? (y/n): "))) {
            t.lanjutkanStatus();
            System.out.println("Status sekarang: " + t.getStatus());
        }
    }

    private void bayar() {
        TransaksiLaundry t = laundry.cariTransaksi(wajib("Kode transaksi: "));
        if (t.isLunas()) { throw new IllegalStateException("Transaksi sudah lunas."); }
        System.out.println("Tagihan: " + uang(t.getTotalBiaya()));
        Pembayaran p = t.bayar(angka("Uang tunai diterima (tanpa titik ribuan): "), LocalDate.now());
        System.out.println("Pembayaran berhasil. Kembalian: " + uang(p.getKembalian()));
        System.out.println(new Invoice(t).cetak());
    }

    private void cetakInvoice() {
        TransaksiLaundry t = laundry.cariTransaksi(wajib("Kode transaksi untuk invoice: "));
        System.out.println(new Invoice(t).cetak());
    }

    private void laporan() {
        LocalDate mulai = LocalDate.parse(wajib("Tanggal awal (YYYY-MM-DD): "));
        LocalDate akhir = LocalDate.parse(wajib("Tanggal akhir (YYYY-MM-DD): "));
        System.out.println("Pendapatan " + mulai + " s.d. " + akhir + ": "
                + uang(laundry.hitungPendapatan(mulai, akhir)));
        System.out.println("Berdasarkan tagihan lunas pada tanggal pembayaran; uang kembalian tidak dihitung.");
    }
}
