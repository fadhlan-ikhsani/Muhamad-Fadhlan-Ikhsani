package com.pesanyuk.util;

import java.util.Map;

/** Label bahasa Indonesia untuk nilai enum yang tersimpan di basis data. */
public final class Labels {

    private static final Map<String, String> PESANAN = Map.of(
            "MENUNGGU", "Menunggu",
            "DIPROSES", "Diproses",
            "SIAP", "Siap",
            "SELESAI", "Selesai",
            "DIBATALKAN", "Dibatalkan");

    private static final Map<String, String> BAYAR = Map.of(
            "MENUNGGU", "Belum Dibayar",
            "LUNAS", "Lunas",
            "GAGAL", "Gagal");

    private static final Map<String, String> METODE = Map.of(
            "TRANSFER_BANK", "Transfer Bank",
            "E_WALLET", "E-Wallet",
            "QRIS", "QRIS");

    private static final Map<String, String> JENIS = Map.of(
            "AMBIL_SENDIRI", "Ambil Sendiri",
            "PENGANTARAN", "Pengantaran");

    private static final Map<String, String> ROLE = Map.of(
            "ADMIN", "Admin",
            "PENJUAL", "Penjual",
            "PELANGGAN", "Pelanggan");

    private static final Map<String, String> UMKM = Map.of(
            "PENDING", "Menunggu Verifikasi",
            "TERVERIFIKASI", "Terverifikasi",
            "DITANGGUHKAN", "Ditangguhkan");

    private Labels() {
    }

    public static String pesanan(String nilai) {
        return PESANAN.getOrDefault(nilai, nilai == null ? "-" : nilai);
    }

    public static String bayar(String nilai) {
        return BAYAR.getOrDefault(nilai, nilai == null ? "Belum Dibayar" : nilai);
    }

    public static String metode(String nilai) {
        return METODE.getOrDefault(nilai, nilai == null ? "-" : nilai);
    }

    public static String jenis(String nilai) {
        return JENIS.getOrDefault(nilai, nilai == null ? "-" : nilai);
    }

    public static String role(String nilai) {
        return ROLE.getOrDefault(nilai, nilai == null ? "-" : nilai);
    }

    public static String statusUmkm(String nilai) {
        return UMKM.getOrDefault(nilai, nilai == null ? "-" : nilai);
    }

    public static String aktif(boolean aktif) {
        return aktif ? "Aktif" : "Nonaktif";
    }
}
