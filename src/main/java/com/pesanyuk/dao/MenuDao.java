package com.pesanyuk.dao;

import com.pesanyuk.db.Database;
import com.pesanyuk.model.Kategori;
import com.pesanyuk.model.Menu;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public final class MenuDao {

    private static final String DASAR = """
            SELECT m.id, m.umkm_id, u.nama_umkm, m.kategori_id, k.nama AS kategori,
                   m.nama_menu, m.deskripsi, m.harga, m.stok, m.foto, m.tersedia
            FROM menu m
            JOIN umkm u ON u.id = m.umkm_id
            JOIN kategori k ON k.id = m.kategori_id
            """;

    public List<Kategori> kategori() throws SQLException {
        List<Kategori> hasil = new ArrayList<>();
        try (Connection c = Database.getConnection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT id, nama FROM kategori ORDER BY id")) {
            while (rs.next()) {
                hasil.add(new Kategori(rs.getInt("id"), rs.getString("nama")));
            }
        }
        return hasil;
    }

    /**
     * Katalog untuk pelanggan: hanya menu tersedia, stok > 0, dan UMKM
     * terverifikasi.
     *
     * @param kategoriId 0 berarti semua kategori
     * @param urutan     nama | termurah | termahal | terbaru
     */
    public List<Menu> katalog(String cari, int kategoriId, String urutan) throws SQLException {
        StringBuilder sql = new StringBuilder(DASAR)
                .append("""
                         WHERE m.tersedia = 1 AND m.stok > 0 AND u.status = 'TERVERIFIKASI'
                        """);
        List<Object> parameter = new ArrayList<>();
        if (cari != null && !cari.isBlank()) {
            sql.append(" AND (m.nama_menu LIKE ? OR u.nama_umkm LIKE ? OR m.deskripsi LIKE ?)");
            String pola = "%" + cari.trim() + "%";
            parameter.add(pola);
            parameter.add(pola);
            parameter.add(pola);
        }
        if (kategoriId > 0) {
            sql.append(" AND m.kategori_id = ?");
            parameter.add(kategoriId);
        }
        sql.append(" ORDER BY ").append(switch (urutan == null ? "" : urutan) {
            case "termurah" -> "m.harga ASC, m.nama_menu ASC";
            case "termahal" -> "m.harga DESC, m.nama_menu ASC";
            case "terbaru" -> "m.dibuat_pada DESC, m.id DESC";
            default -> "m.nama_menu ASC";
        });

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < parameter.size(); i++) {
                ps.setObject(i + 1, parameter.get(i));
            }
            return bacaSemua(ps);
        }
    }

    public List<Menu> byUmkm(int umkmId) throws SQLException {
        String sql = DASAR + " WHERE m.umkm_id = ? ORDER BY m.nama_menu";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, umkmId);
            return bacaSemua(ps);
        }
    }

    public Menu findById(int id) throws SQLException {
        String sql = DASAR + " WHERE m.id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? petakan(rs) : null;
            }
        }
    }

    public int insert(int umkmId, int kategoriId, String nama, String deskripsi,
            double harga, int stok, String foto, boolean tersedia) throws SQLException {
        String sql = """
                INSERT INTO menu (umkm_id, kategori_id, nama_menu, deskripsi, harga, stok, foto, tersedia)
                VALUES (?,?,?,?,?,?,?,?)
                """;
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, umkmId);
            ps.setInt(2, kategoriId);
            ps.setString(3, nama);
            ps.setString(4, deskripsi);
            ps.setDouble(5, harga);
            ps.setInt(6, stok);
            ps.setString(7, foto);
            ps.setBoolean(8, tersedia);
            ps.executeUpdate();
            try (ResultSet kunci = ps.getGeneratedKeys()) {
                kunci.next();
                return kunci.getInt(1);
            }
        }
    }

    public void update(int id, int kategoriId, String nama, String deskripsi,
            double harga, int stok, String foto, boolean tersedia) throws SQLException {
        String sql = """
                UPDATE menu SET kategori_id=?, nama_menu=?, deskripsi=?, harga=?,
                    stok=?, foto=?, tersedia=?
                WHERE id=?
                """;
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, kategoriId);
            ps.setString(2, nama);
            ps.setString(3, deskripsi);
            ps.setDouble(4, harga);
            ps.setInt(5, stok);
            ps.setString(6, foto);
            ps.setBoolean(7, tersedia);
            ps.setInt(8, id);
            ps.executeUpdate();
        }
    }

    public void setTersedia(int id, boolean tersedia) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE menu SET tersedia = ? WHERE id = ?")) {
            ps.setBoolean(1, tersedia);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM menu WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private List<Menu> bacaSemua(PreparedStatement ps) throws SQLException {
        List<Menu> hasil = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                hasil.add(petakan(rs));
            }
        }
        return hasil;
    }

    private Menu petakan(ResultSet rs) throws SQLException {
        return new Menu(
                rs.getInt("id"),
                rs.getInt("umkm_id"),
                rs.getString("nama_umkm"),
                rs.getInt("kategori_id"),
                rs.getString("kategori"),
                rs.getString("nama_menu"),
                rs.getString("deskripsi"),
                rs.getDouble("harga"),
                rs.getInt("stok"),
                rs.getString("foto"),
                rs.getBoolean("tersedia"));
    }
}
