package com.pesanyuk.ui.panel;

import com.pesanyuk.dao.PenggunaDao;
import com.pesanyuk.dao.UmkmDao;
import com.pesanyuk.model.Pengguna;
import com.pesanyuk.model.UmkmAdmin;
import com.pesanyuk.ui.MainFrame;
import com.pesanyuk.ui.Refreshable;
import com.pesanyuk.ui.Ui;
import com.pesanyuk.ui.model.PenggunaTableModel;
import com.pesanyuk.util.Labels;
import com.pesanyuk.util.PasswordUtil;
import com.pesanyuk.util.Session;
import com.pesanyuk.util.Validator;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;

/** Pengelolaan akun pengguna oleh admin: filter, aktivasi, reset sandi. */
public final class AdminPenggunaPanel extends JPanel implements Refreshable {

    private final PenggunaDao dao = new PenggunaDao();
    private final UmkmDao umkmDao = new UmkmDao();
    private final PenggunaTableModel model = new PenggunaTableModel();
    private final JTable tabel = Ui.tabel(model);

    private final JComboBox<String> filter = Ui.pilihan(new String[]{
        "Semua peran", "Admin", "Penjual", "Pelanggan"});
    private final Ui.InputPetunjuk cari = new Ui.InputPetunjuk("Cari nama atau email...");
    private final JLabel info = Ui.muted("0 pengguna");
    private final JLabel terpilihInfo = Ui.muted("Pilih pengguna untuk mengelola akun.");

    private final Ui.Btn aktif = new Ui.Btn("Nonaktifkan", "ghost");
    private final Ui.Btn reset = new Ui.Btn("Reset Kata Sandi", "ghost");

    public AdminPenggunaPanel(MainFrame frame) {
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
        aktif.addActionListener(e -> ubahAktivasi());
        reset.addActionListener(e -> resetSandi());
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
        kepala.add(Ui.tebal("Daftar Pengguna"), BorderLayout.WEST);
        kepala.add(Ui.muted("Admin dapat menonaktifkan akun dan mengatur ulang sandi"),
                BorderLayout.EAST);
        kartu.add(kepala, BorderLayout.NORTH);

        tabel.setAutoCreateRowSorter(true);
        Ui.lebar(tabel, 200, 240, 90, 180, 100, 110);
        Ui.badge(tabel, 4);
        kartu.add(Ui.scroll(tabel), BorderLayout.CENTER);

        JPanel bawah = new JPanel(new BorderLayout(10, 0));
        bawah.setOpaque(false);
        bawah.add(terpilihInfo, BorderLayout.WEST);
        bawah.add(Ui.barisKanan(8, reset, aktif), BorderLayout.EAST);
        kartu.add(bawah, BorderLayout.SOUTH);
        return kartu;
    }

    @Override
    public void onShow() {
        muat();
    }

    private void muat() {
        Pengguna sebelumnya = penggunaTerpilih();
        int idTerpilih = sebelumnya == null ? 0 : sebelumnya.id();
        String role = switch (filter.getSelectedIndex()) {
            case 1 ->
                "ADMIN";
            case 2 ->
                "PENJUAL";
            case 3 ->
                "PELANGGAN";
            default ->
                null;
        };
        try {
            List<Pengguna> semua = dao.findAll(role);
            String kunci = cari.getText().trim().toLowerCase();
            List<Pengguna> tampil = kunci.isEmpty() ? semua : semua.stream()
                    .filter(p -> p.nama().toLowerCase().contains(kunci)
                    || p.email().toLowerCase().contains(kunci))
                    .toList();
            Map<Integer, UmkmAdmin> petaUmkm = umkmDao.findAllLengkap().stream()
                    .collect(Collectors.toMap(UmkmAdmin::penggunaId, u -> u, (a, b) -> a));
            model.setData(tampil, petaUmkm);
            info.setText(tampil.size() + " pengguna");

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
            Ui.galat(this, "Daftar pengguna gagal dimuat:\n" + ex.getMessage());
        }
    }

    private Pengguna penggunaTerpilih() {
        int baris = tabel.getSelectedRow();
        if (baris < 0) {
            return null;
        }
        int modelBaris = tabel.convertRowIndexToModel(baris);
        return modelBaris >= 0 && modelBaris < model.getRowCount() ? model.get(modelBaris) : null;
    }

    private void tampilkanPilihan() {
        Pengguna p = penggunaTerpilih();
        if (p == null) {
            terpilihInfo.setText("Pilih pengguna untuk mengelola akun.");
            aktif.setText("Nonaktifkan");
            aktif.setJenis("ghost");
            aktif.setEnabled(false);
            reset.setEnabled(false);
            return;
        }
        UmkmAdmin umkm = null;
        try {
            umkm = umkmDao.findAllLengkap().stream()
                    .filter(u -> u.penggunaId() == p.id()).findFirst().orElse(null);
        } catch (SQLException ex) {
            // Informasi UMKM bersifat tambahan; abaikan bila gagal dimuat.
        }
        terpilihInfo.setText("<html><b>" + p.nama() + "</b> \u00b7 " + p.email()
                + " \u00b7 " + Labels.role(p.role())
                + (umkm == null ? "" : " \u00b7 " + umkm.namaUmkm()
                        + " (" + Labels.statusUmkm(umkm.status()) + ")")
                + " \u00b7 Akun " + Labels.aktif(p.aktif()).toLowerCase() + "</html>");
        aktif.setText(p.aktif() ? "Nonaktifkan" : "Aktifkan");
        aktif.setJenis(p.aktif() ? "danger" : "primary");
        boolean diriSendiri = p.id() == Session.pengguna().id();
        aktif.setEnabled(!diriSendiri);
        if (diriSendiri) {
            aktif.setText("Akun Anda");
        }
        reset.setEnabled(true);
    }

    private void ubahAktivasi() {
        Pengguna p = penggunaTerpilih();
        if (p == null) {
            return;
        }
        if (p.id() == Session.pengguna().id()) {
            Ui.galat(this, "Anda tidak dapat menonaktifkan akun sendiri.");
            return;
        }
        boolean aktifBaru = !p.aktif();
        String aksi = aktifBaru ? "Mengaktifkan" : "Menonaktifkan";
        if (!Ui.konfirmasi(this, aksi + " akun " + p.nama() + " (" + p.email() + ")?")) {
            return;
        }
        try {
            dao.setAktif(p.id(), aktifBaru);
            Ui.info(this, "Akun " + p.nama() + " berhasil "
                    + (aktifBaru ? "diaktifkan." : "dinonaktifkan."));
            muat();
        } catch (SQLException ex) {
            Ui.galat(this, "Status akun gagal diubah:\n" + ex.getMessage());
        }
    }

    private void resetSandi() {
        Pengguna p = penggunaTerpilih();
        if (p == null) {
            return;
        }
        String baru = JOptionPane.showInputDialog(this,
                "Kata sandi baru untuk " + p.nama() + " (minimal 6 karakter):",
                "Reset Kata Sandi", JOptionPane.PLAIN_MESSAGE);
        if (baru == null) {
            return;
        }
        try {
            Validator.kataSandi(baru);
            dao.ubahKataSandi(p.id(), PasswordUtil.hash(baru));
            Ui.info(this, "Kata sandi " + p.nama() + " berhasil diatur ulang.");
        } catch (IllegalArgumentException ex) {
            Ui.galat(this, ex.getMessage());
        } catch (SQLException ex) {
            Ui.galat(this, "Kata sandi gagal diubah:\n" + ex.getMessage());
        }
    }
}
