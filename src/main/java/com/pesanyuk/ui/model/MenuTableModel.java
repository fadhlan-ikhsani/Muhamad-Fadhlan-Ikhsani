package com.pesanyuk.ui.model;

import com.pesanyuk.model.Menu;
import com.pesanyuk.util.Rupiah;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

public class MenuTableModel extends AbstractTableModel {

    private final boolean tampilUmkm;
    private final String[] kolom;
    private List<Menu> data = new ArrayList<>();

    public MenuTableModel(boolean tampilUmkm) {
        this.tampilUmkm = tampilUmkm;
        kolom = tampilUmkm
                ? new String[]{"Menu", "UMKM", "Kategori", "Harga", "Stok", "Status"}
                : new String[]{"Menu", "Kategori", "Harga", "Stok", "Status"};
    }

    public void setData(List<Menu> data) {
        this.data = data == null ? new ArrayList<>() : new ArrayList<>(data);
        fireTableDataChanged();
    }

    public Menu get(int baris) {
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
        Menu m = data.get(baris);
        return switch (this.kolom[kolom]) {
            case "Menu" ->
                m.nama();
            case "UMKM" ->
                m.umkmNama();
            case "Kategori" ->
                m.kategoriNama();
            case "Harga" ->
                Rupiah.angka(m.harga());
            case "Stok" ->
                m.stok();
            case "Status" ->
                !m.tersedia() ? "Nonaktif" : (m.stok() <= 0 ? "Stok Habis" : "Tersedia");
            default ->
                "";
        };
    }

    public boolean tampilUmkm() {
        return tampilUmkm;
    }
}
