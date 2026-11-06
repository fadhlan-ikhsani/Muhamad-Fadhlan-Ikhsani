package com.pesanyuk.model;

public record Kategori(int id, String nama) {

    @Override
    public String toString() {
        return nama;
    }
}
