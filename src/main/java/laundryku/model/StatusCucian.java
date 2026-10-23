package laundryku.model;

public enum StatusCucian {
    DITERIMA("Diterima"), DICUCI("Dicuci"), DISETRIKA("Disetrika"),
    SIAP_DIAMBIL("Siap diambil"), SELESAI("Selesai / sudah diambil");

    private final String label;

    StatusCucian(String label) { this.label = label; }

    @Override
    public String toString() { return label; }
}
