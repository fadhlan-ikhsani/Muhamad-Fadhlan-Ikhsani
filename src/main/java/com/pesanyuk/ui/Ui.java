package com.pesanyuk.ui;

import com.pesanyuk.util.Labels;
import com.pesanyuk.util.PhotoStore;
import com.pesanyuk.util.Rupiah;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.text.JTextComponent;

/** Kumpulan warna, font, komponen, dan dialog yang dipakai seluruh layar. */
public final class Ui {

    public static final Color PRIMARY = new Color(0xE85D2A);
    public static final Color PRIMARY_HOVER = new Color(0xCF4C1D);
    public static final Color PRIMARY_SOFT = new Color(0xFDEDE6);
    public static final Color SIDEBAR = new Color(0x1E2430);
    public static final Color SIDEBAR_HOVER = new Color(0x2B323F);
    public static final Color BG = new Color(0xF5F6FA);
    public static final Color CARD = Color.WHITE;
    public static final Color TEXT = new Color(0x1F2937);
    public static final Color MUTED = new Color(0x6B7280);
    public static final Color BORDER = new Color(0xE5E7EB);
    public static final Color SUCCESS = new Color(0x15803D);
    public static final Color WARNING = new Color(0xB45309);
    public static final Color DANGER = new Color(0xB91C1C);
    public static final Color BLUE = new Color(0x0369A1);

    public static final Font BASE = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font SMALL_BOLD = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font H1 = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font H2 = new Font("Segoe UI", Font.BOLD, 17);
    public static final Font H3 = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font ANGKA = new Font("Segoe UI", Font.BOLD, 22);

    private static Image ikon;

    private Ui() {
    }

    /** Menyeragamkan font dan warna komponen bawaan Swing. */
    public static void install() {
        String[] kunciFont = {"Label.font", "Button.font", "CheckBox.font", "RadioButton.font",
            "ComboBox.font", "TextField.font", "PasswordField.font", "TextArea.font",
            "Table.font", "TableHeader.font", "Spinner.font", "OptionPane.messageFont",
            "OptionPane.buttonFont", "FileChooser.font", "TitledBorder.font", "ToolTip.font"};
        for (String kunci : kunciFont) {
            UIManager.put(kunci, BASE);
        }
        UIManager.put("Table.selectionBackground", PRIMARY_SOFT);
        UIManager.put("Table.selectionForeground", TEXT);
        UIManager.put("OptionPane.background", CARD);
        UIManager.put("Panel.background", CARD);
        UIManager.put("TextField.caretForeground", TEXT);
        UIManager.put("ComboBox.selectionBackground", PRIMARY_SOFT);
        UIManager.put("ComboBox.selectionForeground", TEXT);
    }

    public static Image ikon() {
        if (ikon == null) {
            BufferedImage gambar = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = gambar.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setPaint(new GradientPaint(0, 0, PRIMARY, 64, 64, new Color(0xB4340F)));
            g.fillRoundRect(2, 2, 60, 60, 18, 18);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Segoe UI", Font.BOLD, 38));
            FontMetrics fm = g.getFontMetrics();
            g.drawString("P", (64 - fm.stringWidth("P")) / 2, 44);
            g.dispose();
            ikon = gambar;
        }
        return ikon;
    }

    public static void pasangIkon(JFrame frame) {
        frame.setIconImage(ikon());
    }

    public static JLabel judul(String teks) {
        JLabel l = new JLabel(teks);
        l.setFont(H2);
        l.setForeground(TEXT);
        return l;
    }

    public static JLabel subjudul(String teks) {
        JLabel l = new JLabel(teks);
        l.setFont(SMALL);
        l.setForeground(MUTED);
        return l;
    }

    public static JLabel label(String teks, Font font, Color warna) {
        JLabel l = new JLabel(teks);
        l.setFont(font);
        l.setForeground(warna);
        return l;
    }

    public static JLabel muted(String teks) {
        return label(teks, SMALL, MUTED);
    }

    public static JLabel tebal(String teks) {
        return label(teks, BOLD, TEXT);
    }

    public static JPanel baris(int jarak, Component... komponen) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, jarak, 0));
        p.setOpaque(false);
        for (Component c : komponen) {
            p.add(c);
        }
        return p;
    }

    public static JPanel barisKanan(int jarak, Component... komponen) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, jarak, 0));
        p.setOpaque(false);
        for (Component c : komponen) {
            p.add(c);
        }
        return p;
    }

    public static JPanel kolom(Component... komponen) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        for (Component c : komponen) {
            if (c instanceof JComponent jc) {
                jc.setAlignmentX(Component.LEFT_ALIGNMENT);
            }
            p.add(c);
        }
        return p;
    }

    public static JPanel field(String label, JComponent input) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setOpaque(false);
        p.add(label(label, SMALL_BOLD, MUTED), BorderLayout.NORTH);
        p.add(input, BorderLayout.CENTER);
        return p;
    }

    public static JPanel kartu(LayoutManager layout) {
        Card card = new Card();
        card.setLayout(layout);
        return card;
    }

    public static JPanel kartu() {
        return kartu(new BorderLayout(0, 14));
    }

    public static JTextField input() {
        JTextField f = new JTextField();
        gayaInput(f);
        return f;
    }

    public static JPasswordField sandi() {
        JPasswordField f = new JPasswordField();
        gayaInput(f);
        return f;
    }

    private static void gayaInput(JTextComponent f) {
        f.setFont(BASE);
        f.setForeground(TEXT);
        f.setBackground(Color.WHITE);
        f.setCaretColor(TEXT);
        f.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true),
                new EmptyBorder(7, 10, 7, 10)));
    }

    public static JTextArea area() {
        JTextArea a = new JTextArea(3, 20);
        a.setFont(BASE);
        a.setForeground(TEXT);
        a.setBackground(Color.WHITE);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true),
                new EmptyBorder(7, 10, 7, 10)));
        return a;
    }

    /** Combo status pesanan dengan label bahasa Indonesia. */
    public static JComboBox<String> comboStatus() {
        JComboBox<String> combo = pilihan(new String[]{"SEMUA", "MENUNGGU", "DIPROSES",
            "SIAP", "SELESAI", "DIBATALKAN"});
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(javax.swing.JList<?> list,
                    Object value, int index, boolean dipilih, boolean fokus) {
                super.getListCellRendererComponent(list, value, index, dipilih, fokus);
                String nilai = String.valueOf(value);
                setText("SEMUA".equals(nilai) ? "Semua status" : Labels.pesanan(nilai));
                setFont(BASE);
                return this;
            }
        });
        return combo;
    }

    /** Ikon avatar bulat berisi inisial nama. */
    public static ImageIcon avatar(String inisial, int ukuran, Color warna) {
        BufferedImage gambar = new BufferedImage(ukuran, ukuran, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = gambar.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(warna);
        g.fillOval(0, 0, ukuran, ukuran);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Segoe UI", Font.BOLD, Math.max(10, ukuran / 3)));
        FontMetrics fm = g.getFontMetrics();
        String teks = inisial == null || inisial.isBlank() ? "?" : inisial;
        g.drawString(teks, (ukuran - fm.stringWidth(teks)) / 2,
                (ukuran - fm.getHeight()) / 2 + fm.getAscent());
        g.dispose();
        return new ImageIcon(gambar);
    }

    public static <T> JComboBox<T> pilihan(T[] item) {
        JComboBox<T> c = new JComboBox<>(item);
        c.setFont(BASE);
        c.setBackground(Color.WHITE);
        c.setForeground(TEXT);
        c.setBorder(new LineBorder(BORDER, 1, true));
        c.setPreferredSize(new Dimension(c.getPreferredSize().width, 34));
        return c;
    }

    public static JSpinner spinner(int min, int maks, int nilai) {
        JSpinner s = new JSpinner(new SpinnerNumberModel(nilai, min, maks, 1));
        s.setFont(BASE);
        s.setBorder(new LineBorder(BORDER, 1, true));
        if (s.getEditor() instanceof JSpinner.DefaultEditor editor) {
            editor.getTextField().setFont(BASE);
            editor.getTextField().setBorder(new EmptyBorder(4, 6, 4, 6));
        }
        s.setPreferredSize(new Dimension(90, 34));
        return s;
    }

    public static JCheckBox centang(String teks) {
        JCheckBox c = new JCheckBox(teks);
        c.setOpaque(false);
        c.setFont(BASE);
        c.setForeground(TEXT);
        return c;
    }

    public static JRadioButton radio(String teks) {
        JRadioButton r = new JRadioButton(teks);
        r.setOpaque(false);
        r.setFont(BASE);
        r.setForeground(TEXT);
        r.setFocusPainted(false);
        return r;
    }

    public static JScrollPane scroll(Component isi) {
        JScrollPane sp = new JScrollPane(isi);
        sp.setBorder(new LineBorder(BORDER, 1, true));
        sp.getViewport().setBackground(CARD);
        sp.setBackground(CARD);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    public static JTable tabel(TableModel model) {
        JTable t = new JTable(model) {
            @Override
            public boolean isCellEditable(int baris, int kolom) {
                return false;
            }
        };
        t.setFont(BASE);
        t.setForeground(TEXT);
        t.setBackground(CARD);
        t.setRowHeight(36);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setSelectionBackground(PRIMARY_SOFT);
        t.setSelectionForeground(TEXT);
        t.setFillsViewportHeight(true);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        t.setDefaultRenderer(Object.class, new IsiTabel());
        t.getTableHeader().setReorderingAllowed(false);
        t.getTableHeader().setDefaultRenderer(new KepalaTabel());
        t.getTableHeader().setPreferredSize(new Dimension(0, 38));
        return t;
    }

    public static void badge(JTable tabel, int kolom) {
        tabel.getColumnModel().getColumn(kolom).setCellRenderer(new Badge());
    }

    public static void lebar(JTable tabel, int... lebar) {
        for (int i = 0; i < lebar.length && i < tabel.getColumnCount(); i++) {
            tabel.getColumnModel().getColumn(i).setPreferredWidth(lebar[i]);
            if (lebar[i] > 0) {
                tabel.getColumnModel().getColumn(i).setMinWidth(Math.min(lebar[i], 70));
            }
        }
    }

    public static void placeholder(JTextField field, String petunjuk) {
        field.putClientProperty("hint", petunjuk);
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                field.repaint();
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                field.repaint();
            }
        });
    }

    public static void info(Component induk, String pesan) {
        JOptionPane.showMessageDialog(induk, pesan, "PesanYuk",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public static void galat(Component induk, String pesan) {
        JOptionPane.showMessageDialog(induk, pesan, "Terjadi Kesalahan",
                JOptionPane.ERROR_MESSAGE);
    }

    public static boolean konfirmasi(Component induk, String pesan) {
        return JOptionPane.showConfirmDialog(induk, pesan, "Konfirmasi",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public static String formatRupiah(double nilai) {
        return Rupiah.format(nilai);
    }

    public static void aturUkuran(Component c, int lebar, int tinggi) {
        Dimension d = new Dimension(lebar, tinggi);
        c.setPreferredSize(d);
        c.setMinimumSize(d);
    }

    /** Mengatur tinggi komponen tanpa mengubah lebar yang diinginkan. */
    public static <T extends JComponent> T tinggi(T komponen, int tinggi) {
        Dimension p = komponen.getPreferredSize();
        komponen.setPreferredSize(new Dimension(p.width, tinggi));
        return komponen;
    }

    /** Panel putih dengan sudut membulat dan garis tepi halus. */
    public static class Card extends JPanel {
        private final int radius;

        public Card() {
            this(16);
        }

        public Card(int radius) {
            this.radius = radius;
            setOpaque(false);
            setBorder(new EmptyBorder(18, 20, 18, 20));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0x0F172A10, true));
            g2.fillRoundRect(1, 2, getWidth() - 2, getHeight() - 2, radius, radius);
            g2.setColor(CARD);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 2, radius, radius);
            g2.setColor(BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 2, radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Tombol dengan sudut membulat, varian warna, dan efek hover. */
    public static class Btn extends JButton {
        private String jenis;
        private boolean aktif;

        public Btn(String teks, String jenis) {
            super(teks);
            this.jenis = jenis;
            setFont(BOLD);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setRolloverEnabled(true);
            if ("nav".equals(jenis)) {
                setHorizontalAlignment(SwingConstants.LEFT);
                setBorder(new EmptyBorder(11, 16, 11, 16));
                setForeground(new Color(0xC9CED8));
            } else {
                setBorder(new EmptyBorder(9, 18, 9, 18));
                setForeground("ghost".equals(jenis) || "soft".equals(jenis) ? TEXT : Color.WHITE);
            }
        }

        public void setJenis(String jenis) {
            this.jenis = jenis;
            if ("nav".equals(jenis)) {
                setForeground(aktif ? Color.WHITE : new Color(0xC9CED8));
            } else {
                setForeground("ghost".equals(jenis) || "soft".equals(jenis)
                        ? TEXT : Color.WHITE);
            }
            repaint();
        }

        public void setAktif(boolean aktif) {
            this.aktif = aktif;
            if ("nav".equals(jenis)) {
                setForeground(aktif ? Color.WHITE : new Color(0xC9CED8));
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            boolean hover = getModel().isRollover() && isEnabled();
            Color isi = switch (jenis) {
                case "primary" -> hover ? PRIMARY_HOVER : PRIMARY;
                case "danger" -> hover ? new Color(0x991B1B) : new Color(0xDC2626);
                case "ghost" -> hover ? new Color(0xF3F4F6) : new Color(0xF9FAFB);
                case "soft" -> hover ? new Color(0xFBD9CB) : PRIMARY_SOFT;
                case "dark" -> hover ? new Color(0x111827) : new Color(0x1F2937);
                case "nav" -> aktif ? PRIMARY : (hover ? SIDEBAR_HOVER : new Color(0, 0, 0, 0));
                default -> new Color(0, 0, 0, 0);
            };
            if (isi.getAlpha() > 0) {
                g2.setColor(isi);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            }
            if (!isEnabled()) {
                g2.setColor(new Color(255, 255, 255, 150));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            }
            g2.dispose();
            if (!isEnabled()) {
                setForeground(new Color(0xA0A5AE));
            }
            super.paintComponent(g);
        }
    }

    /** Input teks yang menampilkan petunjuk saat kosong. */
    public static class InputPetunjuk extends JTextField {
        private final String petunjuk;

        public InputPetunjuk(String petunjuk) {
            this.petunjuk = petunjuk;
            gayaInput(this);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty() && !isFocusOwner()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0x9CA3AF));
                g2.setFont(getFont());
                Insets in = getInsets();
                g2.drawString(petunjuk, in.left, getHeight() / 2 + getFont().getSize() / 2 - 2);
                g2.dispose();
            }
        }
    }

    /** Sel tabel biasa dengan garis pemisah tipis. */
    public static class IsiTabel extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tabel, Object nilai,
                boolean dipilih, boolean fokus, int baris, int kolom) {
            super.getTableCellRendererComponent(tabel, nilai, dipilih, fokus, baris, kolom);
            setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, BORDER),
                    new EmptyBorder(0, 8, 0, 8)));
            setBackground(dipilih ? PRIMARY_SOFT : CARD);
            setForeground(TEXT);
            setFont(BASE);
            return this;
        }
    }

    /** Header tabel abu-abu dengan teks kecil tebal. */
    public static class KepalaTabel extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tabel, Object nilai,
                boolean dipilih, boolean fokus, int baris, int kolom) {
            super.getTableCellRendererComponent(tabel, nilai, dipilih, fokus, baris, kolom);
            setBackground(new Color(0xF9FAFB));
            setForeground(MUTED);
            setFont(SMALL_BOLD);
            setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, BORDER),
                    new EmptyBorder(0, 8, 0, 8)));
            setHorizontalAlignment(SwingConstants.LEFT);
            return this;
        }
    }

    /** Kolom status dengan pil warna. */
    public static class Badge extends DefaultTableCellRenderer {
        private Color latar = PRIMARY_SOFT;
        private Color teks = PRIMARY;

        @Override
        public Component getTableCellRendererComponent(JTable tabel, Object nilai,
                boolean dipilih, boolean fokus, int baris, int kolom) {
            super.getTableCellRendererComponent(tabel, nilai, dipilih, fokus, baris, kolom);
            String isi = nilai == null ? "" : Labels.statusUmkm(Labels.pesanan(nilai.toString()));
            if ("Menunggu Verifikasi".equals(isi)) {
                isi = "Menunggu";
            }
            setText(isi);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder());
            setFont(SMALL_BOLD);
            setHorizontalAlignment(SwingConstants.CENTER);
            switch (getText()) {
                case "LUNAS", "Lunas", "SELESAI", "Selesai", "TERVERIFIKASI",
                     "Terverifikasi", "Aktif", "AKTIF", "Tersedia" -> {
                    latar = new Color(0xDCFCE7);
                    teks = new Color(0x15803D);
                }
                case "DIPROSES", "Diproses", "SIAP", "Siap" -> {
                    latar = new Color(0xE0F2FE);
                    teks = new Color(0x0369A1);
                }
                case "MENUNGGU", "Menunggu", "Belum Dibayar", "PENDING",
                     "Menunggu Verifikasi" -> {
                    latar = new Color(0xFFF4E5);
                    teks = new Color(0xB45309);
                }
                case "DIBATALKAN", "Dibatalkan", "GAGAL", "Gagal", "DITANGGUHKAN",
                     "Ditangguhkan", "Stok Habis", "Nonaktif" -> {
                    latar = new Color(0xFEE2E2);
                    teks = new Color(0xB91C1C);
                }
                default -> {
                    latar = new Color(0xF3F4F6);
                    teks = MUTED;
                }
            }
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            String isi = getText() == null ? "" : getText();
            FontMetrics fm = g2.getFontMetrics(getFont());
            int lebar = Math.min(getWidth() - 10, fm.stringWidth(isi) + 22);
            int x = (getWidth() - lebar) / 2;
            int y = (getHeight() - 22) / 2;
            g2.setColor(latar);
            g2.fillRoundRect(x, y, lebar, 22, 11, 11);
            g2.setColor(teks);
            g2.setFont(getFont());
            g2.drawString(isi, x + (lebar - fm.stringWidth(isi)) / 2,
                    y + 15);
            g2.dispose();
        }
    }

    /** Pratinjau foto menu dengan sudut membulat. */
    public static class Foto extends JComponent {
        private BufferedImage gambar;
        private String kosong = "Belum ada foto";

        public Foto(String kosong) {
            this.kosong = kosong;
            setPreferredSize(new Dimension(280, 170));
        }

        public void setPath(String path) {
            gambar = PhotoStore.muat(path);
            repaint();
        }

        public void setKosong(String teks) {
            this.kosong = teks;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            Shape klip = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14);
            g2.setClip(klip);
            g2.setColor(new Color(0xF3F4F6));
            g2.fillRect(0, 0, getWidth(), getHeight());
            if (gambar != null) {
                double skala = Math.max(getWidth() / (double) gambar.getWidth(),
                        getHeight() / (double) gambar.getHeight());
                int w = (int) Math.ceil(gambar.getWidth() * skala);
                int h = (int) Math.ceil(gambar.getHeight() * skala);
                g2.drawImage(gambar, (getWidth() - w) / 2, (getHeight() - h) / 2, w, h, null);
            } else {
                g2.setColor(new Color(0x9CA3AF));
                g2.setFont(SMALL);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(kosong, (getWidth() - fm.stringWidth(kosong)) / 2,
                        getHeight() / 2 + 4);
            }
            g2.dispose();
        }
    }

    /** Grafik batang sederhana untuk omzet harian. */
    public static class Grafik extends JComponent {
        private List<String> label = List.of();
        private List<Double> nilai = List.of();

        public Grafik() {
            setPreferredSize(new Dimension(420, 260));
        }

        public void setData(List<String> label, List<Double> nilai) {
            this.label = label == null ? List.of() : label;
            this.nilai = nilai == null ? List.of() : nilai;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setFont(SMALL);
            if (nilai.isEmpty()) {
                g2.setColor(MUTED);
                String teks = "Belum ada pesanan selesai pada periode ini";
                g2.drawString(teks, (getWidth() - g2.getFontMetrics().stringWidth(teks)) / 2,
                        getHeight() / 2);
                g2.dispose();
                return;
            }
            int kiri = 62;
            int kanan = 14;
            int atas = 24;
            int bawah = 34;
            int lebarPlot = Math.max(20, getWidth() - kiri - kanan);
            int tinggiPlot = Math.max(20, getHeight() - atas - bawah);
            double maks = nilai.stream().mapToDouble(Double::doubleValue).max().orElse(1);
            if (maks <= 0) {
                maks = 1;
            }

            for (int i = 0; i <= 4; i++) {
                int y = atas + tinggiPlot * i / 4;
                g2.setColor(BORDER);
                g2.drawLine(kiri, y, kiri + lebarPlot, y);
                g2.setColor(MUTED);
                String teks = Rupiah.ringkas(maks * (4 - i) / 4);
                int w = g2.getFontMetrics().stringWidth(teks);
                g2.drawString(teks, kiri - w - 8, y + 4);
            }

            int n = nilai.size();
            double lebarSlot = lebarPlot / (double) n;
            int lebarBar = (int) Math.max(6, Math.min(38, lebarSlot * 0.55));
            for (int i = 0; i < n; i++) {
                double v = nilai.get(i);
                int tinggiBar = (int) Math.round(v / maks * tinggiPlot);
                int x = (int) (kiri + lebarSlot * i + (lebarSlot - lebarBar) / 2);
                int y = atas + tinggiPlot - tinggiBar;
                if (tinggiBar > 0) {
                    g2.setPaint(new GradientPaint(x, y, PRIMARY,
                            x, atas + tinggiPlot, new Color(0xF9A27E)));
                    g2.fillRoundRect(x, y, lebarBar, tinggiBar, 7, 7);
                }
                if (n <= 10 && v > 0) {
                    g2.setColor(MUTED);
                    String nominal = Rupiah.ringkas(v);
                    int w = g2.getFontMetrics().stringWidth(nominal);
                    g2.drawString(nominal, x + (lebarBar - w) / 2, y - 6);
                }
                if (n <= 14 || i % Math.max(1, n / 10) == 0) {
                    g2.setColor(MUTED);
                    String tgl = label.get(i);
                    int w = g2.getFontMetrics().stringWidth(tgl);
                    g2.drawString(tgl, x + (lebarBar - w) / 2, atas + tinggiPlot + 18);
                }
            }
            g2.setColor(BORDER);
            g2.setStroke(new BasicStroke(1f));
            g2.drawLine(kiri, atas + tinggiPlot, kiri + lebarPlot, atas + tinggiPlot);
            g2.dispose();
        }
    }

    /** Kartu statistik ringkas untuk laporan/beranda. */
    public static JPanel statistik(String judul, String nilai, Color aksen) {
        JPanel p = kartu(new BorderLayout(0, 6));
        p.setBorder(new EmptyBorder(16, 18, 16, 18));
        JLabel j = new JLabel(judul);
        j.setFont(SMALL_BOLD);
        j.setForeground(MUTED);
        JLabel n = new JLabel(nilai);
        n.setFont(ANGKA);
        n.setForeground(aksen);
        JPanel garis = new JPanel();
        garis.setBackground(aksen);
        garis.setPreferredSize(new Dimension(36, 3));
        JPanel atas = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        atas.setOpaque(false);
        atas.add(garis);
        p.add(j, BorderLayout.NORTH);
        p.add(n, BorderLayout.CENTER);
        p.add(atas, BorderLayout.SOUTH);
        return p;
    }

    public static void pasangGayaTombolKecil(JButton tombol) {
        tombol.setFont(SMALL_BOLD);
        tombol.setForeground(PRIMARY);
        tombol.setBackground(CARD);
        tombol.setBorder(new EmptyBorder(4, 6, 4, 6));
        tombol.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public static void aturLayar(Component komponen) {
        Toolkit.getDefaultToolkit().setDynamicLayout(true);
        if (komponen instanceof JFrame frame) {
            frame.setLocationRelativeTo(null);
        }
    }
}
