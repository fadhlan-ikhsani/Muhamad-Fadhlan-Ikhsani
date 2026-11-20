package com.pesanyuk.report;

import com.pesanyuk.db.Database;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.swing.WindowConstants;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.view.JasperViewer;

/**
 * Menghasilkan laporan penjualan JasperReports langsung dari database
 * pesanyuk_db. Kelas ini tidak memakai Session maupun Spring, sehingga dapat
 * dipanggil tanpa login.
 */
public final class JasperLaporan {

    private static final String TEMPLATE = "/reports/laporan_penjualan.jrxml";
    private static final DateTimeFormatter FORMAT_PERIODE =
            DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("id", "ID"));
    private static final DateTimeFormatter FORMAT_CETAK =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", new Locale("id", "ID"));

    private static JasperReport laporan;

    private JasperLaporan() {
    }

    private static synchronized JasperReport muatTemplate() throws Exception {
        if (laporan == null) {
            try (InputStream in = JasperLaporan.class.getResourceAsStream(TEMPLATE)) {
                if (in == null) {
                    throw new IllegalStateException(
                            "Template JasperReports tidak ditemukan: " + TEMPLATE);
                }
                laporan = JasperCompileManager.compileReport(in);
            }
        }
        return laporan;
    }

    /**
     * Mengisi report dari query di template. {@code umkmId} null berarti semua
     * UMKM, sehingga laporan tetap dapat dibuat tanpa login.
     */
    public static JasperPrint isi(LocalDate dari, LocalDate sampai,
            String lingkup, Integer umkmId) throws Exception {
        if (dari == null || sampai == null) {
            throw new IllegalArgumentException("Tanggal laporan wajib diisi.");
        }
        if (dari.isAfter(sampai)) {
            throw new IllegalArgumentException(
                    "Tanggal awal tidak boleh setelah tanggal akhir.");
        }

        Map<String, Object> parameter = new HashMap<>();
        parameter.put("DARI", Timestamp.valueOf(dari.atStartOfDay()));
        parameter.put("SAMPAI", Timestamp.valueOf(sampai.plusDays(1).atStartOfDay()));
        parameter.put("UMKM_ID", umkmId == null ? -1 : umkmId);
        parameter.put("LINGKUP", lingkup == null || lingkup.isBlank()
                ? "Semua UMKM" : lingkup);
        parameter.put("PERIODE", FORMAT_PERIODE.format(dari) + " - "
                + FORMAT_PERIODE.format(sampai));
        parameter.put("DICETAK", FORMAT_CETAK.format(LocalDateTime.now()));

        try (Connection koneksi = Database.getConnection()) {
            return JasperFillManager.fillReport(muatTemplate(), parameter, koneksi);
        }
    }

    /** Menyimpan laporan ke PDF dan mengembalikan berkas yang benar-benar ditulis. */
    public static File exportPdf(File file, LocalDate dari, LocalDate sampai,
            String lingkup, Integer umkmId) throws Exception {
        if (file == null) {
            throw new IllegalArgumentException("Berkas tujuan wajib dipilih.");
        }
        File target = file.getName().toLowerCase(Locale.ROOT).endsWith(".pdf")
                ? file : new File(file.getParentFile(), file.getName() + ".pdf");
        if (target.getParentFile() != null) {
            Files.createDirectories(target.getParentFile().toPath());
        }
        JasperPrint print = isi(dari, sampai, lingkup, umkmId);
        try (OutputStream out = Files.newOutputStream(target.toPath())) {
            JasperExportManager.exportReportToPdfStream(print, out);
        }
        return target;
    }

    /** Menampilkan pratinjau JasperReports di jendela sendiri. */
    public static void tampilkan(LocalDate dari, LocalDate sampai,
            String lingkup, Integer umkmId) throws Exception {
        JasperPrint print = isi(dari, sampai, lingkup, umkmId);
        JasperViewer viewer = new JasperViewer(print, false);
        viewer.setTitle("PesanYuk - Laporan Penjualan (JasperReports)");
        viewer.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        viewer.setLocationRelativeTo(null);
        viewer.setVisible(true);
    }
}
