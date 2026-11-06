package com.pesanyuk.ui.panel;

import com.pesanyuk.dao.MenuDao;
import com.pesanyuk.model.Kategori;
import com.pesanyuk.model.Menu;
import com.pesanyuk.ui.MainFrame;
import com.pesanyuk.ui.Refreshable;
import com.pesanyuk.ui.Ui;
import com.pesanyuk.ui.model.MenuTableModel;
import com.pesanyuk.util.PhotoStore;
import com.pesanyuk.util.Rupiah;
import com.pesanyuk.util.Session;
import com.pesanyuk.util.Validator;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.File;
import java.sql.SQLException;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

/** Kelola menu milik UMKM penjual: tambah, ubah, atur stok, dan ketersediaan. */
public final class PenjualMenuPanel extends JPanel implements Refreshable {

    private final MenuDao dao = new MenuDao();
    private final MenuTableModel model = new MenuTableModel(false);
    private final JTable tabel = Ui.tabel(model);

    private final Ui.InputPetunjuk cari = new Ui.InputPetunjuk("Cari nama menu...");
    private final JLabel info = Ui.muted("0 menu");

    private final Ui.Foto foto = new Ui.Foto("Belum ada foto");
    private final JTextField nama = Ui.tinggi(Ui.input(), 36);
    private final JComboBox<Kategori> kategori = Ui.pilihan(new Kategori[]{});
    private final JTextField harga = Ui.tinggi(Ui.input(), 36);
    private final JSpinner stok = Ui.spinner(0, 100_000, 0);
    private final JTextArea deskripsi = Ui.area();
    private final JCheckBox tersedia = Ui.centang("Tersedia untuk dipesan");
    private final JLabel judulForm = Ui.tebal("Menu Baru");
    private final JLabel pesan = Ui.label(" ", Ui.SMALL, Ui.DANGER);
    private final Ui.Btn simpan = new Ui.Btn("Simpan Menu", "primary");
    private final Ui.Btn baru = new Ui.Btn("Menu Baru", "ghost");
    private final Ui.Btn hapus = new Ui.Btn("Hapus Menu", "ghost");
    private final Ui.Btn pilihFoto = new Ui.Btn("Pilih Foto", "soft");

    private Menu terpilih;
    private String fotoPath;

    public PenjualMenuPanel(MainFrame frame) {
        setOpaque(false);
        setLayout(new BorderLayout(0, 14));

        add(buatToolbar(), BorderLayout.NORTH);
        add(buatKonten(), BorderLayout.CENTER);

        cari.addActionListener(e -> muat());
        tabel.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Menu m = menuTerpilih();
                if (m != null) {
                    isiForm(m);
                }
            }
        });
        baru.addActionListener(e -> kosongkanForm());
        simpan.addActionListener(e -> simpan());
        hapus.addActionListener(e -> hapus());
        pilihFoto.addActionListener(e -> pilihFoto());
        kosongkanForm();
    }

    private JPanel buatToolbar() {
        JPanel kartu = Ui.kartu(new BorderLayout(12, 0));
        kartu.setBorder(new EmptyBorder(14, 18, 14, 18));

        cari.setPreferredSize(new Dimension(250, 36));
        Ui.Btn cariTombol = new Ui.Btn("Cari", "primary");
        Ui.Btn reset = new Ui.Btn("Reset", "ghost");
        cariTombol.addActionListener(e -> muat());
        reset.addActionListener(e -> {
            cari.setText("");
            muat();
        });

        JPanel kiri = Ui.baris(10, cari, cariTombol, reset);
        kartu.add(kiri, BorderLayout.WEST);
        kartu.add(Ui.barisKanan(0, info), BorderLayout.EAST);
        return kartu;
    }

    private JPanel buatKonten() {
        JPanel isi = new JPanel(new BorderLayout(16, 0));
        isi.setOpaque(false);
        isi.add(buatTabel(), BorderLayout.CENTER);
        isi.add(buatForm(), BorderLayout.EAST);
        return isi;
    }

    private JPanel buatTabel() {
        JPanel kartu = Ui.kartu(new BorderLayout(0, 10));
        JPanel kepala = new JPanel(new BorderLayout());
        kepala.setOpaque(false);
        kepala.add(Ui.tebal("Daftar Menu UMKM"), BorderLayout.WEST);
        kepala.add(Ui.muted("Klik baris untuk mengubah menu"), BorderLayout.EAST);
        kartu.add(kepala, BorderLayout.NORTH);

        tabel.setAutoCreateRowSorter(true);
        Ui.lebar(tabel, 240, 110, 110, 70, 120);
        Ui.badge(tabel, 4);
        kartu.add(Ui.scroll(tabel), BorderLayout.CENTER);

        JLabel catatan = Ui.muted("Menu yang pernah dipesan tidak bisa dihapus; "
                + "matikan status tersedia untuk menyembunyikannya.");
        kartu.add(catatan, BorderLayout.SOUTH);
        return kartu;
    }

    private JPanel buatForm() {
        JPanel kartu = Ui.kartu(new BorderLayout());
        kartu.setPreferredSize(new Dimension(368, 0));

        JPanel isi = new JPanel();
        isi.setOpaque(false);
        isi.setLayout(new BoxLayout(isi, BoxLayout.Y_AXIS));

        judulForm.setAlignmentX(LEFT_ALIGNMENT);
        JLabel sub = Ui.muted("Lengkapi data menu yang dijual");
        sub.setAlignmentX(LEFT_ALIGNMENT);

        JPanel barisFoto = new JPanel(new BorderLayout(10, 0));
        barisFoto.setOpaque(false);
        barisFoto.setAlignmentX(LEFT_ALIGNMENT);
        barisFoto.setPreferredSize(new Dimension(0, 84));
        barisFoto.setMaximumSize(new Dimension(Integer.MAX_VALUE, 84));
        foto.setPreferredSize(new Dimension(96, 84));
        foto.setMaximumSize(new Dimension(96, 84));
        JPanel kananFoto = new JPanel();
        kananFoto.setOpaque(false);
        kananFoto.setLayout(new BoxLayout(kananFoto, BoxLayout.Y_AXIS));
        pilihFoto.setAlignmentX(LEFT_ALIGNMENT);
        pilihFoto.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        JLabel petunjukFoto = Ui.muted("JPG/PNG, maksimal 2 MB");
        petunjukFoto.setAlignmentX(LEFT_ALIGNMENT);
        kananFoto.add(Box.createVerticalGlue());
        kananFoto.add(pilihFoto);
        kananFoto.add(Box.createVerticalStrut(4));
        kananFoto.add(petunjukFoto);
        kananFoto.add(Box.createVerticalGlue());
        barisFoto.add(foto, BorderLayout.WEST);
        barisFoto.add(kananFoto, BorderLayout.CENTER);

        JPanel barisHarga = new JPanel(new GridLayout(1, 2, 10, 0));
        barisHarga.setOpaque(false);
        barisHarga.add(Ui.field("Harga (Rp)", harga));
        barisHarga.add(Ui.field("Stok", stok));

        deskripsi.setRows(3);
        JScrollPane gulirDeskripsi = new JScrollPane(deskripsi);
        gulirDeskripsi.setBorder(new javax.swing.border.LineBorder(Ui.BORDER, 1, true));
        gulirDeskripsi.setPreferredSize(new Dimension(0, 68));

        isi.add(judulForm);
        isi.add(Box.createVerticalStrut(2));
        isi.add(sub);
        isi.add(Box.createVerticalStrut(10));
        isi.add(barisFoto);
        isi.add(Box.createVerticalStrut(10));
        tambah(isi, Ui.field("Nama menu", nama), 7);
        tambah(isi, Ui.field("Kategori", kategori), 7);
        tambah(isi, barisHarga, 7);
        tambah(isi, Ui.field("Deskripsi", gulirDeskripsi), 7);
        tersedia.setAlignmentX(LEFT_ALIGNMENT);
        isi.add(tersedia);
        isi.add(Box.createVerticalStrut(6));
        isi.add(Box.createVerticalGlue());

        JScrollPane gulir = new JScrollPane(isi);
        gulir.setBorder(null);
        gulir.setOpaque(false);
        gulir.getViewport().setOpaque(false);
        gulir.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        gulir.getVerticalScrollBar().setUnitIncrement(16);
        kartu.add(gulir, BorderLayout.CENTER);

        JPanel bawah = new JPanel();
        bawah.setOpaque(false);
        bawah.setLayout(new BoxLayout(bawah, BoxLayout.Y_AXIS));
        pesan.setAlignmentX(LEFT_ALIGNMENT);
        bawah.add(pesan);
        bawah.add(Box.createVerticalStrut(4));

        JPanel aksi = new JPanel(new GridLayout(1, 2, 8, 0));
        aksi.setOpaque(false);
        aksi.setAlignmentX(LEFT_ALIGNMENT);
        aksi.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        aksi.add(baru);
        aksi.add(simpan);
        bawah.add(aksi);
        bawah.add(Box.createVerticalStrut(8));
        hapus.setAlignmentX(LEFT_ALIGNMENT);
        hapus.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        bawah.add(hapus);
        kartu.add(bawah, BorderLayout.SOUTH);
        return kartu;
    }

    private void tambah(JPanel form, JComponent komponen, int jarak) {
        komponen.setAlignmentX(LEFT_ALIGNMENT);
        komponen.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                komponen.getPreferredSize().height));
        form.add(komponen);
        form.add(Box.createVerticalStrut(jarak));
    }

    @Override
    public void onShow() {
        try {
            muatKategori();
            muat();
        } catch (SQLException ex) {
            Ui.galat(this, "Menu gagal dimuat:\n" + ex.getMessage());
        }
    }

    private void muatKategori() throws SQLException {
        int dipilih = kategori.getSelectedItem() instanceof Kategori k ? k.id() : 0;
        List<Kategori> daftar = dao.kategori();
        kategori.removeAllItems();
        for (Kategori k : daftar) {
            kategori.addItem(k);
        }
        for (int i = 0; i < kategori.getItemCount(); i++) {
            if (kategori.getItemAt(i).id() == dipilih) {
                kategori.setSelectedIndex(i);
                break;
            }
        }
    }

    private void muat() {
        try {
            List<Menu> semua = dao.byUmkm(Session.umkm().id());
            String kunci = cari.getText().trim().toLowerCase();
            List<Menu> tampil = kunci.isEmpty() ? semua : semua.stream()
                    .filter(m -> m.nama().toLowerCase().contains(kunci)
                    || (m.deskripsi() != null && m.deskripsi().toLowerCase().contains(kunci)))
                    .toList();
            int idTerpilih = terpilih == null ? 0 : terpilih.id();
            model.setData(tampil);
            info.setText(tampil.size() + " menu");

            if (idTerpilih > 0) {
                for (int i = 0; i < tampil.size(); i++) {
                    if (tampil.get(i).id() == idTerpilih) {
                        int baris = tabel.convertRowIndexToView(i);
                        tabel.setRowSelectionInterval(baris, baris);
                        tabel.scrollRectToVisible(tabel.getCellRect(baris, 0, true));
                        break;
                    }
                }
            }
        } catch (SQLException ex) {
            Ui.galat(this, "Menu gagal dimuat:\n" + ex.getMessage());
        }
    }

    private Menu menuTerpilih() {
        int baris = tabel.getSelectedRow();
        if (baris < 0) {
            return null;
        }
        int modelBaris = tabel.convertRowIndexToModel(baris);
        return modelBaris >= 0 && modelBaris < model.getRowCount() ? model.get(modelBaris) : null;
    }

    private void isiForm(Menu m) {
        terpilih = m;
        judulForm.setText("Ubah Menu");
        nama.setText(m.nama());
        pilihKategori(m.kategoriId());
        harga.setText(Rupiah.angka(m.harga()));
        stok.setValue(m.stok());
        deskripsi.setText(m.deskripsi() == null ? "" : m.deskripsi());
        tersedia.setSelected(m.tersedia());
        fotoPath = m.foto();
        foto.setPath(m.foto());
        simpan.setText("Simpan Perubahan");
        hapus.setEnabled(true);
        pesan.setText(" ");
    }

    private void pilihKategori(int kategoriId) {
        for (int i = 0; i < kategori.getItemCount(); i++) {
            if (kategori.getItemAt(i).id() == kategoriId) {
                kategori.setSelectedIndex(i);
                return;
            }
        }
        if (kategori.getItemCount() > 0) {
            kategori.setSelectedIndex(0);
        }
    }

    private void kosongkanForm() {
        terpilih = null;
        judulForm.setText("Menu Baru");
        nama.setText("");
        if (kategori.getItemCount() > 0) {
            kategori.setSelectedIndex(0);
        }
        harga.setText("");
        stok.setValue(0);
        deskripsi.setText("");
        tersedia.setSelected(true);
        fotoPath = null;
        foto.setPath(null);
        simpan.setText("Simpan Menu");
        hapus.setEnabled(false);
        pesan.setText(" ");
        tabel.clearSelection();
    }

    private void simpan() {
        pesan.setText(" ");
        try {
            Validator.wajib(nama.getText(), "Nama menu");
            Validator.panjangMaks(nama.getText().trim(), 120, "Nama menu");
            Validator.panjangMaks(deskripsi.getText().trim(), 1000, "Deskripsi");
            double nilaiHarga = Rupiah.parse(harga.getText());
            Validator.positif(nilaiHarga, "Harga");
            if (!(kategori.getSelectedItem() instanceof Kategori k)) {
                throw new IllegalArgumentException("Pilih kategori menu terlebih dahulu.");
            }
            int nilaiStok = (int) stok.getValue();
            String namaMenu = nama.getText().trim();
            String teksDeskripsi = deskripsi.getText().trim();

            if (terpilih == null) {
                dao.insert(Session.umkm().id(), k.id(), namaMenu, teksDeskripsi,
                        nilaiHarga, nilaiStok, fotoPath, tersedia.isSelected());
                Ui.info(this, "Menu \"" + namaMenu + "\" berhasil ditambahkan.");
            } else {
                dao.update(terpilih.id(), k.id(), namaMenu, teksDeskripsi,
                        nilaiHarga, nilaiStok, fotoPath, tersedia.isSelected());
                Ui.info(this, "Menu \"" + namaMenu + "\" berhasil diperbarui.");
            }
            kosongkanForm();
            muat();
        } catch (IllegalArgumentException ex) {
            pesan.setText(ex.getMessage());
        } catch (SQLException ex) {
            Ui.galat(this, "Menu gagal disimpan:\n" + ex.getMessage());
        }
    }

    private void hapus() {
        if (terpilih == null) {
            Ui.info(this, "Pilih menu yang akan dihapus.");
            return;
        }
        if (!Ui.konfirmasi(this, "Hapus menu \"" + terpilih.nama() + "\"?\n"
                + "Tindakan ini tidak dapat dibatalkan.")) {
            return;
        }
        try {
            dao.delete(terpilih.id());
            kosongkanForm();
            muat();
            Ui.info(this, "Menu berhasil dihapus.");
        } catch (SQLException ex) {
            Ui.galat(this, "Menu tidak dapat dihapus karena sudah pernah dipesan.\n"
                    + "Ubah statusnya menjadi tidak tersedia saja.\n\n"
                    + "Detail: " + ex.getMessage());
        }
    }

    private void pilihFoto() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Pilih Foto Menu");
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Gambar (JPG, PNG, GIF)", "jpg", "jpeg", "png", "gif"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File sumber = chooser.getSelectedFile();
        try {
            fotoPath = PhotoStore.simpan(sumber, "menu");
            foto.setPath(fotoPath);
        } catch (IllegalStateException ex) {
            Ui.galat(this, ex.getMessage());
        }
    }
}
