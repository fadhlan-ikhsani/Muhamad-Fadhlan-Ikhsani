package com.pesanyuk.model;

import java.sql.Timestamp;

/** Data UMKM digabung dengan akun pemiliknya untuk halaman admin. */
public record UmkmAdmin(
        int id,
        int penggunaId,
        String namaUmkm,
        String pemilik,
        String email,
        String status,
        String alamat,
        String telepon,
        Timestamp dibuatPada) {
}
