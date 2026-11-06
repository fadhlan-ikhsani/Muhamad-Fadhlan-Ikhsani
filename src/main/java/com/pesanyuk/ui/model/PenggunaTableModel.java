package com.pesanyuk.ui.model;

import com.pesanyuk.model.Pengguna;
import com.pesanyuk.model.UmkmAdmin;
import com.pesanyuk.util.Labels;
import com.pesanyuk.util.Tanggal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.table.AbstractTableModel;

public class PenggunaTableModel extends AbstractTableModel {

    private final String[] kolom = {"Nama", "Email", "Peran", "UMKM", "Status", "Terdaftar"};
    private List<Pengguna> data = new ArrayList<>();
    private Map<Integer, UmkmAdmin> umkm = Map.of();

    public void setData(List<Pengguna> data, Map<Integer, UmkmAdmin> umkm) {
        this.data = data == null ? new ArrayList<>() : new ArrayList<>(data);
        this.umkm = umkm == null ? Map.of() : umkm;
        fireTableDataChanged();
    }

    public Pengguna get(int baris) {
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
        Pengguna p = data.get(baris);
        UmkmAdmin u = umkm.get(p.id());
        return switch (this.kolom[kolom]) {
            case "Nama" ->
                p.nama();
            case "Email" ->
                p.email();
            case "Peran" ->
                Labels.role(p.role());
            case "UMKM" ->
                u == null ? "-" : u.namaUmkm();
            case "Status" ->
                Labels.aktif(p.aktif());
            case "Terdaftar" ->
                Tanggal.singkat(p.dibuatPada().toInstant()
                        .atZone(java.time.ZoneId.systemDefault()).toLocalDate());
            default ->
                "";
        };
    }
}
