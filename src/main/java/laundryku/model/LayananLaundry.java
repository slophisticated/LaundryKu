package laundryku.model;

import java.math.BigDecimal;

public class LayananLaundry {
    private final String kode;
    private final String nama;
    private BigDecimal hargaPerKg;
    private final int durasiHari;
    private final boolean perluCuci;
    private final boolean perluSetrika;

    public LayananLaundry(String kode, String nama, BigDecimal hargaPerKg,
            int durasiHari, boolean perluCuci, boolean perluSetrika) {
        if (kode == null || kode.isBlank() || nama == null || nama.isBlank()) {
            throw new IllegalArgumentException("Kode dan nama layanan wajib diisi.");
        }
        if (durasiHari < 1 || (!perluCuci && !perluSetrika)) {
            throw new IllegalArgumentException("Durasi minimal 1 hari dan layanan harus memiliki proses pengerjaan.");
        }
        this.kode = kode.trim();
        this.nama = nama.trim();
        setHargaPerKg(hargaPerKg);
        this.durasiHari = durasiHari;
        this.perluCuci = perluCuci;
        this.perluSetrika = perluSetrika;
    }

    public void setHargaPerKg(BigDecimal harga) {
        if (harga == null || harga.signum() <= 0) {
            throw new IllegalArgumentException("Harga layanan harus lebih dari nol.");
        }
        try {
            this.hargaPerKg = harga.setScale(0, java.math.RoundingMode.UNNECESSARY);
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
}
