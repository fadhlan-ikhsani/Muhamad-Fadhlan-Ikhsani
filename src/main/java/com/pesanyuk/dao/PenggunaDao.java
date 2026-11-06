package com.pesanyuk.dao;

import com.pesanyuk.db.Database;
import com.pesanyuk.model.Pengguna;
import com.pesanyuk.util.PasswordUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public final class PenggunaDao {

    public Pengguna findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM pengguna WHERE email = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? petakan(rs) : null;
            }
        }
    }

    public Pengguna findById(int id) throws SQLException {
        String sql = "SELECT * FROM pengguna WHERE id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? petakan(rs) : null;
            }
        }
    }

    /** Memeriksa email dan kata sandi, mengembalikan pengguna bila cocok. */
    public Pengguna login(String email, String sandi) throws SQLException {
        String sql = "SELECT * FROM pengguna WHERE email = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                String hash = rs.getString("password_hash");
                return PasswordUtil.cocok(sandi, hash) ? petakan(rs) : null;
            }
        }
    }

    public boolean emailDipakai(String email) throws SQLException {
        String sql = "SELECT 1 FROM pengguna WHERE email = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Registrasi akun. Bila peran PENJUAL, data UMKM langsung dibuat dengan
     * status PENDING dalam transaksi yang sama.
     */
    public int daftar(String nama, String email, String kataSandiTerenkripsi, String role,
            String namaUmkm, String alamatUmkm, String teleponUmkm) throws SQLException {
        try (Connection c = Database.getConnection()) {
            c.setAutoCommit(false);
            try {
                int penggunaId;
                String sql = """
                        INSERT INTO pengguna (nama, email, password_hash, role, aktif)
                        VALUES (?,?,?,?,1)
                        """;
                try (PreparedStatement ps = c.prepareStatement(sql,
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, nama.trim());
                    ps.setString(2, email.trim().toLowerCase());
                    ps.setString(3, kataSandiTerenkripsi);
                    ps.setString(4, role);
                    ps.executeUpdate();
                    try (ResultSet kunci = ps.getGeneratedKeys()) {
                        kunci.next();
                        penggunaId = kunci.getInt(1);
                    }
                }
                if ("PENJUAL".equals(role)) {
                    String sqlUmkm = """
                            INSERT INTO umkm (pengguna_id, nama_umkm, deskripsi, alamat, telepon, status)
                            VALUES (?,?,?,?,?,'PENDING')
                            """;
                    try (PreparedStatement ps = c.prepareStatement(sqlUmkm)) {
                        ps.setInt(1, penggunaId);
                        ps.setString(2, namaUmkm.trim());
                        ps.setString(3, "UMKM baru yang dikelola " + nama.trim());
                        ps.setString(4, alamatUmkm);
                        ps.setString(5, teleponUmkm);
                        ps.executeUpdate();
                    }
                }
                c.commit();
                return penggunaId;
            } catch (SQLException | RuntimeException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    public int insert(String nama, String email, String kataSandiTerenkripsi,
            String role) throws SQLException {
        String sql = "INSERT INTO pengguna (nama, email, password_hash, role, aktif) VALUES (?,?,?,?,1)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nama.trim());
            ps.setString(2, email.trim().toLowerCase());
            ps.setString(3, kataSandiTerenkripsi);
            ps.setString(4, role);
            ps.executeUpdate();
            try (ResultSet kunci = ps.getGeneratedKeys()) {
                kunci.next();
                return kunci.getInt(1);
            }
        }
    }

    /** @param role null atau kosong berarti semua peran. */
    public List<Pengguna> findAll(String role) throws SQLException {
        String sql = "SELECT * FROM pengguna"
                + (role == null || role.isBlank() ? "" : " WHERE role = ?")
                + " ORDER BY dibuat_pada DESC, id DESC";
        List<Pengguna> hasil = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (role != null && !role.isBlank()) {
                ps.setString(1, role);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    hasil.add(petakan(rs));
                }
            }
        }
        return hasil;
    }

    public void setAktif(int id, boolean aktif) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE pengguna SET aktif = ? WHERE id = ?")) {
            ps.setBoolean(1, aktif);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void ubahKataSandi(int id, String kataSandiTerenkripsi) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE pengguna SET password_hash = ? WHERE id = ?")) {
            ps.setString(1, kataSandiTerenkripsi);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    private Pengguna petakan(ResultSet rs) throws SQLException {
        Timestamp dibuat = rs.getTimestamp("dibuat_pada");
        return new Pengguna(
                rs.getInt("id"),
                rs.getString("nama"),
                rs.getString("email"),
                rs.getString("role"),
                rs.getBoolean("aktif"),
                dibuat);
    }
}
