# LaundryKu OOP

Project RPL Karya Anak Bangsa

Project Java console untuk tahap awal OOP bisnis laundry kiloan. Model bisnis LaundryKu mencakup pelanggan, layanan, transaksi, status cucian, pembayaran tunai, invoice, dan rekap pendapatan. Arah pengembangan selanjutnya adalah aplikasi desktop Java di NetBeans dengan MySQL, Hibernate, dan JasperReports untuk laporan PDF. Integrasi tersebut belum diterapkan pada tahap OOP ini.

## Buka di NetBeans

1. Clone repository ini atau unduh ZIP lalu ekstrak terlebih dahulu.
2. Pilih **File > Open Project**, lalu pilih folder project yang berisi `pom.xml`.
3. Gunakan JDK **17 atau lebih baru** sebagai Java Platform project.
4. Klik kanan project lalu pilih **Run**, atau tekan **F6**. Main class: `laundryku.Main`.
5. Masukkan pilihan menu pada panel Output saat program meminta input.

Project memakai Maven. Pembukaan/build pertama melalui NetBeans mungkin memerlukan internet untuk mengunduh plugin build Maven. Kode aplikasinya hanya memakai library standar Java.

Jika console NetBeans tidak menerima ketikan, jalankan **run.bat** di folder project untuk membuka menu di terminal Windows. Jika JAR belum tersedia, script menjalankan build terlebih dahulu. Hasil build ada di `dist/LaundryKuOOP.jar`; bisa juga dijalankan dengan `java -jar dist/LaundryKuOOP.jar` dari terminal. Folder hasil build tidak disimpan di repository.

## Contoh mencoba program

Satu pelanggan contoh dengan ID **P001** sudah tersedia pada awal sesi. Tarif di bawah merupakan data contoh praktikum.

1. Menu **3** menampilkan layanan. **L02** adalah Cuci Setrika seharga Rp7.000/kg dengan estimasi 3 hari.
2. Menu **4**: isi pelanggan `P001`, layanan `L02`, dan berat `3`. Transaksi **TRX0001** memiliki tagihan **Rp21.000**.
3. Menu **5** menampilkan daftar transaksi. Ketik `TRX0001` untuk detail, atau Enter untuk kembali.
4. Menu **6**: ketik `TRX0001`, lalu `y`. Ulangi untuk mengubah Diterima menjadi Dicuci, Disetrika, lalu Siap diambil.
5. Menu **7**: ketik `TRX0001` dan uang tunai `25000`. Status pembayaran menjadi lunas; kembaliannya **Rp4.000**.
6. Menu **6** sekali lagi untuk menyerahkan cucian dan menetapkan status Selesai.
7. Menu **9**: ketik `TRX0001` untuk mencetak ulang invoice. Invoice juga otomatis tampil saat transaksi dibuat dan pembayaran berhasil.
8. Menu **8**: masukkan tanggal pembayaran sebagai tanggal awal dan akhir dalam format `YYYY-MM-DD`. Pendapatan tercatat **Rp21.000**.
9. Menu **0** untuk keluar.

Input uang tanpa pemisah ribuan: `21000`, bukan `21.000`. Berat boleh menggunakan koma atau titik desimal, misalnya `2,5` atau `2.5`, maksimal tiga angka desimal. Total tagihan dibulatkan ke rupiah terdekat dengan HALF_UP.

## Class dan tanggung jawab

| Class | Tanggung jawab |
|---|---|
| `Main` | Menu console, membaca input, dan menampilkan hasil |
| `Pengguna` | Abstract class berisi ID, nama, dan kontrak peran pengguna |
| `Admin` | Turunan Pengguna untuk peran admin |
| `Pelanggan` | Turunan Pengguna dengan nomor telepon |
| `LayananLaundry` | Tarif per kg, durasi, dan tahapan layanan |
| `TransaksiLaundry` | Pelanggan, layanan, berat, biaya, estimasi, status, dan pembayaran |
| `Pembayaran` | Pembayaran tunai, tanggal, tagihan, dan kembalian |
| `Invoice` | Menyusun invoice per transaksi dari data pelanggan, layanan, tagihan, status, dan pembayaran |
| `StatusCucian` | Enum untuk status pengerjaan |
| `LaundryService` | Koleksi pelanggan/layanan/transaksi, pencarian, pembuatan transaksi, dan rekap pendapatan |

## Empat konsep OOP untuk dijelaskan

**Encapsulation:** atribut dibuat `private`, dibaca melalui getter, dan diubah melalui method yang memvalidasi aturan. Contoh: status transaksi tidak dapat diubah langsung; gunakan `lanjutkanStatus()`.

**Inheritance:** `Admin` dan `Pelanggan` menggunakan `extends Pengguna`. ID dan nama diwarisi dari class induk.

**Abstraction:** `Pengguna` adalah `abstract class`. Method abstract `getPeran()` harus diimplementasikan oleh turunannya. Class Pengguna tidak dapat diinstansiasi langsung.

**Polymorphism:** method `cetakProfil(Pengguna pengguna)` pada Main menerima objek Admin maupun Pelanggan. Pemanggilan `getPeran()` melalui `getProfil()` menghasilkan peran sesuai objek yang sebenarnya.

**Relasi objek:** TransaksiLaundry memiliki Pelanggan dan LayananLaundry sebagai asosiasi. TransaksiLaundry juga mengelola satu objek Pembayaran ketika transaksi dilunasi. Ini menunjukkan hubungan antarkelas selain pewarisan.

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

Main --> LaundryService --> daftar Pelanggan, LayananLaundry, TransaksiLaundry
```

## Aturan bisnis yang diterapkan

### Invoice transaksi

Invoice menggunakan nomor `INV-` diikuti kode transaksi. Isinya adalah identitas pelanggan, nomor telepon, tanggal masuk, estimasi selesai, layanan, berat, tarif saat transaksi, total tagihan, status cucian, dan status pembayaran. Setelah lunas, invoice juga menampilkan tanggal pembayaran, uang diterima, serta kembalian. Invoice yang belum dibayar diberi keterangan BELUM LUNAS dan sisa tagihan. Mencetak ulang invoice tidak membuat transaksi atau pembayaran baru.

Class `Invoice` memiliki asosiasi ke satu `TransaksiLaundry`; nilai biaya diambil dari transaksi, bukan dihitung ulang dari tarif layanan terbaru. Method `cetak()` menghasilkan String untuk ditampilkan di console. Tahap ini belum mengekspor PDF atau memakai JasperReports.

### Transaksi dan pembayaran

- Berat dan tarif harus positif; pelanggan dan layanan harus terdaftar.
- Transaksi menyalin tarif saat pencatatan sehingga perubahan tarif tidak mengubah biaya transaksi lama.
- Status berjalan sesuai jenis layanan. Cuci Kering melewati setrika; Setrika Saja melewati cuci.
- Tagihan harus lunas sebelum cucian diserahkan dengan status Selesai.
- Pembayaran kurang, pembayaran ganda, dan tanggal pembayaran sebelum tanggal masuk ditolak.
- Pendapatan dihitung berdasarkan tanggal pembayaran dan nilai tagihan yang lunas; kembalian tidak dihitung sebagai pendapatan.
- Daftar dari service tidak dapat ditambah/dihapus langsung dari luar class.

## Build dan pemeriksaan

Windows dengan JDK 17+ pada PATH:

```bat
build.bat
test.bat
run.bat
```

`test.bat` menjalankan SelfCheck tanpa JUnit atau dependensi tambahan. Pemeriksaan mencakup hitungan biaya, perubahan tarif, validasi input, pembayaran, status layanan, dan filter tanggal laporan. Pemeriksaan ini dijalankan terpisah dari lifecycle test Maven.

Alternatif build/run melalui Maven:

```text
mvn compile exec:java
```

## Batas tahap awal

Data hanya disimpan dalam ArrayList selama program berjalan dan kembali ke data contoh ketika program ditutup. Console ini merupakan simulasi operasional admin; class Admin/Pelanggan menunjukkan model OOP, belum menyediakan login atau pembatasan hak akses. GUI Swing, database, registrasi akun, dan CRUD lengkap dapat ditambahkan pada tahap selanjutnya sesuai instruksi praktikum.
