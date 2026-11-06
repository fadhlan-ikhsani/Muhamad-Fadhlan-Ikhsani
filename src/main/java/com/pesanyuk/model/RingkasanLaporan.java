package com.pesanyuk.model;

/** Angka ringkasan laporan penjualan pada rentang tanggal tertentu. */
public record RingkasanLaporan(double omzet, int jumlahPesanan, int menuTerjual) {

    public double rataRata() {
        return jumlahPesanan == 0 ? 0 : omzet / jumlahPesanan;
    }
}
