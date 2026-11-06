package com.pesanyuk.model;

import java.sql.Timestamp;

/** Pesanan pelanggan beserta ringkasan status pembayarannya. */
public record Pesanan(
        int id,
        String kode,
        int pelangganId,
        String pelangganNama,
        int umkmId,
        String umkmNama,
        String jenis,
        String alamat,
        String catatan,
        double subtotal,
        String status,
        Timestamp dibuatPada,
        String metodeBayar,
        String statusBayar) {

    public boolean sudahDibayar() {
        return "LUNAS".equals(statusBayar);
    }

    public boolean bisaDibatalkan() {
        return "MENUNGGU".equals(status) || "DIPROSES".equals(status);
    }
}
