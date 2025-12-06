package com.NguyenDevs.nDUltimateShop.gui;

import com.NguyenDevs.nDUltimateShop.NDUltimateShop;
import com.NguyenDevs.nDUltimateShop.models.Coupon;
import com.NguyenDevs.nDUltimateShop.models.ShopItem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopGUI extends BaseGUI {

    private final List<ShopItem> items;

    public ShopGUI(NDUltimateShop plugin, Player player) {
        super(plugin, player, "shop");
        this.items = new ArrayList<>(plugin.getShopManager().getAllShopItems());
    }

    @Override
    public void open() {
        String title = plugin.getPlaceholderManager().replacePlaceholders(player, config.getTitle());
        title = title.replace("%page%", String.valueOf(currentPage + 1));

        inventory = Bukkit.createInventory(this, config.getRows() * 9, plugin.getLanguageManager().colorize(title));

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
            inventory.setItem(slot, createShopItemDisplay(shopItem));
        }

        setupButtons();
        fillDecorative();
    }

    private void setupButtons() {
        Map<String, Integer> slots = config.getSlotMapping();
        List<Integer> itemSlots = config.getItemSlots();

        if (currentPage > 0 && slots.containsKey("previous")) {
            inventory.setItem(slots.get("previous"), config.getDecorativeItem("previous-button"));
        }

        int maxItemsPerPage = itemSlots.size();
        if ((currentPage + 1) * maxItemsPerPage < items.size() && slots.containsKey("next")) {
            inventory.setItem(slots.get("next"), config.getDecorativeItem("next-button"));
        }

        if (slots.containsKey("close")) {
            inventory.setItem(slots.get("close"), config.getDecorativeItem("close-button"));
        }
        if (slots.containsKey("info")) {
            ItemStack info = config.getDecorativeItem("info");
            processItemPlaceholders(info, new HashMap<>());
            inventory.setItem(slots.get("info"), info);
        }
    }

    private ItemStack createShopItemDisplay(ShopItem shopItem) {
        ItemStack display = shopItem.getItemStack().clone();
        ItemMeta meta = display.getItemMeta();
        if (meta == null) return display;

        double originalPrice = shopItem.getPrice();
        double finalPrice = plugin.getCouponManager().getDiscountedPrice(player.getUniqueId(), originalPrice);

        Map<String, String> ph = new HashMap<>();
        ph.put("price", String.format("%,.0f", finalPrice));
        ph.put("original_price", String.format("%,.0f", originalPrice));
        ph.put("stock", shopItem.getStock() == -1 ? "∞" : String.valueOf(shopItem.getStock()));

        if (finalPrice < originalPrice) {
            String cCode = plugin.getCouponManager().getActiveCoupon(player.getUniqueId());
            Coupon c = plugin.getCouponManager().getCoupon(cCode);
            ph.put("discount_percent", c != null ? String.valueOf((int)c.getDiscount()) : "0");
        } else {
            ph.put("discount_percent", "0");
        }

        List<String> configLore = config.getLoreFormat();
        if (configLore != null && !configLore.isEmpty()) {
            List<String> finalLore = new ArrayList<>();
            for (String line : configLore) {
                if (line.contains("%lore%")) {
                    if (meta.hasLore()) {
                        for (String originalLine : meta.getLore()) finalLore.add(originalLine);
                    }
                } else {
                    finalLore.add(plugin.getLanguageManager().colorize(
                            plugin.getPlaceholderManager().replacePlaceholders(player, line, ph)
                    ));
                }
            }
            meta.setLore(finalLore);
        }

        display.setItemMeta(meta);
        return display;
    }

    private void processItemPlaceholders(ItemStack item, Map<String, String> ph) {
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta.hasDisplayName()) {
            meta.setDisplayName(plugin.getLanguageManager().colorize(
                    plugin.getPlaceholderManager().replacePlaceholders(player, meta.getDisplayName(), ph)
            ));
        }
        if (meta.hasLore()) {
            List<String> lore = new ArrayList<>();
            for (String line : meta.getLore()) {
                lore.add(plugin.getLanguageManager().colorize(
                        plugin.getPlaceholderManager().replacePlaceholders(player, line, ph)
                ));
            }
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
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