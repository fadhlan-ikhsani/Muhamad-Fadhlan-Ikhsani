package com.pesanyuk.dao;

import com.pesanyuk.db.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class PembayaranDao {

    /**
     * Menandai pesanan sebagai lunas. Simulasi payment gateway: referensi
     * transaksi dibuat lokal seperti respons Midtrans.
     */
    public void bayar(int pesananId, String metode) throws SQLException {
        String referensi = "PAY-" + System.currentTimeMillis();
        String sql = """
                INSERT INTO pembayaran (pesanan_id, metode, jumlah, status, referensi, dibayar_pada)
                VALUES (?, ?, (SELECT subtotal FROM pesanan WHERE id = ?), 'LUNAS', ?, NOW())
                ON DUPLICATE KEY UPDATE metode = VALUES(metode), status = 'LUNAS',
                    referensi = VALUES(referensi), dibayar_pada = NOW()
                """;
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, pesananId);
            ps.setString(2, metode);
            ps.setInt(3, pesananId);
            ps.setString(4, referensi);
            ps.executeUpdate();
        }
    }

    public String status(int pesananId) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT status FROM pembayaran WHERE pesanan_id = ?")) {
            ps.setInt(1, pesananId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : "MENUNGGU";
            }
        }
    }
}
