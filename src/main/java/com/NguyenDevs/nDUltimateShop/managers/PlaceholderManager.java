package com.NguyenDevs.nDUltimateShop.managers;

import com.NguyenDevs.nDUltimateShop.NDUltimateShop;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.Map;

public class PlaceholderManager {

    private final NDUltimateShop plugin;
    private final DecimalFormat df = new DecimalFormat("#.##");
    private boolean hasPAPI = false;

    public PlaceholderManager(NDUltimateShop plugin) {
        this.plugin = plugin;
        this.hasPAPI = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
    }

    public String replacePlaceholders(Player player, String text, Map<String, String> customPlaceholders) {
        if (text == null) return "";

        if (customPlaceholders != null) {
            for (Map.Entry<String, String> entry : customPlaceholders.entrySet()) {
                text = text.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }

        text = text.replace("{player}", player.getName());
        text = text.replace("{balance}", df.format(plugin.getEconomy().getBalance(player)));
        text = text.replace("{world}", player.getWorld().getName());

        String activeCoupon = plugin.getCouponManager().getActiveCoupon(player.getUniqueId());
        if (activeCoupon != null) {
            text = text.replace("{coupon}", activeCoupon);
            text = text.replace("{coupon_discount}", String.valueOf((int) plugin.getCouponManager().getCoupon(activeCoupon).getDiscount()));
        } else {
            text = text.replace("{coupon}", "None");
            text = text.replace("{coupon_discount}", "0");
        }

        text = text.replace("{blackshop_status}", plugin.getBlackShopManager().isOpen() ? "Open" : "Closed");
        text = text.replace("{blackshop_time}", plugin.getBlackShopManager().getOpenTimeString());

        if (hasPAPI && player != null) {
            text = PlaceholderAPI.setPlaceholders(player, text);
        }

        return text;
    }

    public String replacePlaceholders(Player player, String text) {
        return replacePlaceholders(player, text, null);
    }

    public boolean hasPlaceholderAPI() {
        return hasPAPI;
    }
}