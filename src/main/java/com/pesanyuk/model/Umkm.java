package com.pesanyuk.model;

import java.sql.Timestamp;

/** Data usaha mikro, kecil, dan menengah milik akun penjual. */
public record Umkm(
        int id,
        int penggunaId,
        String namaUmkm,
        String deskripsi,
        String alamat,
        String telepon,
        String status,
        Timestamp dibuatPada) {

    public boolean terverifikasi() {
        return "TERVERIFIKASI".equals(status);
    }

    @Override
    public String toString() {
        return namaUmkm;
    }
}
