package com.NguyenDevs.nDUltimateShop.gui;

import com.NguyenDevs.nDUltimateShop.NDUltimateShop;
import com.NguyenDevs.nDUltimateShop.managers.GUIConfigManager;
import com.NguyenDevs.nDUltimateShop.models.ShopItem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class BlackShopGUI {

    private final NDUltimateShop plugin;
    private final Player player;
    private Inventory inventory;
    private int currentPage = 0;
    private final GUIConfigManager.GUIConfig config;
    private final List<ShopItem> items;

    public BlackShopGUI(NDUltimateShop plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.config = plugin.getConfigManager().getGUIConfig("blackshop");
        this.items = new ArrayList<>(plugin.getBlackShopManager().getAllItems());
    }

    public void open() {
        Map<String, String> titlePlaceholders = new HashMap<>();
        titlePlaceholders.put("page", String.valueOf(currentPage + 1));

        String title = plugin.getPlaceholderManager().replacePlaceholders(player, config.getTitle(), titlePlaceholders);
        int rows = config.getRows();
        inventory = Bukkit.createInventory(null, rows * 9,
                plugin.getLanguageManager().colorize(title));

        setupGUI();
        player.openInventory(inventory);
    }

    private void setupGUI() {
        List<Integer> itemSlots = config.getItemSlots();
        int startIndex = currentPage * itemSlots.size();
        int endIndex = Math.min(startIndex + itemSlots.size(), items.size());

        for (int i = startIndex; i < endIndex; i++) {
            ShopItem shopItem = items.get(i);
            int slot = itemSlots.get(i - startIndex);
            ItemStack displayItem = createBlackShopDisplay(shopItem);
            inventory.setItem(slot, displayItem);
        }

        Map<String, Integer> slots = config.getSlotMapping();

        if (currentPage > 0 && slots.containsKey("previous")) {
            ItemStack prevButton = config.getDecorativeItem("previous-button");
            inventory.setItem(slots.get("previous"), prevButton);
        }

        if (endIndex < items.size() && slots.containsKey("next")) {
            ItemStack nextButton = config.getDecorativeItem("next-button");
            inventory.setItem(slots.get("next"), nextButton);
        }

        if (slots.containsKey("close")) {
            ItemStack closeButton = config.getDecorativeItem("close-button");
            inventory.setItem(slots.get("close"), closeButton);
        }

        if (slots.containsKey("info")) {
            ItemStack infoItem = createInfoItem();
            inventory.setItem(slots.get("info"), infoItem);
        }

        fillDecorative();
    }

    private ItemStack createBlackShopDisplay(ShopItem shopItem) {
        ItemStack display = shopItem.getItemStack().clone();
        ItemMeta meta = display.getItemMeta();

        if (meta != null) {
            List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("price", String.format("%.2f", shopItem.getPrice()));
            placeholders.put("stock", shopItem.getStock() == -1 ? "∞" : String.valueOf(shopItem.getStock()));

            if (config.getConfig().getBoolean("show-rare-tag", true)) {
                lore.add(plugin.getPlaceholderManager().replacePlaceholders(player,
                        config.getMessage("lore-rare"), placeholders));
            }

            lore.add(plugin.getPlaceholderManager().replacePlaceholders(player,
                    config.getMessage("lore-price"), placeholders));

            if (shopItem.getStock() != -1) {
                lore.add(plugin.getPlaceholderManager().replacePlaceholders(player,
                        config.getMessage("lore-stock"), placeholders));
            }

            lore.add(plugin.getPlaceholderManager().replacePlaceholders(player,
                    config.getMessage("lore-click-buy"), placeholders));

            List<String> coloredLore = new ArrayList<>();
            for (String line : lore) {
                coloredLore.add(plugin.getLanguageManager().colorize(line));
            }
            meta.setLore(coloredLore);
            display.setItemMeta(meta);
        }

        return display;
    }

    private ItemStack createInfoItem() {
        ItemStack infoItem = config.getDecorativeItem("info");
        if (infoItem == null) return null;

        ItemMeta meta = infoItem.getItemMeta();
        if (meta != null && meta.hasLore()) {
            List<String> lore = new ArrayList<>();
            for (String line : meta.getLore()) {
                lore.add(plugin.getPlaceholderManager().replacePlaceholders(player, line));
            }
            meta.setLore(lore);
            infoItem.setItemMeta(meta);
        }

        return infoItem;
    }

    private void fillDecorative() {
        List<Integer> fillerSlots = config.getFillerSlots();
        if (fillerSlots.isEmpty()) return;

        ItemStack filler = new ItemStack(config.getFillerMaterial());
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            filler.setItemMeta(meta);
        }

        for (int slot : fillerSlots) {
            if (inventory.getItem(slot) == null) {
                inventory.setItem(slot, filler);
            }
        }
    }

    public Player getPlayer() {
        return player;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int page) {
        this.currentPage = page;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public ShopItem getShopItemAt(int slot) {
        List<Integer> itemSlots = config.getItemSlots();
        int index = itemSlots.indexOf(slot);
        if (index == -1) return null;

        int actualIndex = currentPage * itemSlots.size() + index;
        if (actualIndex >= items.size()) return null;

        return items.get(actualIndex);
    }
}