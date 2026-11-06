package com.pesanyuk.ui.panel;

import com.pesanyuk.dao.LaporanDao;
import com.pesanyuk.model.OmzetHarian;
import com.pesanyuk.model.RekapMenu;
import com.pesanyuk.model.RingkasanLaporan;
import com.pesanyuk.ui.MainFrame;
import com.pesanyuk.ui.Refreshable;
import com.pesanyuk.ui.Ui;
import com.pesanyuk.ui.model.RekapMenuTableModel;
import com.pesanyuk.util.CsvExporter;
import com.pesanyuk.util.PdfExporter;
import com.pesanyuk.util.Rupiah;
import com.pesanyuk.util.Tanggal;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerDateModel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

/** Laporan penjualan per periode untuk penjual maupun admin. */
public final class LaporanPanel extends JPanel implements Refreshable {

    private final Integer umkmId;
    private final String scope;
    private final LaporanDao dao = new LaporanDao();

    private final JSpinner dari = tanggalSpinner(LocalDate.now().minusDays(29));
    private final JSpinner sampai = tanggalSpinner(LocalDate.now());
    private final JComboBox<String> preset = Ui.pilihan(new String[]{
        "30 hari terakhir", "Hari ini", "7 hari terakhir", "Bulan ini", "Bulan lalu"});
    private final JLabel periode = Ui.muted(" ");
    private final JLabel infoMenu = Ui.muted("0 menu");

    private final JLabel nilaiOmzet = nilai();
    private final JLabel nilaiPesanan = nilai();
    private final JLabel nilaiMenu = nilai();
    private final JLabel nilaiRata = nilai();

    private final Ui.Grafik grafik = new Ui.Grafik();
    private final RekapMenuTableModel model = new RekapMenuTableModel();
    private final JTable tabel = Ui.tabel(model);

    private RingkasanLaporan ringkasan = new RingkasanLaporan(0, 0, 0);
    private List<OmzetHarian> harian = List.of();
    private List<RekapMenu> rekap = List.of();

    public LaporanPanel(MainFrame frame, Integer umkmId, String scope) {
        this.umkmId = umkmId;
        this.scope = scope == null || scope.isBlank() ? "Semua UMKM" : scope;
        setOpaque(false);
        setLayout(new BorderLayout(0, 14));

        add(buatToolbar(), BorderLayout.NORTH);
        add(buatKonten(), BorderLayout.CENTER);

        preset.addActionListener(e -> {
            if (preset.getSelectedIndex() >= 0) {
                terapkanPreset();
            }
        });
    }

    private JPanel buatToolbar() {
        JPanel kartu = Ui.kartu(new BorderLayout(0, 8));
        kartu.setBorder(new EmptyBorder(14, 18, 14, 18));

        Ui.Btn terapkan = new Ui.Btn("Terapkan", "primary");
        terapkan.addActionListener(e -> muat());
        Ui.tinggi(preset, 36);
        JPanel filter = Ui.baris(8, Ui.label("Periode", Ui.SMALL_BOLD, Ui.MUTED),
                preset, Ui.label("Dari", Ui.SMALL_BOLD, Ui.MUTED), dari,
                Ui.label("Sampai", Ui.SMALL_BOLD, Ui.MUTED), sampai, terapkan);
        kartu.add(filter, BorderLayout.NORTH);

        Ui.Btn pdf = new Ui.Btn("Ekspor PDF", "soft");
        Ui.Btn excel = new Ui.Btn("Ekspor Excel (CSV)", "ghost");
        pdf.addActionListener(e -> eksporPdf());
        excel.addActionListener(e -> eksporCsv());
        JPanel bawah = new JPanel(new BorderLayout());
        bawah.setOpaque(false);
        bawah.add(Ui.muted("Lingkup laporan: " + scope), BorderLayout.WEST);
        bawah.add(Ui.barisKanan(8, pdf, excel), BorderLayout.EAST);
        kartu.add(bawah, BorderLayout.SOUTH);
        return kartu;
    }

    private JPanel buatKonten() {
        JPanel isi = new JPanel();
        isi.setOpaque(false);
        isi.setLayout(new BoxLayout(isi, BoxLayout.Y_AXIS));

        JPanel kartuStatistik = new JPanel(new GridLayout(1, 4, 12, 0));
        kartuStatistik.setOpaque(false);
        kartuStatistik.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        kartuStatistik.add(statistik("Total Omzet", nilaiOmzet, Ui.SUCCESS));
        kartuStatistik.add(statistik("Pesanan Selesai", nilaiPesanan, Ui.PRIMARY));
        kartuStatistik.add(statistik("Menu Terjual", nilaiMenu, Ui.BLUE));
        kartuStatistik.add(statistik("Rata-rata / Pesanan", nilaiRata, Ui.WARNING));
        kartuStatistik.setAlignmentX(LEFT_ALIGNMENT);

        JPanel kartuGrafik = Ui.kartu(new BorderLayout(0, 8));
        JPanel kepalaGrafik = new JPanel(new BorderLayout());
        kepalaGrafik.setOpaque(false);
        kepalaGrafik.add(Ui.tebal("Omzet Harian"), BorderLayout.WEST);
        kepalaGrafik.add(periode, BorderLayout.EAST);
        kartuGrafik.add(kepalaGrafik, BorderLayout.NORTH);
        kartuGrafik.add(grafik, BorderLayout.CENTER);
        kartuGrafik.setPreferredSize(new Dimension(0, 272));
        kartuGrafik.setMaximumSize(new Dimension(Integer.MAX_VALUE, 272));
        kartuGrafik.setAlignmentX(LEFT_ALIGNMENT);

        JPanel kartuTabel = Ui.kartu(new BorderLayout(0, 10));
        JPanel kepalaTabel = new JPanel(new BorderLayout());
        kepalaTabel.setOpaque(false);
        kepalaTabel.add(Ui.tebal("Rekap Menu Terjual"), BorderLayout.WEST);
        kepalaTabel.add(infoMenu, BorderLayout.EAST);
        kartuTabel.add(kepalaTabel, BorderLayout.NORTH);

        tabel.setAutoCreateRowSorter(true);
        Ui.lebar(tabel, 380, 120, 200);
        kartuTabel.add(Ui.scroll(tabel), BorderLayout.CENTER);
        kartuTabel.setPreferredSize(new Dimension(0, 330));
        kartuTabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 330));
        kartuTabel.setAlignmentX(LEFT_ALIGNMENT);

        isi.add(kartuStatistik);
        isi.add(Box.createVerticalStrut(14));
        isi.add(kartuGrafik);
        isi.add(Box.createVerticalStrut(14));
        isi.add(kartuTabel);

        JScrollPane gulir = new JScrollPane(isi);
        gulir.setBorder(null);
        gulir.setOpaque(false);
        gulir.getViewport().setOpaque(false);
        gulir.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        gulir.getVerticalScrollBar().setUnitIncrement(18);

        JPanel luar = new JPanel(new BorderLayout());
        luar.setOpaque(false);
        luar.add(gulir, BorderLayout.CENTER);
        return luar;
    }

    private JPanel statistik(String judul, JLabel nilai, Color aksen) {
        JPanel p = Ui.kartu(new BorderLayout(0, 6));
        p.setBorder(new EmptyBorder(14, 16, 14, 16));
        JLabel label = Ui.label(judul, Ui.SMALL_BOLD, Ui.MUTED);
        nilai.setFont(Ui.ANGKA);
        nilai.setForeground(aksen);
        p.add(label, BorderLayout.NORTH);
        p.add(nilai, BorderLayout.CENTER);
        return p;
    }

    @Override
    public void onShow() {
        muat();
    }

    private void terapkanPreset() {
        LocalDate hariIni = LocalDate.now();
        switch (preset.getSelectedIndex()) {
            case 1 -> aturRentang(hariIni, hariIni);
            case 2 -> aturRentang(hariIni.minusDays(6), hariIni);
            case 3 -> aturRentang(hariIni.withDayOfMonth(1), hariIni);
            case 4 -> {
                LocalDate awal = hariIni.minusMonths(1).withDayOfMonth(1);
                aturRentang(awal, awal.withDayOfMonth(awal.lengthOfMonth()));
            }
            default -> aturRentang(hariIni.minusDays(29), hariIni);
        }
        muat();
    }

    private void aturRentang(LocalDate awal, LocalDate akhir) {
        dari.setValue(java.sql.Date.valueOf(awal));
        sampai.setValue(java.sql.Date.valueOf(akhir));
    }

    private void muat() {
        LocalDate awal = tanggal(dari);
        LocalDate akhir = tanggal(sampai);
        if (awal.isAfter(akhir)) {
            Ui.galat(this, "Tanggal awal tidak boleh setelah tanggal akhir.");
            return;
        }
        try {
            ringkasan = dao.ringkasan(umkmId, awal, akhir);
            harian = dao.harian(umkmId, awal, akhir);
            rekap = dao.rekapMenu(umkmId, awal, akhir);

            model.setData(rekap);
            nilaiOmzet.setText(Rupiah.format(ringkasan.omzet()));
            nilaiPesanan.setText(String.valueOf(ringkasan.jumlahPesanan()));
            nilaiMenu.setText(String.valueOf(ringkasan.menuTerjual()));
            nilaiRata.setText(Rupiah.format(ringkasan.rataRata()));
            infoMenu.setText(rekap.size() + " menu terjual");
            periode.setText(scope + " \u00b7 " + Tanggal.singkat(awal)
                    + " \u2013 " + Tanggal.singkat(akhir));
            tampilkanGrafik(awal, akhir);
        } catch (SQLException ex) {
            Ui.galat(this, "Laporan gagal dimuat:\n" + ex.getMessage());
        }
    }

    private void tampilkanGrafik(LocalDate awal, LocalDate akhir) {
        List<OmzetHarian> titik = harian;
        long hari = ChronoUnit.DAYS.between(awal, akhir) + 1;
        if (hari <= 62) {
            Map<LocalDate, OmzetHarian> peta = new LinkedHashMap<>();
            for (OmzetHarian h : harian) {
                peta.put(h.tanggal(), h);
            }
            titik = new ArrayList<>();
            for (LocalDate t = awal; !t.isAfter(akhir); t = t.plusDays(1)) {
                titik.add(peta.getOrDefault(t, new OmzetHarian(t, 0, 0)));
            }
        }
        List<String> label = new ArrayList<>();
        List<Double> nilai = new ArrayList<>();
        for (OmzetHarian h : titik) {
            label.add(Tanggal.labelGrafik(h.tanggal()));
            nilai.add(h.omzet());
        }
        grafik.setData(label, nilai);
    }

    private void eksporPdf() {
        LocalDate awal = tanggal(dari);
        LocalDate akhir = tanggal(sampai);
        File file = pilihSimpan("laporan-penjualan-" + namaFile(awal) + "-"
                + namaFile(akhir) + ".pdf", "Dokumen PDF", "pdf");
        if (file == null) {
            return;
        }
        List<String> ringkasanBaris = List.of(
                "Lingkup laporan: " + scope,
                "Periode: " + Tanggal.singkat(awal) + " sampai " + Tanggal.singkat(akhir),
                "Total omzet: " + Rupiah.format(ringkasan.omzet()),
                "Jumlah pesanan selesai: " + ringkasan.jumlahPesanan(),
                "Jumlah menu terjual: " + ringkasan.menuTerjual(),
                "Rata-rata per pesanan: " + Rupiah.format(ringkasan.rataRata()));

        List<String[]> barisHarian = new ArrayList<>();
        for (OmzetHarian h : harian) {
            barisHarian.add(new String[]{Tanggal.singkat(h.tanggal()),
                String.valueOf(h.jumlahPesanan()), Rupiah.angka(h.omzet())});
        }
        List<String[]> barisRekap = new ArrayList<>();
        for (RekapMenu r : rekap) {
            barisRekap.add(new String[]{r.namaMenu(), String.valueOf(r.jumlahTerjual()),
                Rupiah.angka(r.omzet())});
        }
        List<PdfExporter.Tabel> tabelPdf = List.of(
                new PdfExporter.Tabel("Omzet Harian",
                        new String[]{"Tanggal", "Pesanan", "Omzet (Rp)"}, barisHarian),
                new PdfExporter.Tabel("Rekap Menu Terjual",
                        new String[]{"Menu", "Terjual", "Omzet (Rp)"}, barisRekap));
        try {
            PdfExporter.export(file, "Laporan Penjualan PesanYuk",
                    "Lingkup: " + scope, ringkasanBaris, tabelPdf);
            Ui.info(this, "Laporan PDF berhasil disimpan:\n" + file.getAbsolutePath());
        } catch (IOException ex) {
            Ui.galat(this, "Laporan PDF gagal disimpan:\n" + ex.getMessage());
        }
    }

    private void eksporCsv() {
        LocalDate awal = tanggal(dari);
        LocalDate akhir = tanggal(sampai);
        File file = pilihSimpan("laporan-penjualan-" + namaFile(awal) + "-"
                + namaFile(akhir) + ".csv", "Berkas CSV", "csv");
        if (file == null) {
            return;
        }
        List<String[]> baris = new ArrayList<>();
        baris.add(new String[]{"Laporan Penjualan PesanYuk"});
        baris.add(new String[]{"Lingkup", scope});
        baris.add(new String[]{"Periode", Tanggal.singkat(awal) + " - "
                + Tanggal.singkat(akhir)});
        baris.add(new String[]{"Total Omzet", Rupiah.angka(ringkasan.omzet())});
        baris.add(new String[]{"Pesanan Selesai", String.valueOf(ringkasan.jumlahPesanan())});
        baris.add(new String[]{"Menu Terjual", String.valueOf(ringkasan.menuTerjual())});
        baris.add(new String[]{"Rata-rata per Pesanan", Rupiah.angka(ringkasan.rataRata())});
        baris.add(new String[]{});
        baris.add(new String[]{"Omzet Harian"});
        baris.add(new String[]{"Tanggal", "Pesanan", "Omzet"});
        for (OmzetHarian h : harian) {
            baris.add(new String[]{Tanggal.singkat(h.tanggal()),
                String.valueOf(h.jumlahPesanan()), Rupiah.angka(h.omzet())});
        }
        baris.add(new String[]{});
        baris.add(new String[]{"Rekap Menu Terjual"});
        baris.add(new String[]{"Menu", "Terjual", "Omzet"});
        for (RekapMenu r : rekap) {
            baris.add(new String[]{r.namaMenu(), String.valueOf(r.jumlahTerjual()),
                Rupiah.angka(r.omzet())});
        }
        try {
            CsvExporter.tulis(file, baris);
            Ui.info(this, "Laporan CSV berhasil disimpan:\n" + file.getAbsolutePath()
                    + "\n\nBerkas dapat dibuka langsung dengan Microsoft Excel.");
        } catch (IOException ex) {
            Ui.galat(this, "Laporan CSV gagal disimpan:\n" + ex.getMessage());
        }
    }

    private File pilihSimpan(String namaBawaan, String deskripsi, String ekstensi) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Simpan " + deskripsi);
        chooser.setSelectedFile(new File(namaBawaan));
        chooser.setFileFilter(new FileNameExtensionFilter(
                deskripsi + " (*." + ekstensi + ")", ekstensi));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith("." + ekstensi)) {
            file = new File(file.getParentFile(), file.getName() + "." + ekstensi);
        }
        return file;
    }

    private static String namaFile(LocalDate tanggal) {
        return String.format("%04d%02d%02d", tanggal.getYear(),
                tanggal.getMonthValue(), tanggal.getDayOfMonth());
    }

    private static JLabel nilai() {
        JLabel label = new JLabel("-");
        label.setFont(Ui.ANGKA);
        label.setForeground(Ui.TEXT);
        return label;
    }

    private static JSpinner tanggalSpinner(LocalDate awal) {
        SpinnerDateModel model = new SpinnerDateModel(java.sql.Date.valueOf(awal),
                null, null, Calendar.DAY_OF_MONTH);
        JSpinner spinner = new JSpinner(model);
        spinner.setEditor(new JSpinner.DateEditor(spinner, "dd/MM/yyyy"));
        spinner.setFont(Ui.BASE);
        spinner.setPreferredSize(new Dimension(126, 36));
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
