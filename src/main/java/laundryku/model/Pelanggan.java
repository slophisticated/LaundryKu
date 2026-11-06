package laundryku.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("PELANGGAN")
public class Pelanggan extends Pengguna {
    // Boleh null di tabel karena baris Admin berbagi tabel yang sama.
    @Column(name = "nomor_telepon", length = 20)
    private String nomorTelepon;

    protected Pelanggan() { }

    public Pelanggan(String id, String nama, String nomorTelepon) {
        super(id, nama);
        setNomorTelepon(nomorTelepon);
    }

    public void setNomorTelepon(String nomorTelepon) {
        if (nomorTelepon == null || !nomorTelepon.matches("[0-9+() -]{6,20}")) {
            throw new IllegalArgumentException("Nomor telepon harus 6-20 karakter berupa angka atau + ( ) spasi dan -.");
        }
        if (nomorTelepon.replaceAll("[^0-9]", "").length() < 6) {
            throw new IllegalArgumentException("Nomor telepon minimal berisi 6 angka.");
        }
        this.nomorTelepon = nomorTelepon.trim();
    }

    public String getNomorTelepon() { return nomorTelepon; }

    @Override
    public String getPeran() { return "Pelanggan"; }
}
