package com.pesanyuk.ui.panel;

import com.pesanyuk.dao.UmkmDao;
import com.pesanyuk.model.UmkmAdmin;
import com.pesanyuk.ui.MainFrame;
import com.pesanyuk.ui.Refreshable;
import com.pesanyuk.ui.Ui;
import com.pesanyuk.ui.model.UmkmTableModel;
import com.pesanyuk.util.Labels;
import com.pesanyuk.util.Tanggal;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.sql.SQLException;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;

/** Verifikasi dan pemantauan UMKM oleh admin. */
public final class AdminUmkmPanel extends JPanel implements Refreshable {

    private final UmkmDao dao = new UmkmDao();
    private final UmkmTableModel model = new UmkmTableModel();
    private final JTable tabel = Ui.tabel(model);

    private final JComboBox<String> filter = Ui.pilihan(new String[]{
        "Semua status", "Menunggu Verifikasi", "Terverifikasi", "Ditangguhkan"});
    private final Ui.InputPetunjuk cari = new Ui.InputPetunjuk("Cari UMKM atau pemilik...");
    private final JLabel info = Ui.muted("0 UMKM");
    private final JLabel detail = Ui.muted("Pilih UMKM untuk melihat rincian.");

    private final Ui.Btn verifikasi = new Ui.Btn("Verifikasi", "primary");
    private final Ui.Btn tangguhkan = new Ui.Btn("Tangguhkan", "ghost");
    private final Ui.Btn aktifkan = new Ui.Btn("Aktifkan Kembali", "ghost");

    public AdminUmkmPanel(MainFrame frame) {
        setOpaque(false);
        setLayout(new BorderLayout(0, 14));

        add(buatToolbar(), BorderLayout.NORTH);
        add(buatKonten(), BorderLayout.CENTER);

        filter.addActionListener(e -> muat());
        cari.addActionListener(e -> muat());
        tabel.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                tampilkanPilihan();
            }
        });
        verifikasi.addActionListener(e -> ubahStatus("TERVERIFIKASI",
                "UMKM berhasil diverifikasi dan dapat berjualan."));
        tangguhkan.addActionListener(e -> ubahStatus("DITANGGUHKAN",
                "UMKM ditangguhkan dan tidak tampil di katalog."));
        aktifkan.addActionListener(e -> ubahStatus("TERVERIFIKASI",
                "UMKM diaktifkan kembali."));
        tampilkanPilihan();
    }

    private JPanel buatToolbar() {
        JPanel kartu = Ui.kartu(new BorderLayout(12, 0));
        kartu.setBorder(new EmptyBorder(14, 18, 14, 18));

        cari.setPreferredSize(new Dimension(250, 36));
        Ui.tinggi(filter, 36);
        Ui.Btn cariTombol = new Ui.Btn("Cari", "primary");
        Ui.Btn muat = new Ui.Btn("Muat ulang", "ghost");
        cariTombol.addActionListener(e -> muat());
        muat.addActionListener(e -> muat());

        JPanel kiri = Ui.baris(10, filter, cari, cariTombol, muat);
        kartu.add(kiri, BorderLayout.WEST);
        kartu.add(Ui.barisKanan(0, info), BorderLayout.EAST);
        return kartu;
    }

    private JPanel buatKonten() {
        JPanel kartu = Ui.kartu(new BorderLayout(0, 10));
        JPanel kepala = new JPanel(new BorderLayout());
        kepala.setOpaque(false);
        kepala.add(Ui.tebal("Daftar UMKM"), BorderLayout.WEST);
        kepala.add(Ui.muted("UMKM berstatus terverifikasi tampil di katalog pelanggan"),
                BorderLayout.EAST);
        kartu.add(kepala, BorderLayout.NORTH);

        tabel.setAutoCreateRowSorter(true);
        Ui.lebar(tabel, 190, 170, 130, 130, 250, 100);
        Ui.badge(tabel, 2);
        kartu.add(Ui.scroll(tabel), BorderLayout.CENTER);

        JPanel bawah = new JPanel(new BorderLayout(10, 0));
        bawah.setOpaque(false);
        bawah.add(detail, BorderLayout.WEST);
        bawah.add(Ui.barisKanan(8, tangguhkan, aktifkan, verifikasi), BorderLayout.EAST);
        kartu.add(bawah, BorderLayout.SOUTH);
        return kartu;
    }

    @Override
    public void onShow() {
        muat();
    }

    private void muat() {
        UmkmAdmin sebelumnya = umkmTerpilih();
        int idTerpilih = sebelumnya == null ? 0 : sebelumnya.id();
        String status = switch (filter.getSelectedIndex()) {
            case 1 ->
                "PENDING";
            case 2 ->
                "TERVERIFIKASI";
            case 3 ->
                "DITANGGUHKAN";
            default ->
                null;
        };
        try {
            List<UmkmAdmin> semua = dao.findAllLengkap();
            String kunci = cari.getText().trim().toLowerCase();
            List<UmkmAdmin> tampil = semua.stream()
                    .filter(u -> status == null || status.equals(u.status()))
                    .filter(u -> kunci.isEmpty()
                    || u.namaUmkm().toLowerCase().contains(kunci)
                    || u.pemilik().toLowerCase().contains(kunci)
                    || u.email().toLowerCase().contains(kunci))
                    .toList();
            model.setData(tampil);
            info.setText(tampil.size() + " UMKM");

            if (idTerpilih > 0) {
                for (int i = 0; i < tampil.size(); i++) {
                    if (tampil.get(i).id() == idTerpilih) {
                        int baris = tabel.convertRowIndexToView(i);
                        tabel.setRowSelectionInterval(baris, baris);
                        tabel.scrollRectToVisible(tabel.getCellRect(baris, 0, true));
                        break;
                    }
                }
            } else {
                tabel.clearSelection();
            }
            tampilkanPilihan();
        } catch (SQLException ex) {
            Ui.galat(this, "Daftar UMKM gagal dimuat:\n" + ex.getMessage());
        }
    }

    private UmkmAdmin umkmTerpilih() {
        int baris = tabel.getSelectedRow();
        if (baris < 0) {
            return null;
        }
        int modelBaris = tabel.convertRowIndexToModel(baris);
        return modelBaris >= 0 && modelBaris < model.getRowCount() ? model.get(modelBaris) : null;
    }

    private void tampilkanPilihan() {
        UmkmAdmin u = umkmTerpilih();
        if (u == null) {
            detail.setText("Pilih UMKM untuk melihat rincian.");
            verifikasi.setEnabled(false);
            tangguhkan.setEnabled(false);
            aktifkan.setEnabled(false);
            return;
        }
        detail.setText("<html><b>" + u.namaUmkm() + "</b> \u00b7 " + u.email()
                + "<br>Pemilik: " + u.pemilik()
                + " \u00b7 Telepon: " + (u.telepon() == null ? "-" : u.telepon())
                + " \u00b7 Terdaftar: " + Tanggal.singkat(u.dibuatPada().toInstant()
                        .atZone(java.time.ZoneId.systemDefault()).toLocalDate())
                + "<br>Alamat: " + (u.alamat() == null ? "-" : u.alamat())
                + " \u00b7 Status: " + Labels.statusUmkm(u.status()) + "</html>");
        verifikasi.setEnabled("PENDING".equals(u.status()));
        tangguhkan.setEnabled(!"DITANGGUHKAN".equals(u.status()));
        aktifkan.setEnabled("DITANGGUHKAN".equals(u.status()));
    }

    private void ubahStatus(String status, String pesan) {
        UmkmAdmin u = umkmTerpilih();
        if (u == null) {
            return;
        }
        if (!Ui.konfirmasi(this, pesan + "\n\nUMKM: " + u.namaUmkm())) {
            return;
        }
        try {
            dao.setStatus(u.id(), status);
            Ui.info(this, pesan);
            muat();
        } catch (SQLException ex) {
            Ui.galat(this, "Status UMKM gagal diubah:\n" + ex.getMessage());
        }
    }
}
