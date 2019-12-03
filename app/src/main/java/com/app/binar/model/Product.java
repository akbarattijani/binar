package com.app.binar.model;

public class Product {
    private long id;
    private String name;
    private int stock;
    private String pemasok;
    private String tanggal;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getPemasok() {
        return pemasok;
    }

    public void setPemasok(String pemasok) {
        this.pemasok = pemasok;
    }

    public String getTanggal() {
        return tanggal;
    }

    public void setTanggal(String tanggal) {
        this.tanggal = tanggal;
    }
}
