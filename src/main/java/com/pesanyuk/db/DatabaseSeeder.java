package com.pesanyuk.db;

import com.pesanyuk.util.PasswordUtil;
import com.pesanyuk.util.PhotoStore;
import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;


public final class DatabaseSeeder {

    private record MenuSeed(int id, String nama, double harga) {
    }

    private static final Map<String, MenuSeed> MENU = new LinkedHashMap<>();

    private DatabaseSeeder() {
    }

    public static void seed() throws SQLException {
        try (Connection koneksi = Database.getConnection()) {
            pastikanKategori(koneksi);
            if (hitung(koneksi, "SELECT COUNT(*) FROM pengguna") > 0) {
                return;
            }
            koneksi.setAutoCommit(false);
            try {
                isiData(koneksi);
                koneksi.commit();
                System.out.println("Data contoh PesanYuk berhasil dibuat.");
            } catch (SQLException | RuntimeException ex) {
                koneksi.rollback();
                throw ex;
            } finally {
                koneksi.setAutoCommit(true);
            }
        }
    }

    private static void isiData(Connection c) throws SQLException {
        int admin = pengguna(c, "Admin PesanYuk", "admin@pesanyuk.id", "admin123", "ADMIN");
        int sari = pengguna(c, "Sari Wulandari", "warung@pesanyuk.id", "penjual123", "PENJUAL");
        int dimas = pengguna(c, "Dimas Pratama", "kopi@pesanyuk.id", "penjual123", "PENJUAL");
        int rina = pengguna(c, "Rina Marlina", "rina@pesanyuk.id", "penjual123", "PENJUAL");
        int fadhlan = pengguna(c, "Muhammad Fadhlan Ikhsani", "pelanggan@pesanyuk.id", "pelanggan123", "PELANGGAN");
        int nadia = pengguna(c, "Nadia Putri", "nadia@pesanyuk.id", "pelanggan123", "PELANGGAN");

        int warung = umkm(c, sari, "Warung Bu Sari",
                "Masakan rumahan khas Depok dengan sambal ijo andalan.",
                "Jl. Margonda Raya No. 12, Depok", "0812-3456-7890", "TERVERIFIKASI");
        int kopi = umkm(c, dimas, "Kedai Kopi Senja",
                "Kopi manual brew dan pastry untuk teman sore.",
                "Jl. Kemang Raya No. 8, Jakarta Selatan", "0813-2233-4455", "TERVERIFIKASI");
        umkm(c, rina, "Dapur Rina",
                "Kue basah dan camilan tradisional.",
                "Jl. Kukusan Teknik No. 3, Depok", "0857-8899-1122", "PENDING");

        int makanan = kategori(c, "Makanan");
        int minuman = kategori(c, "Minuman");
        int camilan = kategori(c, "Camilan");

        daftarMenu(c, "warung", warung, makanan, camilan, "TERVERIFIKASI");
        daftarMenu(c, "kopi", kopi, minuman, camilan, "TERVERIFIKASI");

        LocalDateTime sekarang = LocalDateTime.now().withSecond(0).withNano(0);
        pesanan(c, "PY-DEMO-001", fadhlan, warung, sekarang.minusMinutes(25),
                "MENUNGGU", "PENGANTARAN", "Jl. Kenanga No. 21, Depok",
                "Sambal dipisah ya, terima kasih.", new int[][]{{1, 2}, {4, 2}},
                "QRIS", "LUNAS", true);
        pesanan(c, "PY-DEMO-002", nadia, warung, sekarang.minusMinutes(10),
                "DIPROSES", "AMBIL_SENDIRI", null,
                "Diambil sekitar jam 12.", new int[][]{{2, 1}, {5, 1}},
                "E_WALLET", "LUNAS", true);
        pesanan(c, "PY-DEMO-003", nadia, kopi, sekarang.minusDays(1).withHour(15).withMinute(20),
                "SELESAI", "AMBIL_SENDIRI", null, null, new int[][]{{7, 2}, {9, 1}},
                "TRANSFER_BANK", "LUNAS", true);
        pesanan(c, "PY-DEMO-004", fadhlan, warung, sekarang.minusDays(2).withHour(12).withMinute(5),
                "SELESAI", "PENGANTARAN", "Jl. Kenanga No. 21, Depok", null,
                new int[][]{{1, 1}, {3, 1}, {4, 2}}, "QRIS", "LUNAS", true);
        pesanan(c, "PY-DEMO-005", fadhlan, warung, sekarang.minusDays(3).withHour(19).withMinute(40),
                "SELESAI", "AMBIL_SENDIRI", null, null, new int[][]{{2, 2}, {5, 2}},
                "E_WALLET", "LUNAS", true);
        pesanan(c, "PY-DEMO-006", nadia, warung, sekarang.minusDays(4).withHour(11).withMinute(15),
                "SELESAI", "PENGANTARAN", "Jl. Mawar No. 7, Depok", null,
                new int[][]{{1, 2}, {6, 1}}, "TRANSFER_BANK", "LUNAS", true);
        pesanan(c, "PY-DEMO-007", fadhlan, kopi, sekarang.minusDays(5).withHour(16).withMinute(30),
                "SELESAI", "AMBIL_SENDIRI", null, "Less sugar.", new int[][]{{8, 1}, {10, 1}},
                "QRIS", "LUNAS", true);
        pesanan(c, "PY-DEMO-008", nadia, warung, sekarang.minusDays(6).withHour(13).withMinute(0),
                "DIBATALKAN", "AMBIL_SENDIRI", null, null, new int[][]{{3, 1}},
                "E_WALLET", "GAGAL", false);

        System.out.println("Akun demo: admin@pesanyuk.id/admin123, "
                + "penjual warung@pesanyuk.id/penjual123, pelanggan@pesanyuk.id/pelanggan123");
        System.out.println("Data contoh dibuat oleh admin id " + admin);
    }

    private static void daftarMenu(Connection c, String awalan, int umkmId,
            int makanan, int camilan, String status) throws SQLException {
        if ("warung".equals(awalan)) {
            menu(c, awalan, umkmId, makanan, "Nasi Goreng Kampung",
                    "Nasi goreng dengan telur, ayam, dan sambal bawang.", 18_000, 40,
                    new Color(0xE85D2A), new Color(0xB4340F));
            menu(c, awalan, umkmId, makanan, "Ayam Geprek Sambal Ijo",
                    "Ayam crispy digeprek sambal ijo, plus nasi hangat.", 20_000, 35,
                    new Color(0x2E7D32), new Color(0x1B5E20));
            menu(c, awalan, umkmId, makanan, "Mie Ayam Bakso",
                    "Mie ayam gurih dengan bakso sapi dan pangsit.", 15_000, 30,
                    new Color(0xD4A017), new Color(0x8D6E00));
            menu(c, awalan, umkmId, camilan, "Tahu Crispy",
                    "Tahu goreng renyah dengan bumbu tabur.", 10_000, 50,
                    new Color(0x8D6E63), new Color(0x4E342E));
            menu(c, awalan, umkmId, 0, "Es Teh Manis",
                    "Teh manis dingin, segar untuk menemani makan.", 5_000, 100,
                    new Color(0x3F51B5), new Color(0x1A237E));
            menu(c, awalan, umkmId, 0, "Jus Alpukat",
                    "Alpukat segar dengan susu dan sedikit gula.", 12_000, 25,
                    new Color(0x558B2F), new Color(0x33691E));
        } else {
            menu(c, awalan, umkmId, 0, "Kopi Susu Senja",
                    "Espresso, susu segar, dan gula aren.", 22_000, 40,
                    new Color(0x6D4C41), new Color(0x3E2723));
            menu(c, awalan, umkmId, 0, "Americano",
                    "Kopi hitam manual brew, disajikan dingin.", 18_000, 30,
                    new Color(0x37474F), new Color(0x102027));
            menu(c, awalan, umkmId, camilan, "Croissant Butter",
                    "Croissant panggang dengan mentega premium.", 25_000, 15,
                    new Color(0xF9A825), new Color(0xC17900));
            menu(c, awalan, umkmId, 0, "Matcha Latte",
                    "Matcha Jepang dengan susu creamy.", 24_000, 25,
                    new Color(0x2E7D32), new Color(0x1B5E20));
        }
    }

    private static void menu(Connection c, String awalan, int umkmId, int kategoriId,
            String nama, String deskripsi, double harga, int stok,
            Color warnaAwal, Color warnaAkhir) throws SQLException {
        String berkas = awalan + "-" + nama.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "") + ".png";
        String path = "uploads/menu/" + berkas;
        PhotoStore.buatContoh(path, nama, warnaAwal, warnaAkhir);

        String sql = """
                INSERT INTO menu (umkm_id, kategori_id, nama_menu, deskripsi, harga, stok, foto, tersedia)
                VALUES (?,?,?,?,?,?,?,1)
                """;
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, umkmId);
            ps.setInt(2, kategoriId > 0 ? kategoriId : kategori(c, "Minuman"));
            ps.setString(3, nama);
            ps.setString(4, deskripsi);
            ps.setDouble(5, harga);
            ps.setInt(6, stok);
            ps.setString(7, path);
            ps.executeUpdate();
            try (ResultSet kunci = ps.getGeneratedKeys()) {
                kunci.next();
                MENU.put(nama, new MenuSeed(kunci.getInt(1), nama, harga));
            }
        }
    }

    private static int pengguna(Connection c, String nama, String email,
            String sandi, String role) throws SQLException {
        String sql = "INSERT INTO pengguna (nama, email, password_hash, role, aktif) VALUES (?,?,?,?,1)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nama);
            ps.setString(2, email);
            ps.setString(3, PasswordUtil.hash(sandi));
            ps.setString(4, role);
            ps.executeUpdate();
            try (ResultSet kunci = ps.getGeneratedKeys()) {
                kunci.next();
                return kunci.getInt(1);
            }
        }
    }

    private static int umkm(Connection c, int penggunaId, String nama, String deskripsi,
            String alamat, String telepon, String status) throws SQLException {
        String sql = """
                INSERT INTO umkm (pengguna_id, nama_umkm, deskripsi, alamat, telepon, status)
                VALUES (?,?,?,?,?,?)
                """;
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, penggunaId);
            ps.setString(2, nama);
            ps.setString(3, deskripsi);
            ps.setString(4, alamat);
            ps.setString(5, telepon);
            ps.setString(6, status);
            ps.executeUpdate();
            try (ResultSet kunci = ps.getGeneratedKeys()) {
                kunci.next();
                return kunci.getInt(1);
            }
        }
    }

    private static void pesanan(Connection c, String kode, int pelangganId, int umkmId,
            LocalDateTime waktu, String status, String jenis, String alamat, String catatan,
            int[][] barang, String metode, String statusBayar, boolean dibayar) throws SQLException {
        double subtotal = 0;
        for (int[] baris : barang) {
            MenuSeed menu = cariMenu(baris[0]);
            subtotal += menu.harga() * baris[1];
        }
        int pesananId;
        String sql = """
                INSERT INTO pesanan (kode, pelanggan_id, umkm_id, jenis, alamat, catatan,
                    subtotal, status, dibuat_pada, diperbarui_pada)
                VALUES (?,?,?,?,?,?,?,?,?,?)
                """;
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, kode);
            ps.setInt(2, pelangganId);
            ps.setInt(3, umkmId);
            ps.setString(4, jenis);
            ps.setString(5, alamat);
            ps.setString(6, catatan);
            ps.setDouble(7, subtotal);
            ps.setString(8, status);
            ps.setTimestamp(9, Timestamp.valueOf(waktu));
            ps.setTimestamp(10, Timestamp.valueOf(waktu));
            ps.executeUpdate();
            try (ResultSet kunci = ps.getGeneratedKeys()) {
                kunci.next();
                pesananId = kunci.getInt(1);
            }
        }
        String detail = """
                INSERT INTO detail_pesanan (pesanan_id, menu_id, nama_menu, harga, jumlah, subtotal)
                VALUES (?,?,?,?,?,?)
                """;
        try (PreparedStatement ps = c.prepareStatement(detail)) {
            for (int[] baris : barang) {
                MenuSeed menu = cariMenu(baris[0]);
                ps.setInt(1, pesananId);
                ps.setInt(2, baris[0]);
                ps.setString(3, menu.nama());
                ps.setDouble(4, menu.harga());
                ps.setInt(5, baris[1]);
                ps.setDouble(6, menu.harga() * baris[1]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
        String bayar = """
                INSERT INTO pembayaran (pesanan_id, metode, jumlah, status, referensi,
                    dibayar_pada, dibuat_pada)
                VALUES (?,?,?,?,?,?,?)
                """;
        try (PreparedStatement ps = c.prepareStatement(bayar)) {
            ps.setInt(1, pesananId);
            ps.setString(2, metode);
            ps.setDouble(3, subtotal);
            ps.setString(4, statusBayar);
            ps.setString(5, dibayar ? "PAY-DEMO-" + pesananId : null);
            ps.setTimestamp(6, dibayar ? Timestamp.valueOf(waktu.plusMinutes(2)) : null);
            ps.setTimestamp(7, Timestamp.valueOf(waktu));
            ps.executeUpdate();
        }
    }

    private static MenuSeed cariMenu(int id) {
        return MENU.values().stream().filter(m -> m.id() == id).findFirst()
                .orElseThrow(() -> new IllegalStateException("Menu id " + id + " tidak ada"));
    }

    private static void pastikanKategori(Connection c) throws SQLException {
        for (String nama : new String[]{"Makanan", "Minuman", "Camilan"}) {
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT IGNORE INTO kategori (nama) VALUES (?)")) {
                ps.setString(1, nama);
                ps.executeUpdate();
            }
        }
    }

    private static int kategori(Connection c, String nama) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT id FROM kategori WHERE nama=?")) {
            ps.setString(1, nama);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO kategori (nama) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nama);
            ps.executeUpdate();
            try (ResultSet kunci = ps.getGeneratedKeys()) {
                kunci.next();
                return kunci.getInt(1);
            }
        }
    }

    private static int hitung(Connection c, String sql) throws SQLException {
        try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        }
    }
}
