package com.NguyenDevs.nDUltimateShop.commands;

import com.NguyenDevs.nDUltimateShop.NDUltimateShop;
import com.NguyenDevs.nDUltimateShop.models.Coupon;
import com.NguyenDevs.nDUltimateShop.models.ShopItem;
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
            sender.sendMessage(plugin.getLanguageManager().getPrefixedMessage("no-permission"));
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
            case "nightshop":
            case "blackshop":
                return handleNightShop(sender, args);
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
            completions.addAll(Arrays.asList("reload", "shop", "sell", "coupon", "nightshop"));
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
                case "nightshop":
                case "blackshop":
                    completions.addAll(Arrays.asList("add", "remove", "list", "toggle"));
                    break;
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("shop") && args[1].equalsIgnoreCase("remove")) {
                completions.addAll(plugin.getShopManager().getAllShopItems().stream()
                        .map(ShopItem::getId)
                        .collect(Collectors.toList()));
            } else if (args[0].equalsIgnoreCase("nightshop") && args[1].equalsIgnoreCase("remove")) {
                completions.addAll(plugin.getBlackShopManager().getAllItems().stream()
                        .map(ShopItem::getId)
                        .collect(Collectors.toList()));
            } else if (args[0].equalsIgnoreCase("coupon") && args[1].equalsIgnoreCase("remove")) {
                completions.addAll(plugin.getCouponManager().getAllCoupons().stream()
                        .map(Coupon::getCode)
                        .collect(Collectors.toList()));
            } else if ((args[0].equalsIgnoreCase("shop") || args[0].equalsIgnoreCase("nightshop")) && args[1].equalsIgnoreCase("add")) {
                completions.add("<giá tiền>");
            } else if (args[0].equalsIgnoreCase("coupon") && args[1].equalsIgnoreCase("create")) {
                completions.add("<mã giảm giá>");
            }
        } else if (args.length == 4) {
            if ((args[0].equalsIgnoreCase("shop") || args[0].equalsIgnoreCase("nightshop")) && args[1].equalsIgnoreCase("add")) {
                completions.add("<số lượng>");
            } else if (args[0].equalsIgnoreCase("coupon") && args[1].equalsIgnoreCase("create")) {
                completions.add("<% giảm giá>");
            }
        } else if (args.length == 5) {
            if (args[0].equalsIgnoreCase("coupon") && args[1].equalsIgnoreCase("create")) {
                completions.addAll(Arrays.asList("time", "uses"));
            }
        } else if (args.length == 6) {
            if (args[0].equalsIgnoreCase("coupon") && args[1].equalsIgnoreCase("create")) {
                String type = args[4].toLowerCase();
                if (type.equals("time")) {
                    completions.addAll(Arrays.asList("1d", "12h", "30m", "60s", "3600"));
                } else if (type.equals("uses")) {
                    completions.addAll(Arrays.asList("1", "10", "50", "100"));
                } else {
                    completions.add("<giá trị>");
                }
            }
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
        if (args.length >= 2 && args[1].equalsIgnoreCase("list")) {
            sender.sendMessage("§6§lDanh sách vật phẩm Shop:");
            for (ShopItem item : plugin.getShopManager().getAllShopItems()) {
                sender.sendMessage("§eID: §f" + item.getId() + " §7| §eGiá: §a" + item.getPrice() + "$");
            }
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLanguageManager().getPrefixedMessage("player-only"));
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
        if (args.length >= 2 && args[1].equalsIgnoreCase("list")) {
            sender.sendMessage("§6§lDanh sách giá bán (Custom):");
            sender.sendMessage("§7Chức năng list cho Sell đang phát triển (cần iterate hashmap).");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLanguageManager().getPrefixedMessage("player-only"));
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
        if (args.length >= 2 && args[1].equalsIgnoreCase("list")) {
            sender.sendMessage("§6§lDanh sách mã giảm giá:");
            for (Coupon c : plugin.getCouponManager().getAllCoupons()) {
                sender.sendMessage("§eCode: §f" + c.getCode() + " §7| §eGiảm: §a" + c.getDiscount() + "%");
            }
            return true;
        }

        if (args.length >= 6 && args[1].equalsIgnoreCase("create")) {
            String code = args[2];
            double discount;
            try {
                discount = Double.parseDouble(args[3]);
            } catch (NumberFormatException e) {
                sender.sendMessage(plugin.getLanguageManager().getPrefixedMessage("invalid-number"));
                return true;
            }

            String typeStr = args[4].toLowerCase();
            long value;

            Coupon.CouponType couponType;
            if (typeStr.equals("time")) {
                couponType = Coupon.CouponType.TIME;
                try {
                    value = parseDuration(args[5]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(plugin.getLanguageManager().getPrefix() + " §cĐịnh dạng thời gian không hợp lệ! (VD: 1d, 12h, 30m, 60s)");
                    return true;
                }
            } else {
                couponType = Coupon.CouponType.USES;
                try {
                    value = Long.parseLong(args[5]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(plugin.getLanguageManager().getPrefixedMessage("invalid-number"));
                    return true;
                }
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

    private boolean handleNightShop(CommandSender sender, String[] args) {
        if (args.length >= 2 && args[1].equalsIgnoreCase("toggle")) {
            boolean current = plugin.getBlackShopManager().isSystemEnabled();
            boolean newState = !current;
            plugin.getBlackShopManager().setSystemEnabled(newState);
            sender.sendMessage(plugin.getLanguageManager().getPrefix() + " §aĐã " + (newState ? "BẬT" : "TẮT") + " tính năng Chợ Đêm!");
            playSound(sender, Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            return true;
        }

        if (args.length >= 2 && args[1].equalsIgnoreCase("list")) {
            sender.sendMessage("§5§lDanh sách vật phẩm Chợ Đêm:");
            for (ShopItem item : plugin.getBlackShopManager().getAllItems()) {
                sender.sendMessage("§dID: §f" + item.getId() + " §7| §dGiá: §5" + item.getPrice() + "$");
            }
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLanguageManager().getPrefixedMessage("player-only"));
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
                player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("nightshop-item-added", placeholders));
                playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            } catch (NumberFormatException e) {
                player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("invalid-number"));
                playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            }
            return true;
        }

        if (args.length >= 3 && args[1].equalsIgnoreCase("remove")) {
            plugin.getBlackShopManager().removeItem(args[2]);
            sender.sendMessage("§aĐã xóa vật phẩm " + args[2] + " khỏi Chợ Đêm!");
            playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            return true;
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(plugin.getLanguageManager().getMessage("help-header"));
        sender.sendMessage("§6§lAdmin Commands:");
        sender.sendMessage("§e/ndshop reload §7- Tải lại plugin");

        sender.sendMessage(" ");
        sender.sendMessage("§2§lShop Commands:");
        sender.sendMessage("§e/ndshop shop list §7- Xem danh sách shop");
        sender.sendMessage("§e/ndshop shop add <giá> [kho] §7- Thêm vật phẩm");
        sender.sendMessage("§e/ndshop shop remove <id> §7- Xóa vật phẩm");

        sender.sendMessage(" ");
        sender.sendMessage("§9§lSell Commands:");
        sender.sendMessage("§e/ndshop sell list §7- Xem danh sách giá");
        sender.sendMessage("§e/ndshop sell setprice <giá> §7- Đặt giá bán");

        sender.sendMessage(" ");
        sender.sendMessage("§5§lNightShop Commands:");
        sender.sendMessage("§e/ndshop nightshop toggle §7- Bật/Tắt chợ đêm");
        sender.sendMessage("§e/ndshop nightshop list §7- Xem danh sách");
        sender.sendMessage("§e/ndshop nightshop add <giá> <kho> §7- Thêm vật phẩm");
        sender.sendMessage("§e/ndshop nightshop remove <id> §7- Xóa vật phẩm");

        sender.sendMessage(" ");
        sender.sendMessage("§d§lCoupon Commands:");
        sender.sendMessage("§e/ndshop coupon list §7- Xem danh sách");
        sender.sendMessage("§e/ndshop coupon create <code> <giảm%> <time/uses> <giá trị>");
        sender.sendMessage("§e/ndshop coupon remove <code> §7- Xóa mã");

        playSound(sender, Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
    }

    private void playSound(CommandSender sender, Sound sound, float volume, float pitch) {
        if (sender instanceof Player && plugin.getConfig().getBoolean("sounds.enabled", true)) {
            ((Player) sender).playSound(((Player) sender).getLocation(), sound, volume, pitch);
        }
    }

    private long parseDuration(String input) throws NumberFormatException {
        input = input.toLowerCase().trim();
        long multiplier = 1000L;

        String numberPart = input.replaceAll("[^0-9]", "");
        if (numberPart.isEmpty()) throw new NumberFormatException("Invalid format");

        long value = Long.parseLong(numberPart);

        if (input.endsWith("d") || input.endsWith("day") || input.endsWith("days")) {
            multiplier = 86400000L;
        } else if (input.endsWith("h") || input.endsWith("hour") || input.endsWith("hours")) {
            multiplier = 3600000L;
        } else if (input.endsWith("m") || input.endsWith("min") || input.endsWith("mins") || input.endsWith("minute")) {
            multiplier = 60000L;
        } else if (input.endsWith("s") || input.endsWith("sec") || input.endsWith("second")) {
            multiplier = 1000L;
        }

        return value * multiplier;
    }
}