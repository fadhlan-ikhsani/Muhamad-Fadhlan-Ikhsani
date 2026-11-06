package com.pesanyuk.ui.panel;

import com.pesanyuk.dao.PesananDao;
import com.pesanyuk.model.Pesanan;
import com.pesanyuk.ui.MainFrame;
import com.pesanyuk.ui.Refreshable;
import com.pesanyuk.ui.Ui;
import com.pesanyuk.ui.model.DetailTableModel;
import com.pesanyuk.ui.model.PesananTableModel;
import com.pesanyuk.util.Labels;
import com.pesanyuk.util.Rupiah;
import com.pesanyuk.util.Session;
import com.pesanyuk.util.Tanggal;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.sql.SQLException;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

/** Pesanan masuk untuk satu UMKM beserta aksi perubahan statusnya. */
public final class PenjualPesananPanel extends JPanel implements Refreshable {

    private final PesananDao dao = new PesananDao();
    private final PesananTableModel model = new PesananTableModel(true, false);
    private final JTable tabel = Ui.tabel(model);
    private final DetailTableModel detailModel = new DetailTableModel();
    private final JTable tabelDetail = Ui.tabel(detailModel);

    private final JComboBox<String> filter = Ui.comboStatus();
    private final JLabel info = Ui.muted("0 pesanan");
    private final JLabel detailInfo = Ui.muted("Pilih pesanan untuk melihat rincian.");

    private final Ui.Btn proses = new Ui.Btn("Proses Pesanan", "primary");
    private final Ui.Btn siap = new Ui.Btn("Tandai Siap", "ghost");
    private final Ui.Btn selesai = new Ui.Btn("Selesaikan", "ghost");
    private final Ui.Btn batal = new Ui.Btn("Batalkan", "ghost");
    private final Timer timer;

    public PenjualPesananPanel(MainFrame frame) {
        setOpaque(false);
        setLayout(new BorderLayout(0, 14));

        add(buatToolbar(), BorderLayout.NORTH);
        add(buatKonten(), BorderLayout.CENTER);

        filter.addActionListener(e -> muat());
        tabel.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                tampilkanDetail();
            }
        });
        proses.addActionListener(e -> ubahStatus("DIPROSES",
                "Pesanan mulai diproses."));
        siap.addActionListener(e -> ubahStatus("SIAP",
                "Pesanan siap diambil atau diantar."));
        selesai.addActionListener(e -> ubahStatus("SELESAI",
                "Pesanan telah diselesaikan."));
        batal.addActionListener(e -> batalkan());

        timer = new Timer(5000, e -> {
            if (isShowing()) {
                muat();
            }
        });
    }

    private JPanel buatToolbar() {
        JPanel kartu = Ui.kartu(new BorderLayout(10, 0));
        kartu.setBorder(new EmptyBorder(14, 18, 14, 18));

        Ui.tinggi(filter, 36);
        Ui.Btn muat = new Ui.Btn("Muat ulang", "ghost");
        muat.addActionListener(e -> muat());
        JPanel kiri = Ui.baris(10, Ui.label("Status", Ui.SMALL_BOLD, Ui.MUTED),
                filter, muat);
        kartu.add(kiri, BorderLayout.WEST);
        kartu.add(Ui.barisKanan(12, info,
                Ui.muted("Diperbarui otomatis tiap 5 detik")), BorderLayout.EAST);
        return kartu;
    }

    private JPanel buatKonten() {
        JPanel isi = new JPanel(new BorderLayout(0, 14));
        isi.setOpaque(false);

        JPanel kartuTabel = Ui.kartu(new BorderLayout(0, 10));
        JPanel kepala = new JPanel(new BorderLayout());
        kepala.setOpaque(false);
        kepala.add(Ui.tebal("Pesanan Masuk"), BorderLayout.WEST);
        kepala.add(Ui.muted("Pesanan pelanggan untuk UMKM Anda"), BorderLayout.EAST);
        kartuTabel.add(kepala, BorderLayout.NORTH);

        tabel.setAutoCreateRowSorter(true);
        Ui.lebar(tabel, 140, 170, 150, 120, 120, 110);
        Ui.badge(tabel, model.kolomBayar());
        Ui.badge(tabel, model.kolomStatus());
        kartuTabel.add(Ui.scroll(tabel), BorderLayout.CENTER);
        isi.add(kartuTabel, BorderLayout.CENTER);

        JPanel detail = Ui.kartu(new BorderLayout(0, 10));
        detail.setPreferredSize(new Dimension(0, 250));
        detail.add(detailInfo, BorderLayout.NORTH);
        Ui.lebar(tabelDetail, 320, 130, 80, 140);
        detail.add(Ui.scroll(tabelDetail), BorderLayout.CENTER);

        Ui.Btn segarkan = new Ui.Btn("Segarkan", "ghost");
        segarkan.addActionListener(e -> muat());
        batal.setForeground(Ui.DANGER);
        proses.setEnabled(false);
        siap.setEnabled(false);
        selesai.setEnabled(false);
        batal.setEnabled(false);
        JPanel aksi = new JPanel(new BorderLayout());
        aksi.setOpaque(false);
        aksi.add(Ui.baris(8, proses, siap, selesai, batal), BorderLayout.WEST);
        aksi.add(Ui.barisKanan(0, segarkan), BorderLayout.EAST);
        detail.add(aksi, BorderLayout.SOUTH);
        isi.add(detail, BorderLayout.SOUTH);
        return isi;
    }

    @Override
    public void onShow() {
        muat();
        timer.start();
    }

    private void muat() {
        Pesanan sebelumnya = pesananTerpilih();
        int dipilih = sebelumnya == null ? 0 : sebelumnya.id();
        String status = (String) filter.getSelectedItem();
        try {
            List<Pesanan> daftar = dao.cari(null, Session.umkm().id(), status);
            model.setData(daftar);
            info.setText(daftar.size() + " pesanan");
            if (!daftar.isEmpty()) {
                int baris = 0;
                for (int i = 0; i < daftar.size(); i++) {
                    if (daftar.get(i).id() == dipilih) {
                        baris = i;
                        break;
                    }
                }
                int tampil = tabel.convertRowIndexToView(baris);
                tabel.setRowSelectionInterval(tampil, tampil);
                tabel.scrollRectToVisible(tabel.getCellRect(tampil, 0, true));
            } else {
                kosongkanDetail();
            }
        } catch (SQLException ex) {
            Ui.galat(this, "Pesanan gagal dimuat:\n" + ex.getMessage());
        }
    }

    private Pesanan pesananTerpilih() {
        int baris = tabel.getSelectedRow();
        if (baris < 0) {
            return null;
        }
        int modelBaris = tabel.convertRowIndexToModel(baris);
        return modelBaris >= 0 && modelBaris < model.getRowCount() ? model.get(modelBaris) : null;
    }

    private void tampilkanDetail() {
        Pesanan p = pesananTerpilih();
        if (p == null) {
            kosongkanDetail();
            return;
        }
        try {
            detailModel.setData(dao.detail(p.id()));
            String alamat = "PENGANTARAN".equals(p.jenis())
                    ? "Diantar ke " + (p.alamat() == null ? "-" : p.alamat())
                    : "Diambil sendiri di " + p.umkmNama();
            detailInfo.setText("<html><b>" + p.kode() + "</b> \u00b7 "
                    + p.pelangganNama() + " \u00b7 " + Labels.jenis(p.jenis())
                    + " \u00b7 " + Tanggal.lengkap(p.dibuatPada())
                    + "<br>" + alamat
                    + (p.catatan() == null || p.catatan().isBlank()
                            ? "" : " \u00b7 Catatan: " + p.catatan())
                    + " \u00b7 Pembayaran: " + Labels.bayar(p.statusBayar())
                    + (p.metodeBayar() == null ? "" : " (" + Labels.metode(p.metodeBayar()) + ")")
                    + " \u00b7 Total: <b>" + Rupiah.format(p.subtotal()) + "</b></html>");

            boolean menunggu = "MENUNGGU".equals(p.status());
            proses.setEnabled(menunggu);
            siap.setEnabled("DIPROSES".equals(p.status()));
            selesai.setEnabled("SIAP".equals(p.status()));
            batal.setEnabled(p.bisaDibatalkan());
        } catch (SQLException ex) {
            Ui.galat(this, "Rincian pesanan gagal dimuat:\n" + ex.getMessage());
        }
    }

    private void kosongkanDetail() {
        detailModel.setData(List.of());
        detailInfo.setText("Belum ada pesanan yang dipilih.");
        proses.setEnabled(false);
        siap.setEnabled(false);
        selesai.setEnabled(false);
        batal.setEnabled(false);
    }

    private void ubahStatus(String statusBaru, String pesan) {
        Pesanan p = pesananTerpilih();
        if (p == null) {
            return;
        }
        if ("MENUNGGU".equals(p.status()) && !p.sudahDibayar()) {
            Ui.galat(this, "Pesanan ini belum dibayar pelanggan.\n"
                    + "Minta pelanggan menyelesaikan pembayaran terlebih dahulu.");
            return;
        }
        if (!Ui.konfirmasi(this, pesan + "\n\nPesanan " + p.kode() + "?")) {
            return;
        }
        try {
            dao.ubahStatus(p.id(), statusBaru);
            Ui.info(this, "Status pesanan " + p.kode() + " menjadi "
                    + Labels.pesanan(statusBaru) + ".");
            muat();
        } catch (SQLException ex) {
            Ui.galat(this, "Status pesanan gagal diubah:\n" + ex.getMessage());
        }
    }

    private void batalkan() {
        Pesanan p = pesananTerpilih();
        if (p == null) {
            return;
        }
        if (!Ui.konfirmasi(this, "Batalkan pesanan " + p.kode() + "?\n"
                + "Stok menu akan dikembalikan.")) {
            return;
        }
        try {
            dao.batalkan(p.id());
            Ui.info(this, "Pesanan " + p.kode() + " telah dibatalkan.");
            muat();
        } catch (IllegalArgumentException ex) {
            Ui.galat(this, ex.getMessage());
        } catch (SQLException ex) {
            Ui.galat(this, "Pesanan gagal dibatalkan:\n" + ex.getMessage());
        }
    }
}
