package com.pesanyuk.ui.panel;

import com.pesanyuk.dao.PesananDao;
import com.pesanyuk.model.Pesanan;
import com.pesanyuk.ui.MainFrame;
import com.pesanyuk.ui.Refreshable;
import com.pesanyuk.ui.Ui;
import com.pesanyuk.ui.dialog.PaymentDialog;
import com.pesanyuk.ui.model.DetailTableModel;
import com.pesanyuk.ui.model.PesananTableModel;
import com.pesanyuk.util.Labels;
import com.pesanyuk.util.Rupiah;
import com.pesanyuk.util.Session;
import com.pesanyuk.util.Tanggal;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.sql.SQLException;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

/** Riwayat pesanan pelanggan beserta status dan aksi pembayaran. */
public final class PesananSayaPanel extends JPanel implements Refreshable {

    private final MainFrame frame;
    private final PesananDao dao = new PesananDao();
    private final PesananTableModel model = new PesananTableModel(false, true);
    private final JTable tabel = Ui.tabel(model);
    private final DetailTableModel detailModel = new DetailTableModel();
    private final JTable tabelDetail = Ui.tabel(detailModel);

    private final JComboBox<String> filter = Ui.comboStatus();
    private final JLabel info = Ui.muted("0 pesanan");
    private final JLabel detailInfo = Ui.muted("Pilih pesanan untuk melihat rincian.");
    private final Ui.Btn bayar = new Ui.Btn("Bayar Sekarang", "primary");
    private final Ui.Btn batal = new Ui.Btn("Batalkan Pesanan", "ghost");
    private final Timer timer;

    public PesananSayaPanel(MainFrame frame) {
        this.frame = frame;
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
        bayar.addActionListener(e -> bayar());
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
        JPanel kiri = Ui.baris(10, Ui.label("Status", Ui.SMALL_BOLD, Ui.MUTED), filter);
        Ui.tinggi(filter, 36);
        Ui.Btn muat = new Ui.Btn("Muat ulang", "ghost");
        muat.addActionListener(e -> muat());
        kiri.add(muat);
        kartu.add(kiri, BorderLayout.WEST);
        JPanel kanan = Ui.barisKanan(12, info,
                Ui.muted("Diperbarui otomatis tiap 5 detik"));
        kartu.add(kanan, BorderLayout.EAST);
        return kartu;
    }

    private JPanel buatKonten() {
        JPanel isi = new JPanel(new BorderLayout(0, 14));
        isi.setOpaque(false);

        JPanel kartuTabel = Ui.kartu(new BorderLayout(0, 10));
        kartuTabel.add(Ui.tebal("Riwayat Pesanan"), BorderLayout.NORTH);
        tabel.setAutoCreateRowSorter(true);
        Ui.lebar(tabel, 130, 160, 150, 120, 120, 110);
        Ui.badge(tabel, 4);
        Ui.badge(tabel, 5);
        kartuTabel.add(Ui.scroll(tabel), BorderLayout.CENTER);
        isi.add(kartuTabel, BorderLayout.CENTER);

        JPanel detail = Ui.kartu(new BorderLayout(0, 10));
        detail.setPreferredSize(new Dimension(0, 244));
        detail.add(detailInfo, BorderLayout.NORTH);
        Ui.lebar(tabelDetail, 320, 130, 80, 140);
        detail.add(Ui.scroll(tabelDetail), BorderLayout.CENTER);

        Ui.Btn belanja = new Ui.Btn("Belanja Lagi", "ghost");
        belanja.addActionListener(e -> frame.buka(MainFrame.KATALOG));
        JPanel aksi = new JPanel(new BorderLayout());
        aksi.setOpaque(false);
        aksi.add(Ui.baris(8, bayar, batal), BorderLayout.WEST);
        aksi.add(Ui.barisKanan(0, belanja), BorderLayout.EAST);
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
        int dipilih = pesananTerpilih() == null ? 0 : pesananTerpilih().id();
        String status = (String) filter.getSelectedItem();
        try {
            List<Pesanan> daftar = dao.cari(Session.pengguna().id(), null, status);
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
        return modelBaris < model.getRowCount() ? model.get(modelBaris) : null;
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
                    + Labels.jenis(p.jenis()) + " \u00b7 " + Tanggal.lengkap(p.dibuatPada())
                    + "<br>" + alamat
                    + (p.catatan() == null || p.catatan().isBlank()
                            ? "" : " \u00b7 Catatan: " + p.catatan())
                    + " \u00b7 Pembayaran: " + Labels.bayar(p.statusBayar())
                    + (p.metodeBayar() == null ? "" : " (" + Labels.metode(p.metodeBayar()) + ")")
                    + " \u00b7 Total: <b>" + Rupiah.format(p.subtotal()) + "</b></html>");
            bayar.setEnabled(!p.sudahDibayar() && !"DIBATALKAN".equals(p.status())
                    && !"SELESAI".equals(p.status()));
            batal.setEnabled(p.bisaDibatalkan());
        } catch (SQLException ex) {
            Ui.galat(this, "Rincian pesanan gagal dimuat:\n" + ex.getMessage());
        }
    }

    private void kosongkanDetail() {
        detailModel.setData(List.of());
        detailInfo.setText("Belum ada pesanan yang dipilih.");
        bayar.setEnabled(false);
        batal.setEnabled(false);
    }

    private void bayar() {
        Pesanan p = pesananTerpilih();
        if (p == null) {
            return;
        }
        Window induk = SwingUtilities.getWindowAncestor(this);
        PaymentDialog dialog = new PaymentDialog(induk, p);
        dialog.setVisible(true);
        if (dialog.isDibayar()) {
            Ui.info(induk, "Pembayaran pesanan " + p.kode() + " berhasil.");
            muat();
        }
    }

    private void batalkan() {
        Pesanan p = pesananTerpilih();
        if (p == null) {
            return;
        }
        if (!Ui.konfirmasi(this, "Batalkan pesanan " + p.kode() + "?\n"
                + "Stok menu akan dikembalikan ke penjual.")) {
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
