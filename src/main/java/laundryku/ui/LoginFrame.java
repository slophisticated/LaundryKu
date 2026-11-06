package laundryku.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import laundryku.model.Admin;
import laundryku.model.Pelanggan;
import laundryku.model.Pengguna;
import laundryku.service.LaundryService;

/** Layar pertama aplikasi. Admin dan pelanggan login dengan username dan password. */
public class LoginFrame extends JFrame {
    private final LaundryService laundry;
    private final JTextField username = new JTextField(18);
    private final JPasswordField password = new JPasswordField(18);
    private final JButton masuk = new JButton("Masuk");
    private final JLabel pesan = new JLabel(" ");

    public LoginFrame(LaundryService laundry) {
        super("LaundryKu - Login");
        this.laundry = laundry;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        JPanel isi = new JPanel(new GridBagLayout());
        isi.setBorder(BorderFactory.createEmptyBorder(28, 36, 28, 36));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 4, 5, 4);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0; c.gridwidth = 2;

        JLabel logo = new JLabel("LaundryKu", SwingConstants.CENTER);
        logo.setFont(logo.getFont().deriveFont(Font.BOLD, 28f));
        c.gridy = 0; isi.add(logo, c);
        JLabel sub = new JLabel("Masuk sebagai admin atau pelanggan", SwingConstants.CENTER);
        sub.setForeground(Color.GRAY);
        c.gridy = 1; c.insets = new Insets(0, 4, 18, 4); isi.add(sub, c);

        c.insets = new Insets(5, 4, 5, 4);
        c.gridwidth = 1;
        c.gridy = 2; c.gridx = 0; c.weightx = 0; isi.add(new JLabel("Username"), c);
        c.gridx = 1; c.weightx = 1; isi.add(username, c);
        c.gridy = 3; c.gridx = 0; c.weightx = 0; isi.add(new JLabel("Password"), c);
        c.gridx = 1; c.weightx = 1; isi.add(password, c);

        c.gridx = 0; c.gridwidth = 2;
        pesan.setForeground(new Color(200, 40, 40));
        pesan.setHorizontalAlignment(SwingConstants.CENTER);
        c.gridy = 4; isi.add(pesan, c);
        c.gridy = 5; c.insets = new Insets(8, 4, 4, 4); isi.add(masuk, c);

        masuk.addActionListener(e -> login());
        getRootPane().setDefaultButton(masuk);

        add(isi, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
    }

    private void login() {
        String u = username.getText();
        String p = new String(password.getPassword());
        masuk.setEnabled(false);
        pesan.setText("Memeriksa akun...");
        // Cek password dijalankan di background agar jendela tidak macet.
        new SwingWorker<Pengguna, Void>() {
            @Override
            protected Pengguna doInBackground() { return laundry.login(u, p); }

            @Override
            protected void done() {
                masuk.setEnabled(true);
                try {
                    bukaMenu(get());
                } catch (Exception e) {
                    Throwable sebab = e.getCause() == null ? e : e.getCause();
                    pesan.setText(sebab instanceof IllegalArgumentException
                            ? sebab.getMessage() : "Gagal terhubung: " + Ui.akar(sebab));
                    password.setText("");
                    password.requestFocusInWindow();
                }
            }
        }.execute();
    }

    private void bukaMenu(Pengguna pengguna) {
        JFrame berikutnya;
        if (pengguna instanceof Admin) {
            berikutnya = new AdminFrame(laundry, (Admin) pengguna);
        } else {
            berikutnya = new PelangganFrame(laundry, (Pelanggan) pengguna);
        }
        berikutnya.setVisible(true);
        dispose();
    }
}
