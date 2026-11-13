# PesanYuk Desktop

Aplikasi desktop pemesanan makanan dan minuman UMKM yang dibuat dengan **Java Swing**, **Maven**, dan **MySQL/MariaDB**. Aplikasi ini merupakan adaptasi desktop dari studi kasus **PesanYuk** pada dokumen *Act1_Muhammad_Fadhlan_Ikhsani_4IA24.pdf*.

> Dokumen asli merancang aplikasi berbasis web (Servlet/JSP, Hibernate, Midtrans). Pada versi ini antarmuka diubah menjadi aplikasi desktop Java Swing dengan akses basis data langsung melalui JDBC, sedangkan alur bisnis dan fitur utamanya tetap dipertahankan. Pembayaran daring disimulasikan (tidak terhubung ke Midtrans sungguhan).

---

## 1. Fitur

### Pelanggan
- Registrasi dan login akun pelanggan.
- Katalog menu dari UMKM terverifikasi.
- Pencarian menu/UMKM, filter kategori, dan urutan harga/terbaru.
- Detail menu dengan foto, deskripsi, harga, dan stok.
- Keranjang belanja (satu keranjang berisi menu dari satu UMKM).
- Checkout dengan pilihan **ambil sendiri** atau **pengantaran** beserta alamat dan catatan.
- Pembayaran daring simulasi: **QRIS**, **transfer bank**, dan **e-wallet**.
- Riwayat pesanan, status pesanan, dan pembayaran ulang.
- Pembatalan pesanan sebelum diproses (stok dikembalikan otomatis).

### Penjual
- Login akun penjual yang UMKM-nya sudah diverifikasi admin.
- Kelola menu: tambah, ubah, hapus, unggah foto, atur harga/stok, dan ketersediaan.
- Daftar pesanan masuk khusus UMKM sendiri dengan pembaruan otomatis tiap 5 detik.
- Perubahan status pesanan: **Menunggu → Diproses → Siap → Selesai**.
- Pembatalan pesanan dengan pengembalian stok.
- Laporan penjualan per periode: omzet, jumlah pesanan, menu terjual, grafik harian, dan rekap menu.
- Ekspor laporan ke **PDF** dan **Excel/CSV**.

### Admin
- Kelola pengguna: filter peran, pencarian, aktif/nonaktifkan akun, reset kata sandi.
- Verifikasi UMKM: setujui, tangguhkan, atau aktifkan kembali.
- Memantau seluruh transaksi, status pembayaran, dan rincian item.
- Laporan penjualan semua UMKM dengan ekspor PDF/CSV.

### Keamanan dan data
- Kata sandi disimpan sebagai hash **PBKDF2-HMAC-SHA256** dengan salt acak (bukan teks biasa).
- Transaksi pembuatan pesanan, pengurangan stok, pembatalan, dan pengembalian stok memakai transaksi basis data.
- Skema dan data contoh dibuat otomatis saat aplikasi pertama kali dijalankan.

---

## 2. Kebutuhan

| Perangkat | Versi yang disarankan |
|---|---|
| JDK | 17 atau lebih baru |
| NetBeans | 17 (Maven sudah bawaan) |
| MySQL / MariaDB | MySQL 8.x atau MariaDB 10.x (XAMPP) |
| Maven | 3.8+ (bawaan NetBeans) |

Aplikasi diuji dengan **JDK 17**, **MariaDB 10.11 (XAMPP)**, dan MySQL Connector/J 8.4.0.

---

## 3. Menjalankan Aplikasi

### Langkah 1 - Jalankan server basis data
Buka **XAMPP Control Panel**, lalu klik **Start** pada modul **MySQL**.

Konfigurasi bawaan ada di `src/main/resources/db.properties`:

```properties
db.host=localhost
db.port=3306
db.name=pesanyuk_db
db.user=root
db.password=
```

Ubah `db.user` / `db.password` bila instalasi MySQL Anda memakai sandi. Konfigurasi juga dapat ditimpa dengan variabel lingkungan `PESANYUK_DB_USER` dan `PESANYUK_DB_PASSWORD`.

### Langkah 2 - Buka proyek di NetBeans
1. Jalankan NetBeans.
2. Pilih **File → Open Project**.
3. Arahkan ke folder `PesanYukDesktop` (folder yang berisi `pom.xml`), lalu klik **Open Project**.
4. Tunggu NetBeans menyelesaikan *dependency resolution*.
5. Klik kanan proyek → **Run** (F6).

### Langkah 3 - Alternatif lewat terminal

```powershell
cd PesanYukDesktop
$env:JAVA_HOME="C:\Program Files\Java\jdk-17"

# menjalankan langsung
mvn exec:java

# atau membuat berkas JAR
mvn -DskipTests package
java -jar target/PesanYuk.jar
```

Saat pertama dijalankan, aplikasi otomatis:
1. Membuat database `pesanyuk_db` bila belum ada.
2. Membuat seluruh tabel dan relasi.
3. Mengisi kategori, akun demo, UMKM, menu, dan riwayat pesanan contoh.

Jika ingin membuat skema secara manual melalui phpMyAdmin, impor berkas `sql/pesanyuk.sql` terlebih dahulu. Data contoh tetap akan dibuat otomatis oleh aplikasi.

---

## 4. Akun Demo

| Peran | Email | Kata sandi | Keterangan |
|---|---|---|---|
| Admin | `admin@pesanyuk.id` | `admin123` | Mengelola pengguna, UMKM, dan transaksi |
| Penjual | `warung@pesanyuk.id` | `penjual123` | Warung Bu Sari (terverifikasi) |
| Penjual | `kopi@pesanyuk.id` | `penjual123` | Kedai Kopi Senja (terverifikasi) |
| Penjual | `rina@pesanyuk.id` | `penjual123` | Dapur Rina (menunggu verifikasi) |
| Pelanggan | `pelanggan@pesanyuk.id` | `pelanggan123` | Muhammad Fadhlan Ikhsani |
| Pelanggan | `nadia@pesanyuk.id` | `pelanggan123` | Nadia Putri |

Akun penjual **rina@pesanyuk.id** sengaja berstatus *PENDING* agar alur verifikasi admin dapat dicoba: login admin → menu **Kelola UMKM** → pilih Dapur Rina → **Verifikasi**.

---

## 5. Alur Penggunaan Singkat

### Skenario pelanggan
1. Login sebagai `pelanggan@pesanyuk.id`.
2. Buka **Katalog Menu**, cari menu (misalnya "nasi"), lalu tambahkan ke keranjang.
3. Buka **Keranjang**, pilih metode pengambilan, isi alamat bila memilih pengantaran.
4. Klik **Buat Pesanan & Bayar**, pilih metode pembayaran simulasi, lalu bayar.
5. Pantau status di **Pesanan Saya**.

### Skenario penjual
1. Login sebagai `warung@pesanyuk.id`.
2. Di **Kelola Menu**, tambah/ubah menu, unggah foto, atur stok dan ketersediaan.
3. Buka **Pesanan Masuk**, pilih pesanan, lalu proses sesuai status.
4. Buka **Laporan Penjualan**, pilih periode, lihat grafik/rekap, lalu ekspor PDF atau CSV.

### Skenario admin
1. Login sebagai `admin@pesanyuk.id`.
2. **Kelola Pengguna**: aktif/nonaktifkan akun atau reset kata sandi.
3. **Kelola UMKM**: verifikasi Dapur Rina atau tangguhkan UMKM lain.
4. **Semua Transaksi**: pantau pembayaran dan rincian seluruh pesanan.
5. **Laporan Penjualan**: lihat performa seluruh UMKM dan ekspor laporan.

---

## 6. Struktur Proyek

```
PesanYukDesktop/
├── pom.xml
├── sql/
│   └── pesanyuk.sql                # skema manual untuk phpMyAdmin
├── src/main/resources/
│   └── db.properties               # konfigurasi koneksi basis data
└── src/main/java/com/pesanyuk/
    ├── App.java                    # titik masuk aplikasi
    ├── db/
    │   ├── Database.java           # koneksi, DDL otomatis, bootstrap
    │   └── DatabaseSeeder.java     # data contoh
    ├── model/                      # record data (Pengguna, Menu, Pesanan, ...)
    ├── dao/                        # akses JDBC (Pengguna, Umkm, Menu, Pesanan, ...)
    ├── util/                       # PasswordUtil, Rupiah, Tanggal, Session, ekspor, ...
    └── ui/
        ├── LoginFrame.java         # masuk dan pendaftaran
        ├── MainFrame.java          # kerangka navigasi per peran
        ├── Ui.java                 # tema, komponen, dan tabel
        ├── dialog/PaymentDialog.java
        ├── model/                  # model tabel Swing
        └── panel/                  # katalog, keranjang, pesanan, laporan, admin
```

Folder `uploads/menu/` dibuat otomatis untuk menyimpan foto menu. Foto contoh untuk data demo juga dibuat otomatis di folder tersebut.

---

## 7. Pemetaan Fitur terhadap Dokumen Asli

| Kebutuhan pada dokumen | Implementasi desktop |
|---|---|
| Registrasi & login tiga peran | `LoginFrame`, `PenggunaDao.login`, `PasswordUtil` (PBKDF2) |
| Katalog menu + pencarian/filter | `KatalogPanel`, `MenuDao.katalog` |
| Keranjang belanja | `Cart`, `KeranjangPanel` (satu UMKM per pesanan) |
| Checkout ambil sendiri/pengantaran | `KeranjangPanel`, `PesananDao.buat` |
| Pembayaran online | `PaymentDialog` + `PembayaranDao` (simulasi Midtrans: QRIS, transfer, e-wallet) |
| Status pesanan real-time | Timer 5 detik + `PesananDao.ubahStatus` |
| Kelola menu penjual | `PenjualMenuPanel`, `MenuDao` |
| Kelola pesanan penjual | `PenjualPesananPanel` |
| Laporan penjualan + ekspor | `LaporanPanel`, `LaporanDao`, `PdfExporter`, `CsvExporter` |
| Admin kelola pengguna | `AdminPenggunaPanel` |
| Admin verifikasi UMKM | `AdminUmkmPanel` |
| Admin memantau transaksi | `AdminTransaksiPanel` |

Catatan:
- Laporan hanya menghitung pesanan berstatus **SELESAI**.
- Pembayaran tidak memanggil API Midtrans; dialog pembayaran adalah simulasi agar aplikasi dapat berjalan tanpa kredensial gateway.
- Format ekspor "Excel" berupa CSV dengan pemisah `;` dan BOM UTF-8, sehingga dapat dibuka langsung di Microsoft Excel versi Indonesia.

---

## 8. Troubleshooting

**"Aplikasi tidak dapat terhubung ke basis data"**
- Pastikan MySQL/MariaDB sudah berjalan (XAMPP → Start MySQL).
- Periksa `db.properties` (host, port, user, password).
- Pastikan port 3306 tidak dipakai server lain.

**Menu tidak muncul di katalog**
- Pastikan UMKM berstatus **Terverifikasi**.
- Pastikan menu berstatus **Tersedia** dan stoknya lebih dari 0.

**Login penjual ditolak**
- Akun penjual harus diverifikasi admin terlebih dahulu melalui **Kelola UMKM**.

**Login seolah tidak merespons atau muncul pesan "Uncompilable code"**
- Hentikan aplikasi, klik kanan proyek di NetBeans → **Clean and Build**, lalu **Run** kembali.
- Alternatif dari terminal: `mvn clean package`, lalu `java -jar target/PesanYuk.jar`.
- Pesan tersebut berasal dari hasil kompilasi incremental NetBeans yang menyimpan placeholder error, bukan dari akun atau basis data. Build bersih akan menghilangkannya.

**Laporan kosong**
- Pilih periode yang mencakup tanggal pesanan.
- Hanya pesanan berstatus **Selesai** yang dihitung.

**Gagal menghapus menu**
- Menu yang sudah pernah dipesan tidak dapat dihapus karena terikat riwayat pesanan. Nonaktifkan menu tersebut agar tidak tampil di katalog.
