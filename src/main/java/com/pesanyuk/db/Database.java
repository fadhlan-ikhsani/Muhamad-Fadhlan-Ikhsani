package com.pesanyuk.db;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public final class Database {

    private static final Properties CONFIG = new Properties();
    private static final String NAMA_DB;
    private static final String URL_SERVER;
    private static final String URL_DB;
    private static final String USER;
    private static final String PASSWORD;

    private static final String[] DDL = {
        """
        CREATE TABLE IF NOT EXISTS pengguna (
            id INT AUTO_INCREMENT PRIMARY KEY,
            nama VARCHAR(100) NOT NULL,
            email VARCHAR(120) NOT NULL UNIQUE,
            password_hash VARCHAR(255) NOT NULL,
            role ENUM('ADMIN','PENJUAL','PELANGGAN') NOT NULL,
            aktif TINYINT(1) NOT NULL DEFAULT 1,
            dibuat_pada DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
        """,
        """
        CREATE TABLE IF NOT EXISTS umkm (
            id INT AUTO_INCREMENT PRIMARY KEY,
            pengguna_id INT NOT NULL,
            nama_umkm VARCHAR(120) NOT NULL,
            deskripsi TEXT,
            alamat VARCHAR(255),
            telepon VARCHAR(30),
            status ENUM('PENDING','TERVERIFIKASI','DITANGGUHKAN') NOT NULL DEFAULT 'PENDING',
            dibuat_pada DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
            UNIQUE KEY uq_umkm_pengguna (pengguna_id),
            CONSTRAINT fk_umkm_pengguna FOREIGN KEY (pengguna_id)
                REFERENCES pengguna(id) ON DELETE CASCADE
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
        """,
        """
        CREATE TABLE IF NOT EXISTS kategori (
            id INT AUTO_INCREMENT PRIMARY KEY,
            nama VARCHAR(60) NOT NULL UNIQUE
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
        """,
        """
        CREATE TABLE IF NOT EXISTS menu (
            id INT AUTO_INCREMENT PRIMARY KEY,
            umkm_id INT NOT NULL,
            kategori_id INT NOT NULL,
            nama_menu VARCHAR(120) NOT NULL,
            deskripsi TEXT,
            harga DECIMAL(12,2) NOT NULL,
            stok INT NOT NULL DEFAULT 0,
            foto VARCHAR(255),
            tersedia TINYINT(1) NOT NULL DEFAULT 1,
            dibuat_pada DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
            KEY idx_menu_umkm (umkm_id),
            CONSTRAINT fk_menu_umkm FOREIGN KEY (umkm_id)
                REFERENCES umkm(id) ON DELETE CASCADE,
            CONSTRAINT fk_menu_kategori FOREIGN KEY (kategori_id)
                REFERENCES kategori(id)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
        """,
        """
        CREATE TABLE IF NOT EXISTS pesanan (
            id INT AUTO_INCREMENT PRIMARY KEY,
            kode VARCHAR(30) NOT NULL UNIQUE,
            pelanggan_id INT NOT NULL,
            umkm_id INT NOT NULL,
            jenis ENUM('AMBIL_SENDIRI','PENGANTARAN') NOT NULL,
            alamat VARCHAR(255),
            catatan TEXT,
            subtotal DECIMAL(12,2) NOT NULL,
            status ENUM('MENUNGGU','DIPROSES','SIAP','SELESAI','DIBATALKAN')
                NOT NULL DEFAULT 'MENUNGGU',
            dibuat_pada DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
            diperbarui_pada DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
            KEY idx_pesanan_pelanggan (pelanggan_id),
            KEY idx_pesanan_umkm (umkm_id),
            KEY idx_pesanan_tanggal (dibuat_pada),
            CONSTRAINT fk_pesanan_pelanggan FOREIGN KEY (pelanggan_id)
                REFERENCES pengguna(id),
            CONSTRAINT fk_pesanan_umkm FOREIGN KEY (umkm_id)
                REFERENCES umkm(id)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
        """,
        """
        CREATE TABLE IF NOT EXISTS detail_pesanan (
            id INT AUTO_INCREMENT PRIMARY KEY,
            pesanan_id INT NOT NULL,
            menu_id INT NOT NULL,
            nama_menu VARCHAR(120) NOT NULL,
            harga DECIMAL(12,2) NOT NULL,
            jumlah INT NOT NULL,
            subtotal DECIMAL(12,2) NOT NULL,
            KEY idx_detail_pesanan (pesanan_id),
            CONSTRAINT fk_detail_pesanan FOREIGN KEY (pesanan_id)
                REFERENCES pesanan(id) ON DELETE CASCADE,
            CONSTRAINT fk_detail_menu FOREIGN KEY (menu_id)
                REFERENCES menu(id)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
        """,
        """
        CREATE TABLE IF NOT EXISTS pembayaran (
            id INT AUTO_INCREMENT PRIMARY KEY,
            pesanan_id INT NOT NULL UNIQUE,
            metode ENUM('TRANSFER_BANK','E_WALLET','QRIS') NOT NULL,
            jumlah DECIMAL(12,2) NOT NULL,
            status ENUM('MENUNGGU','LUNAS','GAGAL') NOT NULL DEFAULT 'MENUNGGU',
            referensi VARCHAR(60),
            dibayar_pada DATETIME,
            dibuat_pada DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
            CONSTRAINT fk_pembayaran_pesanan FOREIGN KEY (pesanan_id)
                REFERENCES pesanan(id) ON DELETE CASCADE
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
        """
    };

    static {
        try (InputStream in = Database.class.getResourceAsStream("/db.properties")) {
            if (in == null) {
                throw new IllegalStateException("File db.properties tidak ditemukan.");
            }
            CONFIG.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (IOException ex) {
            throw new IllegalStateException("Gagal membaca db.properties: " + ex.getMessage(), ex);
        }

        String host = CONFIG.getProperty("db.host", "localhost").trim();
        String port = CONFIG.getProperty("db.port", "3306").trim();
        NAMA_DB = CONFIG.getProperty("db.name", "pesanyuk_db").trim();
        String params = CONFIG.getProperty("db.params", "").trim();
        USER = baca("PESANYUK_DB_USER", CONFIG.getProperty("db.user", "root"));
        PASSWORD = baca("PESANYUK_DB_PASSWORD", CONFIG.getProperty("db.password", ""));
        URL_SERVER = "jdbc:mysql://" + host + ":" + port + "/?"
                + (params.isEmpty() ? "" : params + "&") + "connectTimeout=5000";
        URL_DB = "jdbc:mysql://" + host + ":" + port + "/" + NAMA_DB
                + "?" + (params.isEmpty() ? "" : params + "&") + "connectTimeout=5000";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException("Driver MySQL tidak ditemukan di classpath.", ex);
        }
    }

    private Database() {
    }

    private static String baca(String env, String bawaan) {
        String nilai = System.getenv(env);
        return nilai == null || nilai.isBlank() ? bawaan : nilai;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL_DB, USER, PASSWORD);
    }

    /** Membuat basis data + tabel bila belum ada, lalu mengisi data contoh. */
    public static void bootstrap() throws SQLException {
        try (Connection koneksi = DriverManager.getConnection(URL_SERVER, USER, PASSWORD);
             Statement perintah = koneksi.createStatement()) {
            perintah.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + NAMA_DB
                    + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci");
        }
        try (Connection koneksi = getConnection();
             Statement perintah = koneksi.createStatement()) {
            for (String ddl : DDL) {
                perintah.execute(ddl);
            }
        }
        DatabaseSeeder.seed();
    }

    public static String info() {
        return NAMA_DB + " (" + USER + "@" + URL_DB.replaceAll("^.*?//|\\?.*$", "") + ")";
    }

    public static String namaDatabase() {
        return NAMA_DB;
    }
}
