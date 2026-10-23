package laundryku.service;

import laundryku.model.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LaundryService {
    // shortcut: data hanya di memori; tambahkan file atau database saat tahap penyimpanan diminta.
    private final List<Pelanggan> pelanggan = new ArrayList<>();
    private final List<LayananLaundry> layanan = new ArrayList<>();
    private final List<TransaksiLaundry> transaksi = new ArrayList<>();

    public LaundryService() {
        layanan.add(new LayananLaundry("L01", "Cuci Kering", new BigDecimal("5000"), 2, true, false));
        layanan.add(new LayananLaundry("L02", "Cuci Setrika", new BigDecimal("7000"), 3, true, true));
        layanan.add(new LayananLaundry("L03", "Setrika Saja", new BigDecimal("4000"), 1, false, true));
        layanan.add(new LayananLaundry("L04", "Express Cuci Setrika", new BigDecimal("10000"), 1, true, true));
    }

    public Pelanggan tambahPelanggan(String nama, String telepon) {
        Pelanggan baru = new Pelanggan(String.format("P%03d", pelanggan.size() + 1), nama, telepon);
        pelanggan.add(baru);
        return baru;
    }

    public Pelanggan cariPelanggan(String id) {
        return pelanggan.stream().filter(p -> p.getId().equalsIgnoreCase(id.trim())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Pelanggan tidak ditemukan."));
    }

    public LayananLaundry cariLayanan(String kode) {
        return layanan.stream().filter(l -> l.getKode().equalsIgnoreCase(kode.trim())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Layanan tidak ditemukan."));
    }

    public TransaksiLaundry buatTransaksi(String idPelanggan, String kodeLayanan,
            BigDecimal berat, LocalDate tanggal) {
        TransaksiLaundry baru = new TransaksiLaundry(String.format("TRX%04d", transaksi.size() + 1),
                cariPelanggan(idPelanggan), cariLayanan(kodeLayanan), berat, tanggal);
        transaksi.add(baru);
        return baru;
    }

    public TransaksiLaundry cariTransaksi(String kode) {
        return transaksi.stream().filter(t -> t.getKode().equalsIgnoreCase(kode.trim())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Transaksi tidak ditemukan."));
    }

    public BigDecimal hitungPendapatan(LocalDate mulai, LocalDate akhir) {
        if (mulai == null || akhir == null || akhir.isBefore(mulai)) {
            throw new IllegalArgumentException("Rentang tanggal laporan tidak valid.");
        }
        return transaksi.stream().filter(TransaksiLaundry::isLunas)
                .map(TransaksiLaundry::getPembayaran)
                .filter(p -> !p.getTanggal().isBefore(mulai) && !p.getTanggal().isAfter(akhir))
                .map(Pembayaran::getTagihan).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<Pelanggan> getDaftarPelanggan() { return Collections.unmodifiableList(pelanggan); }
    public List<LayananLaundry> getDaftarLayanan() { return Collections.unmodifiableList(layanan); }
    public List<TransaksiLaundry> getDaftarTransaksi() { return Collections.unmodifiableList(transaksi); }
}
