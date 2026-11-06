# LaundryKu OOP

Project RPL Karya Anak Bangsa

Aplikasi desktop Java (Swing) untuk bisnis laundry kiloan. Fiturnya: login admin dan pelanggan, data pelanggan, layanan, transaksi, status cucian, pembayaran tunai, invoice, dan rekap pendapatan. Data disimpan ke MySQL melalui Hibernate. Laporan PDF dengan JasperReports belum diterapkan pada tahap ini.

## Persiapan

1. Install **JDK 17 atau lebih baru** dan **MySQL 8**. Pastikan service MySQL berjalan.
2. Clone repository ini atau unduh ZIP lalu ekstrak.
3. Salin `db.properties.example` menjadi `db.properties` di folder project, lalu isi sesuai MySQL di komputer kamu:

   ```properties
   db.url=jdbc:mysql://localhost:3306/laundryku?createDatabaseIfNotExist=true
   db.username=root
   db.password=password_mysql_kamu
   ```

   `db.properties` tidak ikut di-commit karena berisi password. Database `laundryku` dan semua tabelnya dibuat otomatis saat aplikasi pertama kali dijalankan.

Jika koneksi gagal, aplikasi menampilkan pesan error beserta penyebabnya, misalnya password salah atau MySQL belum berjalan.

## Menjalankan

**NetBeans:** pilih **File > Open Project**, pilih folder yang berisi `pom.xml`, lalu tekan **F6**. Main class: `laundryku.Main`. Build pertama memerlukan internet untuk mengunduh library Maven.

**Windows:** jalankan `run.bat`. Jika JAR belum ada, script menjalankan build terlebih dahulu. Hasil build ada di `dist/LaundryKuOOP.jar` dan bisa dijalankan dengan `java -jar dist/LaundryKuOOP.jar`. Script mencari Maven pada PATH, lalu di instalasi Maven atau NetBeans bawaan.

## Akun awal

Saat database masih kosong, aplikasi membuat:

- Akun admin **`admin` / `admin123`**. Ganti password ini setelah login pertama melalui tombol **Ganti Password**.
- Empat layanan contoh:

| Kode | Layanan | Harga/kg | Estimasi | Proses |
|---|---|---|---|---|
| L01 | Cuci Kering | Rp5.000 | 2 hari | Cuci |
| L02 | Cuci Setrika | Rp7.000 | 3 hari | Cuci + Setrika |
| L03 | Setrika Saja | Rp4.000 | 1 hari | Setrika |
| L04 | Express Cuci Setrika | Rp10.000 | 1 hari | Cuci + Setrika |

Data awal hanya dibuat sekali, tidak dibuat ulang setiap aplikasi dijalankan.

## Contoh mencoba program

Langkah di bawah memakai database yang masih kosong. Pada tab berisi tabel, pilih baris terlebih dahulu sebelum menekan tombol Edit, Hapus, Lanjutkan Status, Bayar Tunai, atau Lihat Invoice.

1. **Login** dengan `admin` / `admin123`.
2. Tab **Pelanggan** > **Tambah**: isi nama `Budi`, telepon `081234567890`, username `budi`, password `budi123`. Pelanggan tersimpan dengan ID **P001**. Username dan password boleh dikosongkan jika pelanggan tidak perlu login.
3. Tab **Transaksi** > **Transaksi Baru**: pilih `P001 - Budi`, layanan `L02 - Cuci Setrika`, berat `3`. Perkiraan biaya **Rp21.000** langsung tampil saat mengetik berat. Klik OK, transaksi **TRX0001** dibuat dan invoice BELUM LUNAS ditampilkan.
4. Pilih TRX0001 > **Lanjutkan Status**. Ulangi untuk mengubah Diterima menjadi Dicuci, Disetrika, lalu Siap diambil.
5. Pilih TRX0001 > **Bayar Tunai**: isi `25000`. Status menjadi LUNAS, invoice diperbarui, dan kembalian **Rp4.000** ditampilkan.
6. **Lanjutkan Status** sekali lagi untuk menyerahkan cucian dengan status Selesai. Jika belum lunas, langkah ini ditolak.
7. Pilih TRX0001 > **Lihat Invoice** untuk mencetak ulang invoice. Mencetak ulang tidak membuat transaksi atau pembayaran baru.
8. Tab **Laporan Pendapatan**: isi tanggal awal dan akhir yang mencakup tanggal pembayaran (format `YYYY-MM-DD`), lalu klik **Tampilkan**. Pendapatan tercatat **Rp21.000**, bukan Rp25.000, karena kembalian tidak dihitung.
9. Tab **Layanan** > pilih L02 > **Edit**, ubah harga menjadi `8000`. Invoice TRX0001 tetap memakai tarif Rp7.000; tarif baru hanya berlaku untuk transaksi berikutnya.
10. **Logout**, lalu login sebagai `budi` / `budi123`. Pelanggan hanya melihat cucian dan invoice miliknya sendiri.

Data tetap tersimpan setelah aplikasi ditutup karena disimpan di MySQL.

**Format input:** uang tanpa pemisah ribuan (`21000`, bukan `21.000`). Berat boleh memakai koma atau titik desimal, misalnya `2,5` atau `2.5`, maksimal tiga angka desimal. Total tagihan dibulatkan ke rupiah terdekat dengan HALF_UP.

## Menu aplikasi

| Peran | Tab / tombol | Fungsi |
|---|---|---|
| Admin | Transaksi | Transaksi baru, lanjutkan status, bayar tunai, lihat invoice |
| Admin | Pelanggan | Tambah, edit, hapus pelanggan, dan atur akun login pelanggan |
| Admin | Layanan | Tambah, edit tarif/durasi/proses, dan hapus layanan |
| Admin | Laporan Pendapatan | Rekap pendapatan dan daftar transaksi lunas per rentang tanggal |
| Admin | Akun Admin | Tambah dan hapus akun admin |
| Pelanggan | Cucian Saya | Melihat status cucian dan invoice milik sendiri |
| Semua | Header | Ganti password dan logout |

Saat mengedit pelanggan, kosongkan password untuk mempertahankan password lama, atau kosongkan username untuk menghapus akun login pelanggan tersebut.

## Struktur project

```text
src/main/java/laundryku/
├── Main.java              Titik awal: koneksi database lalu membuka layar login
├── db/HibernateUtil.java  Membuat SessionFactory dari hibernate.cfg.xml + db.properties
├── model/                 Entity Hibernate (Pengguna, Admin, Pelanggan, Layanan, Transaksi, Pembayaran)
├── service/               LaundryService: aturan bisnis dan query database
├── ui/                    Layar Swing (login, menu admin, menu pelanggan, tab-tab)
└── util/Password.java     Hash password PBKDF2 + salt
src/main/resources/hibernate.cfg.xml   Konfigurasi Hibernate
src/test/java/laundryku/SelfCheck.java Pemeriksaan otomatis
```

## Class dan tanggung jawab

| Class | Tanggung jawab |
|---|---|
| `Main` | Menyiapkan koneksi database, tema tampilan, dan membuka `LoginFrame` |
| `HibernateUtil` | Membaca konfigurasi dan membuat `SessionFactory` Hibernate |
| `Pengguna` | Abstract entity berisi ID, nama, username, hash password, dan kontrak peran pengguna |
| `Admin` | Turunan Pengguna untuk peran admin |
| `Pelanggan` | Turunan Pengguna dengan nomor telepon |
| `LayananLaundry` | Tarif per kg, durasi, dan tahapan layanan |
| `TransaksiLaundry` | Pelanggan, layanan, berat, biaya, estimasi, status, dan pembayaran |
| `Pembayaran` | Pembayaran tunai, tanggal, tagihan, dan kembalian |
| `Invoice` | Menyusun teks invoice dari data transaksi (tidak disimpan ke database) |
| `StatusCucian` | Enum untuk status pengerjaan |
| `LaundryService` | Login, CRUD pelanggan/layanan/admin, transaksi, pembayaran, dan rekap pendapatan melalui Hibernate |
| `LoginFrame` | Layar login; membuka menu admin atau pelanggan sesuai peran |
| `MenuFrame` | Induk menu setelah login: header, ganti password, logout |
| `AdminFrame` / `PelangganFrame` | Menu utama untuk admin dan pelanggan |
| `TransaksiPanel`, `PelangganPanel`, `LayananPanel`, `LaporanPanel`, `AkunAdminPanel` | Isi setiap tab menu admin |
| `Ui` | Helper tampilan: format rupiah, validasi input, tabel, dialog, dan pesan error |
| `Password` | Membuat dan memeriksa hash password |

## Empat konsep OOP untuk dijelaskan

**Encapsulation:** atribut dibuat `private`, dibaca melalui getter, dan diubah melalui method yang memvalidasi aturan. Contoh: status transaksi tidak dapat diubah langsung; gunakan `lanjutkanStatus()`. Password hanya bisa diubah melalui `gantiPassword()` yang memeriksa password lama.

**Inheritance:** `Admin` dan `Pelanggan` menggunakan `extends Pengguna`. ID, nama, dan akun login diwarisi dari class induk. Pada GUI, `AdminFrame` dan `PelangganFrame` sama-sama `extends MenuFrame`.

**Abstraction:** `Pengguna` adalah `abstract class`. Method abstract `getPeran()` harus diimplementasikan oleh turunannya. Class Pengguna tidak dapat diinstansiasi langsung.

**Polymorphism:** `LaundryService.login()` mengembalikan tipe `Pengguna`. Hibernate membuat objek `Admin` atau `Pelanggan` sesuai kolom `peran`, lalu `LoginFrame` membuka menu yang sesuai. `getPeran()` dan `getProfil()` menghasilkan peran sesuai objek yang sebenarnya.

**Relasi objek:** TransaksiLaundry memiliki Pelanggan dan LayananLaundry sebagai asosiasi. TransaksiLaundry juga mengelola satu objek Pembayaran ketika transaksi dilunasi.

```text
                 Pengguna (abstract)
                  /              \
                Admin          Pelanggan
                                   |
                                   | 1 pelanggan dapat memiliki banyak transaksi
                                   v
LayananLaundry  <------------ TransaksiLaundry --------> Pembayaran
       1                      banyak transaksi           0 atau 1
                                   |
                              StatusCucian (enum)

LoginFrame --> AdminFrame / PelangganFrame --> LaundryService --> Hibernate --> MySQL
```

## Hibernate dan database

| Tabel | Entity | Keterangan |
|---|---|---|
| `pengguna` | `Pengguna`, `Admin`, `Pelanggan` | `@Inheritance(SINGLE_TABLE)`: admin dan pelanggan di satu tabel, dibedakan kolom `peran` (`ADMIN` / `PELANGGAN`) |
| `layanan_laundry` | `LayananLaundry` | Primary key `kode` |
| `transaksi_laundry` | `TransaksiLaundry` | `@ManyToOne` ke pelanggan (`pelanggan_id`) dan layanan (`layanan_kode`); status disimpan sebagai teks dengan `@Enumerated(STRING)` |
| `pembayaran` | `Pembayaran` | `@OneToOne` dari transaksi (`pembayaran_id`) dengan cascade, sehingga ikut tersimpan saat transaksi dibayar; ID otomatis (`IDENTITY`) |

- `hibernate.cfg.xml` berisi driver MySQL, daftar entity, dan `hbm2ddl.auto=update` sehingga tabel dibuat/ditambah kolom otomatis tanpa menghapus data.
- `HibernateUtil` menimpa URL, username, dan password dari `db.properties`.
- Setiap method `LaundryService` berjalan dalam satu transaksi database (`sessionFactory.fromTransaction`). Jika aturan bisnis menolak, misalnya uang kurang, perubahan dibatalkan (rollback).
- Query memakai HQL, misalnya `from Pengguna p where p.username = :u` untuk login dan `sum(p.tagihan)` untuk laporan pendapatan.
- Set `db.show_sql=true` di `db.properties` untuk melihat SQL yang dibuat Hibernate di console.

## Aturan bisnis yang diterapkan

### Login dan akun

- Username 3-30 karakter (huruf, angka, titik, garis bawah), unik, dan tidak membedakan huruf besar/kecil.
- Password minimal 6 karakter dan disimpan sebagai hash PBKDF2 + salt, bukan teks asli.
- Admin tidak dapat menghapus akunnya sendiri, dan minimal harus ada satu admin.
- Pelanggan hanya dapat melihat transaksi dan invoice miliknya sendiri.

### Invoice transaksi

Invoice menggunakan nomor `INV-` diikuti kode transaksi. Isinya adalah identitas pelanggan, nomor telepon, tanggal masuk, estimasi selesai, layanan, berat, tarif saat transaksi, total tagihan, status cucian, dan status pembayaran. Setelah lunas, invoice juga menampilkan tanggal pembayaran, uang diterima, serta kembalian. Invoice yang belum dibayar diberi keterangan BELUM LUNAS dan sisa tagihan.

Class `Invoice` memiliki asosiasi ke satu `TransaksiLaundry`; nilai biaya diambil dari transaksi, bukan dihitung ulang dari tarif layanan terbaru. Method `cetak()` menghasilkan teks yang ditampilkan di jendela invoice. Tahap ini belum mengekspor PDF atau memakai JasperReports.

### Transaksi dan pembayaran

- Berat dan tarif harus positif; pelanggan dan layanan harus terdaftar.
- Transaksi menyalin tarif saat pencatatan sehingga perubahan tarif tidak mengubah biaya transaksi lama.
- Status berjalan sesuai jenis layanan. Cuci Kering melewati setrika; Setrika Saja melewati cuci.
- Tagihan harus lunas sebelum cucian diserahkan dengan status Selesai.
- Pembayaran kurang, pembayaran ganda, dan tanggal pembayaran sebelum tanggal masuk ditolak.
- Pendapatan dihitung berdasarkan tanggal pembayaran dan nilai tagihan yang lunas; kembalian tidak dihitung sebagai pendapatan.
- Pelanggan atau layanan yang sudah dipakai transaksi tidak dapat dihapus agar riwayat transaksi tetap utuh.
- Kode baru (P001, TRX0001, A001) melanjutkan nomor terbesar yang sudah ada di database.

## Build dan pemeriksaan

```bat
build.bat
test.bat
run.bat
```

`test.bat` menjalankan SelfCheck tanpa JUnit. SelfCheck memakai database **H2 di memori**, sehingga data MySQL tidak tersentuh dan MySQL tidak perlu berjalan. Pemeriksaan mencakup login dan akun, hitungan biaya, perubahan tarif, validasi input, pembayaran, status layanan, laporan, CRUD layanan/pelanggan/admin, serta penyimpanan data melalui Hibernate.

Alternatif melalui Maven:

```text
mvn compile exec:java
```

## Batas tahap ini

- Belum ada registrasi akun mandiri; akun pelanggan dibuat oleh admin.
- Pembayaran hanya tunai.
- Invoice dan laporan ditampilkan di layar, belum diekspor ke PDF (rencana: JasperReports).
- Belum ada fitur reset password jika admin lupa password.
