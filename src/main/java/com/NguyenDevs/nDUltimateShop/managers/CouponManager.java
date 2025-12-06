package com.NguyenDevs.nDUltimateShop.managers;

import com.NguyenDevs.nDUltimateShop.NDUltimateShop;
import com.NguyenDevs.nDUltimateShop.models.Coupon;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class CouponManager {

    private final NDUltimateShop plugin;
    private final Map<String, Coupon> coupons;
    private final Map<UUID, String> activeCoupons; // player -> coupon code

    public CouponManager(NDUltimateShop plugin) {
        this.plugin = plugin;
        this.coupons = new HashMap<>();
        this.activeCoupons = new HashMap<>();
        loadCoupons();
    }

    public void loadCoupons() {
        coupons.clear();
        FileConfiguration config = plugin.getConfigManager().getConfig("coupons.yml");

        ConfigurationSection couponsSection = config.getConfigurationSection("coupons");
        if (couponsSection != null) {
            for (String code : couponsSection.getKeys(false)) {
                double discount = couponsSection.getDouble(code + ".discount");
                String typeStr = couponsSection.getString(code + ".type");
                long value = couponsSection.getLong(code + ".value");
                long createdTime = couponsSection.getLong(code + ".created");

                Set<UUID> usedBy = new HashSet<>();
                List<String> usedByList = couponsSection.getStringList(code + ".usedBy");
                for (String uuidStr : usedByList) {
                    usedBy.add(UUID.fromString(uuidStr));
                }

                Coupon.CouponType type = Coupon.CouponType.valueOf(typeStr);
                Coupon coupon = new Coupon(code, discount, type, value, createdTime, usedBy);

                if (!coupon.isExpired()) {
                    coupons.put(code.toLowerCase(), coupon);
                }
            }
        }

        plugin.getLogger().info("Đã tải " + coupons.size() + " mã giảm giá!");
    }

    public void saveCoupons() {
        FileConfiguration config = plugin.getConfigManager().getConfig("coupons.yml");
        config.set("coupons", null);

        for (Coupon coupon : coupons.values()) {
            String path = "coupons." + coupon.getCode();
            config.set(path + ".discount", coupon.getDiscount());
            config.set(path + ".type", coupon.getType().name());
            config.set(path + ".value", coupon.getValue());
            config.set(path + ".created", coupon.getCreatedTime());

            List<String> usedByList = new ArrayList<>();
            for (UUID uuid : coupon.getUsedBy()) {
                usedByList.add(uuid.toString());
            }
            config.set(path + ".usedBy", usedByList);
        }

        plugin.getConfigManager().saveConfig("coupons.yml");
    }

    public void createCoupon(String code, double discount, Coupon.CouponType type, long value) {
        Coupon coupon = new Coupon(code, discount, type, value);
        coupons.put(code.toLowerCase(), coupon);
        saveCoupons();
    }

    public boolean removeCoupon(String code) {
        if (coupons.remove(code.toLowerCase()) != null) {
            saveCoupons();
            return true;
        }
        return false;
    }

    public Coupon getCoupon(String code) {
        return coupons.get(code.toLowerCase());
    }

    public boolean applyCoupon(UUID playerUUID, String code) {
        Coupon coupon = getCoupon(code);
        if (coupon == null) return false;
        if (!coupon.canUse(playerUUID)) return false;

        activeCoupons.put(playerUUID, code.toLowerCase());
        coupon.use(playerUUID);
        saveCoupons();
        return true;
    }

    public void removeCouponFromPlayer(UUID playerUUID) {
        activeCoupons.remove(playerUUID);
    }

    public String getActiveCoupon(UUID playerUUID) {
        return activeCoupons.get(playerUUID);
    }

    public double getDiscountedPrice(UUID playerUUID, double originalPrice) {
        String couponCode = activeCoupons.get(playerUUID);
        if (couponCode == null) return originalPrice;

        Coupon coupon = getCoupon(couponCode);
        if (coupon == null || coupon.isExpired()) {
            activeCoupons.remove(playerUUID);
            return originalPrice;
        }

        return coupon.applyDiscount(originalPrice);
    }

    public Collection<Coupon> getAllCoupons() {
        return coupons.values();
    }
}