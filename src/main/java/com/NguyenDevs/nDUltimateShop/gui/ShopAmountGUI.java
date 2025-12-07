package com.NguyenDevs.nDUltimateShop.gui;

import com.NguyenDevs.nDUltimateShop.NDUltimateShop;
import com.NguyenDevs.nDUltimateShop.managers.LanguageManager;
import com.NguyenDevs.nDUltimateShop.models.ShopItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopAmountGUI implements InventoryHolder {

    private final NDUltimateShop plugin;
    private final Player player;
    private final ShopItem shopItem;
    private Inventory inventory;
    private int amount = 1;

    public ShopAmountGUI(NDUltimateShop plugin, Player player, ShopItem shopItem) {
        this.plugin = plugin;
        this.player = player;
        this.shopItem = shopItem;
    }

    public void open() {
        String title = plugin.getLanguageManager().getMessage("shop-selector-title");
        inventory = Bukkit.createInventory(this, 9, LanguageManager.colorize(title));
        setupGUI();
        player.openInventory(inventory);
    }

    private void setupGUI() {
        inventory.setItem(0, createItem(Material.OAK_DOOR, plugin.getLanguageManager().getMessage("selector-back")));

        inventory.setItem(1, createItem(Material.RED_STAINED_GLASS_PANE, plugin.getLanguageManager().getMessage("selector-minus").replace("%amount%", "64")));
        inventory.setItem(2, createItem(Material.RED_STAINED_GLASS_PANE, plugin.getLanguageManager().getMessage("selector-minus").replace("%amount%", "10")));
        inventory.setItem(3, createItem(Material.RED_STAINED_GLASS_PANE, plugin.getLanguageManager().getMessage("selector-minus").replace("%amount%", "1")));

        updateCenterItem();

        inventory.setItem(5, createItem(Material.LIME_STAINED_GLASS_PANE, plugin.getLanguageManager().getMessage("selector-plus").replace("%amount%", "1")));
        inventory.setItem(6, createItem(Material.LIME_STAINED_GLASS_PANE, plugin.getLanguageManager().getMessage("selector-plus").replace("%amount%", "10")));
        inventory.setItem(7, createItem(Material.LIME_STAINED_GLASS_PANE, plugin.getLanguageManager().getMessage("selector-plus").replace("%amount%", "64")));

        inventory.setItem(8, createItem(Material.LIME_DYE, plugin.getLanguageManager().getMessage("selector-confirm")));
    }

    private void updateCenterItem() {
        ItemStack item = shopItem.getItemStack().clone();
        item.setAmount(1);
        ItemMeta meta = item.getItemMeta();

        String originalName = meta.hasDisplayName() ? meta.getDisplayName() :
                LanguageManager.colorize("&f" + item.getType().name().replace("_", " "));

        String displayName = LanguageManager.colorize("&e&lx" + amount + " &r") + originalName;
        meta.setDisplayName(displayName);

        List<String> lore = new ArrayList<>();
        if (shopItem.getItemStack().getItemMeta() != null &&
                shopItem.getItemStack().getItemMeta().hasLore()) {
            lore.addAll(shopItem.getItemStack().getItemMeta().getLore());
        }

        lore.add(" ");

        double singlePrice = plugin.getCouponManager().getDiscountedPrice(player.getUniqueId(), shopItem.getPrice());
        double totalPrice = singlePrice * amount;

        lore.add(LanguageManager.colorize("&8&m------------------"));
        lore.add(LanguageManager.colorize("&eSố lượng: &f" + amount));
        lore.add(LanguageManager.colorize("&6Đơn giá: &a" + String.format("%,.2f", singlePrice) + "$"));
        lore.add(LanguageManager.colorize("&8------------------"));
        lore.add(LanguageManager.colorize("&2Tổng giá: &a" + String.format("%,.2f", totalPrice) + "$"));
        lore.add(LanguageManager.colorize("&8&m------------------"));
        lore.add(LanguageManager.colorize(" "));

        meta.setLore(lore);
        item.setItemMeta(meta);
        inventory.setItem(4, item);
    }

    private ItemStack createItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(LanguageManager.colorize(name));
            item.setItemMeta(meta);
        }
        return item;
    }

    public void increaseAmount(int value) {
        amount += value;
        updateCenterItem();
    }

    public void decreaseAmount(int value) {
        amount -= value;
        if (amount < 1) amount = 1;
        updateCenterItem();
    }

    public int getAmount() {
        return amount;
    }

    public ShopItem getShopItem() {
        return shopItem;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}