package laundryku.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "transaksi_laundry")
public class TransaksiLaundry {
    @Id
    @Column(length = 12)
    private String kode;

    // Banyak transaksi dapat dimiliki satu pelanggan (foreign key pelanggan_id).
    @ManyToOne(optional = false)
    @JoinColumn(name = "pelanggan_id", nullable = false)
    private Pelanggan pelanggan;

    // Banyak transaksi dapat memakai satu layanan (foreign key layanan_kode).
    @ManyToOne(optional = false)
    @JoinColumn(name = "layanan_kode", nullable = false)
    private LayananLaundry layanan;

    @Column(name = "berat_kg", nullable = false, precision = 10, scale = 3)
    private BigDecimal beratKg;

    @Column(name = "tarif_saat_transaksi", nullable = false, precision = 12, scale = 0)
    private BigDecimal tarifSaatTransaksi;

    @Column(name = "total_biaya", nullable = false, precision = 12, scale = 0)
    private BigDecimal totalBiaya;

    @Column(name = "tanggal_masuk", nullable = false)
    private LocalDate tanggalMasuk;

    @Column(name = "estimasi_selesai", nullable = false)
    private LocalDate estimasiSelesai;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusCucian status = StatusCucian.DITERIMA;

    // Satu transaksi memiliki 0 atau 1 pembayaran; ikut tersimpan saat transaksi disimpan.
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "pembayaran_id", unique = true)
    private Pembayaran pembayaran;

    protected TransaksiLaundry() { }

    public TransaksiLaundry(String kode, Pelanggan pelanggan, LayananLaundry layanan,
            BigDecimal beratKg, LocalDate tanggalMasuk) {
        if (kode == null || kode.isBlank()) {
            throw new IllegalArgumentException("Kode transaksi wajib diisi.");
        }
        if (beratKg == null || beratKg.signum() <= 0
                || beratKg.stripTrailingZeros().scale() > 3) {
            throw new IllegalArgumentException("Berat harus positif dengan maksimal 3 angka desimal.");
        }
        if (beratKg.compareTo(new BigDecimal("1000")) >= 0) {
            throw new IllegalArgumentException("Berat harus kurang dari 1000 kg.");
        }
        this.kode = kode;
        this.pelanggan = Objects.requireNonNull(pelanggan, "Pelanggan wajib dipilih.");
        this.layanan = Objects.requireNonNull(layanan, "Layanan wajib dipilih.");
        this.beratKg = beratKg;
        this.tanggalMasuk = Objects.requireNonNull(tanggalMasuk, "Tanggal wajib diisi.");
        // Tarif disalin agar perubahan harga layanan tidak mengubah transaksi lama.
        this.tarifSaatTransaksi = layanan.getHargaPerKg();
        this.totalBiaya = hitungTotal(beratKg, tarifSaatTransaksi);
        if (totalBiaya.signum() <= 0) {
            throw new IllegalArgumentException("Total tagihan minimal Rp1 setelah pembulatan.");
        }
        this.estimasiSelesai = tanggalMasuk.plusDays(layanan.getDurasiHari());
    }

    public static BigDecimal hitungTotal(BigDecimal beratKg, BigDecimal tarif) {
        return beratKg.multiply(tarif).setScale(0, RoundingMode.HALF_UP);
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
