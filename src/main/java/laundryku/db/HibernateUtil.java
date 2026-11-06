package laundryku.db;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/** Membuat SessionFactory Hibernate dari hibernate.cfg.xml dan db.properties. */
public final class HibernateUtil {
    public static final String FILE_DB = "db.properties";

    private HibernateUtil() { }

    /** Dipakai aplikasi: baca db.properties (jika ada) lalu sambungkan ke MySQL. */
    public static SessionFactory buatDariKonfigurasi() {
        Properties db = new Properties();
        Path file = Path.of(FILE_DB);
        if (Files.exists(file)) {
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                db.load(reader);
            } catch (IOException e) {
                throw new IllegalStateException("File " + FILE_DB + " tidak dapat dibaca.", e);
            }
        }
        Properties tambahan = new Properties();
        salin(db, "db.url", tambahan, "hibernate.connection.url");
        salin(db, "db.username", tambahan, "hibernate.connection.username");
        salin(db, "db.password", tambahan, "hibernate.connection.password");
        salin(db, "db.show_sql", tambahan, "hibernate.show_sql");
        return buat(tambahan);
    }

    /** Pengaturan tambahan menimpa nilai di hibernate.cfg.xml (dipakai juga oleh SelfCheck). */
    public static SessionFactory buat(Properties pengaturanTambahan) {
        Configuration konfigurasi = new Configuration().configure();
        konfigurasi.addProperties(pengaturanTambahan);
        return konfigurasi.buildSessionFactory();
    }

    private static void salin(Properties dari, String kunciDari, Properties ke, String kunciKe) {
        String nilai = dari.getProperty(kunciDari);
        if (nilai != null) { ke.setProperty(kunciKe, nilai.trim()); }
    }
}
