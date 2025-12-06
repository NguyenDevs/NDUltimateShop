package com.NguyenDevs.nDUltimateShop;

import com.NguyenDevs.nDUltimateShop.commands.*;
import com.NguyenDevs.nDUltimateShop.listeners.*;
import com.NguyenDevs.nDUltimateShop.managers.*;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class NDUltimateShop extends JavaPlugin {

    private static NDUltimateShop instance;
    private Economy economy;
    private ConfigManager configManager;
    private LanguageManager languageManager;
    private ShopManager shopManager;
    private AuctionManager auctionManager;
    private SellManager sellManager;
    private CouponManager couponManager;
    private BlackShopManager blackShopManager;

    @Override
    public void onEnable() {
        instance = this;

        // Setup Vault Economy
        if (!setupEconomy()) {
            getLogger().severe("Vault không tìm thấy! Plugin bị vô hiệu hóa.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize managers
        configManager = new ConfigManager(this);
        languageManager = new LanguageManager(this);
        shopManager = new ShopManager(this);
        auctionManager = new AuctionManager(this);
        sellManager = new SellManager(this);
        couponManager = new CouponManager(this);
        blackShopManager = new BlackShopManager(this);

        // Load configs
        configManager.loadAllConfigs();
        languageManager.loadLanguage();
        shopManager.loadShops();
        auctionManager.loadAuctions();
        sellManager.loadSellPrices();
        blackShopManager.loadBlackShop();

        // Register commands
        registerCommands();

        // Register listeners
        registerListeners();

        // Start schedulers
        blackShopManager.startScheduler();
        auctionManager.startExpirationChecker();

        getLogger().info("NDUltimateShop đã được kích hoạt thành công!");
    }

    @Override
    public void onDisable() {
        if (auctionManager != null) {
            auctionManager.saveAuctions();
        }
        if (blackShopManager != null) {
            blackShopManager.stopScheduler();
        }
        getLogger().info("NDUltimateShop đã được vô hiệu hóa!");
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

    private void registerCommands() {
        getCommand("shop").setExecutor(new ShopCommand(this));
        getCommand("auction").setExecutor(new AuctionCommand(this));
        getCommand("ah").setExecutor(new AuctionCommand(this));
        getCommand("sell").setExecutor(new SellCommand(this));
        getCommand("coupon").setExecutor(new CouponCommand(this));
        getCommand("blackshop").setExecutor(new BlackShopCommand(this));
        getCommand("ndshop").setExecutor(new AdminCommand(this));
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new ShopListener(this), this);
        getServer().getPluginManager().registerEvents(new AuctionListener(this), this);
        getServer().getPluginManager().registerEvents(new SellListener(this), this);
    }

    // Getters
    public static NDUltimateShop getInstance() {
        return instance;
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

    public CouponManager getCouponManager() {
        return couponManager;
    }

    public BlackShopManager getBlackShopManager() {
        return blackShopManager;
    }
}