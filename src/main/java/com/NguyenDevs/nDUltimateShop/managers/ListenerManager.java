package com.NguyenDevs.nDUltimateShop.managers;

import com.NguyenDevs.nDUltimateShop.NDUltimateShop;
import com.NguyenDevs.nDUltimateShop.gui.*;
import com.NguyenDevs.nDUltimateShop.listeners.*;
import org.bukkit.entity.Player;

public class ListenerManager {

    private final NDUltimateShop plugin;
    private ShopListener shopListener;
    private AuctionListener auctionListener;
    private SellListener sellListener;
    private BlackShopListener blackShopListener;

    public ListenerManager(NDUltimateShop plugin) {
        this.plugin = plugin;
    }

    public void registerListeners() {
        shopListener = new ShopListener(plugin);
        auctionListener = new AuctionListener(plugin);
        sellListener = new SellListener(plugin);
        blackShopListener = new BlackShopListener(plugin);

        plugin.getServer().getPluginManager().registerEvents(shopListener, plugin);
        plugin.getServer().getPluginManager().registerEvents(auctionListener, plugin);
        plugin.getServer().getPluginManager().registerEvents(sellListener, plugin);
        plugin.getServer().getPluginManager().registerEvents(blackShopListener, plugin);
    }

    public void openShopGUI(Player player) {
        ShopGUI gui = new ShopGUI(plugin, player);
        shopListener.registerGUI(player, gui);
        gui.open();
    }

    public void openAuctionGUI(Player player) {
        AuctionGUI gui = new AuctionGUI(plugin, player);
        auctionListener.registerGUI(player, gui);
        gui.open();
    }

    public void openSellGUI(Player player) {
        SellGUI gui = new SellGUI(plugin, player);
        sellListener.registerGUI(player, gui);
        gui.open();
    }

    public void openBlackShopGUI(Player player) {
        BlackShopGUI gui = new BlackShopGUI(plugin, player);
        blackShopListener.registerGUI(player, gui);
        gui.open();
    }

    public void unregisterGUI(Player player) {
        shopListener.unregisterGUI(player);
        auctionListener.unregisterGUI(player);
        sellListener.unregisterGUI(player);
        blackShopListener.unregisterGUI(player);
    }
}