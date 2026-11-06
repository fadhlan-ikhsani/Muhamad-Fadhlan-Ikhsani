package com.pesanyuk.dao;

import com.pesanyuk.db.Database;
import com.pesanyuk.model.DetailPesanan;
import com.pesanyuk.model.Menu;
import com.pesanyuk.model.Pesanan;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public final class PesananDao {

    /** Satu baris yang akan dipesan: menu dan jumlah porsi. */
    public record Baris(Menu menu, int jumlah) {
    }

    private static final String DASAR = """
            SELECT p.id, p.kode, p.pelanggan_id, pl.nama AS pelanggan_nama, p.umkm_id,
                   u.nama_umkm, p.jenis, p.alamat, p.catatan, p.subtotal, p.status,
                   p.dibuat_pada, b.metode, b.status AS status_bayar
            FROM pesanan p
            JOIN pengguna pl ON pl.id = p.pelanggan_id
            JOIN umkm u ON u.id = p.umkm_id
            LEFT JOIN pembayaran b ON b.pesanan_id = p.id
            """;

    /**
     * Membuat pesanan dalam satu transaksi: pesanan, rincian, dan pengurangan
     * stok. Pembayaran dibuat terpisah saat pelanggan membayar.
     */
    public Pesanan buat(int pelangganId, int umkmId, String jenis, String alamat,
            String catatan, List<Baris> baris) throws SQLException {
        if (baris == null || baris.isEmpty()) {
            throw new IllegalArgumentException("Keranjang masih kosong.");
        }
        try (Connection c = Database.getConnection()) {
            c.setAutoCommit(false);
            try {
                double subtotal = baris.stream()
                        .mapToDouble(b -> b.menu().harga() * b.jumlah()).sum();
                String kode = kodeBaru(c);

                int pesananId;
                String sqlPesanan = """
                        INSERT INTO pesanan (kode, pelanggan_id, umkm_id, jenis, alamat, catatan,
                            subtotal, status)
                        VALUES (?,?,?,?,?,?,?,'MENUNGGU')
                        """;
                try (PreparedStatement ps = c.prepareStatement(sqlPesanan,
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, kode);
                    ps.setInt(2, pelangganId);
                    ps.setInt(3, umkmId);
                    ps.setString(4, jenis);
                    ps.setString(5, alamat);
                    ps.setString(6, catatan);
                    ps.setDouble(7, subtotal);
                    ps.executeUpdate();
                    try (ResultSet kunci = ps.getGeneratedKeys()) {
                        kunci.next();
                        pesananId = kunci.getInt(1);
                    }
                }

                try (PreparedStatement detail = c.prepareStatement("""
                        INSERT INTO detail_pesanan (pesanan_id, menu_id, nama_menu, harga, jumlah, subtotal)
                        VALUES (?,?,?,?,?,?)
                        """);
                     PreparedStatement stok = c.prepareStatement("""
                        UPDATE menu SET stok = stok - ? WHERE id = ? AND stok >= ? AND tersedia = 1
                        """)) {
                    for (Baris b : baris) {
                        stok.setInt(1, b.jumlah());
                        stok.setInt(2, b.menu().id());
                        stok.setInt(3, b.jumlah());
                        if (stok.executeUpdate() == 0) {
                            throw new IllegalArgumentException("Stok " + b.menu().nama()
                                    + " tidak mencukupi atau menu sedang tidak tersedia.");
                        }
                        detail.setInt(1, pesananId);
                        detail.setInt(2, b.menu().id());
                        detail.setString(3, b.menu().nama());
                        detail.setDouble(4, b.menu().harga());
                        detail.setInt(5, b.jumlah());
                        detail.setDouble(6, b.menu().harga() * b.jumlah());
                        detail.executeUpdate();
                    }
                }
                c.commit();
                return findById(pesananId);
            } catch (SQLException | RuntimeException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    public List<Pesanan> cari(Integer pelangganId, Integer umkmId, String status) throws SQLException {
        StringBuilder sql = new StringBuilder(DASAR).append(" WHERE 1=1");
        List<Object> parameter = new ArrayList<>();
        if (pelangganId != null) {
            sql.append(" AND p.pelanggan_id = ?");
            parameter.add(pelangganId);
        }
        if (umkmId != null) {
            sql.append(" AND p.umkm_id = ?");
            parameter.add(umkmId);
        }
        if (status != null && !status.isBlank() && !"SEMUA".equals(status)) {
            sql.append(" AND p.status = ?");
            parameter.add(status);
        }
        sql.append(" ORDER BY p.dibuat_pada DESC, p.id DESC");
        List<Pesanan> hasil = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < parameter.size(); i++) {
                ps.setObject(i + 1, parameter.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    hasil.add(petakan(rs));
                }
            }
        }
        return hasil;
    }

    public Pesanan findById(int id) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(DASAR + " WHERE p.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? petakan(rs) : null;
            }
        }
    }

    public List<DetailPesanan> detail(int pesananId) throws SQLException {
        String sql = """
                SELECT id, pesanan_id, menu_id, nama_menu, harga, jumlah, subtotal
                FROM detail_pesanan WHERE pesanan_id = ? ORDER BY id
                """;
        List<DetailPesanan> hasil = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, pesananId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    hasil.add(new DetailPesanan(
                            rs.getInt("id"),
                            rs.getInt("pesanan_id"),
                            rs.getInt("menu_id"),
                            rs.getString("nama_menu"),
                            rs.getDouble("harga"),
                            rs.getInt("jumlah"),
                            rs.getDouble("subtotal")));
                }
            }
        }
        return hasil;
    }

    public void ubahStatus(int pesananId, String statusBaru) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE pesanan SET status = ?, diperbarui_pada = NOW() WHERE id = ?")) {
            ps.setString(1, statusBaru);
            ps.setInt(2, pesananId);
            ps.executeUpdate();
        }
    }

    /** Membatalkan pesanan dan mengembalikan stok menu yang dipesan. */
    public void batalkan(int pesananId) throws SQLException {
        try (Connection c = Database.getConnection()) {
            c.setAutoCommit(false);
            try {
                String status;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT status FROM pesanan WHERE id = ? FOR UPDATE")) {
                    ps.setInt(1, pesananId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException("Pesanan tidak ditemukan.");
                        }
                        status = rs.getString(1);
                    }
                }
                if (!"MENUNGGU".equals(status) && !"DIPROSES".equals(status)) {
                    throw new IllegalArgumentException(
                            "Pesanan dengan status ini tidak dapat dibatalkan.");
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT menu_id, jumlah FROM detail_pesanan WHERE pesanan_id = ?")) {
                    ps.setInt(1, pesananId);
                    try (ResultSet rs = ps.executeQuery()) {
                        List<int[]> item = new ArrayList<>();
                        while (rs.next()) {
                            item.add(new int[]{rs.getInt(1), rs.getInt(2)});
                        }
                        try (PreparedStatement stok = c.prepareStatement(
                                "UPDATE menu SET stok = stok + ? WHERE id = ?")) {
                            for (int[] i : item) {
                                stok.setInt(1, i[1]);
                                stok.setInt(2, i[0]);
                                stok.executeUpdate();
                            }
                        }
                    }
                }
                try (PreparedStatement ps = c.prepareStatement("""
                        UPDATE pesanan SET status = 'DIBATALKAN', diperbarui_pada = NOW()
                        WHERE id = ?
                        """)) {
                    ps.setInt(1, pesananId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = c.prepareStatement("""
                        UPDATE pembayaran SET status = 'GAGAL'
                        WHERE pesanan_id = ? AND status <> 'LUNAS'
                        """)) {
                    ps.setInt(1, pesananId);
                    ps.executeUpdate();
                }
                c.commit();
            } catch (SQLException | RuntimeException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    private String kodeBaru(Connection c) throws SQLException {
        String tanggal = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        for (int percobaan = 0; percobaan < 20; percobaan++) {
            String acak = String.format(Locale.ROOT, "%04d",
                    ThreadLocalRandom.current().nextInt(10_000));
            String kode = "PY-" + tanggal + "-" + acak;
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT 1 FROM pesanan WHERE kode = ?")) {
                ps.setString(1, kode);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return kode;
                    }
                }
            }
        }
        return "PY-" + tanggal + "-" + System.currentTimeMillis() % 100_000;
    }

    private Pesanan petakan(ResultSet rs) throws SQLException {
        return new Pesanan(
                rs.getInt("id"),
                rs.getString("kode"),
                rs.getInt("pelanggan_id"),
                rs.getString("pelanggan_nama"),
                rs.getInt("umkm_id"),
                rs.getString("nama_umkm"),
                rs.getString("jenis"),
                rs.getString("alamat"),
                rs.getString("catatan"),
                rs.getDouble("subtotal"),
                rs.getString("status"),
                rs.getTimestamp("dibuat_pada"),
                rs.getString("metode"),
                rs.getString("status_bayar"));
    }
}
