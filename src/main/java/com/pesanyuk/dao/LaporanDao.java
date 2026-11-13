package com.pesanyuk.dao;

import com.pesanyuk.db.Database;
import com.pesanyuk.model.OmzetHarian;
import com.pesanyuk.model.RekapMenu;
import com.pesanyuk.model.RingkasanLaporan;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * Query laporan penjualan. Semua laporan hanya menghitung pesanan berstatus
 * SELESAI. {@code umkmId} bernilai null berarti seluruh UMKM (admin).
 */
@Repository
public final class LaporanDao {

    public RingkasanLaporan ringkasan(Integer umkmId, LocalDate dari, LocalDate sampai)
            throws SQLException {
        String filter = "WHERE p.status = 'SELESAI' AND p.dibuat_pada >= ? AND p.dibuat_pada < ?"
                + (umkmId == null ? "" : " AND p.umkm_id = ?");
        String sql = "SELECT COALESCE(SUM(p.subtotal),0) AS omzet, COUNT(*) AS jumlah FROM pesanan p "
                + filter;
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            pasangRentang(ps, umkmId, dari, sampai);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                double omzet = rs.getDouble("omzet");
                int jumlah = rs.getInt("jumlah");
                return new RingkasanLaporan(omzet, jumlah,
                        menuTerjual(c, umkmId, dari, sampai));
            }
        }
    }

    private int menuTerjual(Connection c, Integer umkmId, LocalDate dari, LocalDate sampai)
            throws SQLException {
        String sql = """
                SELECT COALESCE(SUM(d.jumlah),0) AS total
                FROM detail_pesanan d JOIN pesanan p ON p.id = d.pesanan_id
                WHERE p.status = 'SELESAI' AND p.dibuat_pada >= ? AND p.dibuat_pada < ?
                """ + (umkmId == null ? "" : " AND p.umkm_id = ?");
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            pasangRentang(ps, umkmId, dari, sampai);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("total");
            }
        }
    }

    public List<OmzetHarian> harian(Integer umkmId, LocalDate dari, LocalDate sampai)
            throws SQLException {
        String sql = """
                SELECT DATE(p.dibuat_pada) AS tanggal,
                       COALESCE(SUM(p.subtotal),0) AS omzet,
                       COUNT(*) AS jumlah
                FROM pesanan p
                WHERE p.status = 'SELESAI' AND p.dibuat_pada >= ? AND p.dibuat_pada < ?
                """ + (umkmId == null ? "" : " AND p.umkm_id = ?")
                + " GROUP BY DATE(p.dibuat_pada) ORDER BY tanggal";
        List<OmzetHarian> hasil = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            pasangRentang(ps, umkmId, dari, sampai);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    hasil.add(new OmzetHarian(
                            rs.getDate("tanggal").toLocalDate(),
                            rs.getDouble("omzet"),
                            rs.getInt("jumlah")));
                }
            }
        }
        return hasil;
    }

    public List<RekapMenu> rekapMenu(Integer umkmId, LocalDate dari, LocalDate sampai)
            throws SQLException {
        String sql = """
                SELECT d.nama_menu, SUM(d.jumlah) AS jumlah, SUM(d.subtotal) AS omzet
                FROM detail_pesanan d JOIN pesanan p ON p.id = d.pesanan_id
                WHERE p.status = 'SELESAI' AND p.dibuat_pada >= ? AND p.dibuat_pada < ?
                """ + (umkmId == null ? "" : " AND p.umkm_id = ?")
                + " GROUP BY d.nama_menu ORDER BY omzet DESC";
        List<RekapMenu> hasil = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            pasangRentang(ps, umkmId, dari, sampai);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    hasil.add(new RekapMenu(
                            rs.getString("nama_menu"),
                            rs.getInt("jumlah"),
                            rs.getDouble("omzet")));
                }
            }
        }
        return hasil;
    }

    private void pasangRentang(PreparedStatement ps, Integer umkmId,
            LocalDate dari, LocalDate sampai) throws SQLException {
        ps.setTimestamp(1, Timestamp.valueOf(dari.atStartOfDay()));
        ps.setTimestamp(2, Timestamp.valueOf(sampai.plusDays(1).atStartOfDay()));
        if (umkmId != null) {
            ps.setInt(3, umkmId);
        }
    }
}
