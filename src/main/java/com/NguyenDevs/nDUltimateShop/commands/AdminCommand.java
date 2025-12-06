package com.NguyenDevs.nDUltimateShop.commands;

import com.NguyenDevs.nDUltimateShop.NDUltimateShop;
import com.NguyenDevs.nDUltimateShop.models.Coupon;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class AdminCommand implements CommandExecutor, TabCompleter {

    private final NDUltimateShop plugin;

    public AdminCommand(NDUltimateShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ndshop.admin")) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("no-permission"));
            playSound(sender, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
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

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (!sender.hasPermission("ndshop.admin")) return completions;

        if (args.length == 1) {
            completions.addAll(Arrays.asList("reload", "shop", "sell", "coupon", "blackshop"));
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "shop":
                    completions.addAll(Arrays.asList("add", "remove", "list", "setprice"));
                    break;
                case "sell":
                    completions.addAll(Arrays.asList("setprice", "removeprice", "list"));
                    break;
                case "coupon":
                    completions.addAll(Arrays.asList("create", "remove", "list"));
                    break;
                case "blackshop":
                    completions.addAll(Arrays.asList("add", "remove", "list", "toggle"));
                    break;
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("shop") && args[1].equalsIgnoreCase("remove")) {
                completions.addAll(plugin.getShopManager().getAllShopItems().stream()
                        .map(item -> item.getId())
                        .collect(Collectors.toList()));
            } else if (args[0].equalsIgnoreCase("coupon") && args[1].equalsIgnoreCase("remove")) {
                completions.addAll(plugin.getCouponManager().getAllCoupons().stream()
                        .map(Coupon::getCode)
                        .collect(Collectors.toList()));
            } else if ((args[0].equalsIgnoreCase("shop") || args[0].equalsIgnoreCase("blackshop")) && args[1].equalsIgnoreCase("add")) {
                completions.add("<price>");
            }
        } else if (args.length == 4) {
            if ((args[0].equalsIgnoreCase("shop") || args[0].equalsIgnoreCase("blackshop")) && args[1].equalsIgnoreCase("add")) {
                completions.add("<stock>");
            }
        } else if (args.length == 5 && args[0].equalsIgnoreCase("coupon") && args[1].equalsIgnoreCase("create")) {
            completions.addAll(Arrays.asList("time", "uses"));
        }

        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
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
        playSound(sender, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 2.0f);
        return true;
    }

    private boolean handleShop(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("player-only"));
            return true;
        }

        Player player = (Player) sender;

        if (args.length >= 3 && args[1].equalsIgnoreCase("add")) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item == null || item.getType() == Material.AIR) {
                player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("hold-item"));
                playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
                return true;
            }

            try {
                double price = Double.parseDouble(args[2]);
                int stock = args.length >= 4 ? Integer.parseInt(args[3]) : -1;

                String id = "item_" + System.currentTimeMillis();
                plugin.getShopManager().addShopItem(id, item.clone(), price, stock);

                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("item", item.getType().name());
                placeholders.put("price", String.format("%.2f", price));
                placeholders.put("stock", String.valueOf(stock));
                player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("shop-item-added", placeholders));
                playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            } catch (NumberFormatException e) {
                player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("invalid-number"));
                playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            }
            return true;
        }

        if (args.length >= 3 && args[1].equalsIgnoreCase("remove")) {
            plugin.getShopManager().removeShopItem(args[2]);
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("id", args[2]);
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("shop-item-removed", placeholders));
            playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
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

        if (args.length >= 3 && args[1].equalsIgnoreCase("setprice")) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item == null || item.getType() == Material.AIR) {
                player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("hold-item"));
                playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
                return true;
            }

            try {
                double price = Double.parseDouble(args[2]);
                plugin.getSellManager().setCustomItemPrice(item, price);

                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("item", item.getType().name());
                placeholders.put("price", String.format("%.2f", price));
                player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("sell-price-set", placeholders));
                playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            } catch (NumberFormatException e) {
                player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("invalid-number"));
                playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            }
            return true;
        }

        return true;
    }

    private boolean handleCoupon(CommandSender sender, String[] args) {
        if (args.length >= 6 && args[1].equalsIgnoreCase("create")) {
            String code = args[2];
            double discount = Double.parseDouble(args[3]);
            String type = args[4].toLowerCase();
            long value;

            Coupon.CouponType couponType;
            if (type.equals("time")) {
                couponType = Coupon.CouponType.TIME;
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
            playSound(sender, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 2.0f);
            return true;
        }

        if (args.length >= 3 && args[1].equalsIgnoreCase("remove")) {
            String code = args[2];
            if (plugin.getCouponManager().removeCoupon(code)) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("code", code);
                sender.sendMessage(plugin.getLanguageManager().getPrefixedMessage("coupon-removed", placeholders));
                playSound(sender, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            } else {
                sender.sendMessage(plugin.getLanguageManager().getPrefixedMessage("coupon-not-found"));
                playSound(sender, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
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

        if (args.length >= 4 && args[1].equalsIgnoreCase("add")) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item == null || item.getType() == Material.AIR) {
                player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("hold-item"));
                playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
                return true;
            }

            try {
                double price = Double.parseDouble(args[2]);
                int stock = Integer.parseInt(args[3]);

                String id = "blackitem_" + System.currentTimeMillis();
                plugin.getBlackShopManager().addItem(id, item.clone(), price, stock);

                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("item", item.getType().name());
                placeholders.put("price", String.format("%.2f", price));
                placeholders.put("stock", String.valueOf(stock));
                player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("blackshop-item-added", placeholders));
                playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            } catch (NumberFormatException e) {
                player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("invalid-number"));
                playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
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
        playSound(sender, Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
    }

    private void playSound(CommandSender sender, Sound sound, float volume, float pitch) {
        if (sender instanceof Player && plugin.getConfig().getBoolean("sounds.enabled", true)) {
            ((Player) sender).playSound(((Player) sender).getLocation(), sound, volume, pitch);
        }
    }
}