package laundryku.model;

public class Admin extends Pengguna {
    public Admin(String id, String nama) { super(id, nama); }

    @Override
    public String getPeran() { return "Admin"; }
}
