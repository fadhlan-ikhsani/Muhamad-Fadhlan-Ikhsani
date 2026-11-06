package com.pesanyuk.util;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class Tanggal {

    private static final DateTimeFormatter LENGKAP =
            DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm", new Locale("id", "ID"));
    private static final DateTimeFormatter SINGKAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", new Locale("id", "ID"));
    private static final DateTimeFormatter LABEL_GRAFIK =
            DateTimeFormatter.ofPattern("dd/MM", new Locale("id", "ID"));

    private Tanggal() {
    }

    public static String lengkap(Timestamp waktu) {
        return waktu == null ? "-" : LENGKAP.format(waktu.toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime());
    }

    public static String singkat(LocalDate tanggal) {
        return tanggal == null ? "-" : SINGKAT.format(tanggal);
    }

    public static String labelGrafik(LocalDate tanggal) {
        return tanggal == null ? "-" : LABEL_GRAFIK.format(tanggal);
    }

    public static String hariIni() {
        return SINGKAT.format(LocalDate.now());
    }
}
