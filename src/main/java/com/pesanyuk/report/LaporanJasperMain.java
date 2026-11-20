package com.pesanyuk.report;

import com.pesanyuk.db.Database;
import com.pesanyuk.ui.Ui;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Titik masuk laporan JasperReports tanpa login.
 *
 * <pre>
 * Tanpa argumen          : membuka jendela laporan.
 * --pdf [berkas]         : langsung membuat PDF, misalnya untuk demonstrasi.
 * --dari / --sampai      : rentang tanggal yyyy-MM-dd.
 * --umkm ID              : membatasi laporan pada satu UMKM.
 * </pre>
 */
public final class LaporanJasperMain {

    private static final String BERKAS_PDF_BAWAAN =
            "laporan" + File.separator + "Laporan_Penjualan_Jasper.pdf";

    private LaporanJasperMain() {
    }

    public static void main(String[] args) {
        Map<String, String> opsi = bacaOpsi(args);
        if (opsi.containsKey("help")) {
            cetakBantuan();
            return;
        }
        if (opsi.containsKey("pdf")) {
            buatPdf(opsi);
            return;
        }

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                // Tetap gunakan look and feel bawaan.
            }
            Ui.install();

            try {
                Database.bootstrap();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null,
                        "Laporan tidak dapat terhubung ke basis data.\n\n"
                        + "Detail: " + ex.getMessage() + "\n\n"
                        + "Pastikan server MySQL/MariaDB berjalan dan konfigurasi "
                        + "src/main/resources/db.properties benar.\n"
                        + "Database yang dituju: " + Database.info(),
                        "Koneksi Basis Data Gagal",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            new LaporanJasperFrame().setVisible(true);
        });
    }

    private static void buatPdf(Map<String, String> opsi) {
        try {
            Database.bootstrap();
            LocalDate dari = tanggal(opsi.get("dari"), LocalDate.now().minusDays(29));
            LocalDate sampai = tanggal(opsi.get("sampai"), LocalDate.now());
            Integer umkmId = opsi.containsKey("umkm")
                    ? Integer.valueOf(opsi.get("umkm")) : null;
            String lingkup = opsi.getOrDefault("lingkup", "Semua UMKM");
            File hasil = JasperLaporan.exportPdf(
                    new File(opsi.get("pdf")), dari, sampai, lingkup, umkmId);
            System.out.println("PDF JasperReports: " + hasil.getAbsolutePath());
        } catch (NumberFormatException ex) {
            System.err.println("Nilai --umkm harus berupa angka.");
            System.exit(1);
        } catch (DateTimeParseException ex) {
            System.err.println("Tanggal harus memakai format yyyy-MM-dd, contoh 2026-10-01.");
            System.exit(1);
        } catch (Exception ex) {
            System.err.println("Gagal membuat laporan JasperReports: " + ex.getMessage());
            ex.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private static LocalDate tanggal(String nilai, LocalDate bawaan) {
        return nilai == null || nilai.isBlank() ? bawaan : LocalDate.parse(nilai);
    }

    private static Map<String, String> bacaOpsi(String[] args) {
        Map<String, String> opsi = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("--help".equals(arg) || "-h".equals(arg)) {
                opsi.put("help", "true");
            } else if ("--pdf".equals(arg)) {
                String berkas = BERKAS_PDF_BAWAAN;
                if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                    berkas = args[++i];
                }
                opsi.put("pdf", berkas);
            } else if (arg.startsWith("--dari=")) {
                opsi.put("dari", arg.substring("--dari=".length()));
            } else if (arg.startsWith("--sampai=")) {
                opsi.put("sampai", arg.substring("--sampai=".length()));
            } else if (arg.startsWith("--umkm=")) {
                opsi.put("umkm", arg.substring("--umkm=".length()));
            } else if (arg.startsWith("--lingkup=")) {
                opsi.put("lingkup", arg.substring("--lingkup=".length()));
            } else if ("--dari".equals(arg) && i + 1 < args.length) {
                opsi.put("dari", args[++i]);
            } else if ("--sampai".equals(arg) && i + 1 < args.length) {
                opsi.put("sampai", args[++i]);
            } else if ("--umkm".equals(arg) && i + 1 < args.length) {
                opsi.put("umkm", args[++i]);
            } else if ("--lingkup".equals(arg) && i + 1 < args.length) {
                opsi.put("lingkup", args[++i]);
            }
        }
        return opsi;
    }

    private static void cetakBantuan() {
        System.out.println("""
                Laporan JasperReports PesanYuk - tanpa login

                Membuka jendela laporan:
                  java -cp target/PesanYuk.jar com.pesanyuk.report.LaporanJasperMain

                Membuat PDF langsung:
                  java -cp target/PesanYuk.jar com.pesanyuk.report.LaporanJasperMain --pdf
                  java -cp target/PesanYuk.jar com.pesanyuk.report.LaporanJasperMain --pdf hasil.pdf --dari 2026-10-01 --sampai 2026-11-30
                  java -cp target/PesanYuk.jar com.pesanyuk.report.LaporanJasperMain --pdf hasil.pdf --umkm 1

                Opsi:
                  --pdf [berkas]       Berkas PDF tujuan. Bila tanpa nama, memakai
                                       laporan/Laporan_Penjualan_Jasper.pdf.
                  --dari yyyy-MM-dd    Tanggal awal (bawaan: 30 hari terakhir).
                  --sampai yyyy-MM-dd  Tanggal akhir (bawaan: hari ini).
                  --umkm ID            Batasi laporan pada satu UMKM.
                  --lingkup teks       Label lingkup pada header laporan.
                """);
    }
}
