package laundryku.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;
import laundryku.model.Admin;
import laundryku.model.LayananLaundry;
import laundryku.model.Pelanggan;
import laundryku.model.Pembayaran;
import laundryku.model.Pengguna;
import laundryku.model.TransaksiLaundry;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

/**
 * Semua aturan bisnis LaundryKu. Data disimpan ke database melalui Hibernate.
 * Setiap method membuka satu Session dan satu transaksi database; jika terjadi
 * error, perubahan dibatalkan (rollback) sehingga data tetap konsisten.
 */
public class LaundryService {
    public static final String ADMIN_DEFAULT_USERNAME = "admin";
    public static final String ADMIN_DEFAULT_PASSWORD = "admin123";

    private final SessionFactory sessionFactory;

    public LaundryService(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        isiDataAwal();
    }

    private <T> T tx(Function<Session, T> kerja) {
        return sessionFactory.fromTransaction(kerja);
    }

    /** Layanan contoh dan akun admin pertama dibuat jika tabel masih kosong. */
    private void isiDataAwal() {
        tx(s -> {
            if (hitung(s, "select count(l) from LayananLaundry l") == 0) {
                s.persist(new LayananLaundry("L01", "Cuci Kering", new BigDecimal("5000"), 2, true, false));
                s.persist(new LayananLaundry("L02", "Cuci Setrika", new BigDecimal("7000"), 3, true, true));
                s.persist(new LayananLaundry("L03", "Setrika Saja", new BigDecimal("4000"), 1, false, true));
                s.persist(new LayananLaundry("L04", "Express Cuci Setrika", new BigDecimal("10000"), 1, true, true));
            }
            if (hitung(s, "select count(a) from Admin a") == 0) {
                Admin admin = new Admin("A001", "Administrator");
                admin.aturAkun(ADMIN_DEFAULT_USERNAME, ADMIN_DEFAULT_PASSWORD);
                s.persist(admin);
            }
            return null;
        });
    }

    private static long hitung(Session s, String hql) {
        return s.createSelectionQuery(hql, Long.class).getSingleResult();
    }

    /** Membuat ID berikutnya dari angka terbesar yang pernah dipakai, misalnya P007 -> P008. */
    private static String idBerikutnya(Session s, String hql, String awalan, int digit) {
        int maks = 0;
        for (String id : s.createSelectionQuery(hql, String.class).getResultList()) {
            String angka = id.substring(awalan.length());
            if (angka.matches("[0-9]+")) { maks = Math.max(maks, Integer.parseInt(angka)); }
        }
        return awalan + String.format("%0" + digit + "d", maks + 1);
    }

    private static String teks(String nilai) {
        return nilai == null ? "" : nilai.trim();
    }

    // ===================== Login dan akun =====================

    public Pengguna login(String username, String password) {
        String u = teks(username).toLowerCase();
        if (u.isEmpty() || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Username dan password wajib diisi.");
        }
        Pengguna p = tx(s -> s.createSelectionQuery("from Pengguna p where p.username = :u", Pengguna.class)
                .setParameter("u", u).uniqueResult());
        if (p == null || !p.cekPassword(password)) {
            throw new IllegalArgumentException("Username atau password salah.");
        }
        return p;
    }

    private static void pastikanUsernameBebas(Session s, String username, String idPemilik) {
        String u = teks(username).toLowerCase();
        Long dipakai = s.createSelectionQuery(
                "select count(p) from Pengguna p where p.username = :u and p.id <> :id", Long.class)
                .setParameter("u", u).setParameter("id", idPemilik == null ? "" : idPemilik)
                .getSingleResult();
        if (dipakai > 0) {
            throw new IllegalArgumentException("Username '" + u + "' sudah dipakai.");
        }
    }

    public void gantiPassword(String idPengguna, String passwordLama, String passwordBaru) {
        tx(s -> {
            Pengguna p = s.get(Pengguna.class, idPengguna);
            if (p == null) { throw new IllegalArgumentException("Pengguna tidak ditemukan."); }
            p.gantiPassword(passwordLama, passwordBaru);
            return null;
        });
    }

    // ===================== Admin =====================

    public Admin tambahAdmin(String nama, String username, String password) {
        return tx(s -> {
            pastikanUsernameBebas(s, username, null);
            Admin admin = new Admin(idBerikutnya(s, "select a.id from Admin a", "A", 3), nama);
            admin.aturAkun(username, password);
            s.persist(admin);
            return admin;
        });
    }

    public void hapusAdmin(String id, String idYangLogin) {
        tx(s -> {
            if (id.equalsIgnoreCase(idYangLogin)) {
                throw new IllegalStateException("Admin tidak dapat menghapus akunnya sendiri.");
            }
            Admin admin = s.get(Admin.class, id);
            if (admin == null) { throw new IllegalArgumentException("Admin tidak ditemukan."); }
            if (hitung(s, "select count(a) from Admin a") <= 1) {
                throw new IllegalStateException("Minimal harus ada satu admin.");
            }
            s.remove(admin);
            return null;
        });
    }

    public List<Admin> getDaftarAdmin() {
        return tx(s -> s.createSelectionQuery("from Admin a order by a.id", Admin.class).getResultList());
    }

    // ===================== Pelanggan =====================

    public Pelanggan tambahPelanggan(String nama, String telepon) {
        return tambahPelanggan(nama, telepon, null, null);
    }

    /** Username boleh kosong jika pelanggan tidak perlu login. */
    public Pelanggan tambahPelanggan(String nama, String telepon, String username, String password) {
        return tx(s -> {
            Pelanggan baru = new Pelanggan(idBerikutnya(s, "select p.id from Pelanggan p", "P", 3), nama, telepon);
            if (!teks(username).isEmpty()) {
                pastikanUsernameBebas(s, username, null);
                baru.aturAkun(username, password);
            }
            s.persist(baru);
            return baru;
        });
    }

    /**
     * Mengubah data pelanggan. Username kosong berarti akun login dihapus;
     * password kosong berarti password lama tetap dipakai.
     */
    public Pelanggan ubahPelanggan(String id, String nama, String telepon, String username, String passwordBaru) {
        return tx(s -> {
            Pelanggan p = s.get(Pelanggan.class, teks(id));
            if (p == null) { throw new IllegalArgumentException("Pelanggan tidak ditemukan."); }
            p.setNama(nama);
            p.setNomorTelepon(telepon);
            if (teks(username).isEmpty()) {
                p.hapusAkun();
            } else {
                pastikanUsernameBebas(s, username, p.getId());
                p.aturAkun(username, passwordBaru);
            }
            return p;
        });
    }

    public void hapusPelanggan(String id) {
        tx(s -> {
            Pelanggan p = s.get(Pelanggan.class, teks(id));
            if (p == null) { throw new IllegalArgumentException("Pelanggan tidak ditemukan."); }
            long dipakai = s.createSelectionQuery(
                    "select count(t) from TransaksiLaundry t where t.pelanggan = :p", Long.class)
                    .setParameter("p", p).getSingleResult();
            if (dipakai > 0) {
                throw new IllegalStateException("Pelanggan memiliki " + dipakai + " transaksi sehingga tidak dapat dihapus.");
            }
            s.remove(p);
            return null;
        });
    }

    public Pelanggan cariPelanggan(String id) {
        Pelanggan p = tx(s -> s.get(Pelanggan.class, teks(id).toUpperCase()));
        if (p == null) { throw new IllegalArgumentException("Pelanggan tidak ditemukan."); }
        return p;
    }

    public List<Pelanggan> getDaftarPelanggan() {
        return tx(s -> s.createSelectionQuery("from Pelanggan p order by p.id", Pelanggan.class).getResultList());
    }

    // ===================== Layanan =====================

    public LayananLaundry tambahLayanan(String kode, String nama, BigDecimal harga, int durasiHari,
            boolean perluCuci, boolean perluSetrika) {
        String k = teks(kode).toUpperCase();
        if (!k.matches("[A-Z0-9]{2,10}")) {
            throw new IllegalArgumentException("Kode layanan 2-10 karakter huruf/angka, misalnya L05.");
        }
        return tx(s -> {
            if (s.get(LayananLaundry.class, k) != null) {
                throw new IllegalArgumentException("Kode layanan " + k + " sudah dipakai.");
            }
            LayananLaundry l = new LayananLaundry(k, nama, harga, durasiHari, perluCuci, perluSetrika);
            s.persist(l);
            return l;
        });
    }

    public LayananLaundry ubahLayanan(String kode, String nama, BigDecimal harga, int durasiHari,
            boolean perluCuci, boolean perluSetrika) {
        return tx(s -> {
            LayananLaundry l = s.get(LayananLaundry.class, teks(kode).toUpperCase());
            if (l == null) { throw new IllegalArgumentException("Layanan tidak ditemukan."); }
            l.ubah(nama, harga, durasiHari, perluCuci, perluSetrika);
            return l;
        });
    }

    public void hapusLayanan(String kode) {
        tx(s -> {
            LayananLaundry l = s.get(LayananLaundry.class, teks(kode).toUpperCase());
            if (l == null) { throw new IllegalArgumentException("Layanan tidak ditemukan."); }
            long dipakai = s.createSelectionQuery(
                    "select count(t) from TransaksiLaundry t where t.layanan = :l", Long.class)
                    .setParameter("l", l).getSingleResult();
            if (dipakai > 0) {
                throw new IllegalStateException("Layanan sudah dipakai " + dipakai + " transaksi sehingga tidak dapat dihapus.");
            }
            s.remove(l);
            return null;
        });
    }

    public LayananLaundry cariLayanan(String kode) {
        LayananLaundry l = tx(s -> s.get(LayananLaundry.class, teks(kode).toUpperCase()));
        if (l == null) { throw new IllegalArgumentException("Layanan tidak ditemukan."); }
        return l;
    }

    public List<LayananLaundry> getDaftarLayanan() {
        return tx(s -> s.createSelectionQuery("from LayananLaundry l order by l.kode", LayananLaundry.class)
                .getResultList());
    }

    // ===================== Transaksi =====================

    public TransaksiLaundry buatTransaksi(String idPelanggan, String kodeLayanan,
            BigDecimal berat, LocalDate tanggal) {
        return tx(s -> {
            Pelanggan p = s.get(Pelanggan.class, teks(idPelanggan).toUpperCase());
            if (p == null) { throw new IllegalArgumentException("Pelanggan tidak ditemukan."); }
            LayananLaundry l = s.get(LayananLaundry.class, teks(kodeLayanan).toUpperCase());
            if (l == null) { throw new IllegalArgumentException("Layanan tidak ditemukan."); }
            // Validasi dulu sebelum mengambil nomor, agar input gagal tidak menghabiskan kode transaksi.
            TransaksiLaundry baru = new TransaksiLaundry(
                    idBerikutnya(s, "select t.kode from TransaksiLaundry t", "TRX", 4), p, l, berat, tanggal);
            s.persist(baru);
            return baru;
        });
    }

    public TransaksiLaundry cariTransaksi(String kode) {
        TransaksiLaundry t = tx(s -> s.get(TransaksiLaundry.class, teks(kode).toUpperCase()));
        if (t == null) { throw new IllegalArgumentException("Transaksi tidak ditemukan."); }
        return t;
    }

    public TransaksiLaundry lanjutkanStatus(String kode) {
        return tx(s -> {
            TransaksiLaundry t = s.get(TransaksiLaundry.class, teks(kode).toUpperCase());
            if (t == null) { throw new IllegalArgumentException("Transaksi tidak ditemukan."); }
            t.lanjutkanStatus();
            return t;
        });
    }

    /** Pembayaran ikut tersimpan karena relasi transaksi -> pembayaran memakai cascade. */
    public Pembayaran bayar(String kode, BigDecimal nominal, LocalDate tanggal) {
        return tx(s -> {
            TransaksiLaundry t = s.get(TransaksiLaundry.class, teks(kode).toUpperCase());
            if (t == null) { throw new IllegalArgumentException("Transaksi tidak ditemukan."); }
            return t.bayar(nominal, tanggal);
        });
    }

    public List<TransaksiLaundry> getDaftarTransaksi() {
        return tx(s -> s.createSelectionQuery("from TransaksiLaundry t order by t.kode", TransaksiLaundry.class)
                .getResultList());
    }

    public List<TransaksiLaundry> getTransaksiPelanggan(String idPelanggan) {
        return tx(s -> s.createSelectionQuery(
                "from TransaksiLaundry t where t.pelanggan.id = :id order by t.kode", TransaksiLaundry.class)
                .setParameter("id", teks(idPelanggan)).getResultList());
    }

    // ===================== Laporan =====================

    /** Pendapatan = jumlah tagihan lunas berdasarkan tanggal pembayaran (kembalian tidak dihitung). */
    public BigDecimal hitungPendapatan(LocalDate mulai, LocalDate akhir) {
        if (mulai == null || akhir == null || akhir.isBefore(mulai)) {
            throw new IllegalArgumentException("Rentang tanggal laporan tidak valid.");
        }
        return tx(s -> s.createSelectionQuery(
                "select coalesce(sum(p.tagihan), 0) from Pembayaran p where p.tanggal between :a and :b",
                BigDecimal.class).setParameter("a", mulai).setParameter("b", akhir).getSingleResult());
    }

    public List<TransaksiLaundry> getTransaksiLunas(LocalDate mulai, LocalDate akhir) {
        if (mulai == null || akhir == null || akhir.isBefore(mulai)) {
            throw new IllegalArgumentException("Rentang tanggal laporan tidak valid.");
        }
        return tx(s -> s.createSelectionQuery(
                "from TransaksiLaundry t where t.pembayaran.tanggal between :a and :b "
                        + "order by t.pembayaran.tanggal, t.kode", TransaksiLaundry.class)
                .setParameter("a", mulai).setParameter("b", akhir).getResultList());
    }
}
