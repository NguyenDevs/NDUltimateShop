package com.NguyenDevs.nDUltimateShop.commands;

import com.NguyenDevs.nDUltimateShop.NDUltimateShop;
import com.NguyenDevs.nDUltimateShop.models.Coupon;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class AdminCommand implements CommandExecutor {

    private final NDUltimateShop plugin;

    public AdminCommand(NDUltimateShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ndshop.admin")) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("no-permission"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                return handleReload(sender);
            case "shop":
                return handleShop(sender, args);
            case "sell":
                return handleSell(sender, args);
            case "coupon":
                return handleCoupon(sender, args);
            case "blackshop":
                return handleBlackShop(sender, args);
            default:
                sendHelp(sender);
        }

        return true;
    }

    private boolean handleReload(CommandSender sender) {
        plugin.getConfigManager().reloadAllConfigs();
        plugin.getLanguageManager().loadLanguage();
        plugin.getShopManager().loadShops();
        plugin.getAuctionManager().loadAuctions();
        plugin.getSellManager().loadSellPrices();
        plugin.getBlackShopManager().loadBlackShop();
        plugin.getCouponManager().loadCoupons();

        sender.sendMessage(plugin.getLanguageManager().getPrefixedMessage("reload-success"));
        return true;
    }

    private boolean handleShop(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("player-only"));
            return true;
        }

        Player player = (Player) sender;

        // /ndshop shop add <price> [stock]
        if (args.length >= 3 && args[1].equalsIgnoreCase("add")) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item == null || item.getType() == Material.AIR) {
                player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("auction-invalid-price"));
                return true;
            }

            try {
                double price = Double.parseDouble(args[2]);
                int stock = args.length >= 4 ? Integer.parseInt(args[3]) : -1;

                String id = "item_" + System.currentTimeMillis();
                plugin.getShopManager().addShopItem(id, item.clone(), price, stock);

                player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("shop-item-added"));
            } catch (NumberFormatException e) {
                player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("auction-invalid-price"));
            }
            return true;
        }

        // /ndshop shop remove <id>
        if (args.length >= 3 && args[1].equalsIgnoreCase("remove")) {
            plugin.getShopManager().removeShopItem(args[2]);
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("shop-item-removed"));
            return true;
        }

        return true;
    }

    private boolean handleSell(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("player-only"));
            return true;
        }

        Player player = (Player) sender;

        // /ndshop sell setprice <price>
        if (args.length >= 3 && args[1].equalsIgnoreCase("setprice")) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item == null || item.getType() == Material.AIR) {
                player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("auction-invalid-price"));
                return true;
            }

            try {
                double price = Double.parseDouble(args[2]);
                plugin.getSellManager().setCustomItemPrice(item, price);

                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("item", item.getType().name());
                placeholders.put("price", String.format("%.2f", price));
                player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("sell-price-set", placeholders));
            } catch (NumberFormatException e) {
                player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("auction-invalid-price"));
            }
            return true;
        }

        return true;
    }

    private boolean handleCoupon(CommandSender sender, String[] args) {
        // /ndshop coupon create <code> <discount%> <time|uses> <value>
        if (args.length >= 6 && args[1].equalsIgnoreCase("create")) {
            String code = args[2];
            double discount = Double.parseDouble(args[3]);
            String type = args[4].toLowerCase();
            long value;

            Coupon.CouponType couponType;
            if (type.equals("time")) {
                couponType = Coupon.CouponType.TIME;
                // Convert hours to milliseconds
                value = Long.parseLong(args[5]) * 3600 * 1000;
            } else {
                couponType = Coupon.CouponType.USES;
                value = Long.parseLong(args[5]);
            }

            plugin.getCouponManager().createCoupon(code, discount, couponType, value);

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("code", code);
            placeholders.put("discount", String.format("%.0f", discount));
            sender.sendMessage(plugin.getLanguageManager().getPrefixedMessage("coupon-created", placeholders));
            return true;
        }

        // /ndshop coupon remove <code>
        if (args.length >= 3 && args[1].equalsIgnoreCase("remove")) {
            String code = args[2];
            if (plugin.getCouponManager().removeCoupon(code)) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("code", code);
                sender.sendMessage(plugin.getLanguageManager().getPrefixedMessage("coupon-removed", placeholders));
            } else {
                sender.sendMessage(plugin.getLanguageManager().getPrefixedMessage("coupon-not-found"));
            }
            return true;
        }

        return true;
    }

    private boolean handleBlackShop(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("player-only"));
            return true;
        }

        Player player = (Player) sender;

        // /ndshop blackshop add <price> <stock>
        if (args.length >= 4 && args[1].equalsIgnoreCase("add")) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item == null || item.getType() == Material.AIR) {
                player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("auction-invalid-price"));
                return true;
            }

            try {
                double price = Double.parseDouble(args[2]);
                int stock = Integer.parseInt(args[3]);

                String id = "blackitem_" + System.currentTimeMillis();
                plugin.getBlackShopManager().addItem(id, item.clone(), price, stock);

                player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("shop-item-added"));
            } catch (NumberFormatException e) {
                player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("auction-invalid-price"));
            }
            return true;
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(plugin.getLanguageManager().getMessage("help-header"));
        sender.sendMessage(plugin.getLanguageManager().getMessage("help-admin"));
        sender.sendMessage(plugin.getLanguageManager().getMessage("help-admin-coupon"));
        sender.sendMessage(plugin.getLanguageManager().getMessage("help-admin-shop"));
        sender.sendMessage(plugin.getLanguageManager().getMessage("help-admin-sell"));
    }
}