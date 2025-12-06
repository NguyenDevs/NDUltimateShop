package com.NguyenDevs.nDUltimateShop.managers;

import com.NguyenDevs.nDUltimateShop.NDUltimateShop;
import com.NguyenDevs.nDUltimateShop.gui.AuctionGUI;
import com.NguyenDevs.nDUltimateShop.gui.NightShopGUI;
import com.NguyenDevs.nDUltimateShop.gui.SellGUI;
import com.NguyenDevs.nDUltimateShop.gui.ShopGUI;
import com.NguyenDevs.nDUltimateShop.listeners.AuctionListener;
import com.NguyenDevs.nDUltimateShop.listeners.NightShopListener;
import com.NguyenDevs.nDUltimateShop.listeners.SellListener;
import com.NguyenDevs.nDUltimateShop.listeners.ShopListener;
import org.bukkit.entity.Player;

public class ListenerManager {

    private final NDUltimateShop plugin;

    public ListenerManager(NDUltimateShop plugin) {
        this.plugin = plugin;
    }

    public void registerListeners() {
        plugin.getServer().getPluginManager().registerEvents(new ShopListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new AuctionListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new SellListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new NightShopListener(plugin), plugin);
    }

    public void openShopGUI(Player player) {
        new ShopGUI(plugin, player).open();
    }

    public void openAuctionGUI(Player player) {
        new AuctionGUI(plugin, player).open();
    }

    public void openSellGUI(Player player) {
        new SellGUI(plugin, player).open();
    }

    public void openBlackShopGUI(Player player) {
        new NightShopGUI(plugin, player).open();
    }
}