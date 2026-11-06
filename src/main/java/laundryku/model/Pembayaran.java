package laundryku.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "pembayaran")
public class Pembayaran {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate tanggal;

    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal tagihan;

    @Column(name = "uang_diterima", nullable = false, precision = 12, scale = 0)
    private BigDecimal uangDiterima;

    protected Pembayaran() { }

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

    public Long getId() { return id; }
    public LocalDate getTanggal() { return tanggal; }
    public BigDecimal getTagihan() { return tagihan; }
    public BigDecimal getUangDiterima() { return uangDiterima; }
    public BigDecimal getKembalian() { return uangDiterima.subtract(tagihan); }
}
