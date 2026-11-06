package laundryku.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import org.hibernate.HibernateException;

/** Kumpulan helper tampilan yang dipakai semua layar. */
final class Ui {
    private Ui() { }

    static String uang(BigDecimal nilai) {
        NumberFormat f = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"));
        f.setMaximumFractionDigits(0);
        f.setMinimumFractionDigits(0);
        return f.format(nilai == null ? BigDecimal.ZERO : nilai);
    }

    static BigDecimal angka(String teks, String namaKolom) {
        String t = teks == null ? "" : teks.trim();
        if (!t.matches("[0-9]+([.,][0-9]+)?")) {
            throw new IllegalArgumentException(namaKolom + " harus angka tanpa pemisah ribuan, misalnya 3,5 atau 21000.");
        }
        return new BigDecimal(t.replace(',', '.'));
    }

    static int bulat(String teks, String namaKolom) {
        String t = teks == null ? "" : teks.trim();
        if (!t.matches("[0-9]{1,4}")) {
            throw new IllegalArgumentException(namaKolom + " harus bilangan bulat.");
        }
        return Integer.parseInt(t);
    }

    static LocalDate tanggal(String teks, String namaKolom) {
        try {
            return LocalDate.parse(teks.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(namaKolom + " tidak valid. Gunakan format YYYY-MM-DD.");
        }
    }

    static String beratTeks(BigDecimal berat) {
        return berat.stripTrailingZeros().toPlainString() + " kg";
    }

    /** Menjalankan aksi dan menampilkan pesan yang mudah dipahami jika gagal. */
    static boolean coba(Component induk, Runnable aksi) {
        try {
            aksi.run();
            return true;
        } catch (IllegalArgumentException | IllegalStateException | NullPointerException e) {
            gagal(induk, e.getMessage());
        } catch (HibernateException e) {
            gagal(induk, "Terjadi masalah database:\n" + akar(e));
        }
        return false;
    }

    static String akar(Throwable e) {
        Throwable t = e;
        while (t.getCause() != null) { t = t.getCause(); }
        return t.getMessage() == null ? t.getClass().getSimpleName() : t.getMessage();
    }

    static void gagal(Component induk, String pesan) {
        JOptionPane.showMessageDialog(induk, pesan, "Tidak berhasil", JOptionPane.WARNING_MESSAGE);
    }

    static void info(Component induk, String pesan) {
        JOptionPane.showMessageDialog(induk, pesan, "LaundryKu", JOptionPane.INFORMATION_MESSAGE);
    }

    static boolean yakin(Component induk, String pesan) {
        return JOptionPane.showConfirmDialog(induk, pesan, "Konfirmasi",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    static DefaultTableModel model(String... kolom) {
        return new DefaultTableModel(kolom, 0) {
            @Override
            public boolean isCellEditable(int baris, int kolom) { return false; }
        };
    }

    static JTable tabel(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setRowHeight(26);
        t.setAutoCreateRowSorter(true);
        t.getTableHeader().setReorderingAllowed(false);
        return t;
    }

    /** Nilai kolom pertama baris terpilih (biasanya ID/kode), atau null jika tidak ada. */
    static String terpilih(JTable tabel) {
        int baris = tabel.getSelectedRow();
        if (baris < 0) { return null; }
        return String.valueOf(tabel.getModel().getValueAt(tabel.convertRowIndexToModel(baris), 0));
    }

    static JButton tombol(String teks, Runnable aksi) {
        JButton b = new JButton(teks);
        b.addActionListener(e -> aksi.run());
        return b;
    }

    static JLabel judul(String teks) {
        JLabel l = new JLabel(teks);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 18f));
        l.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        return l;
    }

    static Insets jarak() { return new Insets(4, 4, 4, 4); }

    /** Menampilkan form label + isian dalam dialog OK/Batal. Mengembalikan true jika OK. */
    static boolean form(Component induk, String judul, String[] label, JComponent[] isian) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = jarak();
        c.anchor = GridBagConstraints.WEST;
        for (int i = 0; i < label.length; i++) {
            c.gridy = i;
            c.gridx = 0; c.weightx = 0; c.fill = GridBagConstraints.NONE;
            panel.add(new JLabel(label[i]), c);
            c.gridx = 1; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
            panel.add(isian[i], c);
        }
        return JOptionPane.showConfirmDialog(induk, panel, judul, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION;
    }

    /** Menampilkan teks invoice dengan huruf monospace agar kolomnya rapi. */
    static void tampilkanTeks(Component induk, String judul, String teks) {
        JTextArea area = new JTextArea(teks);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setCaretPosition(0);
        JScrollPane gulir = new JScrollPane(area);
        gulir.setPreferredSize(new Dimension(560, 520));
        JOptionPane.showMessageDialog(induk, gulir, judul, JOptionPane.PLAIN_MESSAGE);
    }
}
