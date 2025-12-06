package com.NguyenDevs.nDUltimateShop;

import com.NguyenDevs.nDUltimateShop.commands.*;
import com.NguyenDevs.nDUltimateShop.managers.*;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class NDUltimateShop extends JavaPlugin {

    private Economy economy;
    private ConfigManager configManager;
    private LanguageManager languageManager;
    private ShopManager shopManager;
    private AuctionManager auctionManager;
    private SellManager sellManager;
    private BlackShopManager blackShopManager;
    private CouponManager couponManager;
    private PlaceholderManager placeholderManager;
    private ListenerManager listenerManager;

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            getLogger().severe("Vault không tìm thấy! Plugin sẽ bị vô hiệu hóa.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        configManager = new ConfigManager(this);
        configManager.loadAllConfigs();

        languageManager = new LanguageManager(this);
        languageManager.loadLanguage();

        placeholderManager = new PlaceholderManager(this);

        shopManager = new ShopManager(this);
        shopManager.loadShops();

        auctionManager = new AuctionManager(this);
        auctionManager.loadAuctions();
        auctionManager.startExpirationChecker();

        sellManager = new SellManager(this);
        sellManager.loadSellPrices();

        blackShopManager = new BlackShopManager(this);
        blackShopManager.loadBlackShop();
        blackShopManager.startScheduler();

        couponManager = new CouponManager(this);
        couponManager.loadCoupons();

        listenerManager = new ListenerManager(this);
        listenerManager.registerListeners();

        registerCommands();
        printLogo();
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&',"&7[&dUltimate&5Shop&7] UltimateShop plugin enabled successfully!"));
    }

    @Override
    public void onDisable() {
        if (auctionManager != null) {
            auctionManager.saveAuctions();
        }
        if (shopManager != null) {
            shopManager.saveShops();
        }
        if (blackShopManager != null) {
            blackShopManager.stopScheduler();
            blackShopManager.saveBlackShop();
        }
        if (couponManager != null) {
            couponManager.saveCoupons();
        }
        if (sellManager != null) {
            sellManager.saveSellPrices();
        }

        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&',"&7[&dUltimate&5Shop&7] UltimateShop plugin has been disabled!"));
    }

    private void registerCommands() {
        AdminCommand adminCommand = new AdminCommand(this);
        getCommand("ndshop").setExecutor(adminCommand);
        getCommand("ndshop").setTabCompleter(adminCommand);

        ShopCommand shopCommand = new ShopCommand(this);
        getCommand("shop").setExecutor(shopCommand);
        getCommand("shop").setTabCompleter(shopCommand);

        AuctionCommand auctionCommand = new AuctionCommand(this);
        getCommand("ah").setExecutor(auctionCommand);
        getCommand("ah").setTabCompleter(auctionCommand);

        SellCommand sellCommand = new SellCommand(this);
        getCommand("sell").setExecutor(sellCommand);
        getCommand("sell").setTabCompleter(sellCommand);

        BlackShopCommand blackShopCommand = new BlackShopCommand(this);
        getCommand("blackshop").setExecutor(blackShopCommand);
        getCommand("blackshop").setTabCompleter(blackShopCommand);

        CouponCommand couponCommand = new CouponCommand(this);
        getCommand("coupon").setExecutor(couponCommand);
        getCommand("coupon").setTabCompleter(couponCommand);
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    public Economy getEconomy() {
        return economy;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public ShopManager getShopManager() {
        return shopManager;
    }

    public AuctionManager getAuctionManager() {
        return auctionManager;
    }

    public SellManager getSellManager() {
        return sellManager;
    }

    public BlackShopManager getBlackShopManager() {
        return blackShopManager;
    }

    public CouponManager getCouponManager() {
        return couponManager;
    }

    public PlaceholderManager getPlaceholderManager() {
        return placeholderManager;
    }

    public ListenerManager getListenerManager() {
        return listenerManager;
    }

    public void printLogo() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&d   ██╗   ██╗██╗  ████████╗██╗███╗   ███╗ █████╗ ████████╗███████╗"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&d   ██║   ██║██║  ╚══██╔══╝██║████╗ ████║██╔══██╗╚══██╔══╝██╔════╝"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&d   ██║   ██║██║     ██║   ██║██╔████╔██║███████║   ██║   █████╗  "));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&d   ██║   ██║██║     ██║   ██║██║╚██╔╝██║██╔══██║   ██║   ██╔══╝  "));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&d   ╚██████╔╝███████╗██║   ██║██║ ╚═╝ ██║██║  ██║   ██║   ███████╗"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&d    ╚═════╝ ╚══════╝╚═╝   ╚═╝╚═╝     ╚═╝╚═╝  ╚═╝   ╚═╝   ╚══════╝"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&5   ███████╗██╗  ██╗ ██████╗ ██████╗ "));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&5   ██╔════╝██║  ██║██╔═══██╗██╔══██╗"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&5   ███████╗███████║██║   ██║██████╔╝"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&5   ╚════██║██╔══██║██║   ██║██╔═══╝ "));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&5   ███████║██║  ██║╚██████╔╝██║     "));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&5   ╚══════╝╚═╝  ╚═╝ ╚═════╝ ╚═╝     "));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&d         Ultimate Shop"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&5         Version " + getDescription().getVersion()));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&b         Development by NguyenDevs"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
    }
}