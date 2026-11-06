package com.pesanyuk.model;

/** Satu item menu yang dijual oleh sebuah UMKM. */
public record Menu(
        int id,
        int umkmId,
        String umkmNama,
        int kategoriId,
        String kategoriNama,
        String nama,
        String deskripsi,
        double harga,
        int stok,
        String foto,
        boolean tersedia) {

    public boolean bisaDipesan() {
        return tersedia && stok > 0;
    }

    @Override
    public String toString() {
        return nama;
    }
}
