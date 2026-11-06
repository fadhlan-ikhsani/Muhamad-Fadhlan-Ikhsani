package com.pesanyuk.ui.panel;

import com.pesanyuk.dao.PesananDao;
import com.pesanyuk.model.Pesanan;
import com.pesanyuk.ui.MainFrame;
import com.pesanyuk.ui.Refreshable;
import com.pesanyuk.ui.Ui;
import com.pesanyuk.ui.dialog.PaymentDialog;
import com.pesanyuk.util.Cart;
import com.pesanyuk.util.Rupiah;
import com.pesanyuk.util.Session;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Window;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;

/** Keranjang belanja dan formulir checkout pelanggan. */
public final class KeranjangPanel extends JPanel implements Refreshable {

    private static final class ModelKeranjang extends AbstractTableModel {
        private final String[] kolom = {"Menu", "Harga", "Jumlah", "Subtotal"};
        private List<Cart.Item> data = List.of();

        void setData(List<Cart.Item> data) {
            this.data = data;
            fireTableDataChanged();
        }

        Cart.Item get(int baris) {
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
            Cart.Item item = data.get(baris);
            return switch (this.kolom[kolom]) {
                case "Menu" ->
                    item.menu().nama();
                case "Harga" ->
                    Rupiah.format(item.menu().harga());
                case "Jumlah" ->
                    item.jumlah();
                case "Subtotal" ->
                    Rupiah.format(item.subtotal());
                default ->
                    "";
            };
        }
    }

    private final MainFrame frame;
    private final PesananDao dao = new PesananDao();
    private final ModelKeranjang model = new ModelKeranjang();
    private final JTable tabel = Ui.tabel(model);

    private final JLabel labelUmkm = Ui.muted("Belum ada item");
    private final JLabel labelItem = Ui.muted("0 item");
    private final JLabel total = Ui.label("Rp 0", new java.awt.Font("Segoe UI",
            java.awt.Font.BOLD, 28), Ui.PRIMARY);

    private final JRadioButton ambil = Ui.radio("Ambil sendiri");
    private final JRadioButton antar = Ui.radio("Diantar penjual");
    private final JTextField alamat = Ui.tinggi(Ui.input(), 36);
    private final JTextArea catatan = Ui.area();
    private final Ui.Btn checkout = new Ui.Btn("Buat Pesanan & Bayar", "primary");

    public KeranjangPanel(MainFrame frame) {
        this.frame = frame;
        setOpaque(false);
        setLayout(new BorderLayout(16, 0));

        add(buatDaftar(), BorderLayout.CENTER);
        add(buatRingkasan(), BorderLayout.EAST);

        ButtonGroup grup = new ButtonGroup();
        grup.add(ambil);
        grup.add(antar);
        ambil.setSelected(true);
        alamat.setEnabled(false);
        antar.addActionListener(e -> alamat.setEnabled(true));
        ambil.addActionListener(e -> alamat.setEnabled(false));

        checkout.addActionListener(e -> prosesCheckout());
    }

    private JPanel buatDaftar() {
        JPanel kartu = Ui.kartu(new BorderLayout(0, 12));
        JPanel kepala = new JPanel(new BorderLayout());
        kepala.setOpaque(false);
        kepala.add(Ui.tebal("Item di Keranjang"), BorderLayout.WEST);
        kepala.add(labelUmkm, BorderLayout.EAST);
        kartu.add(kepala, BorderLayout.NORTH);

        Ui.lebar(tabel, 260, 130, 80, 140);
        kartu.add(Ui.scroll(tabel), BorderLayout.CENTER);

        Ui.Btn kurang = new Ui.Btn("\u2212", "ghost");
        Ui.Btn tambah = new Ui.Btn("+", "ghost");
        Ui.Btn hapus = new Ui.Btn("Hapus item", "ghost");
        hapus.setForeground(Ui.DANGER);
        kurang.setPreferredSize(new Dimension(56, 36));
        tambah.setPreferredSize(new Dimension(56, 36));
        kurang.addActionListener(e -> ubahJumlah(-1));
        tambah.addActionListener(e -> ubahJumlah(1));
        hapus.addActionListener(e -> hapusItem());

        JPanel kontrol = new JPanel(new BorderLayout());
        kontrol.setOpaque(false);
        JPanel kiri = Ui.baris(8, kurang, tambah, hapus);
        kontrol.add(kiri, BorderLayout.WEST);
        kontrol.add(Ui.muted("Pilih item lalu ubah jumlahnya"), BorderLayout.EAST);
        kartu.add(kontrol, BorderLayout.SOUTH);
        return kartu;
    }

    private JPanel buatRingkasan() {
        JPanel kartu = Ui.kartu();
        kartu.setPreferredSize(new Dimension(360, 0));
        kartu.setLayout(new BoxLayout(kartu, BoxLayout.Y_AXIS));

        JLabel judul = Ui.tebal("Ringkasan Pesanan");
        judul.setAlignmentX(LEFT_ALIGNMENT);
        JLabel sub = Ui.muted("Periksa kembali pesanan Anda");
        sub.setAlignmentX(LEFT_ALIGNMENT);

        JPanel baris1 = new JPanel(new BorderLayout());
        baris1.setOpaque(false);
        baris1.setAlignmentX(LEFT_ALIGNMENT);
        baris1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        baris1.add(Ui.muted("Jumlah item"), BorderLayout.WEST);
        baris1.add(labelItem, BorderLayout.EAST);

        JLabel totalLabel = Ui.muted("Total pembayaran");
        totalLabel.setAlignmentX(LEFT_ALIGNMENT);
        JPanel totalBox = new JPanel(new BorderLayout());
        totalBox.setOpaque(false);
        totalBox.setAlignmentX(LEFT_ALIGNMENT);
        totalBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        totalBox.add(totalLabel, BorderLayout.NORTH);
        totalBox.add(total, BorderLayout.CENTER);

        kartu.add(judul);
        kartu.add(Box.createVerticalStrut(2));
        kartu.add(sub);
        kartu.add(Box.createVerticalStrut(14));
        kartu.add(baris1);
        kartu.add(Box.createVerticalStrut(8));
        kartu.add(totalBox);
        kartu.add(Box.createVerticalStrut(14));
        kartu.add(pemisah());
        kartu.add(Box.createVerticalStrut(12));

        JLabel cara = Ui.label("Metode pengambilan", Ui.SMALL_BOLD, Ui.MUTED);
        cara.setAlignmentX(LEFT_ALIGNMENT);
        kartu.add(cara);
        kartu.add(Box.createVerticalStrut(4));
        ambil.setAlignmentX(LEFT_ALIGNMENT);
        antar.setAlignmentX(LEFT_ALIGNMENT);
        kartu.add(ambil);
        kartu.add(antar);
        kartu.add(Box.createVerticalStrut(10));

        JLabel alamatLabel = Ui.label("Alamat pengantaran", Ui.SMALL_BOLD, Ui.MUTED);
        alamatLabel.setAlignmentX(LEFT_ALIGNMENT);
        kartu.add(alamatLabel);
        kartu.add(Box.createVerticalStrut(4));
        alamat.setAlignmentX(LEFT_ALIGNMENT);
        alamat.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        kartu.add(alamat);
        kartu.add(Box.createVerticalStrut(10));

        JLabel catatanLabel = Ui.label("Catatan untuk penjual", Ui.SMALL_BOLD, Ui.MUTED);
        catatanLabel.setAlignmentX(LEFT_ALIGNMENT);
        kartu.add(catatanLabel);
        kartu.add(Box.createVerticalStrut(4));
        catatan.setRows(2);
        JScrollPane gulirCatatan = new JScrollPane(catatan);
        gulirCatatan.setBorder(new javax.swing.border.LineBorder(Ui.BORDER, 1, true));
        gulirCatatan.setAlignmentX(LEFT_ALIGNMENT);
        gulirCatatan.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        kartu.add(gulirCatatan);
        kartu.add(Box.createVerticalStrut(14));

        JLabel info = Ui.muted("<html>Pembayaran diproses melalui payment gateway "
                + "(simulasi Midtrans) dengan QRIS, transfer bank, atau e-wallet.</html>");
        info.setAlignmentX(LEFT_ALIGNMENT);
        kartu.add(info);
        kartu.add(Box.createVerticalGlue());

        checkout.setAlignmentX(LEFT_ALIGNMENT);
        checkout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        checkout.setEnabled(false);
        kartu.add(checkout);
        return kartu;
    }

    private JPanel pemisah() {
        JPanel garis = new JPanel();
        garis.setBackground(Ui.BORDER);
        garis.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        garis.setPreferredSize(new Dimension(0, 1));
        garis.setAlignmentX(LEFT_ALIGNMENT);
        return garis;
    }

    @Override
    public void onShow() {
        muat();
    }

    private void muat() {
        Cart cart = Session.cart();
        model.setData(cart.items());
        labelUmkm.setText(cart.kosong() ? "Keranjang kosong" : cart.umkmNama());
        labelItem.setText(cart.jumlahItem() + " item");
        total.setText(Rupiah.format(cart.total()));
        checkout.setEnabled(!cart.kosong());
        if (tabel.getRowCount() > 0 && tabel.getSelectedRow() < 0) {
            tabel.setRowSelectionInterval(0, 0);
        }
    }

    private Cart.Item terpilih() {
        int baris = tabel.getSelectedRow();
        return baris < 0 ? null : model.get(baris);
    }

    private void ubahJumlah(int selisih) {
        Cart.Item item = terpilih();
        if (item == null) {
            Ui.info(this, "Pilih item terlebih dahulu.");
            return;
        }
        try {
            Session.cart().ubahJumlah(item.menu().id(), item.jumlah() + selisih);
            muat();
            frame.perbaruiKeranjang();
        } catch (RuntimeException ex) {
            Ui.galat(this, ex.getMessage());
        }
    }

    private void hapusItem() {
        Cart.Item item = terpilih();
        if (item == null) {
            Ui.info(this, "Pilih item terlebih dahulu.");
            return;
        }
        Session.cart().hapus(item.menu().id());
        muat();
        frame.perbaruiKeranjang();
    }

    private void prosesCheckout() {
        Cart cart = Session.cart();
        if (cart.kosong()) {
            Ui.info(this, "Keranjang masih kosong.");
            return;
        }
        boolean diantar = antar.isSelected();
        String alamatKirim = diantar ? alamat.getText().trim() : null;
        if (diantar && (alamatKirim == null || alamatKirim.isEmpty())) {
            Ui.galat(this, "Alamat pengantaran wajib diisi.");
            alamat.requestFocusInWindow();
            return;
        }

        List<PesananDao.Baris> baris = new ArrayList<>();
        for (Cart.Item item : cart.items()) {
            baris.add(new PesananDao.Baris(item.menu(), item.jumlah()));
        }

        try {
            Pesanan pesanan = dao.buat(Session.pengguna().id(), cart.umkmId(),
                    diantar ? "PENGANTARAN" : "AMBIL_SENDIRI", alamatKirim,
                    catatan.getText().trim(), baris);

            Window induk = SwingUtilities.getWindowAncestor(this);
            PaymentDialog dialog = new PaymentDialog(induk, pesanan);
            dialog.setVisible(true);

            Session.cart().bersihkan();
            frame.perbaruiKeranjang();
            muat();
            catatan.setText("");

            String pesan = dialog.isDibayar()
                    ? "Pesanan " + pesanan.kode() + " berhasil dibuat dan sudah dibayar."
                    : "Pesanan " + pesanan.kode() + " berhasil dibuat.\n"
                    + "Pembayaran belum diselesaikan. Anda dapat membayarnya dari "
                    + "halaman Pesanan Saya.";
            Ui.info(induk, pesan);
            frame.buka(MainFrame.PESANAN);
        } catch (IllegalArgumentException ex) {
            Ui.galat(this, ex.getMessage());
        } catch (SQLException ex) {
            Ui.galat(this, "Pesanan gagal dibuat:\n" + ex.getMessage());
        }
    }
}
