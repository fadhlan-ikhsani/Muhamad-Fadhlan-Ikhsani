package com.pesanyuk.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class Rupiah {

    private Rupiah() {
    }

    private static DecimalFormat format() {
        DecimalFormatSymbols simbol = new DecimalFormatSymbols(new Locale("id", "ID"));
        simbol.setGroupingSeparator('.');
        simbol.setDecimalSeparator(',');
        DecimalFormat df = new DecimalFormat("#,##0", simbol);
        df.setMaximumFractionDigits(0);
        return df;
    }

    /** Contoh: 18000 -> "Rp 18.000". */
    public static String format(double nilai) {
        return "Rp " + format().format(Math.round(nilai));
    }

    /** Contoh: 18000 -> "18.000" (tanpa awalan Rp). */
    public static String angka(double nilai) {
        return format().format(Math.round(nilai));
    }

    /** Format ringkas untuk label grafik: 1250000 -> "1,3 jt". */
    public static String ringkas(double nilai) {
        if (nilai >= 1_000_000_000) {
            return String.format(Locale.US, "%.1f M", nilai / 1_000_000_000);
        }
        if (nilai >= 1_000_000) {
            return String.format(Locale.US, "%.1f jt", nilai / 1_000_000);
        }
        if (nilai >= 1_000) {
            return String.format(Locale.US, "%.0f rb", nilai / 1_000);
        }
        return String.valueOf(Math.round(nilai));
    }

    /** Membaca angka dari input pengguna, menerima titik/koma/spasi. */
    public static double parse(String teks) {
        if (teks == null) {
            throw new NumberFormatException("Harga belum diisi");
        }
        String bersih = teks.replace("Rp", "").replace(" ", "")
                .replace(".", "").replace(",", ".").trim();
        if (bersih.isEmpty()) {
            throw new NumberFormatException("Harga belum diisi");
        }
        return Double.parseDouble(bersih);
    }
}
