package com.pesanyuk.ui;

import com.pesanyuk.dao.PenggunaDao;
import com.pesanyuk.dao.UmkmDao;
import com.pesanyuk.model.Pengguna;
import com.pesanyuk.model.Umkm;
import com.pesanyuk.util.PasswordUtil;
import com.pesanyuk.util.Session;
import com.pesanyuk.util.Validator;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.sql.SQLException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Jendela masuk dan pendaftaran akun. */
@Component
public final class LoginFrame extends JFrame {

    private final PenggunaDao penggunaDao;
    private final UmkmDao umkmDao;

    private final CardLayout kartuForm = new CardLayout();
    private final JPanel areaForm = new JPanel(kartuForm);
    private JScrollPane gulir;

    private final Ui.Btn tabMasuk = new Ui.Btn("Masuk", "soft");
    private final Ui.Btn tabDaftar = new Ui.Btn("Daftar", "ghost");

    private final JTextField emailMasuk = Ui.tinggi(Ui.input(), 36);
    private final JPasswordField sandiMasuk = Ui.tinggi(Ui.sandi(), 36);
    private final JLabel pesanMasuk = pesan();

    private final JTextField namaDaftar = Ui.tinggi(Ui.input(), 36);
    private final JTextField emailDaftar = Ui.tinggi(Ui.input(), 36);
    private final JPasswordField sandiDaftar = Ui.tinggi(Ui.sandi(), 36);
    private final JPasswordField ulangSandi = Ui.tinggi(Ui.sandi(), 36);
    private final JComboBox<String> peranDaftar =
            Ui.pilihan(new String[]{"Pelanggan", "Penjual"});
    private final JTextField namaUmkm = Ui.tinggi(Ui.input(), 36);
    private final JTextField alamatUmkm = Ui.tinggi(Ui.input(), 36);
    private final JTextField teleponUmkm = Ui.tinggi(Ui.input(), 36);
    private final JPanel panelUmkm = new JPanel();
    private final JLabel pesanDaftar = pesan();

    private final Ui.Btn tombolMasuk = new Ui.Btn("Masuk ke akun", "primary");
    private final Ui.Btn tombolDaftar = new Ui.Btn("Buat akun", "primary");

    public LoginFrame() {
        this(new PenggunaDao(), new UmkmDao());
    }

    @Autowired
    public LoginFrame(PenggunaDao penggunaDao, UmkmDao umkmDao) {
        this.penggunaDao = penggunaDao;
        this.umkmDao = umkmDao;
        setTitle("PesanYuk - Masuk");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Ui.pasangIkon(this);
        Dimension layar = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setMinimumSize(new Dimension(Math.min(980, layar.width - 40),
                Math.min(680, layar.height - 100)));
        setSize(new Dimension(Math.min(1080, layar.width - 60),
                Math.min(740, layar.height - 100)));
        setLocationRelativeTo(null);

        JPanel akar = new JPanel(new BorderLayout());
        akar.setBackground(Ui.BG);
        akar.setBorder(new EmptyBorder(28, 28, 28, 28));

        JPanel kartu = Ui.kartu(new BorderLayout());
        kartu.setBorder(new EmptyBorder(0, 0, 0, 0));
        kartu.add(new PanelMerek(), BorderLayout.WEST);
        kartu.add(buatPanelForm(), BorderLayout.CENTER);

        akar.add(kartu, BorderLayout.CENTER);
        setContentPane(akar);

        tabMasuk.addActionListener(e -> tampilkanForm("masuk"));
        tabDaftar.addActionListener(e -> tampilkanForm("daftar"));
        tombolMasuk.addActionListener(e -> masuk());
        tombolDaftar.addActionListener(e -> daftar());
        emailMasuk.addActionListener(e -> masuk());
        sandiMasuk.addActionListener(e -> masuk());

        ButtonGroupBantu.pilih(tabMasuk, tabDaftar);
        tampilkanForm("masuk");
    }

    private JPanel buatPanelForm() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(Ui.CARD);
        panel.setBorder(new EmptyBorder(34, 40, 28, 40));

        JPanel kepala = new JPanel();
        kepala.setOpaque(false);
        kepala.setLayout(new BoxLayout(kepala, BoxLayout.Y_AXIS));
        JLabel petit = Ui.label("PESANYUK DESKTOP", Ui.SMALL_BOLD, Ui.PRIMARY);
        petit.setAlignmentX(LEFT_ALIGNMENT);
        kepala.add(petit);
        kepala.add(Box.createVerticalStrut(12));

        JPanel tabs = new JPanel(new GridLayout(1, 2, 8, 0));
        tabs.setOpaque(false);
        tabMasuk.setPreferredSize(new Dimension(0, 40));
        tabDaftar.setPreferredSize(new Dimension(0, 40));
        tabs.add(tabMasuk);
        tabs.add(tabDaftar);
        tabs.setAlignmentX(LEFT_ALIGNMENT);
        kepala.add(tabs);

        areaForm.setOpaque(false);
        areaForm.add(buatFormMasuk(), "masuk");
        areaForm.add(buatFormDaftar(), "daftar");

        JPanel isi = new JPanel(new BorderLayout());
        isi.setOpaque(false);
        isi.add(areaForm, BorderLayout.NORTH);
        isi.add(Box.createVerticalGlue(), BorderLayout.CENTER);

        gulir = new JScrollPane(isi);
        gulir.setBorder(null);
        gulir.setOpaque(false);
        gulir.getViewport().setOpaque(false);
        gulir.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        gulir.getVerticalScrollBar().setUnitIncrement(16);
        gulir.setBorder(new EmptyBorder(0, 0, 0, 0));

        panel.add(kepala, BorderLayout.NORTH);
        panel.add(gulir, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buatFormMasuk() {
        JPanel f = form();
        tambah(f, Ui.judul("Selamat datang kembali"));
        tambah(f, Ui.subjudul("Masuk untuk memesan menu favorit dari UMKM."));
        f.add(Box.createVerticalStrut(12));

        tambah(f, Ui.field("Email", emailMasuk));
        tambah(f, Ui.field("Kata sandi", sandiMasuk));
        tambah(f, pesanMasuk);
        tambah(f, tombolMasuk);

        f.add(Box.createVerticalStrut(6));
        tambah(f, kartuInfoDemo());
        return f;
    }

    private JPanel buatFormDaftar() {
        JPanel f = form();
        tambah(f, Ui.judul("Buat akun baru"));
        tambah(f, Ui.subjudul("Daftar sebagai pelanggan atau penjual UMKM."));
        f.add(Box.createVerticalStrut(10));

        tambah(f, Ui.field("Nama lengkap", namaDaftar));
        tambah(f, Ui.field("Email", emailDaftar));

        JPanel sandi = new JPanel(new GridLayout(1, 2, 10, 0));
        sandi.setOpaque(false);
        sandi.add(Ui.field("Kata sandi", sandiDaftar));
        sandi.add(Ui.field("Ulangi kata sandi", ulangSandi));
        tambah(f, sandi);

        JPanel peran = new JPanel(new BorderLayout(0, 5));
        peran.setOpaque(false);
        peran.add(Ui.label("Daftar sebagai", Ui.SMALL_BOLD, Ui.MUTED), BorderLayout.NORTH);
        peran.add(peranDaftar, BorderLayout.CENTER);
        tambah(f, peran);

        panelUmkm.setOpaque(false);
        panelUmkm.setLayout(new BoxLayout(panelUmkm, BoxLayout.Y_AXIS));
        JPanel judulUmkm = new JPanel(new BorderLayout());
        judulUmkm.setOpaque(false);
        judulUmkm.add(Ui.label("Data UMKM", Ui.SMALL_BOLD, Ui.PRIMARY), BorderLayout.WEST);
        judulUmkm.setAlignmentX(LEFT_ALIGNMENT);
        panelUmkm.add(judulUmkm);
        panelUmkm.add(Box.createVerticalStrut(8));
        panelUmkm.add(fieldPenuh(Ui.field("Nama UMKM", namaUmkm)));
        panelUmkm.add(Box.createVerticalStrut(9));
        panelUmkm.add(fieldPenuh(Ui.field("Alamat", alamatUmkm)));
        panelUmkm.add(Box.createVerticalStrut(9));
        panelUmkm.add(fieldPenuh(Ui.field("Nomor telepon", teleponUmkm)));
        tambah(f, panelUmkm);
        panelUmkm.setVisible(false);

        JLabel catatan = Ui.muted("Akun penjual perlu diverifikasi admin sebelum bisa berjualan.");
        tambah(f, catatan);
        tambah(f, pesanDaftar);
        tambah(f, tombolDaftar);

        peranDaftar.addActionListener(e -> {
            boolean penjual = peranDaftar.getSelectedIndex() == 1;
            panelUmkm.setVisible(penjual);
            panelUmkm.revalidate();
            areaForm.revalidate();
        });
        return f;
    }

    private JPanel kartuInfoDemo() {
        JPanel p = new JPanel();
        p.setOpaque(true);
        p.setBackground(new Color(0xF9FAFB));
        p.setBorder(new javax.swing.border.CompoundBorder(
                new LineBorder(Ui.BORDER, 1, true), new EmptyBorder(10, 12, 10, 12)));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel judul = Ui.label("Akun demo", Ui.SMALL_BOLD, Ui.MUTED);
        judul.setAlignmentX(LEFT_ALIGNMENT);
        p.add(judul);
        p.add(Box.createVerticalStrut(4));
        String[][] akun = {
            {"Admin", "admin@pesanyuk.id / admin123"},
            {"Penjual", "warung@pesanyuk.id / penjual123"},
            {"Pelanggan", "pelanggan@pesanyuk.id / pelanggan123"}
        };
        for (String[] a : akun) {
            JLabel baris = Ui.label(a[0] + ": " + a[1], Ui.SMALL, Ui.MUTED);
            baris.setAlignmentX(LEFT_ALIGNMENT);
            p.add(baris);
        }
        return p;
    }

    private JPanel form() {
        JPanel f = new JPanel();
        f.setOpaque(false);
        f.setLayout(new BoxLayout(f, BoxLayout.Y_AXIS));
        f.setBorder(new EmptyBorder(18, 0, 0, 0));
        return f;
    }

    private JPanel fieldPenuh(JPanel field) {
        field.setAlignmentX(LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                field.getPreferredSize().height));
        return field;
    }

    private void tambah(JPanel form, javax.swing.JComponent komponen) {
        komponen.setAlignmentX(LEFT_ALIGNMENT);
        Dimension pref = komponen.getPreferredSize();
        if (komponen instanceof Ui.Btn) {
            komponen.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            komponen.setPreferredSize(new Dimension(pref.width, 42));
            form.add(Box.createVerticalStrut(4));
        } else {
            komponen.setMaximumSize(new Dimension(Integer.MAX_VALUE, pref.height));
        }
        form.add(komponen);
        form.add(Box.createVerticalStrut(10));
    }

    private void tampilkanForm(String nama) {
        kartuForm.show(areaForm, nama);
        boolean masuk = "masuk".equals(nama);
        tabMasuk.setJenis(masuk ? "soft" : "ghost");
        tabDaftar.setJenis(masuk ? "ghost" : "soft");
        tabMasuk.setForeground(masuk ? Ui.PRIMARY : Ui.MUTED);
        tabDaftar.setForeground(masuk ? Ui.MUTED : Ui.PRIMARY);
        getRootPane().setDefaultButton(masuk ? tombolMasuk : tombolDaftar);
        if (gulir != null) {
            gulir.getVerticalScrollBar().setValue(0);
        }
        if (masuk) {
            emailMasuk.requestFocusInWindow();
        } else {
            namaDaftar.requestFocusInWindow();
        }
    }

    private void masuk() {
        pesanMasuk.setText(" ");
        String email = emailMasuk.getText().trim();
        String sandi = new String(sandiMasuk.getPassword());
        try {
            Validator.email(email);
            if (sandi.isEmpty()) {
                throw new IllegalArgumentException("Kata sandi wajib diisi.");
            }
            Pengguna pengguna = penggunaDao.login(email, sandi);
            if (pengguna == null) {
                pesanMasuk.setText("Email atau kata sandi salah.");
                return;
            }
            if (!pengguna.aktif()) {
                pesanMasuk.setText("Akun ini dinonaktifkan. Hubungi admin.");
                return;
            }
            Umkm umkm = null;
            if (pengguna.isPenjual()) {
                umkm = umkmDao.findByPengguna(pengguna.id());
                if (umkm == null) {
                    pesanMasuk.setText("Data UMKM untuk akun ini tidak ditemukan.");
                    return;
                }
                if (!umkm.terverifikasi()) {
                    pesanMasuk.setText("UMKM \"" + umkm.namaUmkm() + "\" "
                            + ("PENDING".equals(umkm.status())
                                    ? "masih menunggu verifikasi admin."
                                    : "sedang ditangguhkan admin."));
                    return;
                }
            }
            Session.mulai(pengguna, umkm);
            MainFrame utama = new MainFrame();
            utama.setVisible(true);
            dispose();
        } catch (IllegalArgumentException ex) {
            pesanMasuk.setText(ex.getMessage());
        } catch (SQLException ex) {
            Ui.galat(this, "Gagal mengakses basis data:\n" + ex.getMessage());
        } catch (RuntimeException ex) {
            Ui.galat(this, "Gagal membuka aplikasi:\n" + ex.getMessage()
                    + "\n\nCoba jalankan Clean and Build pada proyek, lalu Run kembali.");
        }
    }

    private void daftar() {
        pesanDaftar.setText(" ");
        String nama = namaDaftar.getText().trim();
        String email = emailDaftar.getText().trim();
        String sandi = new String(sandiDaftar.getPassword());
        String ulangi = new String(ulangSandi.getPassword());
        boolean penjual = peranDaftar.getSelectedIndex() == 1;
        try {
            Validator.wajib(nama, "Nama");
            Validator.email(email);
            Validator.kataSandi(sandi);
            if (!sandi.equals(ulangi)) {
                throw new IllegalArgumentException("Kata sandi dan ulangannya tidak sama.");
            }
            if (penjual) {
                Validator.wajib(namaUmkm.getText(), "Nama UMKM");
                Validator.wajib(alamatUmkm.getText(), "Alamat UMKM");
            }
            if (penggunaDao.emailDipakai(email)) {
                throw new IllegalArgumentException("Email sudah terdaftar. Gunakan email lain.");
            }

            tombolDaftar.setEnabled(false);
            int id = penggunaDao.daftar(nama, email, PasswordUtil.hash(sandi),
                    penjual ? "PENJUAL" : "PELANGGAN",
                    namaUmkm.getText().trim(), alamatUmkm.getText().trim(),
                    teleponUmkm.getText().trim());
            tombolDaftar.setEnabled(true);

            if (penjual) {
                Ui.info(this, "Pendaftaran berhasil!\n\nAkun penjual dan UMKM \""
                        + namaUmkm.getText().trim()
                        + "\" menunggu verifikasi admin sebelum dapat digunakan.");
                bersihkanDaftar();
                tampilkanForm("masuk");
                return;
            }

            Pengguna pengguna = penggunaDao.findById(id);
            Session.mulai(pengguna, null);
            MainFrame utama = new MainFrame();
            utama.setVisible(true);
            dispose();
        } catch (IllegalArgumentException ex) {
            pesanDaftar.setText(ex.getMessage());
        } catch (SQLException ex) {
            tombolDaftar.setEnabled(true);
            Ui.galat(this, "Pendaftaran gagal:\n" + ex.getMessage());
        }
    }

    private void bersihkanDaftar() {
        namaDaftar.setText("");
        emailDaftar.setText("");
        sandiDaftar.setText("");
        ulangSandi.setText("");
        namaUmkm.setText("");
        alamatUmkm.setText("");
        teleponUmkm.setText("");
        peranDaftar.setSelectedIndex(0);
        panelUmkm.setVisible(false);
        pesanDaftar.setText(" ");
    }

    private JLabel pesan() {
        JLabel l = Ui.label(" ", Ui.SMALL, Ui.DANGER);
        l.setPreferredSize(new Dimension(10, 18));
        return l;
    }

    /** Panel kiri dengan gradasi dan ringkasan keunggulan aplikasi. */
    private static final class PanelMerek extends JPanel {

        PanelMerek() {
            setOpaque(false);
            setPreferredSize(new Dimension(400, 0));
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new EmptyBorder(44, 42, 38, 36));

            JPanel logo = new JPanel();
            logo.setOpaque(false);
            logo.setLayout(new BoxLayout(logo, BoxLayout.X_AXIS));
            JLabel ikon = new JLabel(Ui.avatar("P", 46, Ui.PRIMARY));
            JLabel nama = new JLabel("PesanYuk");
            nama.setFont(new Font("Segoe UI", Font.BOLD, 27));
            nama.setForeground(Color.WHITE);
            logo.add(ikon);
            logo.add(Box.createHorizontalStrut(12));
            logo.add(nama);
            logo.setAlignmentX(LEFT_ALIGNMENT);
            add(logo);
            add(Box.createVerticalGlue());

            JLabel semboyan = new JLabel("<html>Makanan UMKM,<br>lebih dekat<br>ke kamu.</html>");
            semboyan.setFont(new Font("Segoe UI", Font.BOLD, 30));
            semboyan.setForeground(Color.WHITE);
            semboyan.setAlignmentX(LEFT_ALIGNMENT);
            add(semboyan);
            add(Box.createVerticalStrut(16));

            JLabel penjelasan = new JLabel("<html>Pesan makanan dan minuman favorit "
                    + "dari UMKM sekitar. Bayar daring, pantau pesanan, dan nikmati "
                    + "tanpa antre.</html>");
            penjelasan.setFont(Ui.SMALL);
            penjelasan.setForeground(new Color(0xD5D9E2));
            penjelasan.setAlignmentX(LEFT_ALIGNMENT);
            add(penjelasan);
            add(Box.createVerticalStrut(28));

            for (String teks : new String[]{"Katalog menu dengan pencarian",
                "Pesanan dan status real-time", "Pembayaran daring yang aman",
                "Laporan penjualan lengkap"}) {
                JLabel baris = new JLabel("\u2022  " + teks);
                baris.setFont(Ui.BASE);
                baris.setForeground(new Color(0xEDEFF4));
                baris.setAlignmentX(LEFT_ALIGNMENT);
                add(baris);
                add(Box.createVerticalStrut(9));
            }
            add(Box.createVerticalGlue());

            JLabel kaki = new JLabel("<html>Laboratorium Informatika<br>"
                    + "Universitas Gunadarma</html>");
            kaki.setFont(Ui.SMALL);
            kaki.setForeground(new Color(0x9AA2B1));
            kaki.setAlignmentX(LEFT_ALIGNMENT);
            add(kaki);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, new Color(0x232A38),
                    getWidth(), getHeight(), new Color(0x12161F)));
            g2.fillRoundRect(0, 0, getWidth() + 30, getHeight(), 22, 22);
            g2.setColor(new Color(255, 255, 255, 16));
            g2.fillOval(-100, getHeight() - 260, 360, 360);
            g2.setColor(new Color(232, 93, 42, 55));
            g2.fillOval(getWidth() - 150, -90, 280, 280);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Membantu menjaga tampilan tab tetap konsisten. */
    private static final class ButtonGroupBantu {

        static void pilih(Ui.Btn aktif, Ui.Btn... lain) {
            // Hanya penanda visual; aksi penggantian ditangani tampilkanForm.
        }
    }
}
