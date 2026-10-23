package laundryku.model;

public abstract class Pengguna {
    private final String id;
    private final String nama;

    protected Pengguna(String id, String nama) {
        if (id == null || id.isBlank() || nama == null || nama.isBlank()) {
            throw new IllegalArgumentException("ID dan nama wajib diisi.");
        }
        this.id = id.trim();
        this.nama = nama.trim();
    }

    public String getId() { return id; }
    public String getNama() { return nama; }
    public abstract String getPeran();

    public String getProfil() {
        return id + " | " + nama + " | " + getPeran();
    }
}
