package laundryku.model;

public class Pelanggan extends Pengguna {
    private final String nomorTelepon;

    public Pelanggan(String id, String nama, String nomorTelepon) {
        super(id, nama);
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
