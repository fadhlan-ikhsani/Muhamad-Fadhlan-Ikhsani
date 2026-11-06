package com.pesanyuk.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import javax.imageio.ImageIO;

/** Menyimpan, memuat, dan membuat gambar menu di folder uploads/menu. */
public final class PhotoStore {

    private static final Path FOLDER = Path.of("uploads", "menu");

    private PhotoStore() {
    }

    public static Path folder() {
        return FOLDER;
    }

    /** Menyalin foto pilihan pengguna, mengembalikan path relatif untuk basis data. */
    public static String simpan(File sumber, String awalan) {
        try {
            Files.createDirectories(FOLDER);
            String namaAsli = sumber.getName();
            String ekstensi = "png";
            int titik = namaAsli.lastIndexOf('.');
            if (titik >= 0) {
                ekstensi = namaAsli.substring(titik + 1).toLowerCase(Locale.ROOT);
            }
            String namaBaru = awalan + "-" + System.currentTimeMillis() + "." + ekstensi;
            Path tujuan = FOLDER.resolve(namaBaru);
            Files.copy(sumber.toPath(), tujuan, StandardCopyOption.REPLACE_EXISTING);
            return FOLDER.resolve(namaBaru).toString().replace('\\', '/');
        } catch (IOException ex) {
            throw new IllegalStateException("Foto gagal disimpan: " + ex.getMessage(), ex);
        }
    }

    public static BufferedImage muat(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        try {
            File file = new File(path);
            if (!file.exists()) {
                return null;
            }
            return ImageIO.read(file);
        } catch (IOException ex) {
            return null;
        }
    }

    /** Membuat gambar contoh untuk data awal sehingga katalog tidak kosong. */
    public static void buatContoh(String path, String judul, Color awal, Color akhir) {
        try {
            Path tujuan = Path.of(path);
            if (Files.exists(tujuan)) {
                return;
            }
            Files.createDirectories(tujuan.getParent());
            BufferedImage gambar = new BufferedImage(640, 420, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = gambar.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setPaint(new GradientPaint(0, 0, awal, 640, 420, akhir));
            g.fillRect(0, 0, 640, 420);

            g.setColor(new Color(255, 255, 255, 35));
            g.fillOval(-80, 240, 300, 300);
            g.fillOval(430, -70, 300, 300);

            g.setColor(new Color(255, 255, 255, 235));
            g.setFont(new Font("Segoe UI", Font.BOLD, 190));
            String inisial = judul.substring(0, 1).toUpperCase(Locale.ROOT);
            int lebar = g.getFontMetrics().stringWidth(inisial);
            g.drawString(inisial, (640 - lebar) / 2, 300);

            g.setFont(new Font("Segoe UI", Font.BOLD, 26));
            int lebarJudul = g.getFontMetrics().stringWidth(judul);
            g.setColor(new Color(255, 255, 255, 220));
            g.drawString(judul, (640 - lebarJudul) / 2, 370);
            g.dispose();
            ImageIO.write(gambar, "png", tujuan.toFile());
        } catch (IOException ex) {
            System.err.println("Gagal membuat foto contoh: " + ex.getMessage());
        }
    }
}
