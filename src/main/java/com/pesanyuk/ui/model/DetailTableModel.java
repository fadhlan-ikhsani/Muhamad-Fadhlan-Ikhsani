package com.pesanyuk.ui.model;

import com.pesanyuk.model.DetailPesanan;
import com.pesanyuk.util.Rupiah;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

public class DetailTableModel extends AbstractTableModel {

    private final String[] kolom = {"Menu", "Harga", "Jumlah", "Subtotal"};
    private List<DetailPesanan> data = new ArrayList<>();

    public void setData(List<DetailPesanan> data) {
        this.data = data == null ? new ArrayList<>() : new ArrayList<>(data);
        fireTableDataChanged();
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
        DetailPesanan d = data.get(baris);
        return switch (this.kolom[kolom]) {
            case "Menu" ->
                d.namaMenu();
            case "Harga" ->
                Rupiah.format(d.harga());
            case "Jumlah" ->
                d.jumlah();
            case "Subtotal" ->
                Rupiah.format(d.subtotal());
            default ->
                "";
        };
    }
}
