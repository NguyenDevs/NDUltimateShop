package com.NguyenDevs.nDUltimateShop.listeners;

import com.NguyenDevs.nDUltimateShop.NDUltimateShop;
import com.NguyenDevs.nDUltimateShop.gui.ShopAmountGUI;
import com.NguyenDevs.nDUltimateShop.gui.ShopGUI;
import com.NguyenDevs.nDUltimateShop.models.ShopItem;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class ShopAmountListener implements Listener {

    private final NDUltimateShop plugin;

    public ShopAmountListener(NDUltimateShop plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof ShopAmountGUI)) return;

        event.setCancelled(true);

        ShopAmountGUI gui = (ShopAmountGUI) event.getInventory().getHolder();
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();

        if (slot >= event.getInventory().getSize()) return;

        if (slot == 0) {
            new ShopGUI(plugin, player).open();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            return;
        }

        switch (slot) {
            case 1: gui.decreaseAmount(64); break;
            case 2: gui.decreaseAmount(10); break;
            case 3: gui.decreaseAmount(1); break;
            case 5: gui.increaseAmount(1); break;
            case 6: gui.increaseAmount(10); break;
            case 7: gui.increaseAmount(64); break;
            case 8: confirmPurchase(player, gui); return;
        }

        if (slot >= 1 && slot <= 7 && slot != 4) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof ShopAmountGUI) {
            event.setCancelled(true);
        }
    }

    private void confirmPurchase(Player player, ShopAmountGUI gui) {
        ShopItem shopItem = gui.getShopItem();
        int amount = gui.getAmount();

        if (shopItem.getStock() != -1 && amount > shopItem.getStock()) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.5f);
            Map<String, String> ph = new HashMap<>();
            ph.put("stock", String.valueOf(shopItem.getStock()));
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("shop-stock-limit", ph));
            return;
        }

        double singlePrice = plugin.getCouponManager().getDiscountedPrice(player.getUniqueId(), shopItem.getPrice());
        double totalPrice = singlePrice * amount;

        if (plugin.getEconomy().getBalance(player) < totalPrice) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.5f);
            Map<String, String> ph = new HashMap<>();
            ph.put("amount", String.format("%,.2f", totalPrice));
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("not-enough-money", ph));
            return;
        }

        plugin.getEconomy().withdrawPlayer(player, totalPrice);

        ItemStack itemToGive = shopItem.getItemStack().clone();
        int remaining = amount;

        int maxStackSize = itemToGive.getMaxStackSize();
        while (remaining > 0) {
            int stackSize = Math.min(remaining, maxStackSize);
            ItemStack stack = itemToGive.clone();
            stack.setAmount(stackSize);

            for (ItemStack drop : player.getInventory().addItem(stack).values()) {
                player.getWorld().dropItem(player.getLocation(), drop);
            }
            remaining -= stackSize;
        }

        if (shopItem.getStock() != -1) {
            plugin.getShopManager().purchaseItem(shopItem.getId(), amount);
        }

        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);

        Map<String, String> ph = new HashMap<>();
        ph.put("amount", String.valueOf(amount));
        String itemName = shopItem.getItemStack().getType().name();
        if (shopItem.getItemStack().hasItemMeta() && shopItem.getItemStack().getItemMeta().hasDisplayName()) {
            itemName = shopItem.getItemStack().getItemMeta().getDisplayName();
        }
        ph.put("item", itemName);
        ph.put("price", String.format("%,.2f", totalPrice));
        player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("shop-item-bought", ph));

        player.closeInventory();
    }
}