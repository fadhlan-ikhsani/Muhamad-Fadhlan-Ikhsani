package com.pesanyuk.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

/** Ekspor tabel ke CSV yang dapat dibuka langsung di Excel. */
public final class CsvExporter {

    private CsvExporter() {
    }

    public static void tulis(File file, List<String[]> baris) throws IOException {
        StringBuilder sb = new StringBuilder("\uFEFF");
        for (String[] isi : baris) {
            for (int i = 0; i < isi.length; i++) {
                if (i > 0) {
                    sb.append(';');
                }
                sb.append(kutip(isi[i]));
            }
            sb.append("\r\n");
        }
        Files.createDirectories(file.toPath().toAbsolutePath().getParent());
        Files.writeString(file.toPath(), sb.toString(), StandardCharsets.UTF_8);
    }

    private static String kutip(String nilai) {
        String teks = nilai == null ? "" : nilai;
        if (teks.contains(";") || teks.contains("\"") || teks.contains("\n")
                || teks.contains("\r")) {
            return "\"" + teks.replace("\"", "\"\"") + "\"";
        }
        return teks;
    }
}
