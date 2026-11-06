package laundryku;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import laundryku.db.HibernateUtil;
import laundryku.service.LaundryService;
import laundryku.ui.LoginFrame;
import org.hibernate.SessionFactory;

public class Main {
    public static void main(String[] args) {
        FlatLightLaf.setup();
        SessionFactory sessionFactory;
        LaundryService laundry;
        try {
            sessionFactory = HibernateUtil.buatDariKonfigurasi();
            laundry = new LaundryService(sessionFactory);
        } catch (RuntimeException e) {
            Throwable akar = e;
            while (akar.getCause() != null) { akar = akar.getCause(); }
            JOptionPane.showMessageDialog(null,
                    "Tidak dapat terhubung ke database MySQL.\n\n" + akar.getMessage()
                            + "\n\nPastikan MySQL berjalan dan isi " + HibernateUtil.FILE_DB
                            + " (lihat db.properties.example) sudah benar.",
                    "LaundryKu - Database", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return;
        }
        Runtime.getRuntime().addShutdownHook(new Thread(sessionFactory::close));
        LaundryService siap = laundry;
        SwingUtilities.invokeLater(() -> new LoginFrame(siap).setVisible(true));
    }
}
