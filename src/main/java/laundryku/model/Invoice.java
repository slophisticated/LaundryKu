package laundryku.model;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

/** Invoice mengambil data transaksi agar biaya dan pembayaran tidak dihitung ulang. */
public class Invoice {
    private final TransaksiLaundry transaksi;

    public Invoice(TransaksiLaundry transaksi) {
        this.transaksi = Objects.requireNonNull(transaksi, "Transaksi invoice wajib diisi.");
    }

    public String getNomor() { return "INV-" + transaksi.getKode(); }

    public String cetak() {
        NumberFormat rupiah = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"));
        rupiah.setMaximumFractionDigits(0);
        rupiah.setMinimumFractionDigits(0);
        String garis = "============================================================";
        StringBuilder teks = new StringBuilder();
        teks.append(garis).append('\n');
        teks.append("                        LAUNDRYKU\n");
        teks.append("                    INVOICE TRANSAKSI\n");
        teks.append(garis).append('\n');
        baris(teks, "Nomor invoice", getNomor());
        baris(teks, "Kode transaksi", transaksi.getKode());
        baris(teks, "Tanggal masuk", transaksi.getTanggalMasuk().toString());
        baris(teks, "Estimasi selesai", transaksi.getEstimasiSelesai().toString());
        baris(teks, "ID pelanggan", transaksi.getPelanggan().getId());
        baris(teks, "Nama pelanggan", transaksi.getPelanggan().getNama());
        baris(teks, "Nomor telepon", transaksi.getPelanggan().getNomorTelepon());
        teks.append("------------------------------------------------------------\n");
        baris(teks, "Layanan", transaksi.getLayanan().getNama());
        baris(teks, "Berat cucian", transaksi.getBeratKg().stripTrailingZeros().toPlainString() + " kg");
        baris(teks, "Tarif per kg", rupiah.format(transaksi.getTarifSaatTransaksi()));
        baris(teks, "TOTAL TAGIHAN", rupiah.format(transaksi.getTotalBiaya()));
        teks.append("------------------------------------------------------------\n");
        baris(teks, "Status cucian", transaksi.getStatus().toString());
        baris(teks, "Status pembayaran", transaksi.isLunas() ? "LUNAS" : "BELUM LUNAS");
        if (transaksi.isLunas()) {
            Pembayaran pembayaran = transaksi.getPembayaran();
            baris(teks, "Tanggal bayar", pembayaran.getTanggal().toString());
            baris(teks, "Metode pembayaran", "Tunai");
            baris(teks, "Uang diterima", rupiah.format(pembayaran.getUangDiterima()));
            baris(teks, "Kembalian", rupiah.format(pembayaran.getKembalian()));
        } else {
            baris(teks, "Tanggal bayar", "-");
            baris(teks, "Uang diterima", rupiah.format(BigDecimal.ZERO));
            baris(teks, "Kembalian", rupiah.format(BigDecimal.ZERO));
        }
        baris(teks, "Sisa tagihan", rupiah.format(transaksi.isLunas() ? BigDecimal.ZERO : transaksi.getTotalBiaya()));
        teks.append(garis).append('\n');
        teks.append(transaksi.isLunas()
                ? "Pembayaran sudah diterima. Terima kasih.\n"
                : "Invoice ini adalah tagihan, belum menjadi bukti pelunasan.\n");
        teks.append("Sebutkan kode transaksi saat mengambil cucian.\n");
        return teks.toString();
    }

    private void baris(StringBuilder teks, String label, String nilai) {
        teks.append(String.format("%-19s : %s%n", label, nilai));
    }
}
