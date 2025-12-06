package com.NguyenDevs.nDUltimateShop.commands;

import com.NguyenDevs.nDUltimateShop.NDUltimateShop;
import com.NguyenDevs.nDUltimateShop.gui.SellGUI;
import com.NguyenDevs.nDUltimateShop.managers.GUIConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SellCommand implements CommandExecutor, TabCompleter {

    private final NDUltimateShop plugin;

    public SellCommand(NDUltimateShop plugin) {
        this.plugin = plugin;
    }

    private void playConfigSound(Player player, String key) {
        GUIConfigManager.GUIConfig guiConfig = plugin.getConfigManager().getGUIConfig("sell");
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

        if (!player.hasPermission("ndshop.sell.use")) {
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("no-permission"));
            playConfigSound(player, "error");
            return true;
        }

        SellGUI gui = new SellGUI(plugin, player);
        gui.open();
        gui.getConfig().playSound(player, "open");

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}