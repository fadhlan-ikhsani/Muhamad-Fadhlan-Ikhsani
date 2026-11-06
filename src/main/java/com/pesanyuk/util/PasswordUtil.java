package com.pesanyuk.util;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Penyimpanan kata sandi menggunakan PBKDF2-HMAC-SHA256 dengan salt acak.
 * Format tersimpan: pbkdf2$iterasi$saltBase64$hashBase64
 */
public final class PasswordUtil {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int ITERASI = 120_000;
    private static final int PANJANG_SALT = 16;
    private static final int PANJANG_KEY = 32;

    private PasswordUtil() {
    }

    public static String hash(String kataSandi) {
        byte[] salt = new byte[PANJANG_SALT];
        RANDOM.nextBytes(salt);
        byte[] hash = turunkan(kataSandi.toCharArray(), salt, ITERASI);
        return "pbkdf2$" + ITERASI + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean cocok(String kataSandi, String tersimpan) {
        if (kataSandi == null || tersimpan == null) {
            return false;
        }
        String[] bagian = tersimpan.split("\\$");
        if (bagian.length != 4 || !"pbkdf2".equals(bagian[0])) {
            return false;
        }
        try {
            int iterasi = Integer.parseInt(bagian[1]);
            byte[] salt = Base64.getDecoder().decode(bagian[2]);
            byte[] hash = Base64.getDecoder().decode(bagian[3]);
            byte[] uji = turunkan(kataSandi.toCharArray(), salt, iterasi);
            return MessageDigest.isEqual(hash, uji);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private static byte[] turunkan(char[] kataSandi, byte[] salt, int iterasi) {
        try {
            KeySpec spec = new PBEKeySpec(kataSandi, salt, iterasi, PANJANG_KEY * 8);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec).getEncoded();
        } catch (Exception ex) {
            throw new IllegalStateException("Gagal memproses kata sandi", ex);
        }
    }
}
