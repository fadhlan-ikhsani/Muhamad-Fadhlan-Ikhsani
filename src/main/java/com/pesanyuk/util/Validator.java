package com.pesanyuk.util;

import java.util.regex.Pattern;

public final class Validator {

    private static final Pattern EMAIL =
            Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    private Validator() {
    }

    public static void wajib(String nilai, String namaField) {
        if (nilai == null || nilai.isBlank()) {
            throw new IllegalArgumentException(namaField + " wajib diisi.");
        }
    }

    public static void email(String nilai) {
        wajib(nilai, "Email");
        if (!EMAIL.matcher(nilai.trim()).matches()) {
            throw new IllegalArgumentException("Format email tidak valid.");
        }
    }

    public static void kataSandi(String nilai) {
        wajib(nilai, "Kata sandi");
        if (nilai.length() < 6) {
            throw new IllegalArgumentException("Kata sandi minimal 6 karakter.");
        }
    }

    public static void positif(double nilai, String namaField) {
        if (nilai <= 0) {
            throw new IllegalArgumentException(namaField + " harus lebih dari 0.");
        }
    }

    public static void panjangMaks(String nilai, int maks, String namaField) {
        if (nilai != null && nilai.length() > maks) {
            throw new IllegalArgumentException(namaField + " maksimal " + maks + " karakter.");
        }
    }
}
