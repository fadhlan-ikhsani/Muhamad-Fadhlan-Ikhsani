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

/** Pemantauan seluruh transaksi pelanggan oleh admin. */
public final class AdminTransaksiPanel extends JPanel implements Refreshable {

    private final PesananDao dao = new PesananDao();
    private final PesananTableModel model = new PesananTableModel(true, true);
    private final JTable tabel = Ui.tabel(model);
    private final DetailTableModel detailModel = new DetailTableModel();
    private final JTable tabelDetail = Ui.tabel(detailModel);

    private final JComboBox<String> filter = Ui.comboStatus();
    private final Ui.InputPetunjuk cari = new Ui.InputPetunjuk("Cari kode, pelanggan, atau UMKM...");
    private final JLabel info = Ui.muted("0 transaksi");
    private final JLabel totalLunas = Ui.muted("Total lunas: Rp 0");
    private final JLabel detailInfo = Ui.muted("Pilih transaksi untuk melihat rincian.");
    private final Timer timer;

    public AdminTransaksiPanel(MainFrame frame) {
        setOpaque(false);
        setLayout(new BorderLayout(0, 14));

        add(buatToolbar(), BorderLayout.NORTH);
        add(buatKonten(), BorderLayout.CENTER);

        filter.addActionListener(e -> muat());
        cari.addActionListener(e -> muat());
        tabel.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                tampilkanDetail();
            }
        });
        timer = new Timer(5000, e -> {
            if (isShowing()) {
                muat();
            }
        });
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
        kartu.add(Ui.barisKanan(14, totalLunas, info), BorderLayout.EAST);
        return kartu;
    }

    private JPanel buatKonten() {
        JPanel isi = new JPanel(new BorderLayout(0, 14));
        isi.setOpaque(false);

        JPanel kartuTabel = Ui.kartu(new BorderLayout(0, 10));
        JPanel kepala = new JPanel(new BorderLayout());
        kepala.setOpaque(false);
        kepala.add(Ui.tebal("Semua Transaksi"), BorderLayout.WEST);
        kepala.add(Ui.muted("Diperbarui otomatis tiap 5 detik"), BorderLayout.EAST);
        kartuTabel.add(kepala, BorderLayout.NORTH);

        tabel.setAutoCreateRowSorter(true);
        Ui.lebar(tabel, 140, 160, 150, 140, 110, 110, 100);
        Ui.badge(tabel, model.kolomBayar());
        Ui.badge(tabel, model.kolomStatus());
        kartuTabel.add(Ui.scroll(tabel), BorderLayout.CENTER);
        isi.add(kartuTabel, BorderLayout.CENTER);

        JPanel detail = Ui.kartu(new BorderLayout(0, 10));
        detail.setPreferredSize(new Dimension(0, 238));
        detail.add(detailInfo, BorderLayout.NORTH);
        Ui.lebar(tabelDetail, 320, 130, 80, 140);
        detail.add(Ui.scroll(tabelDetail), BorderLayout.CENTER);
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
        int idTerpilih = sebelumnya == null ? 0 : sebelumnya.id();
        String status = (String) filter.getSelectedItem();
        try {
            List<Pesanan> semua = dao.cari(null, null, status);
            String kunci = cari.getText().trim().toLowerCase();
            List<Pesanan> tampil = kunci.isEmpty() ? semua : semua.stream()
                    .filter(p -> p.kode().toLowerCase().contains(kunci)
                    || p.pelangganNama().toLowerCase().contains(kunci)
                    || p.umkmNama().toLowerCase().contains(kunci))
                    .toList();
            model.setData(tampil);
            info.setText(tampil.size() + " transaksi");
            double lunas = tampil.stream().filter(Pesanan::sudahDibayar)
                    .mapToDouble(Pesanan::subtotal).sum();
            totalLunas.setText("Total lunas: " + Rupiah.format(lunas));

            if (!tampil.isEmpty()) {
                int baris = 0;
                for (int i = 0; i < tampil.size(); i++) {
                    if (tampil.get(i).id() == idTerpilih) {
                        baris = i;
                        break;
                    }
                }
                int view = tabel.convertRowIndexToView(baris);
                tabel.setRowSelectionInterval(view, view);
                tabel.scrollRectToVisible(tabel.getCellRect(view, 0, true));
            } else {
                kosongkanDetail();
            }
        } catch (SQLException ex) {
            Ui.galat(this, "Transaksi gagal dimuat:\n" + ex.getMessage());
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
                    : "Ambil sendiri di " + p.umkmNama();
            detailInfo.setText("<html><b>" + p.kode() + "</b> \u00b7 "
                    + p.pelangganNama() + " \u00b7 " + p.umkmNama() + " \u00b7 "
                    + Tanggal.lengkap(p.dibuatPada())
                    + "<br>" + alamat
                    + (p.catatan() == null || p.catatan().isBlank()
                            ? "" : " \u00b7 Catatan: " + p.catatan())
                    + " \u00b7 Pembayaran: " + Labels.bayar(p.statusBayar())
                    + (p.metodeBayar() == null ? "" : " (" + Labels.metode(p.metodeBayar()) + ")")
                    + " \u00b7 Total: <b>" + Rupiah.format(p.subtotal()) + "</b></html>");
        } catch (SQLException ex) {
            Ui.galat(this, "Rincian transaksi gagal dimuat:\n" + ex.getMessage());
        }
    }

    private void kosongkanDetail() {
        detailModel.setData(List.of());
        detailInfo.setText("Belum ada transaksi yang dipilih.");
    }
}
