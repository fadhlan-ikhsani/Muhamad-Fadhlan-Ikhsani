package com.pesanyuk.ui;

import com.pesanyuk.model.Pengguna;
import com.pesanyuk.util.Labels;
import com.pesanyuk.util.Session;
import com.pesanyuk.ui.panel.AdminPenggunaPanel;
import com.pesanyuk.ui.panel.AdminTransaksiPanel;
import com.pesanyuk.ui.panel.AdminUmkmPanel;
import com.pesanyuk.ui.panel.KatalogPanel;
import com.pesanyuk.ui.panel.KeranjangPanel;
import com.pesanyuk.ui.panel.LaporanPanel;
import com.pesanyuk.ui.panel.PenjualMenuPanel;
import com.pesanyuk.ui.panel.PenjualPesananPanel;
import com.pesanyuk.ui.panel.PesananSayaPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

/** Jendela utama setelah pengguna masuk; navigasi dan tata letak aplikasi. */
public final class MainFrame extends JFrame {

    public static final String KATALOG = "katalog";
    public static final String KERANJANG = "keranjang";
    public static final String PESANAN = "pesanan";
    public static final String MENU = "menu";
    public static final String PESANAN_MASUK = "pesanan-masuk";
    public static final String LAPORAN = "laporan";
    public static final String PENGGUNA = "pengguna";
    public static final String UMKM = "umkm";
    public static final String TRANSAKSI = "transaksi";

    private record Nav(String label, String judul, String sub) {
    }

    private final CardLayout kartu = new CardLayout();
    private final JPanel isi = new JPanel(kartu);
    private final JPanel daftarNav = new JPanel();
    private final Map<String, Ui.Btn> tombolNav = new LinkedHashMap<>();
    private final Map<String, Nav> infoNav = new LinkedHashMap<>();
    private final Map<String, JComponent> halaman = new LinkedHashMap<>();

    private JLabel judulHalaman = new JLabel();
    private JLabel subjudulHalaman = new JLabel();
    private Ui.Btn badgeKeranjang;
    private String halamanAwal = KATALOG;

    public MainFrame() {
        Pengguna pengguna = Session.pengguna();
        if (pengguna == null) {
            throw new IllegalStateException("Belum ada pengguna yang masuk.");
        }
        setTitle("PesanYuk - " + Labels.role(pengguna.role()));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Ui.pasangIkon(this);
        Dimension layar = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setMinimumSize(new Dimension(Math.min(1120, layar.width - 40),
                Math.min(700, layar.height - 100)));
        setSize(new Dimension(Math.min(1320, layar.width - 60),
                Math.min(820, layar.height - 100)));
        setLocationRelativeTo(null);

        JPanel akar = new JPanel(new BorderLayout());
        akar.setBackground(Ui.BG);
        akar.add(buatSidebar(pengguna), BorderLayout.WEST);
        akar.add(buatIsiUtama(pengguna), BorderLayout.CENTER);
        setContentPane(akar);

        daftarHalaman(pengguna);
        buka(halamanAwal);
        perbaruiKeranjang();
    }

    private JPanel buatSidebar(Pengguna pengguna) {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(Ui.SIDEBAR);
        sidebar.setPreferredSize(new Dimension(248, 0));
        sidebar.setBorder(new EmptyBorder(24, 16, 20, 16));

        JPanel atas = new JPanel();
        atas.setOpaque(false);
        atas.setLayout(new BoxLayout(atas, BoxLayout.Y_AXIS));

        JPanel merek = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        merek.setOpaque(false);
        merek.setAlignmentX(LEFT_ALIGNMENT);
        JLabel logo = new JLabel(Ui.avatar("P", 38, Ui.PRIMARY));
        JLabel nama = new JLabel("PesanYuk");
        nama.setFont(new Font("Segoe UI", Font.BOLD, 21));
        nama.setForeground(Color.WHITE);
        merek.add(logo);
        merek.add(nama);
        atas.add(merek);
        atas.add(Box.createVerticalStrut(24));

        JPanel kartuPengguna = new JPanel(new BorderLayout(10, 0));
        kartuPengguna.setOpaque(true);
        kartuPengguna.setBackground(Ui.SIDEBAR_HOVER);
        kartuPengguna.setBorder(new EmptyBorder(12, 12, 12, 12));
        kartuPengguna.setMaximumSize(new Dimension(Integer.MAX_VALUE, 66));
        kartuPengguna.setAlignmentX(LEFT_ALIGNMENT);
        JLabel avatar = new JLabel(Ui.avatar(pengguna.inisial(), 40, Ui.PRIMARY));
        JPanel identitas = new JPanel();
        identitas.setOpaque(false);
        identitas.setLayout(new BoxLayout(identitas, BoxLayout.Y_AXIS));
        JLabel namaPengguna = new JLabel(pengguna.nama());
        namaPengguna.setFont(Ui.SMALL_BOLD);
        namaPengguna.setForeground(Color.WHITE);
        JLabel peran = new JLabel(Labels.role(pengguna.role())
                + (Session.umkm() == null ? "" : " \u00b7 " + Session.umkm().namaUmkm()));
        peran.setFont(Ui.SMALL);
        peran.setForeground(new Color(0xAAB1BE));
        identitas.add(namaPengguna);
        identitas.add(peran);
        kartuPengguna.add(avatar, BorderLayout.WEST);
        kartuPengguna.add(identitas, BorderLayout.CENTER);
        atas.add(kartuPengguna);
        atas.add(Box.createVerticalStrut(26));

        JLabel labelMenu = new JLabel("MENU UTAMA");
        labelMenu.setFont(new Font("Segoe UI", Font.BOLD, 10));
        labelMenu.setForeground(new Color(0x7C8596));
        labelMenu.setAlignmentX(LEFT_ALIGNMENT);
        atas.add(labelMenu);
        atas.add(Box.createVerticalStrut(8));

        daftarNav.setOpaque(false);
        daftarNav.setLayout(new BoxLayout(daftarNav, BoxLayout.Y_AXIS));
        daftarNav.setAlignmentX(LEFT_ALIGNMENT);
        atas.add(daftarNav);

        sidebar.add(atas, BorderLayout.NORTH);

        JPanel bawah = new JPanel();
        bawah.setOpaque(false);
        bawah.setLayout(new BoxLayout(bawah, BoxLayout.Y_AXIS));
        JLabel versi = new JLabel("PesanYuk Desktop v1.0");
        versi.setFont(Ui.SMALL);
        versi.setForeground(new Color(0x7C8596));
        versi.setAlignmentX(LEFT_ALIGNMENT);
        bawah.add(versi);
        bawah.add(Box.createVerticalStrut(10));
        Ui.Btn keluar = new Ui.Btn("Keluar dari akun", "nav");
        keluar.setAlignmentX(LEFT_ALIGNMENT);
        keluar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        keluar.addActionListener(e -> keluar());
        bawah.add(keluar);
        sidebar.add(bawah, BorderLayout.SOUTH);
        return sidebar;
    }

    private JPanel buatIsiUtama(Pengguna pengguna) {
        JPanel utama = new JPanel(new BorderLayout());
        utama.setBackground(Ui.BG);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Ui.CARD);
        header.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, Ui.BORDER),
                new EmptyBorder(15, 26, 15, 26)));

        JPanel teks = new JPanel();
        teks.setOpaque(false);
        teks.setLayout(new BoxLayout(teks, BoxLayout.Y_AXIS));
        judulHalaman.setFont(new Font("Segoe UI", Font.BOLD, 20));
        judulHalaman.setForeground(Ui.TEXT);
        subjudulHalaman.setFont(Ui.SMALL);
        subjudulHalaman.setForeground(Ui.MUTED);
        teks.add(judulHalaman);
        teks.add(Box.createVerticalStrut(3));
        teks.add(subjudulHalaman);
        header.add(teks, BorderLayout.WEST);

        JPanel kanan = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        kanan.setOpaque(false);
        if (pengguna.isPelanggan()) {
            badgeKeranjang = new Ui.Btn("Keranjang \u00b7 0", "soft");
            badgeKeranjang.addActionListener(e -> buka(KERANJANG));
            kanan.add(badgeKeranjang);
            kanan.add(Box.createHorizontalStrut(4));
        }
        JLabel avatar = new JLabel(Ui.avatar(pengguna.inisial(), 38,
                pengguna.isAdmin() ? new Color(0x0F766E)
                        : pengguna.isPenjual() ? new Color(0x7C3AED) : Ui.PRIMARY));
        JPanel identitas = new JPanel();
        identitas.setOpaque(false);
        identitas.setLayout(new BoxLayout(identitas, BoxLayout.Y_AXIS));
        JLabel nama = new JLabel(pengguna.nama());
        nama.setFont(Ui.SMALL_BOLD);
        nama.setForeground(Ui.TEXT);
        JLabel peran = new JLabel(Labels.role(pengguna.role()));
        peran.setFont(Ui.SMALL);
        peran.setForeground(Ui.MUTED);
        identitas.add(nama);
        identitas.add(peran);
        JPanel profil = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        profil.setOpaque(false);
        profil.add(avatar);
        profil.add(identitas);
        kanan.add(profil);
        header.add(kanan, BorderLayout.EAST);

        isi.setBackground(Ui.BG);
        isi.setBorder(new EmptyBorder(20, 24, 24, 24));

        utama.add(header, BorderLayout.NORTH);
        utama.add(isi, BorderLayout.CENTER);
        return utama;
    }

    private void daftarHalaman(Pengguna pengguna) {
        if (pengguna.isPelanggan()) {
            halaman(KATALOG, "Katalog Menu", "Katalog Menu",
                    "Temukan makanan dan minuman dari UMKM terverifikasi.",
                    new KatalogPanel(this));
            halaman(KERANJANG, "Keranjang", "Keranjang Belanja",
                    "Periksa item pesanan dan lanjutkan ke pembayaran.",
                    new KeranjangPanel(this));
            halaman(PESANAN, "Pesanan Saya", "Pesanan Saya",
                    "Pantau status dan pembayaran seluruh pesanan Anda.",
                    new PesananSayaPanel(this));
            halamanAwal = KATALOG;
        } else if (pengguna.isPenjual()) {
            halaman(MENU, "Kelola Menu", "Kelola Menu",
                    "Tambah, ubah, dan atur ketersediaan menu UMKM Anda.",
                    new PenjualMenuPanel(this));
            halaman(PESANAN_MASUK, "Pesanan Masuk", "Pesanan Masuk",
                    "Terima dan proses pesanan pelanggan secara real-time.",
                    new PenjualPesananPanel(this));
            halaman(LAPORAN, "Laporan Penjualan", "Laporan Penjualan",
                    "Rekapitulasi omzet dan menu terjual pada periode tertentu.",
                    new LaporanPanel(this, Session.umkm().id(), Session.umkm().namaUmkm()));
            halamanAwal = MENU;
        } else {
            halaman(PENGGUNA, "Kelola Pengguna", "Kelola Pengguna",
                    "Verifikasi, aktifkan, dan atur akun pengguna aplikasi.",
                    new AdminPenggunaPanel(this));
            halaman(UMKM, "Kelola UMKM", "Kelola UMKM",
                    "Verifikasi dan pantau seluruh UMKM yang terdaftar.",
                    new AdminUmkmPanel(this));
            halaman(TRANSAKSI, "Semua Transaksi", "Semua Transaksi",
                    "Pantau seluruh transaksi pelanggan dan status pembayarannya.",
                    new AdminTransaksiPanel(this));
            halaman(LAPORAN, "Laporan Penjualan", "Laporan Penjualan",
                    "Rekapitulasi penjualan seluruh UMKM pada periode tertentu.",
                    new LaporanPanel(this, null, "Semua UMKM"));
            halamanAwal = PENGGUNA;
        }
        daftarNav.revalidate();
    }

    private void halaman(String kunci, String navLabel, String judul, String sub,
            JComponent panel) {
        isi.add(panel, kunci);
        halaman.put(kunci, panel);
        infoNav.put(kunci, new Nav(navLabel, judul, sub));

        Ui.Btn tombol = new Ui.Btn(navLabel, "nav");
        tombol.setAlignmentX(LEFT_ALIGNMENT);
        tombol.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        tombol.addActionListener(e -> buka(kunci));
        tombolNav.put(kunci, tombol);
        daftarNav.add(tombol);
        daftarNav.add(Box.createVerticalStrut(4));
    }

    public void buka(String kunci) {
        Nav nav = infoNav.get(kunci);
        if (nav == null) {
            return;
        }
        kartu.show(isi, kunci);
        judulHalaman.setText(nav.judul());
        subjudulHalaman.setText(nav.sub());
        tombolNav.forEach((k, t) -> t.setAktif(k.equals(kunci)));
        Component panel = halaman.get(kunci);
        if (panel instanceof Refreshable refresh) {
            refresh.onShow();
        }
        isi.revalidate();
        isi.repaint();
    }

    public void perbaruiKeranjang() {
        if (badgeKeranjang != null) {
            badgeKeranjang.setText("Keranjang \u00b7 " + Session.cart().jumlahItem());
        }
    }

    private void keluar() {
        if (!Ui.konfirmasi(this, "Keluar dari akun ini?")) {
            return;
        }
        dispose();
        Session.akhiri();
        LoginFrame login = new LoginFrame();
        login.setVisible(true);
    }
}
