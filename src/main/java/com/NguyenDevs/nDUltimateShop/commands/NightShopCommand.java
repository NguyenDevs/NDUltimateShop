package com.NguyenDevs.nDUltimateShop.commands;

import com.NguyenDevs.nDUltimateShop.NDUltimateShop;
import com.NguyenDevs.nDUltimateShop.gui.NightShopGUI;
import com.NguyenDevs.nDUltimateShop.managers.GUIConfigManager;
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

    private void playConfigSound(Player player, String key) {
        GUIConfigManager.GUIConfig guiConfig = plugin.getConfigManager().getGUIConfig("nightshop");
        if (guiConfig != null) {
            guiConfig.playSound(player, key);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLanguageManager().getPrefixedMessage("player-only"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("ndshop.nightshop.use")) {
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("no-permission"));
            playConfigSound(player, "error");
            return true;
        }

        if (!plugin.getBlackShopManager().isOpen()) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("open", String.valueOf(plugin.getConfig().getInt("nightshop.open-time")));
            placeholders.put("close", String.valueOf(plugin.getConfig().getInt("nightshop.close-time")));
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("nightshop-closed"));
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("nightshop-open-time", placeholders));
            playConfigSound(player, "error");
            return true;
        }

        NightShopGUI gui = new NightShopGUI(plugin, player);
        gui.open();
        gui.getConfig().playSound(player, "open");

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}