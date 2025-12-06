package com.NguyenDevs.nDUltimateShop.models;

import org.bukkit.inventory.ItemStack;

public class ShopItem {

    private final String id;
    private final ItemStack itemStack;
    private double price;
    private int stock; // -1 = unlimited

    public ShopItem(String id, ItemStack itemStack, double price, int stock) {
        this.id = id;
        this.itemStack = itemStack;
        this.price = price;
        this.stock = stock;
    }

    public String getId() {
        return id;
    }

    public ItemStack getItemStack() {
        return itemStack.clone();
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public boolean hasStock() {
        return stock == -1 || stock > 0;
    }

    public boolean decreaseStock(int amount) {
        if (stock == -1) return true;
        if (stock >= amount) {
            stock -= amount;
            return true;
        }
        return false;
    }

    public void increaseStock(int amount) {
        if (stock != -1) {
            stock += amount;
        }
    }
}