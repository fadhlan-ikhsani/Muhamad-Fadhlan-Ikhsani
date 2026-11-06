package com.pesanyuk.ui.model;

import com.pesanyuk.model.Pesanan;
import com.pesanyuk.util.Labels;
import com.pesanyuk.util.Rupiah;
import com.pesanyuk.util.Tanggal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/** Model tabel pesanan yang dipakai pelanggan, penjual, dan admin. */
public class PesananTableModel extends AbstractTableModel {

    private final String[] kolom;
    private final int kolomBayar;
    private final int kolomStatus;
    private List<Pesanan> data = new ArrayList<>();

    public PesananTableModel(boolean tampilPelanggan, boolean tampilUmkm) {
        List<String> judul = new ArrayList<>();
        judul.add("Kode");
        if (tampilPelanggan) {
            judul.add("Pelanggan");
        }
        if (tampilUmkm) {
            judul.add("UMKM");
        }
        judul.add("Tanggal");
        judul.add("Total");
        judul.add("Pembayaran");
        judul.add("Status");
        kolom = judul.toArray(String[]::new);
        kolomBayar = Arrays.asList(kolom).indexOf("Pembayaran");
        kolomStatus = Arrays.asList(kolom).indexOf("Status");
    }

    public void setData(List<Pesanan> data) {
        this.data = data == null ? new ArrayList<>() : new ArrayList<>(data);
        fireTableDataChanged();
    }

    public Pesanan get(int baris) {
        return data.get(baris);
    }

    public int kolomBayar() {
        return kolomBayar;
    }

    public int kolomStatus() {
        return kolomStatus;
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return kolom.length;
    }

    @Override
    public String getColumnName(int kolom) {
        return this.kolom[kolom];
    }

    @Override
    public Object getValueAt(int baris, int kolom) {
        Pesanan p = data.get(baris);
        return switch (this.kolom[kolom]) {
            case "Kode" ->
                p.kode();
            case "Pelanggan" ->
                p.pelangganNama();
            case "UMKM" ->
                p.umkmNama();
            case "Tanggal" ->
                Tanggal.lengkap(p.dibuatPada());
            case "Total" ->
                Rupiah.format(p.subtotal());
            case "Pembayaran" ->
                Labels.bayar(p.statusBayar());
            case "Status" ->
                p.status();
            default ->
                "";
        };
    }
}
