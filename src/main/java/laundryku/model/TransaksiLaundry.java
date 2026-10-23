package laundryku.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

public class TransaksiLaundry {
    private final String kode;
    private final Pelanggan pelanggan;
    private final LayananLaundry layanan;
    private final BigDecimal beratKg;
    private final BigDecimal tarifSaatTransaksi;
    private final BigDecimal totalBiaya;
    private final LocalDate tanggalMasuk;
    private final LocalDate estimasiSelesai;
    private StatusCucian status = StatusCucian.DITERIMA;
    private Pembayaran pembayaran;

    public TransaksiLaundry(String kode, Pelanggan pelanggan, LayananLaundry layanan,
            BigDecimal beratKg, LocalDate tanggalMasuk) {
        if (kode == null || kode.isBlank()) {
            throw new IllegalArgumentException("Kode transaksi wajib diisi.");
        }
        if (beratKg == null || beratKg.signum() <= 0
                || beratKg.stripTrailingZeros().scale() > 3) {
            throw new IllegalArgumentException("Berat harus positif dengan maksimal 3 angka desimal.");
        }
        this.kode = kode;
        this.pelanggan = Objects.requireNonNull(pelanggan, "Pelanggan wajib dipilih.");
        this.layanan = Objects.requireNonNull(layanan, "Layanan wajib dipilih.");
        this.beratKg = beratKg;
        this.tanggalMasuk = Objects.requireNonNull(tanggalMasuk, "Tanggal wajib diisi.");
        // Tarif disalin agar perubahan harga layanan tidak mengubah transaksi lama.
        this.tarifSaatTransaksi = layanan.getHargaPerKg();
        this.totalBiaya = beratKg.multiply(tarifSaatTransaksi).setScale(0, RoundingMode.HALF_UP);
        if (totalBiaya.signum() <= 0) {
            throw new IllegalArgumentException("Total tagihan minimal Rp1 setelah pembulatan.");
        }
        this.estimasiSelesai = tanggalMasuk.plusDays(layanan.getDurasiHari());
    }

    public StatusCucian getStatusBerikutnya() {
        switch (status) {
            case DITERIMA:
                return layanan.isPerluCuci() ? StatusCucian.DICUCI : StatusCucian.DISETRIKA;
            case DICUCI:
                return layanan.isPerluSetrika() ? StatusCucian.DISETRIKA : StatusCucian.SIAP_DIAMBIL;
            case DISETRIKA:
                return StatusCucian.SIAP_DIAMBIL;
            case SIAP_DIAMBIL:
                return StatusCucian.SELESAI;
            default:
                throw new IllegalStateException("Transaksi sudah selesai.");
        }
    }

    public void lanjutkanStatus() {
        StatusCucian berikutnya = getStatusBerikutnya();
        if (berikutnya == StatusCucian.SELESAI && !isLunas()) {
            throw new IllegalStateException("Lunasi tagihan sebelum cucian diserahkan.");
        }
        status = berikutnya;
    }

    public Pembayaran bayar(BigDecimal nominal, LocalDate tanggal) {
        if (isLunas()) { throw new IllegalStateException("Transaksi ini sudah lunas."); }
        Objects.requireNonNull(tanggal, "Tanggal pembayaran wajib diisi.");
        if (tanggal.isBefore(tanggalMasuk)) {
            throw new IllegalArgumentException("Tanggal pembayaran tidak boleh sebelum tanggal masuk.");
        }
        pembayaran = new Pembayaran(tanggal, totalBiaya, nominal);
        return pembayaran;
    }

    public String getKode() { return kode; }
    public Pelanggan getPelanggan() { return pelanggan; }
    public LayananLaundry getLayanan() { return layanan; }
    public BigDecimal getBeratKg() { return beratKg; }
    public BigDecimal getTarifSaatTransaksi() { return tarifSaatTransaksi; }
    public BigDecimal getTotalBiaya() { return totalBiaya; }
    public LocalDate getTanggalMasuk() { return tanggalMasuk; }
    public LocalDate getEstimasiSelesai() { return estimasiSelesai; }
    public StatusCucian getStatus() { return status; }
    public boolean isLunas() { return pembayaran != null; }
    public Pembayaran getPembayaran() { return pembayaran; }
}
