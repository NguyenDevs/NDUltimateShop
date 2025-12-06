package com.NguyenDevs.nDUltimateShop.commands;

import com.NguyenDevs.nDUltimateShop.NDUltimateShop;
import com.NguyenDevs.nDUltimateShop.gui.NightShopGUI;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NightShopCommand implements CommandExecutor, TabCompleter {

    private final NDUltimateShop plugin;

    public NightShopCommand(NDUltimateShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("player-only"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("ndshop.nightshop.use")) {
            player.sendMessage(plugin.getLanguageManager().getMessage("no-permission"));
            playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return true;
        }

        if (!plugin.getBlackShopManager().isOpen()) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("open", String.valueOf(plugin.getConfig().getInt("blackshop.open-time")));
            placeholders.put("close", String.valueOf(plugin.getConfig().getInt("blackshop.close-time")));
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("blackshop-closed"));
            player.sendMessage(plugin.getLanguageManager().getMessage("blackshop-open-time", placeholders));
            playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return true;
        }

        new NightShopGUI(plugin, player).open();
        playSound(player, Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }

    private void playSound(Player player, Sound sound, float volume, float pitch) {
        if (plugin.getConfig().getBoolean("sounds.enabled", true)) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }
}