package com.pesanyuk.ui.dialog;

import com.pesanyuk.dao.PembayaranDao;
import com.pesanyuk.model.Pesanan;
import com.pesanyuk.ui.Ui;
import com.pesanyuk.util.Labels;
import com.pesanyuk.util.Rupiah;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.geom.Path2D;
import java.sql.SQLException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/** Dialog simulasi payment gateway (Midtrans) untuk satu pesanan. */
public final class PaymentDialog extends JDialog {

    private final Pesanan pesanan;
    private boolean dibayar;
    private final CardLayout kartu = new CardLayout();
    private final JPanel isi = new JPanel(kartu);
    private final ButtonGroup grup = new ButtonGroup();
    private final String[] kodeMetode = {"QRIS", "TRANSFER_BANK", "E_WALLET"};

    public PaymentDialog(Window induk, Pesanan pesanan) {
        super(induk, "Pembayaran Pesanan", ModalityType.APPLICATION_MODAL);
        this.pesanan = pesanan;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel akar = new JPanel(new BorderLayout());
        akar.setBackground(Ui.BG);
        akar.setBorder(new EmptyBorder(16, 16, 16, 16));
        isi.setOpaque(false);
        isi.add(buatForm(), "form");
        isi.add(buatProses(), "proses");
        isi.add(buatSukses(), "sukses");
        akar.add(isi, BorderLayout.CENTER);
        setContentPane(akar);

        kartu.show(isi, "form");
        setSize(500, 660);
        setLocationRelativeTo(induk);
    }

    public boolean isDibayar() {
        return dibayar;
    }

    private JPanel buatForm() {
        JPanel kartuPembayaran = Ui.kartu(new BorderLayout(0, 0));
        kartuPembayaran.setBorder(new EmptyBorder(24, 24, 22, 24));

        JPanel atas = new JPanel();
        atas.setOpaque(false);
        atas.setLayout(new BoxLayout(atas, BoxLayout.Y_AXIS));
        JLabel label = Ui.label("PEMBAYARAN PESANAN", Ui.SMALL_BOLD, Ui.MUTED);
        label.setAlignmentX(LEFT_ALIGNMENT);
        JLabel kode = Ui.label(pesanan.kode(), Ui.H2, Ui.TEXT);
        kode.setAlignmentX(LEFT_ALIGNMENT);
        JLabel jumlah = Ui.label(Rupiah.format(pesanan.subtotal()), new java.awt.Font(
                "Segoe UI", java.awt.Font.BOLD, 30), Ui.PRIMARY);
        jumlah.setAlignmentX(LEFT_ALIGNMENT);
        JLabel info = Ui.label(Labels.jenis(pesanan.jenis()) + " \u00b7 "
                + pesanan.umkmNama(), Ui.SMALL, Ui.MUTED);
        info.setAlignmentX(LEFT_ALIGNMENT);
        atas.add(label);
        atas.add(Box.createVerticalStrut(4));
        atas.add(kode);
        atas.add(Box.createVerticalStrut(14));
        atas.add(jumlah);
        atas.add(Box.createVerticalStrut(2));
        atas.add(info);
        atas.add(Box.createVerticalStrut(18));

        JLabel pilih = Ui.label("Pilih metode pembayaran", Ui.SMALL_BOLD, Ui.MUTED);
        pilih.setAlignmentX(LEFT_ALIGNMENT);
        atas.add(pilih);
        atas.add(Box.createVerticalStrut(8));

        String[][] opsi = {
            {"QRIS", "Pindai kode QR dengan aplikasi bank atau e-wallet."},
            {"Transfer Bank", "Virtual account BCA, Mandiri, BNI (simulasi)."},
            {"E-Wallet", "GoPay, OVO, DANA, atau ShopeePay (simulasi)."}
        };
        for (int i = 0; i < opsi.length; i++) {
            atas.add(kotakMetode(opsi[i][0], opsi[i][1], i == 0));
            atas.add(Box.createVerticalStrut(8));
        }

        JLabel catatan = Ui.muted("<html>Ini adalah simulasi payment gateway. Tidak ada "
                + "transaksi uang sungguhan yang terjadi.</html>");
        catatan.setAlignmentX(LEFT_ALIGNMENT);
        atas.add(Box.createVerticalStrut(4));
        atas.add(catatan);

        Ui.Btn bayar = new Ui.Btn("Bayar Sekarang", "primary");
        bayar.setPreferredSize(new Dimension(0, 44));
        bayar.addActionListener(e -> prosesBayar());
        Ui.Btn nanti = new Ui.Btn("Nanti Saja", "ghost");
        nanti.setPreferredSize(new Dimension(0, 44));
        nanti.addActionListener(e -> dispose());

        JPanel tombol = new JPanel(new GridLayout(1, 2, 10, 0));
        tombol.setOpaque(false);
        tombol.add(nanti);
        tombol.add(bayar);

        kartuPembayaran.add(atas, BorderLayout.CENTER);
        kartuPembayaran.add(tombol, BorderLayout.SOUTH);
        return kartuPembayaran;
    }

    private JPanel kotakMetode(String nama, String keterangan, boolean terpilih) {
        JPanel kotak = new JPanel(new BorderLayout(10, 0));
        kotak.setOpaque(true);
        kotak.setBackground(terpilih ? Ui.PRIMARY_SOFT : Color.WHITE);
        kotak.setBorder(new javax.swing.border.CompoundBorder(
                new LineBorder(terpilih ? Ui.PRIMARY : Ui.BORDER, 1, true),
                new EmptyBorder(10, 12, 10, 12)));

        JRadioButton radio = Ui.radio("");
        radio.setSelected(terpilih);
        grup.add(radio);
        int indeks = grup.getButtonCount() - 1;
        radio.addActionListener(e -> pilihMetode(indeks));

        JPanel teks = new JPanel();
        teks.setOpaque(false);
        teks.setLayout(new BoxLayout(teks, BoxLayout.Y_AXIS));
        JLabel judul = Ui.tebal(nama);
        JLabel sub = Ui.muted(keterangan);
        teks.add(judul);
        teks.add(sub);
        kotak.add(radio, BorderLayout.WEST);
        kotak.add(teks, BorderLayout.CENTER);
        return kotak;
    }

    private void pilihMetode(int indeks) {
        JPanel kartuPembayaran = (JPanel) isi.getComponent(0);
        JPanel atas = (JPanel) kartuPembayaran.getComponent(0);
        int nomor = 0;
        for (java.awt.Component c : atas.getComponents()) {
            if (c instanceof JPanel panel && panel.getComponentCount() == 2
                    && panel.getComponent(0) instanceof JRadioButton) {
                boolean aktif = nomor == indeks;
                panel.setBackground(aktif ? Ui.PRIMARY_SOFT : Color.WHITE);
                panel.setBorder(new javax.swing.border.CompoundBorder(
                        new LineBorder(aktif ? Ui.PRIMARY : Ui.BORDER, 1, true),
                        new EmptyBorder(10, 12, 10, 12)));
                nomor++;
            }
        }
    }

    private String metodeTerpilih() {
        java.util.Enumeration<javax.swing.AbstractButton> semua = grup.getElements();
        int i = 0;
        while (semua.hasMoreElements()) {
            if (semua.nextElement().isSelected()) {
                return kodeMetode[Math.min(i, kodeMetode.length - 1)];
            }
            i++;
        }
        return "QRIS";
    }

    private void prosesBayar() {
        kartu.show(isi, "proses");
        Timer timer = new Timer(1100, e -> {
            try {
                new PembayaranDao().bayar(pesanan.id(), metodeTerpilih());
                dibayar = true;
                JLabel detail = (JLabel) ((JPanel) isi.getComponent(2)).getClientProperty("detail");
                if (detail != null) {
                    detail.setText("<html>Pesanan " + pesanan.kode()
                            + " sudah dibayar melalui " + Labels.metode(metodeTerpilih())
                            + ". Penjual akan segera memproses pesanan Anda.</html>");
                }
                kartu.show(isi, "sukses");
            } catch (SQLException ex) {
                kartu.show(isi, "form");
                Ui.galat(this, "Pembayaran gagal:\n" + ex.getMessage());
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    private JPanel buatProses() {
        JPanel panel = Ui.kartu(new BorderLayout());
        JPanel tengah = new JPanel();
        tengah.setOpaque(false);
        tengah.setLayout(new BoxLayout(tengah, BoxLayout.Y_AXIS));
        JLabel judul = Ui.label("Memproses pembayaran...", Ui.H2, Ui.TEXT);
        judul.setAlignmentX(CENTER_ALIGNMENT);
        JLabel sub = Ui.muted("Menghubungkan ke payment gateway (simulasi)");
        sub.setAlignmentX(CENTER_ALIGNMENT);
        tengah.add(Box.createVerticalGlue());
        tengah.add(judul);
        tengah.add(Box.createVerticalStrut(8));
        tengah.add(sub);
        tengah.add(Box.createVerticalGlue());
        panel.add(tengah, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buatSukses() {
        JPanel panel = Ui.kartu(new BorderLayout());
        JPanel tengah = new JPanel();
        tengah.setOpaque(false);
        tengah.setLayout(new BoxLayout(tengah, BoxLayout.Y_AXIS));
        IkonCentang ikon = new IkonCentang();
        ikon.setAlignmentX(CENTER_ALIGNMENT);
        JLabel judul = Ui.label("Pembayaran berhasil", Ui.H2, Ui.SUCCESS);
        judul.setAlignmentX(CENTER_ALIGNMENT);
        JLabel detail = Ui.muted("");
        detail.setAlignmentX(CENTER_ALIGNMENT);
        JLabel referensi = Ui.label("Status pembayaran: " + Labels.bayar("LUNAS"),
                Ui.SMALL_BOLD, Ui.MUTED);
        referensi.setAlignmentX(CENTER_ALIGNMENT);
        tengah.add(Box.createVerticalGlue());
        tengah.add(ikon);
        tengah.add(Box.createVerticalStrut(16));
        tengah.add(judul);
        tengah.add(Box.createVerticalStrut(8));
        tengah.add(detail);
        tengah.add(Box.createVerticalStrut(10));
        tengah.add(referensi);
        tengah.add(Box.createVerticalGlue());

        Ui.Btn selesai = new Ui.Btn("Selesai", "primary");
        selesai.setPreferredSize(new Dimension(0, 42));
        selesai.addActionListener(e -> dispose());

        panel.add(tengah, BorderLayout.CENTER);
        panel.add(selesai, BorderLayout.SOUTH);
        panel.putClientProperty("detail", detail);
        return panel;
    }

    private static final class IkonCentang extends JComponent {
        IkonCentang() {
            setPreferredSize(new Dimension(84, 84));
            setMaximumSize(new Dimension(84, 84));
            setAlignmentX(CENTER_ALIGNMENT);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0xDCFCE7));
            g2.fillOval(0, 0, 84, 84);
            g2.setColor(new Color(0x15803D));
            g2.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Path2D centang = new Path2D.Double();
            centang.moveTo(24, 44);
            centang.lineTo(37, 57);
            centang.lineTo(61, 29);
            g2.draw(centang);
            g2.dispose();
        }
    }
}
