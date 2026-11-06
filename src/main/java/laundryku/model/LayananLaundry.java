package laundryku.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "layanan_laundry")
public class LayananLaundry {
    @Id
    @Column(length = 10)
    private String kode;

    @Column(nullable = false, length = 60)
    private String nama;

    @Column(name = "harga_per_kg", nullable = false, precision = 12, scale = 0)
    private BigDecimal hargaPerKg;

    @Column(name = "durasi_hari", nullable = false)
    private int durasiHari;

    @Column(name = "perlu_cuci", nullable = false)
    private boolean perluCuci;

    @Column(name = "perlu_setrika", nullable = false)
    private boolean perluSetrika;

    protected LayananLaundry() { }

    public LayananLaundry(String kode, String nama, BigDecimal hargaPerKg,
            int durasiHari, boolean perluCuci, boolean perluSetrika) {
        if (kode == null || kode.isBlank()) {
            throw new IllegalArgumentException("Kode dan nama layanan wajib diisi.");
        }
        this.kode = kode.trim();
        ubah(nama, hargaPerKg, durasiHari, perluCuci, perluSetrika);
    }

    /** Mengubah data layanan. Transaksi lama tidak terpengaruh karena menyalin tarif sendiri. */
    public void ubah(String nama, BigDecimal hargaPerKg, int durasiHari,
            boolean perluCuci, boolean perluSetrika) {
        if (nama == null || nama.isBlank()) {
            throw new IllegalArgumentException("Kode dan nama layanan wajib diisi.");
        }
        if (nama.trim().length() > 60) {
            throw new IllegalArgumentException("Nama layanan maksimal 60 karakter.");
        }
        if (durasiHari < 1 || (!perluCuci && !perluSetrika)) {
            throw new IllegalArgumentException("Durasi minimal 1 hari dan layanan harus memiliki proses pengerjaan.");
        }
        setHargaPerKg(hargaPerKg);
        this.nama = nama.trim();
        this.durasiHari = durasiHari;
        this.perluCuci = perluCuci;
        this.perluSetrika = perluSetrika;
    }

    public void setHargaPerKg(BigDecimal harga) {
        if (harga == null || harga.signum() <= 0) {
            throw new IllegalArgumentException("Harga layanan harus lebih dari nol.");
        }
        try {
            this.hargaPerKg = harga.setScale(0, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Harga layanan harus berupa rupiah bulat.");
        }
    }

    public String getKode() { return kode; }
    public String getNama() { return nama; }
    public BigDecimal getHargaPerKg() { return hargaPerKg; }
    public int getDurasiHari() { return durasiHari; }
    public boolean isPerluCuci() { return perluCuci; }
    public boolean isPerluSetrika() { return perluSetrika; }

    public String getProses() {
        if (perluCuci && perluSetrika) { return "Cuci + Setrika"; }
        return perluCuci ? "Cuci" : "Setrika";
    }

    @Override
    public String toString() { return kode + " - " + nama; }
}
