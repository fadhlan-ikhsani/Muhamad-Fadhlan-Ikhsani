package com.pesanyuk.model;

/** Satu baris rincian item di dalam pesanan. */
public record DetailPesanan(
        int id,
        int pesananId,
        int menuId,
        String namaMenu,
        double harga,
        int jumlah,
        double subtotal) {
}
