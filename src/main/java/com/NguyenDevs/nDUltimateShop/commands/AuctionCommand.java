package com.NguyenDevs.nDUltimateShop.commands;

import com.NguyenDevs.nDUltimateShop.NDUltimateShop;
import com.NguyenDevs.nDUltimateShop.gui.AuctionGUI;
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

public class AuctionCommand implements CommandExecutor, TabCompleter {

    private final NDUltimateShop plugin;

    public AuctionCommand(NDUltimateShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("player-only"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("ndshop.auction.use")) {
            player.sendMessage(plugin.getLanguageManager().getMessage("no-permission"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return true;
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("sell")) {
            return handleSell(player, args);
        }

        new AuctionGUI(plugin, player).open();
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("sell");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("sell")) {
            completions.add("<price>");
        }

        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }

    private boolean handleSell(Player player, String[] args) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("hold-item"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return true;
        }

        double price;
        try {
            price = Double.parseDouble(args[1]);
            if (price <= 0) {
                player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("invalid-price"));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
                return true;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("invalid-number"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return true;
        }

        double commissionRate = plugin.getAuctionManager().getCommissionFee();
        double commission = price * (commissionRate / 100.0);

        if (plugin.getEconomy().getBalance(player) < commission) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("amount", String.format("%.2f", commission));
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("not-enough-money", placeholders));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return true;
        }

        String listingId = plugin.getAuctionManager().createListing(player, item.clone(), price);
        if (listingId == null) {
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("auction-max-listings"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return true;
        }

        plugin.getEconomy().withdrawPlayer(player, commission);
        player.getInventory().setItemInMainHand(null);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("item", item.getType().name());
        placeholders.put("price", String.format("%.2f", price));
        placeholders.put("duration", formatDuration(plugin.getConfig().getLong("auction.duration", 86400)));
        player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("auction-item-listed", placeholders));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 2.0f);

        Map<String, String> feePlaceholders = new HashMap<>();
        feePlaceholders.put("fee", String.format("%.2f", commission));
        player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("auction-fee-paid", feePlaceholders));

        return true;
    }

    private String formatDuration(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;

        if (hours > 0) {
            return hours + " " + plugin.getLanguageManager().getMessage("time-hours");
        } else {
            return minutes + " " + plugin.getLanguageManager().getMessage("time-minutes");
        }
    }
}