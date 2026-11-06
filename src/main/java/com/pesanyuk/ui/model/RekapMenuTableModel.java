package com.pesanyuk.ui.model;

import com.pesanyuk.model.RekapMenu;
import com.pesanyuk.util.Rupiah;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

public class RekapMenuTableModel extends AbstractTableModel {

    private final String[] kolom = {"Menu", "Terjual", "Omzet"};
    private List<RekapMenu> data = new ArrayList<>();

    public void setData(List<RekapMenu> data) {
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
        RekapMenu r = data.get(baris);
        return switch (this.kolom[kolom]) {
            case "Menu" ->
                r.namaMenu();
            case "Terjual" ->
                r.jumlahTerjual();
            case "Omzet" ->
                Rupiah.format(r.omzet());
            default ->
                "";
        };
    }
}
