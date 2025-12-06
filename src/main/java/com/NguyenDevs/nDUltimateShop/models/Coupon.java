package com.NguyenDevs.nDUltimateShop.models;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Coupon {

    private final String code;
    private final double discount;
    private final CouponType type;
    private final long value;
    private long createdTime;
    private final Set<UUID> usedBy;

    public enum CouponType {
        TIME,
        USES
    }

    public Coupon(String code, double discount, CouponType type, long value) {
        this.code = code;
        this.discount = discount;
        this.type = type;
        this.value = value;
        this.createdTime = System.currentTimeMillis();
        this.usedBy = new HashSet<>();
    }

    public Coupon(String code, double discount, CouponType type, long value, long createdTime, Set<UUID> usedBy) {
        this.code = code;
        this.discount = discount;
        this.type = type;
        this.value = value;
        this.createdTime = createdTime;
        this.usedBy = usedBy;
    }

    public String getCode() {
        return code;
    }

    public double getDiscount() {
        return discount;
    }

    public CouponType getType() {
        return type;
    }

    public long getValue() {
        return value;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public Set<UUID> getUsedBy() {
        return usedBy;
    }

    public boolean isExpired() {
        if (type == CouponType.TIME) {
            return System.currentTimeMillis() >= (createdTime + value);
        } else {
            return usedBy.size() >= value;
        }
    }

    public boolean canUse(UUID playerUUID) {
        if (isExpired()) return false;

        int maxUsesPerPlayer = 1;
        return !usedBy.contains(playerUUID);
    }

    public void use(UUID playerUUID) {
        usedBy.add(playerUUID);
    }

    public long getTimeLeft() {
        if (type == CouponType.TIME) {
            return Math.max(0, (createdTime + value) - System.currentTimeMillis());
        }
        return 0;
    }

    public int getUsesLeft() {
        if (type == CouponType.USES) {
            return (int) Math.max(0, value - usedBy.size());
        }
        return 0;
    }

    public double applyDiscount(double originalPrice) {
        return originalPrice * (1 - discount / 100.0);
    }
}