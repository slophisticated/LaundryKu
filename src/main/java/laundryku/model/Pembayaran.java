package laundryku.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class Pembayaran {
    private final LocalDate tanggal;
    private final BigDecimal tagihan;
    private final BigDecimal uangDiterima;

    public Pembayaran(LocalDate tanggal, BigDecimal tagihan, BigDecimal uangDiterima) {
        this.tanggal = Objects.requireNonNull(tanggal, "Tanggal wajib diisi.");
        if (tagihan == null || tagihan.signum() <= 0 || uangDiterima == null
                || uangDiterima.compareTo(tagihan) < 0) {
            throw new IllegalArgumentException("Uang pembayaran tidak boleh kurang dari total tagihan.");
        }
        if (uangDiterima.stripTrailingZeros().scale() > 0) {
            throw new IllegalArgumentException("Pembayaran harus berupa rupiah bulat.");
        }
        this.tagihan = tagihan;
        this.uangDiterima = uangDiterima;
    }

    public LocalDate getTanggal() { return tanggal; }
    public BigDecimal getTagihan() { return tagihan; }
    public BigDecimal getUangDiterima() { return uangDiterima; }
    public BigDecimal getKembalian() { return uangDiterima.subtract(tagihan); }
}
