package com.NguyenDevs.nDUltimateShop.commands;

import com.NguyenDevs.nDUltimateShop.NDUltimateShop;
import com.NguyenDevs.nDUltimateShop.gui.AuctionGUI;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class AuctionCommand implements CommandExecutor {

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
            return true;
        }

        // /ah sell <price>
        if (args.length >= 2 && args[0].equalsIgnoreCase("sell")) {
            return handleSell(player, args);
        }

        // /ah - open GUI
        new AuctionGUI(plugin, player).open();
        player.sendMessage(plugin.getLanguageManager().getMessage("auction-opened"));

        return true;
    }

    private boolean handleSell(Player player, String[] args) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("auction-invalid-price"));
            return true;
        }

        double price;
        try {
            price = Double.parseDouble(args[1]);
            if (price <= 0) {
                player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("auction-invalid-price"));
                return true;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("auction-invalid-price"));
            return true;
        }

        // Calculate commission fee
        double commissionRate = plugin.getAuctionManager().getCommissionFee();
        double commission = price * (commissionRate / 100.0);

        if (plugin.getEconomy().getBalance(player) < commission) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("amount", String.format("%.2f", commission));
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("not-enough-money", placeholders));
            return true;
        }

        // Create listing
        String listingId = plugin.getAuctionManager().createListing(player, item.clone(), price);
        if (listingId == null) {
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("auction-max-listings"));
            return true;
        }

        // Charge commission
        plugin.getEconomy().withdrawPlayer(player, commission);

        // Remove item from hand
        player.getInventory().setItemInMainHand(null);

        // Send messages
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("item", item.getType().name());
        placeholders.put("price", String.format("%.2f", price));
        placeholders.put("duration", formatDuration(plugin.getConfig().getLong("auction.duration", 86400)));
        player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("auction-item-listed", placeholders));

        Map<String, String> feePlaceholders = new HashMap<>();
        feePlaceholders.put("fee", String.format("%.2f", commission));
        player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("auction-fee-paid", feePlaceholders));

        return true;
    }

    private String formatDuration(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;

        if (hours > 0) {
            return hours + " giờ";
        } else {
            return minutes + " phút";
        }
    }
}