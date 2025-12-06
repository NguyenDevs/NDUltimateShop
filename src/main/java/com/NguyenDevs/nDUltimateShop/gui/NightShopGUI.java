package com.NguyenDevs.nDUltimateShop.gui;

import com.NguyenDevs.nDUltimateShop.NDUltimateShop;
import com.NguyenDevs.nDUltimateShop.models.ShopItem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NightShopGUI extends BaseGUI {

    private List<ShopItem> items;

    public NightShopGUI(NDUltimateShop plugin, Player player) {
        super(plugin, player, "nightshop");
        this.items = new ArrayList<>();
    }

    @Override
    public void open() {
        loadItems();
        // Sắp xếp items
        sortItems(items);

        Map<String, String> ph = new HashMap<>();
        ph.put("page", String.valueOf(currentPage + 1));
        String title = plugin.getPlaceholderManager().replacePlaceholders(player, config.getTitle(), ph);

        inventory = Bukkit.createInventory(this, config.getRows() * 9, plugin.getLanguageManager().colorize(title));

        setupGUI();
        player.openInventory(inventory);
    }

    private void loadItems() {
        boolean hideOutOfStock = config.getConfig().getBoolean("hide-out-of-stock", true);
        this.items = plugin.getBlackShopManager().getAllItems().stream()
                .filter(item -> !hideOutOfStock || item.hasStock())
                .collect(Collectors.toList());
    }

    private void setupGUI() {
        List<Integer> itemSlots = config.getItemSlots();
        int startIndex = currentPage * itemSlots.size();
        int endIndex = Math.min(startIndex + itemSlots.size(), items.size());

        for (int i = startIndex; i < endIndex; i++) {
            ShopItem shopItem = items.get(i);
            int slot = itemSlots.get(i - startIndex);
            inventory.setItem(slot, createBlackShopDisplay(shopItem));
        }

        Map<String, Integer> slots = config.getSlotMapping();

        ItemStack filler = new ItemStack(config.getFillerMaterial());
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.setDisplayName(" ");
            filler.setItemMeta(fillerMeta);
        }

        if (slots.containsKey("previous")) {
            int prevSlot = slots.get("previous");
            if (currentPage > 0) {
                inventory.setItem(prevSlot, config.getDecorativeItem("previous-button"));
            } else {
                inventory.setItem(prevSlot, filler);
            }
        }

        if (slots.containsKey("next")) {
            int nextSlot = slots.get("next");
            int maxItemsPerPage = itemSlots.size();
            if ((currentPage + 1) * maxItemsPerPage < items.size()) {
                inventory.setItem(nextSlot, config.getDecorativeItem("next-button"));
            } else {
                inventory.setItem(nextSlot, filler);
            }
        }

        if (slots.containsKey("close")) {
            inventory.setItem(slots.get("close"), config.getDecorativeItem("close-button"));
        }
        if (slots.containsKey("info")) {
            ItemStack info = config.getDecorativeItem("info");
            processDecorativePlaceholders(info);
            inventory.setItem(slots.get("info"), info);
        }

        // Add Sort Button
        inventory.setItem(getSortSlot(), getSortButton());

        fillDecorative();
    }

    private void processDecorativePlaceholders(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta.hasDisplayName()) {
            meta.setDisplayName(plugin.getLanguageManager().colorize(
                    plugin.getPlaceholderManager().replacePlaceholders(player, meta.getDisplayName())));
        }
        if (meta.hasLore()) {
            List<String> lore = new ArrayList<>();
            for (String line : meta.getLore()) {
                lore.add(plugin.getLanguageManager().colorize(
                        plugin.getPlaceholderManager().replacePlaceholders(player, line)));
            }
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
    }

    private ItemStack createBlackShopDisplay(ShopItem shopItem) {
        ItemStack display = shopItem.getItemStack().clone();
        ItemMeta meta = display.getItemMeta();
        if (meta == null) return display;

        Map<String, String> ph = new HashMap<>();
        ph.put("price", String.format("%,.2f", shopItem.getPrice()));
        ph.put("stock", shopItem.getStock() == -1 ? "∞" : String.valueOf(shopItem.getStock()));
        ph.put("rare_tag", config.getConfig().getBoolean("show-rare-tag", true) ? config.getMessage("lore-rare") : "");

        List<String> configLore = config.getLoreFormat();
        if (configLore != null && !configLore.isEmpty()) {
            List<String> finalLore = new ArrayList<>();
            for (String line : configLore) {
                if (line.contains("%lore%")) {
                    // Logic fix: Chỉ thêm nếu item gốc có lore, tránh lỗi hiển thị
                    if (meta.hasLore()) {
                        for (String originalLine : meta.getLore()) {
                            finalLore.add(originalLine);
                        }
                    }
                } else {
                    String processed = plugin.getLanguageManager().colorize(
                            plugin.getPlaceholderManager().replacePlaceholders(player, line, ph));
                    // Chỉ add nếu dòng không rỗng để tránh khoảng trống thừa,
                    // nhưng nếu config cố tình để trống "" thì vẫn giữ.
                    finalLore.add(processed);
                }
            }
            meta.setLore(finalLore);
        }
        display.setItemMeta(meta);
        return display;
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