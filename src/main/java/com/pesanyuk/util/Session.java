package com.pesanyuk.util;

import com.pesanyuk.model.Pengguna;
import com.pesanyuk.model.Umkm;

/** Menyimpan pengguna dan UMKM yang sedang masuk selama aplikasi berjalan. */
public final class Session {

    private static Pengguna pengguna;
    private static Umkm umkm;
    private static final Cart CART = new Cart();

    private Session() {
    }

    public static Pengguna pengguna() {
        return pengguna;
    }

    public static Umkm umkm() {
        return umkm;
    }

    public static Cart cart() {
        return CART;
    }

    public static void mulai(Pengguna user, Umkm usaha) {
        pengguna = user;
        umkm = usaha;
        CART.bersihkan();
    }

    public static void akhiri() {
        pengguna = null;
        umkm = null;
        CART.bersihkan();
    }

    public static boolean masuk() {
        return pengguna != null;
    }
}
