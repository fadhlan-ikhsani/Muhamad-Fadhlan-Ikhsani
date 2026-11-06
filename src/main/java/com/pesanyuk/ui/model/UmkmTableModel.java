package com.pesanyuk.ui.model;

import com.pesanyuk.model.UmkmAdmin;
import com.pesanyuk.util.Tanggal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.table.AbstractTableModel;

public class UmkmTableModel extends AbstractTableModel {

    private final String[] kolom = {"UMKM", "Pemilik", "Status", "Telepon", "Alamat", "Terdaftar"};
    private List<UmkmAdmin> data = new ArrayList<>();

    public void setData(List<UmkmAdmin> data) {
        this.data = data == null ? new ArrayList<>() : new ArrayList<>(data);
        fireTableDataChanged();
    }

    public UmkmAdmin get(int baris) {
        return data.get(baris);
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
        UmkmAdmin u = data.get(baris);
        return switch (this.kolom[kolom]) {
            case "UMKM" ->
                u.namaUmkm();
            case "Pemilik" ->
                u.pemilik();
            case "Status" ->
                u.status();
            case "Telepon" ->
                u.telepon() == null ? "-" : u.telepon();
            case "Alamat" ->
                u.alamat() == null ? "-" : u.alamat();
            case "Terdaftar" ->
                u.dibuatPada() == null ? "-" : Tanggal.singkat(u.dibuatPada().toInstant()
                        .atZone(java.time.ZoneId.systemDefault()).toLocalDate());
            default ->
                "";
        };
    }

    public String namaUmkm(int baris) {
        return get(baris).namaUmkm().toUpperCase(Locale.ROOT);
    }
}
