package com.NguyenDevs.nDUltimateShop.gui;

import com.NguyenDevs.nDUltimateShop.NDUltimateShop;
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

public class BlackShopGUI extends BaseGUI {

    private static final int ITEMS_PER_PAGE = 45;

    public BlackShopGUI(NDUltimateShop plugin, Player player) {
        super(plugin, player);
    }

    @Override
    public void open() {
        String title = plugin.getConfig().getString("blackshop.gui-title", "&5&lCHỢ ĐÊM");
        inventory = Bukkit.createInventory(null, 54,
                com.NguyenDevs.nDUltimateShop.managers.LanguageManager.colorize(title));

        loadItems();
        player.openInventory(inventory);
    }

    private void loadItems() {
        List<ShopItem> items = new ArrayList<>(plugin.getBlackShopManager().getAllItems());

        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, items.size());

        for (int i = startIndex; i < endIndex; i++) {
            ShopItem shopItem = items.get(i);
            ItemStack displayItem = createBlackShopDisplay(shopItem);
            inventory.setItem(i - startIndex, displayItem);
        }

        if (endIndex < items.size()) {
            ItemStack nextButton = createItem(
                    Material.ARROW,
                    plugin.getLanguageManager().getMessage("gui-next-page")
            );
            inventory.setItem(inventory.getSize() - 1, nextButton);
        }

        addNavigationButtons(inventory.getSize());
    }

    private ItemStack createBlackShopDisplay(ShopItem shopItem) {
        ItemStack display = shopItem.getItemStack();
        ItemMeta meta = display.getItemMeta();

        if (meta != null) {
            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();

            Map<String, String> pricePlaceholder = new HashMap<>();
            pricePlaceholder.put("price", String.format("%.2f", shopItem.getPrice()));
            lore.add(plugin.getLanguageManager().getMessage("lore-price", pricePlaceholder));

            Map<String, String> stockPlaceholder = new HashMap<>();
            stockPlaceholder.put("stock", String.valueOf(shopItem.getStock()));
            lore.add(plugin.getLanguageManager().getMessage("lore-stock", stockPlaceholder));

            lore.add(plugin.getLanguageManager().getMessage("lore-click-buy"));

            meta.setLore(lore);
            display.setItemMeta(meta);
        }

        return display;
    }
}