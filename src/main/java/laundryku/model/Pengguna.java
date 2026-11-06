package laundryku.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import laundryku.util.Password;

/**
 * Admin dan Pelanggan disimpan dalam satu tabel "pengguna".
 * Kolom "peran" (discriminator) menentukan objek turunan yang dibuat Hibernate.
 */
@Entity
@Table(name = "pengguna")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "peran", length = 20)
public abstract class Pengguna {
    @Id
    @Column(length = 10)
    private String id;

    @Column(nullable = false, length = 100)
    private String nama;

    @Column(unique = true, length = 30)
    private String username;

    @Column(name = "password_hash", length = 200)
    private String passwordHash;

    /** Dibutuhkan Hibernate untuk membuat objek dari baris tabel. */
    protected Pengguna() { }

    protected Pengguna(String id, String nama) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ID dan nama wajib diisi.");
        }
        this.id = id.trim();
        setNama(nama);
    }

    public void setNama(String nama) {
        if (nama == null || nama.isBlank()) {
            throw new IllegalArgumentException("ID dan nama wajib diisi.");
        }
        if (nama.trim().length() > 100) {
            throw new IllegalArgumentException("Nama maksimal 100 karakter.");
        }
        this.nama = nama.trim();
    }

    /**
     * Membuat atau mengubah akun login. Password boleh kosong jika akun sudah ada,
     * artinya password lama tetap dipakai.
     */
    public void aturAkun(String username, String passwordBaru) {
        String u = username == null ? "" : username.trim().toLowerCase();
        if (!u.matches("[a-z0-9._]{3,30}")) {
            throw new IllegalArgumentException("Username 3-30 karakter berupa huruf, angka, titik, atau garis bawah.");
        }
        boolean adaPassword = passwordBaru != null && !passwordBaru.isEmpty();
        if (!adaPassword && passwordHash == null) {
            throw new IllegalArgumentException("Password wajib diisi untuk akun baru.");
        }
        if (adaPassword) { gantiPassword(passwordBaru); }
        this.username = u;
    }

    public void hapusAkun() {
        username = null;
        passwordHash = null;
    }

    private void gantiPassword(String passwordBaru) {
        if (passwordBaru == null || passwordBaru.length() < 6) {
            throw new IllegalArgumentException("Password minimal 6 karakter.");
        }
        passwordHash = Password.hash(passwordBaru);
    }

    public void gantiPassword(String passwordLama, String passwordBaru) {
        if (!cekPassword(passwordLama)) {
            throw new IllegalArgumentException("Password lama salah.");
        }
        gantiPassword(passwordBaru);
    }

    public boolean cekPassword(String password) {
        return passwordHash != null && Password.cocok(password, passwordHash);
    }

    public boolean punyaAkun() { return username != null; }
    public String getId() { return id; }
    public String getNama() { return nama; }
    public String getUsername() { return username; }
    public abstract String getPeran();

    public String getProfil() {
        return id + " | " + nama + " | " + getPeran();
    }

    @Override
    public String toString() { return id + " - " + nama; }
}
