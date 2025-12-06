package com.NguyenDevs.nDUltimateShop.gui;

import com.NguyenDevs.nDUltimateShop.NDUltimateShop;
import com.NguyenDevs.nDUltimateShop.models.Coupon;
import com.NguyenDevs.nDUltimateShop.models.ShopItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopGUI extends BaseGUI {

    private static final int ITEMS_PER_PAGE = 45;

    public ShopGUI(NDUltimateShop plugin, Player player) {
        super(plugin, player);
    }

    @Override
    public void open() {
        String title = plugin.getConfig().getString("shop.gui-title", "&6&lCỬA HÀNG");
        int rows = plugin.getConfig().getInt("shop.gui-rows", 6);
        inventory = Bukkit.createInventory(null, rows * 9,
                com.NguyenDevs.nDUltimateShop.managers.LanguageManager.colorize(title));

        loadItems();
        player.openInventory(inventory);
    }

    private void loadItems() {
        List<ShopItem> items = new ArrayList<>(plugin.getShopManager().getAllShopItems());

        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, items.size());

        for (int i = startIndex; i < endIndex; i++) {
            ShopItem shopItem = items.get(i);
            ItemStack displayItem = createShopItemDisplay(shopItem);
            inventory.setItem(i - startIndex, displayItem);
        }

        // Add navigation
        if (endIndex < items.size()) {
            ItemStack nextButton = createItem(
                    Material.ARROW,
                    plugin.getLanguageManager().getMessage("gui-next-page")
            );
            inventory.setItem(inventory.getSize() - 1, nextButton);
        }

        addNavigationButtons(inventory.getSize());
    }

    private ItemStack createShopItemDisplay(ShopItem shopItem) {
        ItemStack display = shopItem.getItemStack();
        ItemMeta meta = display.getItemMeta();

        if (meta != null) {
            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();

            // Get price with discount if applicable
            double originalPrice = shopItem.getPrice();
            double finalPrice = plugin.getCouponManager().getDiscountedPrice(player.getUniqueId(), originalPrice);

            // Add discount indicator
            if (finalPrice < originalPrice) {
                String activeCoupon = plugin.getCouponManager().getActiveCoupon(player.getUniqueId());
                Coupon coupon = plugin.getCouponManager().getCoupon(activeCoupon);
                if (coupon != null) {
                    Map<String, String> discountPlaceholder = new HashMap<>();
                    discountPlaceholder.put("discount", String.format("%.0f", coupon.getDiscount()));
                    lore.add(plugin.getLanguageManager().getMessage("lore-discount", discountPlaceholder));

                    Map<String, String> originalPlaceholder = new HashMap<>();
                    originalPlaceholder.put("price", String.format("%.2f", originalPrice));
                    lore.add(plugin.getLanguageManager().getMessage("lore-original-price", originalPlaceholder));
                }
            }

            Map<String, String> pricePlaceholder = new HashMap<>();
            pricePlaceholder.put("price", String.format("%.2f", finalPrice));
            lore.add(plugin.getLanguageManager().getMessage("lore-price", pricePlaceholder));

            if (shopItem.getStock() != -1) {
                Map<String, String> stockPlaceholder = new HashMap<>();
                stockPlaceholder.put("stock", String.valueOf(shopItem.getStock()));
                lore.add(plugin.getLanguageManager().getMessage("lore-stock", stockPlaceholder));
            }

            lore.add(plugin.getLanguageManager().getMessage("lore-click-buy"));

            meta.setLore(lore);
            display.setItemMeta(meta);
        }

        return display;
    }
}