package com.pesanyuk.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Penulis PDF sederhana tanpa pustaka tambahan. Mendukung judul, paragraf
 * ringkasan, dan beberapa tabel dengan pemenggalan halaman otomatis.
 */
public final class PdfExporter {

    public record Tabel(String judul, String[] kolom, List<String[]> baris) {
    }

    private static final int LEBAR_HALAMAN = 595;
    private static final int TINGGI_HALAMAN = 842;
    private static final int MARGIN = 45;
    private static final int LEBAR_ISI = LEBAR_HALAMAN - 2 * MARGIN;

    private final List<StringBuilder> halaman = new ArrayList<>();
    private StringBuilder isi;
    private double y;

    private PdfExporter() {
        halamanBaru();
    }

    public static void export(File file, String judul, String subjudul,
            List<String> ringkasan, List<Tabel> tabel) throws IOException {
        PdfExporter pdf = new PdfExporter();
        pdf.tulisJudul(judul, subjudul);
        for (String baris : ringkasan) {
            pdf.pastikanRuang(20);
            pdf.teks(MARGIN, pdf.y, false, 10, "374151", baris);
            pdf.y -= 15;
        }
        pdf.y -= 8;
        for (Tabel t : tabel) {
            pdf.tabel(t);
        }
        pdf.footer();
        Files.createDirectories(file.toPath().toAbsolutePath().getParent());
        Files.write(file.toPath(), pdf.keByte());
    }

    private void tulisJudul(String judul, String subjudul) {
        teks(MARGIN, y, true, 19, "1F2937", judul);
        y -= 22;
        teks(MARGIN, y, false, 9, "6B7280", subjudul);
        y -= 10;
        garis(MARGIN, y, LEBAR_HALAMAN - MARGIN, y, "E5E7EB");
        y -= 22;
    }

    private void tabel(Tabel t) {
        pastikanRuang(70);
        teks(MARGIN, y, true, 11, "B45309", t.judul());
        y -= 12;

        int[] lebar = lebarKolom(t.kolom(), t.baris());
        header(t.kolom(), lebar);

        if (t.baris().isEmpty()) {
            pastikanRuang(20);
            teks(MARGIN + 4, y - 4, false, 9, "6B7280", "Belum ada data.");
            y -= 20;
        }
        for (String[] baris : t.baris()) {
            if (y < 66) {
                halamanBaru();
                teks(MARGIN, y, true, 11, "B45309", t.judul() + " (lanjutan)");
                y -= 12;
                header(t.kolom(), lebar);
            }
            int x = MARGIN;
            for (int i = 0; i < baris.length && i < lebar.length; i++) {
                teks(x + 4, y - 5, false, 9, "1F2937", potong(baris[i], lebar[i]));
                x += lebar[i];
            }
            y -= 17;
            garis(MARGIN, y + 2, LEBAR_HALAMAN - MARGIN, y + 2, "F1F2F4");
        }
        y -= 14;
    }

    private void header(String[] kolom, int[] lebar) {
        if (y < 80) {
            halamanBaru();
        }
        kotak(MARGIN, y - 8, LEBAR_ISI, 20, "F3F4F6");
        int x = MARGIN;
        for (int i = 0; i < kolom.length && i < lebar.length; i++) {
            teks(x + 4, y + 5, true, 9, "374151", potong(kolom[i], lebar[i]));
            x += lebar[i];
        }
        y -= 20;
    }

    private int[] lebarKolom(String[] kolom, List<String[]> baris) {
        int n = kolom.length;
        double[] bobot = new double[n];
        for (int i = 0; i < n; i++) {
            int maks = Math.min(28, kolom[i].length());
            for (String[] r : baris) {
                if (i < r.length && r[i] != null) {
                    maks = Math.max(maks, Math.min(28, r[i].length()));
                }
            }
            bobot[i] = Math.max(6, maks);
        }
        double total = 0;
        for (double b : bobot) {
            total += b;
        }
        int[] lebar = new int[n];
        int terpakai = 0;
        for (int i = 0; i < n; i++) {
            lebar[i] = Math.max(42, (int) Math.round(bobot[i] / total * LEBAR_ISI));
            terpakai += lebar[i];
        }
        int sisa = LEBAR_ISI - terpakai;
        lebar[n - 1] = Math.max(42, lebar[n - 1] + sisa);
        return lebar;
    }

    private String potong(String nilai, int lebar) {
        String teks = bersih(nilai == null ? "" : nilai);
        int maks = Math.max(3, (int) ((lebar - 8) / 4.3));
        if (teks.length() <= maks) {
            return teks;
        }
        return teks.substring(0, maks - 3) + "...";
    }

    private void pastikanRuang(double butuh) {
        if (y < 60 + butuh) {
            halamanBaru();
        }
    }

    private void halamanBaru() {
        isi = new StringBuilder();
        halaman.add(isi);
        y = TINGGI_HALAMAN - 52;
    }

    private void teks(double x, double y, boolean tebal, double ukuran,
            String warna, String teks) {
        isi.append("BT /").append(tebal ? "F2" : "F1").append(' ')
                .append(nomor(ukuran)).append(" Tf ")
                .append(rgb(warna)).append(" rg ")
                .append(nomor(x)).append(' ').append(nomor(y)).append(" Td (")
                .append(escape(teks)).append(") Tj ET\n");
    }

    private void garis(double x1, double y1, double x2, double y2, String warna) {
        isi.append("0.5 w ").append(rgb(warna)).append(" RG ")
                .append(nomor(x1)).append(' ').append(nomor(y1)).append(" m ")
                .append(nomor(x2)).append(' ').append(nomor(y2)).append(" l S\n");
    }

    private void kotak(double x, double y, double lebar, double tinggi, String warna) {
        isi.append(rgb(warna)).append(" rg ")
                .append(nomor(x)).append(' ').append(nomor(y)).append(' ')
                .append(nomor(lebar)).append(' ').append(nomor(tinggi)).append(" re f\n");
    }

    private void footer() {
        int total = halaman.size();
        for (int i = 0; i < total; i++) {
            StringBuilder simpan = isi;
            isi = halaman.get(i);
            garis(MARGIN, 38, LEBAR_HALAMAN - MARGIN, 38, "E5E7EB");
            teks(MARGIN, 26, false, 8, "9CA3AF",
                    "PesanYuk Desktop - dicetak " + Tanggal.lengkap(
                            java.sql.Timestamp.valueOf(LocalDateTime.now())));
            String nomor = "Halaman " + (i + 1) + " dari " + total;
            teks(LEBAR_HALAMAN - MARGIN - nomor.length() * 4.2, 26, false, 8,
                    "9CA3AF", nomor);
            isi = simpan;
        }
    }

    private byte[] keByte() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int jumlahHalaman = halaman.size();
        int jumlahObjek = 4 + jumlahHalaman * 2;
        int[] offset = new int[jumlahObjek + 1];

        tulis(out, "%PDF-1.4\n");
        out.write(new byte[]{(byte) '%', (byte) 0xE2, (byte) 0xE3, (byte) 0xCF,
            (byte) 0xD3, '\n'});

        offset[1] = out.size();
        tulis(out, "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");

        StringBuilder kids = new StringBuilder();
        for (int i = 0; i < jumlahHalaman; i++) {
            if (i > 0) {
                kids.append(' ');
            }
            kids.append(5 + i * 2).append(" 0 R");
        }
        offset[2] = out.size();
        tulis(out, "2 0 obj\n<< /Type /Pages /Kids [" + kids + "] /Count "
                + jumlahHalaman + " >>\nendobj\n");

        offset[3] = out.size();
        tulis(out, "3 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n");
        offset[4] = out.size();
        tulis(out, "4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>\nendobj\n");

        for (int i = 0; i < jumlahHalaman; i++) {
            int objekHalaman = 5 + i * 2;
            int objekIsi = objekHalaman + 1;
            offset[objekHalaman] = out.size();
            tulis(out, objekHalaman + " 0 obj\n<< /Type /Page /Parent 2 0 R"
                    + " /MediaBox [0 0 " + LEBAR_HALAMAN + " " + TINGGI_HALAMAN + "]"
                    + " /Resources << /Font << /F1 3 0 R /F2 4 0 R >> >>"
                    + " /Contents " + objekIsi + " 0 R >>\nendobj\n");

            byte[] isiHalaman = halaman.get(i).toString()
                    .getBytes(StandardCharsets.ISO_8859_1);
            offset[objekIsi] = out.size();
            tulis(out, objekIsi + " 0 obj\n<< /Length " + isiHalaman.length + " >>\nstream\n");
            out.write(isiHalaman);
            tulis(out, "\nendstream\nendobj\n");
        }

        int xref = out.size();
        tulis(out, "xref\n0 " + (jumlahObjek + 1) + "\n");
        tulis(out, "0000000000 65535 f \n");
        for (int i = 1; i <= jumlahObjek; i++) {
            tulis(out, String.format("%010d 00000 n \n", offset[i]));
        }
        tulis(out, "trailer\n<< /Size " + (jumlahObjek + 1)
                + " /Root 1 0 R >>\nstartxref\n" + xref + "\n%%EOF\n");
        return out.toByteArray();
    }

    private void tulis(ByteArrayOutputStream out, String teks) throws IOException {
        out.write(teks.getBytes(StandardCharsets.ISO_8859_1));
    }

    private String nomor(double nilai) {
        if (nilai == Math.rint(nilai)) {
            return String.valueOf((long) nilai);
        }
        return String.format(java.util.Locale.US, "%.2f", nilai);
    }

    private String rgb(String hex) {
        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);
        return String.format(java.util.Locale.US, "%.3f %.3f %.3f",
                r / 255.0, g / 255.0, b / 255.0);
    }

    private String escape(String teks) {
        return bersih(teks).replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }

    /** PDF standar Helvetica hanya mendukung karakter Latin-1. */
    private String bersih(String teks) {
        String tanpaAksen = Normalizer.normalize(teks, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        StringBuilder sb = new StringBuilder();
        for (char c : tanpaAksen.toCharArray()) {
            if (c >= 32 && c <= 255) {
                sb.append(c);
            } else if (c == '\n' || c == '\t') {
                sb.append(' ');
            } else {
                sb.append('?');
            }
        }
        return sb.toString();
    }
}
