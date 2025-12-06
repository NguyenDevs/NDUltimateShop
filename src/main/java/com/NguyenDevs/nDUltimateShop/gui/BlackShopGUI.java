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

public class BlackShopGUI extends BaseGUI {

    private List<ShopItem> items; // Không để final để có thể refresh

    public BlackShopGUI(NDUltimateShop plugin, Player player) {
        super(plugin, player, "blackshop");
        this.items = new ArrayList<>(); // Khởi tạo list rỗng
    }

    @Override
    public void open() {
        // REFRESH DATA: Tải lại danh sách item mỗi khi mở GUI
        loadItems();

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

        // Logic mũi tên trang trước: Chỉ hiện khi không phải trang 1
        if (currentPage > 0 && slots.containsKey("previous")) {
            inventory.setItem(slots.get("previous"), config.getDecorativeItem("previous-button"));
        }

        // Logic mũi tên trang sau: Chỉ hiện khi còn item phía sau
        if (endIndex < items.size() && slots.containsKey("next")) {
            inventory.setItem(slots.get("next"), config.getDecorativeItem("next-button"));
        }

        if (slots.containsKey("close")) {
            inventory.setItem(slots.get("close"), config.getDecorativeItem("close-button"));
        }
        if (slots.containsKey("info")) {
            ItemStack info = config.getDecorativeItem("info");
            processDecorativePlaceholders(info);
            inventory.setItem(slots.get("info"), info);
        }

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
                    if (meta.hasLore()) finalLore.addAll(meta.getLore());
                } else {
                    String processed = plugin.getLanguageManager().colorize(
                            plugin.getPlaceholderManager().replacePlaceholders(player, line, ph));
                    if (!processed.isEmpty()) finalLore.add(processed);
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