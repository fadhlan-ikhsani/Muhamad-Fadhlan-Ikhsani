package com.pesanyuk.util;

import com.pesanyuk.model.Menu;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Keranjang belanja di memori. Satu keranjang hanya boleh berisi menu dari
 * satu UMKM agar setiap pesanan dapat diproses oleh satu penjual.
 */
public final class Cart {

    public static final class Item {
        private final Menu menu;
        private int jumlah;

        Item(Menu menu, int jumlah) {
            this.menu = menu;
            this.jumlah = jumlah;
        }

        public Menu menu() {
            return menu;
        }

        public int jumlah() {
            return jumlah;
        }

        public double subtotal() {
            return menu.harga() * jumlah;
        }
    }

    private final Map<Integer, Item> items = new LinkedHashMap<>();
    private Integer umkmId;
    private String umkmNama;

    public Integer umkmId() {
        return umkmId;
    }

    public String umkmNama() {
        return umkmNama;
    }

    public List<Item> items() {
        return new ArrayList<>(items.values());
    }

    public Item item(int menuId) {
        return items.get(menuId);
    }

    public boolean kosong() {
        return items.isEmpty();
    }

    public int jumlahItem() {
        return items.values().stream().mapToInt(Item::jumlah).sum();
    }

    public double total() {
        return items.values().stream().mapToDouble(Item::subtotal).sum();
    }

    /**
     * Menambahkan menu. Jika keranjang berisi UMKM lain, pemanggil harus
     * membersihkan keranjang terlebih dahulu (lihat {@link #bersihkan()}).
     */
    public void tambah(Menu menu, int jumlah) {
        if (jumlah <= 0) {
            throw new IllegalArgumentException("Jumlah harus lebih dari 0.");
        }
        if (umkmId != null && umkmId != menu.umkmId()) {
            throw new IllegalStateException(
                    "Keranjang hanya boleh berisi menu dari satu UMKM.");
        }
        if (!menu.bisaDipesan()) {
            throw new IllegalArgumentException("Menu sedang tidak tersedia.");
        }
        Item lama = items.get(menu.id());
        int totalBaru = (lama == null ? 0 : lama.jumlah()) + jumlah;
        if (totalBaru > menu.stok()) {
            throw new IllegalArgumentException(
                    "Stok " + menu.nama() + " hanya " + menu.stok() + " porsi.");
        }
        if (lama == null) {
            items.put(menu.id(), new Item(menu, jumlah));
        } else {
            items.put(menu.id(), new Item(menu, totalBaru));
        }
        umkmId = menu.umkmId();
        umkmNama = menu.umkmNama();
    }

    public void ubahJumlah(int menuId, int jumlah) {
        Item item = items.get(menuId);
        if (item == null) {
            return;
        }
        if (jumlah <= 0) {
            hapus(menuId);
            return;
        }
        if (jumlah > item.menu().stok()) {
            throw new IllegalArgumentException(
                    "Stok " + item.menu().nama() + " hanya " + item.menu().stok() + " porsi.");
        }
        items.put(menuId, new Item(item.menu(), jumlah));
    }

    public void hapus(int menuId) {
        items.remove(menuId);
        if (items.isEmpty()) {
            bersihkan();
        }
    }

    public void bersihkan() {
        items.clear();
        umkmId = null;
        umkmNama = null;
    }
}
