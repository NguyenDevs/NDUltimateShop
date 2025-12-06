package com.NguyenDevs.nDUltimateShop.commands;

import com.NguyenDevs.nDUltimateShop.NDUltimateShop;
import com.NguyenDevs.nDUltimateShop.models.Coupon;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class CouponCommand implements CommandExecutor, TabCompleter {

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
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(plugin.getLanguageManager().getMessage("help-coupon-use"));
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            return true;
        }

        String code = args[0];
        Coupon coupon = plugin.getCouponManager().getCoupon(code);

        if (coupon == null) {
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("coupon-not-found"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return true;
        }

        if (coupon.isExpired()) {
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("coupon-expired"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return true;
        }

        if (!coupon.canUse(player.getUniqueId())) {
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("coupon-already-used"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return true;
        }

        if (plugin.getCouponManager().applyCoupon(player.getUniqueId(), code)) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("code", code);
            placeholders.put("discount", String.format("%.0f", coupon.getDiscount()));
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("coupon-applied", placeholders));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 2.0f);

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

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(plugin.getCouponManager().getAllCoupons().stream()
                    .filter(c -> !c.isExpired())
                    .map(Coupon::getCode)
                    .collect(Collectors.toList()));
        }

        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }

    private String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + " " + plugin.getLanguageManager().getMessage("time-days");
        } else if (hours > 0) {
            return hours + " " + plugin.getLanguageManager().getMessage("time-hours");
        } else if (minutes > 0) {
            return minutes + " " + plugin.getLanguageManager().getMessage("time-minutes");
        } else {
            return seconds + " " + plugin.getLanguageManager().getMessage("time-seconds");
        }
    }
}