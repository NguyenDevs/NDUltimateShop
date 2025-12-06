package com.NguyenDevs.nDUltimateShop.commands;

import com.NguyenDevs.nDUltimateShop.NDUltimateShop;
import com.NguyenDevs.nDUltimateShop.models.Coupon;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class CouponCommand implements CommandExecutor {

    private final NDUltimateShop plugin;

    public CouponCommand(NDUltimateShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("player-only"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("ndshop.coupon.use")) {
            player.sendMessage(plugin.getLanguageManager().getMessage("no-permission"));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(plugin.getLanguageManager().getMessage("help-coupon-use"));
            return true;
        }

        String code = args[0];
        Coupon coupon = plugin.getCouponManager().getCoupon(code);

        if (coupon == null) {
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("coupon-not-found"));
            return true;
        }

        if (coupon.isExpired()) {
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("coupon-expired"));
            return true;
        }

        if (!coupon.canUse(player.getUniqueId())) {
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("coupon-already-used"));
            return true;
        }

        if (plugin.getCouponManager().applyCoupon(player.getUniqueId(), code)) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("code", code);
            placeholders.put("discount", String.format("%.0f", coupon.getDiscount()));
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("coupon-applied", placeholders));

            // Show additional info
            if (coupon.getType() == Coupon.CouponType.TIME) {
                Map<String, String> timePlaceholders = new HashMap<>();
                timePlaceholders.put("time", formatTime(coupon.getTimeLeft()));
                player.sendMessage(plugin.getLanguageManager().getMessage("coupon-time-left", timePlaceholders));
            } else {
                Map<String, String> usesPlaceholders = new HashMap<>();
                usesPlaceholders.put("uses", String.valueOf(coupon.getUsesLeft()));
                player.sendMessage(plugin.getLanguageManager().getMessage("coupon-uses-left", usesPlaceholders));
            }
        }

        return true;
    }

    private String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + " ngày";
        } else if (hours > 0) {
            return hours + " giờ";
        } else if (minutes > 0) {
            return minutes + " phút";
        } else {
            return seconds + " giây";
        }
    }
}