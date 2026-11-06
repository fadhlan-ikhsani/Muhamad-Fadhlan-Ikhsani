package com.pesanyuk;

import com.pesanyuk.db.Database;
import com.pesanyuk.ui.LoginFrame;
import com.pesanyuk.ui.Ui;
import java.awt.Font;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Titik masuk aplikasi desktop PesanYuk. */
public final class App {

    private App() {
    }

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                // Tetap gunakan look and feel bawaan bila gagal.
            }
            Ui.install();

            try {
                Database.bootstrap();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null,
                        "Aplikasi tidak dapat terhubung ke basis data.\n\n"
                        + "Detail: " + ex.getMessage() + "\n\n"
                        + "Periksa hal berikut:\n"
                        + "1. Server MySQL/MariaDB sudah berjalan (mis. XAMPP).\n"
                        + "2. Konfigurasi pada src/main/resources/db.properties sesuai.\n"
                        + "3. Pengguna dan kata sandi basis data benar.\n\n"
                        + "Database yang dituju: " + Database.info(),
                        "Koneksi Basis Data Gagal",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }

            LoginFrame login = new LoginFrame();
            login.setVisible(true);
        });
    }

    static {
        // Memastikan font dasar tersedia sebelum UI dibangun.
        if (Font.getFont("Segoe UI") == null) {
            System.setProperty("swing.plaf.metal.userFont", "SansSerif");
        }
    }
}
