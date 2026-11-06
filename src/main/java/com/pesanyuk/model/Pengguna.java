package com.pesanyuk.model;

import java.sql.Timestamp;

/** Data akun pengguna aplikasi: pelanggan, penjual, atau admin. */
public record Pengguna(
        int id,
        String nama,
        String email,
        String role,
        boolean aktif,
        Timestamp dibuatPada) {

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    public boolean isPenjual() {
        return "PENJUAL".equals(role);
    }

    public boolean isPelanggan() {
        return "PELANGGAN".equals(role);
    }

    public String inisial() {
        if (nama == null || nama.isBlank()) {
            return "?";
        }
        String[] bagian = nama.trim().split("\\s+");
        String hasil = bagian[0].substring(0, 1);
        if (bagian.length > 1) {
            hasil += bagian[bagian.length - 1].substring(0, 1);
        }
        return hasil.toUpperCase();
    }
}
