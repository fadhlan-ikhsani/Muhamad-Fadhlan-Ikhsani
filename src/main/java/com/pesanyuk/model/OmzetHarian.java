package com.pesanyuk.model;

import java.time.LocalDate;

/** Omzet satu hari, dipakai untuk grafik dan ekspor laporan. */
public record OmzetHarian(LocalDate tanggal, double omzet, int jumlahPesanan) {
}
