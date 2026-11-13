package com.pesanyuk.dao;

import com.pesanyuk.db.Database;
import com.pesanyuk.model.Umkm;
import com.pesanyuk.model.UmkmAdmin;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public final class UmkmDao {

    public Umkm findByPengguna(int penggunaId) throws SQLException {
        String sql = "SELECT * FROM umkm WHERE pengguna_id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, penggunaId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? petakan(rs) : null;
            }
        }
    }

    public Umkm findById(int id) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM umkm WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? petakan(rs) : null;
            }
        }
    }

    public int insert(int penggunaId, String nama, String deskripsi,
            String alamat, String telepon) throws SQLException {
        String sql = """
                INSERT INTO umkm (pengguna_id, nama_umkm, deskripsi, alamat, telepon, status)
                VALUES (?,?,?,?,?,'PENDING')
                """;
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, penggunaId);
            ps.setString(2, nama.trim());
            ps.setString(3, deskripsi);
            ps.setString(4, alamat);
            ps.setString(5, telepon);
            ps.executeUpdate();
            try (ResultSet kunci = ps.getGeneratedKeys()) {
                kunci.next();
                return kunci.getInt(1);
            }
        }
    }

    public void setStatus(int id, String status) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE umkm SET status = ? WHERE id = ?")) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public List<UmkmAdmin> findAllLengkap() throws SQLException {
        String sql = """
                SELECT u.*, p.nama AS pemilik, p.email
                FROM umkm u JOIN pengguna p ON p.id = u.pengguna_id
                ORDER BY u.dibuat_pada DESC, u.id DESC
                """;
        List<UmkmAdmin> hasil = new ArrayList<>();
        try (Connection c = Database.getConnection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                hasil.add(new UmkmAdmin(
                        rs.getInt("id"),
                        rs.getInt("pengguna_id"),
                        rs.getString("nama_umkm"),
                        rs.getString("pemilik"),
                        rs.getString("email"),
                        rs.getString("status"),
                        rs.getString("alamat"),
                        rs.getString("telepon"),
                        rs.getTimestamp("dibuat_pada")));
            }
        }
        return hasil;
    }

    private Umkm petakan(ResultSet rs) throws SQLException {
        return new Umkm(
                rs.getInt("id"),
                rs.getInt("pengguna_id"),
                rs.getString("nama_umkm"),
                rs.getString("deskripsi"),
                rs.getString("alamat"),
                rs.getString("telepon"),
                rs.getString("status"),
                rs.getTimestamp("dibuat_pada"));
    }
}
