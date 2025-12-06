package com.NguyenDevs.nDUltimateShop.models;

import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class AuctionListing {

    private final String id;
    private final UUID sellerUUID;
    private final String sellerName;
    private final ItemStack itemStack;
    private double price;
    private final long expirationTime;

    public AuctionListing(String id, UUID sellerUUID, String sellerName, ItemStack itemStack, double price, long expirationTime) {
        this.id = id;
        this.sellerUUID = sellerUUID;
        this.sellerName = sellerName;
        this.itemStack = itemStack;
        this.price = price;
        this.expirationTime = expirationTime;
    }

    public String getId() {
        return id;
    }

    public UUID getSellerUUID() {
        return sellerUUID;
    }

    public String getSellerName() {
        return sellerName;
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

    public long getExpirationTime() {
        return expirationTime;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() >= expirationTime;
    }

    public long getTimeLeft() {
        return Math.max(0, expirationTime - System.currentTimeMillis());
    }
}