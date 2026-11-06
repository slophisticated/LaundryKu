package laundryku.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ADMIN")
public class Admin extends Pengguna {
    protected Admin() { }

    public Admin(String id, String nama) { super(id, nama); }

    @Override
    public String getPeran() { return "Admin"; }
}
