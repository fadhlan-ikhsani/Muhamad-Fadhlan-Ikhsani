package com.pesanyuk.ui.panel;

import com.pesanyuk.dao.MenuDao;
import com.pesanyuk.model.Kategori;
import com.pesanyuk.model.Menu;
import com.pesanyuk.ui.MainFrame;
import com.pesanyuk.ui.Refreshable;
import com.pesanyuk.ui.Ui;
import com.pesanyuk.ui.model.MenuTableModel;
import com.pesanyuk.util.Rupiah;
import com.pesanyuk.util.Session;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

/** Katalog menu pelanggan dengan pencarian, filter, dan detail menu. */
public final class KatalogPanel extends JPanel implements Refreshable {

    private static final Kategori SEMUA = new Kategori(0, "Semua Kategori");
    private static final String[] KUNCI_URUTAN = {"nama", "termurah", "termahal", "terbaru"};

    private final MainFrame frame;
    private final MenuDao dao = new MenuDao();
    private final MenuTableModel model = new MenuTableModel(true);
    private final JTable tabel = Ui.tabel(model);

    private final Ui.InputPetunjuk cari = new Ui.InputPetunjuk("Cari menu atau UMKM...");
    private final JComboBox<Kategori> kategori = Ui.pilihan(new Kategori[]{SEMUA});
    private final JComboBox<String> urutan = Ui.pilihan(new String[]{
        "Nama A-Z", "Harga termurah", "Harga termahal", "Terbaru"});
    private final JLabel jumlah = Ui.muted("0 menu ditemukan");

    private final Ui.Foto foto = new Ui.Foto("Pilih menu untuk melihat foto");
    private final JLabel namaMenu = Ui.label("Pilih salah satu menu", Ui.H2, Ui.TEXT);
    private final JLabel namaUmkm = Ui.muted(" ");
    private final JLabel harga = Ui.label(" ", new java.awt.Font("Segoe UI",
            java.awt.Font.BOLD, 24), Ui.PRIMARY);
    private final JLabel stok = Ui.muted(" ");
    private final JTextArea deskripsi = Ui.area();
    private final JSpinner jumlahPesan = Ui.spinner(0, 999, 0);
    private final Ui.Btn tambah = new Ui.Btn("Tambah ke Keranjang", "primary");
    private final JLabel umpanBalik = Ui.label(" ", Ui.SMALL_BOLD, Ui.SUCCESS);

    private boolean memuatKategori;
    private List<Menu> data = List.of();

    public KatalogPanel(MainFrame frame) {
        this.frame = frame;
        setOpaque(false);
        setLayout(new BorderLayout(0, 14));

        add(buatFilter(), BorderLayout.NORTH);
        add(buatKonten(), BorderLayout.CENTER);

        cari.addActionListener(e -> muat());
        kategori.addActionListener(e -> {
            if (!memuatKategori) {
                muat();
            }
        });
        urutan.addActionListener(e -> muat());
        tambah.addActionListener(e -> tambahKeKeranjang());
        tabel.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                tampilkanDetail();
            }
        });
        tabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    jumlahPesan.setValue(1);
                    tambahKeKeranjang();
                }
            }
        });
    }

    private JPanel buatFilter() {
        JPanel kartu = Ui.kartu(new BorderLayout(12, 0));
        kartu.setBorder(new EmptyBorder(14, 18, 14, 18));

        JPanel kiri = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 0));
        kiri.setOpaque(false);
        cari.setPreferredSize(new Dimension(250, 36));
        Ui.tinggi(kategori, 36);
        Ui.tinggi(urutan, 36);
        Ui.Btn cariTombol = new Ui.Btn("Cari", "primary");
        Ui.Btn reset = new Ui.Btn("Reset", "ghost");
        cariTombol.addActionListener(e -> muat());
        reset.addActionListener(e -> {
            cari.setText("");
            kategori.setSelectedIndex(0);
            urutan.setSelectedIndex(0);
            muat();
        });
        kiri.add(cari);
        kiri.add(kategori);
        kiri.add(urutan);
        kiri.add(cariTombol);
        kiri.add(reset);

        kartu.add(kiri, BorderLayout.CENTER);
        kartu.add(Ui.barisKanan(0, jumlah), BorderLayout.EAST);
        return kartu;
    }

    private JPanel buatKonten() {
        JPanel isi = new JPanel(new BorderLayout(16, 0));
        isi.setOpaque(false);

        JPanel kartuTabel = Ui.kartu(new BorderLayout(0, 10));
        JPanel kepala = new JPanel(new BorderLayout());
        kepala.setOpaque(false);
        kepala.add(Ui.tebal("Menu Tersedia"), BorderLayout.WEST);
        kepala.add(Ui.muted("Klik dua kali untuk menambah"), BorderLayout.EAST);
        kartuTabel.add(kepala, BorderLayout.NORTH);

        tabel.setAutoCreateRowSorter(true);
        Ui.lebar(tabel, 200, 150, 100, 90, 50, 100);
        JScrollPane gulir = Ui.scroll(tabel);
        kartuTabel.add(gulir, BorderLayout.CENTER);

        isi.add(kartuTabel, BorderLayout.CENTER);
        isi.add(buatDetail(), BorderLayout.EAST);
        return isi;
    }

    private JPanel buatDetail() {
        JPanel kartu = Ui.kartu();
        kartu.setPreferredSize(new Dimension(336, 0));
        kartu.setLayout(new BoxLayout(kartu, BoxLayout.Y_AXIS));

        foto.setAlignmentX(LEFT_ALIGNMENT);
        foto.setMaximumSize(new Dimension(Integer.MAX_VALUE, 168));
        foto.setPreferredSize(new Dimension(296, 168));

        namaMenu.setAlignmentX(LEFT_ALIGNMENT);
        namaUmkm.setAlignmentX(LEFT_ALIGNMENT);
        harga.setAlignmentX(LEFT_ALIGNMENT);
        stok.setAlignmentX(LEFT_ALIGNMENT);

        deskripsi.setEditable(false);
        deskripsi.setOpaque(false);
        deskripsi.setBorder(new EmptyBorder(0, 0, 0, 0));
        deskripsi.setFont(Ui.SMALL);
        deskripsi.setForeground(Ui.MUTED);
        deskripsi.setRows(4);
        deskripsi.setLineWrap(true);
        deskripsi.setWrapStyleWord(true);
        JScrollPane gulirDeskripsi = new JScrollPane(deskripsi);
        gulirDeskripsi.setBorder(null);
        gulirDeskripsi.setOpaque(false);
        gulirDeskripsi.getViewport().setOpaque(false);
        gulirDeskripsi.setAlignmentX(LEFT_ALIGNMENT);
        gulirDeskripsi.setMaximumSize(new Dimension(Integer.MAX_VALUE, 78));

        JPanel barisJumlah = new JPanel(new GridLayout(1, 2, 10, 0));
        barisJumlah.setOpaque(false);
        barisJumlah.setAlignmentX(LEFT_ALIGNMENT);
        barisJumlah.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        JLabel labelJumlah = Ui.label("Jumlah", Ui.SMALL_BOLD, Ui.MUTED);
        JPanel kiri = new JPanel(new BorderLayout(0, 2));
        kiri.setOpaque(false);
        kiri.add(labelJumlah, BorderLayout.NORTH);
        kiri.add(jumlahPesan, BorderLayout.CENTER);
        barisJumlah.add(kiri);
        barisJumlah.add(new JLabel());

        tambah.setAlignmentX(LEFT_ALIGNMENT);
        tambah.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        tambah.setEnabled(false);
        umpanBalik.setAlignmentX(LEFT_ALIGNMENT);

        kartu.add(foto);
        kartu.add(Box.createVerticalStrut(14));
        kartu.add(namaMenu);
        kartu.add(Box.createVerticalStrut(3));
        kartu.add(namaUmkm);
        kartu.add(Box.createVerticalStrut(10));
        kartu.add(harga);
        kartu.add(Box.createVerticalStrut(4));
        kartu.add(stok);
        kartu.add(Box.createVerticalStrut(12));
        kartu.add(gulirDeskripsi);
        kartu.add(Box.createVerticalStrut(12));
        kartu.add(barisJumlah);
        kartu.add(Box.createVerticalStrut(12));
        kartu.add(tambah);
        kartu.add(Box.createVerticalStrut(6));
        kartu.add(umpanBalik);
        kartu.add(Box.createVerticalGlue());
        return kartu;
    }

    @Override
    public void onShow() {
        try {
            muatKategori();
            muat();
        } catch (SQLException ex) {
            Ui.galat(this, "Katalog gagal dimuat:\n" + ex.getMessage());
        }
    }

    private void muatKategori() throws SQLException {
        memuatKategori = true;
        try {
            int dipilih = kategori.getSelectedItem() instanceof Kategori k ? k.id() : 0;
            List<Kategori> daftar = dao.kategori();
            kategori.removeAllItems();
            kategori.addItem(SEMUA);
            for (Kategori k : daftar) {
                kategori.addItem(k);
            }
            for (int i = 0; i < kategori.getItemCount(); i++) {
                if (kategori.getItemAt(i).id() == dipilih) {
                    kategori.setSelectedIndex(i);
                    break;
                }
            }
        } finally {
            memuatKategori = false;
        }
    }

    private void muat() {
        try {
            Kategori k = (Kategori) kategori.getSelectedItem();
            int kategoriId = k == null ? 0 : k.id();
            String kunci = KUNCI_URUTAN[Math.max(0, urutan.getSelectedIndex())];
            data = dao.katalog(cari.getText(), kategoriId, kunci);
            model.setData(data);
            jumlah.setText(data.size() + " menu ditemukan");
            if (data.isEmpty()) {
                kosongkanDetail();
            } else {
                tabel.setRowSelectionInterval(0, 0);
                tabel.scrollRectToVisible(tabel.getCellRect(0, 0, true));
            }
        } catch (SQLException ex) {
            Ui.galat(this, "Katalog gagal dimuat:\n" + ex.getMessage());
        }
    }

    private Menu terpilih() {
        int baris = tabel.getSelectedRow();
        if (baris < 0) {
            return null;
        }
        int modelBaris = tabel.convertRowIndexToModel(baris);
        return modelBaris >= 0 && modelBaris < model.getRowCount() ? model.get(modelBaris) : null;
    }

    private void tampilkanDetail() {
        Menu m = terpilih();
        if (m == null) {
            kosongkanDetail();
            return;
        }
        namaMenu.setText(m.nama());
        namaUmkm.setText(m.umkmNama() + " \u00b7 " + m.kategoriNama());
        harga.setText(Rupiah.format(m.harga()));
        stok.setText("Stok tersedia: " + m.stok() + " porsi");
        deskripsi.setText(m.deskripsi() == null || m.deskripsi().isBlank()
                ? "Belum ada deskripsi untuk menu ini." : m.deskripsi());
        foto.setPath(m.foto());
        jumlahPesan.setModel(new javax.swing.SpinnerNumberModel(
                Math.min(1, m.stok()), 1, Math.max(1, m.stok()), 1));
        tambah.setEnabled(m.bisaDipesan());
        tambah.setText(m.bisaDipesan() ? "Tambah ke Keranjang" : "Stok Habis");
    }

    private void kosongkanDetail() {
        namaMenu.setText("Pilih salah satu menu");
        namaUmkm.setText(" ");
        harga.setText(" ");
        stok.setText(" ");
        deskripsi.setText("");
        foto.setPath(null);
        tambah.setEnabled(false);
        tambah.setText("Tambah ke Keranjang");
    }

    private void tambahKeKeranjang() {
        Menu m = terpilih();
        if (m == null) {
            Ui.info(this, "Pilih menu terlebih dahulu.");
            return;
        }
        if (Session.cart().umkmId() != null && Session.cart().umkmId() != m.umkmId()) {
            if (!Ui.konfirmasi(this, "Keranjang berisi menu dari "
                    + Session.cart().umkmNama() + ".\nKosongkan keranjang dan tambahkan "
                    + m.nama() + "?")) {
                return;
            }
            Session.cart().bersihkan();
        }
        try {
            int qty = (int) jumlahPesan.getValue();
            Session.cart().tambah(m, qty);
            frame.perbaruiKeranjang();
            umpanBalik.setText(qty + " \u00d7 " + m.nama() + " ditambahkan \u2713");
            Timer timer = new Timer(2600, e -> umpanBalik.setText(" "));
            timer.setRepeats(false);
            timer.start();
        } catch (RuntimeException ex) {
            Ui.galat(this, ex.getMessage());
        }
    }
}
