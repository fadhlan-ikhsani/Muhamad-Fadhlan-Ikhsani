package com.pesanyuk.report;

import com.pesanyuk.ui.Ui;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerDateModel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Jendela laporan JasperReports yang dapat dibuka langsung dari halaman login.
 * Tidak membaca Session dan tidak memerlukan akun.
 */
public final class LaporanJasperFrame extends JFrame {

    private final JSpinner dari = tanggalSpinner(LocalDate.now().minusDays(29));
    private final JSpinner sampai = tanggalSpinner(LocalDate.now());
    private final JLabel status = Ui.muted("Laporan akan dibuat dari database pesanyuk_db.");
    private final Ui.Btn tampilkan = new Ui.Btn("Tampilkan Laporan", "primary");
    private final Ui.Btn simpan = new Ui.Btn("Simpan PDF", "soft");

    public LaporanJasperFrame() {
        setTitle("PesanYuk - Laporan JasperReports (Tanpa Login)");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        Ui.pasangIkon(this);
        setMinimumSize(new Dimension(600, 420));
        setSize(new Dimension(680, 470));
        setLocationRelativeTo(null);

        JPanel akar = new JPanel(new BorderLayout());
        akar.setBackground(Ui.BG);
        akar.setBorder(new EmptyBorder(24, 24, 24, 24));

        JPanel kartu = Ui.kartu(new BorderLayout(0, 18));
        kartu.setBorder(new EmptyBorder(24, 26, 22, 26));

        JPanel kepala = new JPanel();
        kepala.setOpaque(false);
        kepala.setLayout(new BoxLayout(kepala, BoxLayout.Y_AXIS));
        JLabel petit = Ui.label("PESANYUK DESKTOP", Ui.SMALL_BOLD, Ui.PRIMARY);
        petit.setAlignmentX(LEFT_ALIGNMENT);
        JLabel judul = Ui.judul("Laporan Penjualan");
        judul.setAlignmentX(LEFT_ALIGNMENT);
        JLabel sub = Ui.subjudul("JasperReports - dapat diakses tanpa login");
        sub.setAlignmentX(LEFT_ALIGNMENT);
        kepala.add(petit);
        kepala.add(Box.createVerticalStrut(8));
        kepala.add(judul);
        kepala.add(Box.createVerticalStrut(3));
        kepala.add(sub);

        JPanel filter = new JPanel(new GridLayout(1, 2, 12, 0));
        filter.setOpaque(false);
        filter.add(Ui.field("Dari tanggal", dari));
        filter.add(Ui.field("Sampai tanggal", sampai));

        JTextArea catatan = new JTextArea(
                "Laporan membaca langsung tabel pesanan, pengguna, umkm, dan "
                + "pembayaran. Hanya pesanan berstatus SELESAI yang dihitung. "
                + "Data dapat ditampilkan atau disimpan sebagai PDF tanpa masuk "
                + "ke akun admin, penjual, maupun pelanggan.");
        catatan.setEditable(false);
        catatan.setOpaque(false);
        catatan.setLineWrap(true);
        catatan.setWrapStyleWord(true);
        catatan.setFont(Ui.SMALL);
        catatan.setForeground(Ui.MUTED);
        catatan.setBorder(null);

        JPanel isi = new JPanel();
        isi.setOpaque(false);
        isi.setLayout(new BoxLayout(isi, BoxLayout.Y_AXIS));
        filter.setAlignmentX(LEFT_ALIGNMENT);
        catatan.setAlignmentX(LEFT_ALIGNMENT);
        isi.add(filter);
        isi.add(Box.createVerticalStrut(14));
        isi.add(catatan);

        status.setAlignmentX(LEFT_ALIGNMENT);
        JPanel bawah = new JPanel();
        bawah.setOpaque(false);
        bawah.setLayout(new BoxLayout(bawah, BoxLayout.Y_AXIS));
        JPanel tombol = Ui.barisKanan(10, simpan, tampilkan);
        tombol.setAlignmentX(LEFT_ALIGNMENT);
        bawah.add(tombol);
        bawah.add(Box.createVerticalStrut(8));
        bawah.add(status);

        kartu.add(kepala, BorderLayout.NORTH);
        kartu.add(isi, BorderLayout.CENTER);
        kartu.add(bawah, BorderLayout.SOUTH);
        akar.add(kartu, BorderLayout.CENTER);
        setContentPane(akar);

        tampilkan.addActionListener(e -> tampilkanLaporan());
        simpan.addActionListener(e -> simpanPdf());
        getRootPane().setDefaultButton(tampilkan);
    }

    private void tampilkanLaporan() {
        LocalDate awal = tanggal(dari);
        LocalDate akhir = tanggal(sampai);
        try {
            sibuk(true, "Menyusun laporan JasperReports...");
            JasperLaporan.tampilkan(awal, akhir, "Semua UMKM", null);
            status.setText("Pratinjau dibuka. Gunakan ikon simpan pada jendela "
                    + "pratinjau bila ingin menyimpan PDF.");
        } catch (IllegalArgumentException ex) {
            Ui.galat(this, ex.getMessage());
            status.setText("Periksa kembali rentang tanggal laporan.");
        } catch (SQLException ex) {
            Ui.galat(this, "Tidak dapat membaca database pesanyuk_db:\n"
                    + ex.getMessage());
            status.setText("Koneksi database gagal.");
        } catch (Exception ex) {
            Ui.galat(this, "Laporan JasperReports gagal dibuat:\n" + ex.getMessage());
            status.setText("Laporan gagal dibuat.");
        } finally {
            sibuk(false, status.getText());
        }
    }

    private void simpanPdf() {
        LocalDate awal = tanggal(dari);
        LocalDate akhir = tanggal(sampai);
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Simpan Laporan JasperReports");
        chooser.setSelectedFile(new File("Laporan_Penjualan_PesanYuk.pdf"));
        chooser.setFileFilter(new FileNameExtensionFilter("Dokumen PDF (*.pdf)", "pdf"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".pdf")) {
            file = new File(file.getParentFile(), file.getName() + ".pdf");
        }
        try {
            sibuk(true, "Menyimpan laporan PDF...");
            File hasil = JasperLaporan.exportPdf(file, awal, akhir, "Semua UMKM", null);
            status.setText("PDF tersimpan: " + hasil.getName());
            Ui.info(this, "Laporan JasperReports berhasil disimpan:\n"
                    + hasil.getAbsolutePath());
            bukaPdf(hasil);
        } catch (IllegalArgumentException ex) {
            Ui.galat(this, ex.getMessage());
        } catch (SQLException ex) {
            Ui.galat(this, "Tidak dapat membaca database pesanyuk_db:\n"
                    + ex.getMessage());
        } catch (Exception ex) {
            Ui.galat(this, "Laporan PDF gagal disimpan:\n" + ex.getMessage());
        } finally {
            sibuk(false, status.getText());
        }
    }

    private void bukaPdf(File file) {
        try {
            if (Desktop.isDesktopSupported()
                    && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(file);
            }
        } catch (Exception ex) {
            // PDF tetap tersimpan meski aplikasi pembuka tidak tersedia.
        }
    }

    private void sibuk(boolean aktif, String pesan) {
        setCursor(Cursor.getPredefinedCursor(
                aktif ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR));
        tampilkan.setEnabled(!aktif);
        simpan.setEnabled(!aktif);
        status.setText(pesan);
    }

    private static JSpinner tanggalSpinner(LocalDate awal) {
        SpinnerDateModel model = new SpinnerDateModel(java.sql.Date.valueOf(awal),
                null, null, Calendar.DAY_OF_MONTH);
        JSpinner spinner = new JSpinner(model);
        spinner.setEditor(new JSpinner.DateEditor(spinner, "dd/MM/yyyy"));
        spinner.setFont(Ui.BASE);
        spinner.setPreferredSize(new Dimension(150, 36));
        if (spinner.getEditor() instanceof JSpinner.DefaultEditor editor) {
            editor.getTextField().setFont(Ui.BASE);
            editor.getTextField().setBorder(new EmptyBorder(4, 6, 4, 6));
        }
        return spinner;
    }

    private static LocalDate tanggal(JSpinner spinner) {
        java.util.Date nilai = (java.util.Date) spinner.getValue();
        return nilai.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
