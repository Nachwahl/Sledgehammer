package com.noahhusby.sledgehammer.gui.inventories.warp;

import com.noahhusby.sledgehammer.data.warp.WarpPayload;
import com.noahhusby.sledgehammer.gui.inventories.general.GUIChild;
import com.noahhusby.sledgehammer.gui.inventories.general.GUIRegistry;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class WarpSortInventory extends GUIChild {
    private final WarpPayload payload;
    public WarpSortInventory(WarpPayload payload) {
        this.payload = payload;
    }

    @Override
    public void init() {
        for(int x = 0; x < 27; x++) {
            ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 15);

            ItemMeta meta = glass.getItemMeta();
            meta.setDisplayName(ChatColor.RESET+"");
            meta.setDisplayName(null);
            glass.setItemMeta(meta);

            inventory.setItem(x, glass);
        }

        byte pinColor = payload.getDefaultPage().equalsIgnoreCase("pinned") ? (byte) 14 : (byte) 0;
        byte allColor = payload.getDefaultPage().equalsIgnoreCase("all") ? (byte) 14 : (byte) 0;
        byte groupColor = payload.getDefaultPage().equalsIgnoreCase("group") ? (byte) 14 : (byte) 0;

        {
            ItemStack item = new ItemStack(Material.WOOL, 1, pinColor);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Pinned Warps");

            List<String> lore = new ArrayList<>();
            if(payload.getDefaultPage().equalsIgnoreCase("pinned")) lore.add(ChatColor.RED + "Current Default");
            lore.add(ChatColor.BLUE + "This will show pinned warps when opened.");
            meta.setLore(lore);

            item.setItemMeta(meta);

            inventory.setItem(11, item);
        }

        {
            ItemStack item = new ItemStack(Material.WOOL, 1, allColor);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "All Warps");

            List<String> lore = new ArrayList<>();
            if(payload.getDefaultPage().equalsIgnoreCase("all")) lore.add(ChatColor.RED + "Current Default");
            lore.add(ChatColor.BLUE + "This will show all warps when opened.");
            meta.setLore(lore);

            item.setItemMeta(meta);

            inventory.setItem(13, item);
        }

        {
            ItemStack item = new ItemStack(Material.WOOL, 1, groupColor);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Group Warps");

            List<String> lore = new ArrayList<>();
            if(payload.getDefaultPage().equalsIgnoreCase("group")) lore.add(ChatColor.RED + "Current Default");
            lore.add(ChatColor.BLUE + "This will show your local group when opened.");
            meta.setLore(lore);

            item.setItemMeta(meta);

            inventory.setItem(15, item);
        }

    }

    @Override
    public void onInventoryClick(InventoryClickEvent e) {
        e.setCancelled(true);

        WarpSortInventoryController controller = (WarpSortInventoryController) getController();

        if(e.getCurrentItem() == null) return;
        if(e.getCurrentItem().getItemMeta() == null) return;
        if(e.getCurrentItem().getItemMeta().getDisplayName() == null) return;

        if(e.getSlot() == 11) {
            payload.setDefaultPage("pinned");
        }

        if(e.getSlot() == 13) {
            payload.setDefaultPage("all");
        }

        if(e.getSlot() == 15) {
            payload.setDefaultPage("group");
        }

        controller.close();
        GUIRegistry.register(new WarpInventoryController(getPlayer(), payload));
    }
}
