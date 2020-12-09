/*
 * Copyright (c) 2020 Noah Husby
 * sledgehammer - SetServerWarpInventory.java
 *
 * Sledgehammer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Sledgehammer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Sledgehammer.  If not, see <https://github.com/noahhusby/Sledgehammer/blob/master/LICENSE/>.
 */

package com.noahhusby.sledgehammer.gui.inventories.warp;

import com.noahhusby.sledgehammer.Constants;
import com.noahhusby.sledgehammer.SledgehammerUtil;
import com.noahhusby.sledgehammer.data.warp.Warp;
import com.noahhusby.sledgehammer.data.warp.WarpGroup;
import com.noahhusby.sledgehammer.gui.inventories.general.GUIChild;
import com.noahhusby.sledgehammer.gui.inventories.general.GUIHelper;
import com.noahhusby.sledgehammer.gui.inventories.general.GUIRegistry;
import com.noahhusby.sledgehammer.gui.inventories.warp.config.WarpConfigController;
import com.noahhusby.sledgehammer.network.S2P.S2PWarpPacket;
import com.noahhusby.sledgehammer.network.S2P.S2PWebMapPacket;
import com.noahhusby.sledgehammer.network.SledgehammerNetworkManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GroupListWarpInventory extends GUIChild {
    private final int page;
    private final List<WarpGroup> groups;

    private Inventory inventory;

    public GroupListWarpInventory(int page, List<WarpGroup> groups) {
        this.page = page;
        this.groups = groups;
    }

    @Override
    public void init() {
        this.inventory = getInventory();
        int total_pages = (int) Math.ceil(groups.size() / 27.0);

        for(int x = 0; x < 54; x++) {
            ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 15);

            ItemMeta meta = glass.getItemMeta();
            meta.setDisplayName(ChatColor.RESET+"");
            meta.setDisplayName(null);
            glass.setItemMeta(meta);

            inventory.setItem(x, glass);
        }

        inventory.setItem(4, SledgehammerUtil.getSkull(Constants.monitorHead, ChatColor.GREEN + "" + ChatColor.BOLD + "Groups"));
        inventory.setItem(40, generateCompass());
        inventory.setItem(49, GUIHelper.generateExit());

        ItemStack sort = new ItemStack(Material.HOPPER, 1);
        ItemMeta sortMeta = sort.getItemMeta();
        sortMeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Sort");
        sort.setItemMeta(sortMeta);
        inventory.setItem(45, sort);

        inventory.setItem(46, SledgehammerUtil.getSkull(Constants.lampHead, ChatColor.GOLD + "" + ChatColor.BOLD
                + "Show pinned warps"));

        if(((GroupListWarpInventoryController) getController()).getPayload().isEditAccess()) {
            ItemStack anvil = new ItemStack(Material.ANVIL, 1);
            ItemMeta meta = anvil.getItemMeta();
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Configure Warps");
            anvil.setItemMeta(meta);
            inventory.setItem(47, anvil);
        }

        boolean paged = false;
        if(page != 0) {
            ItemStack head = SledgehammerUtil.getSkull(Constants.arrowLeftHead, ChatColor.AQUA + "" + ChatColor.BOLD + "Previous Page");
            inventory.setItem(51, head);
            paged = true;
        }

        if(groups.size() > (page + 1) * Constants.warpsPerPage) {
            ItemStack head = SledgehammerUtil.getSkull(Constants.arrowRightHead, ChatColor.AQUA + "" + ChatColor.BOLD + "Next Page");
            inventory.setItem(53, head);
            paged = true;
        }

        if(paged) {
            inventory.setItem(52, SledgehammerUtil.NumberHeads.getHead((page + 1), ChatColor.GREEN +
                    "" + ChatColor.BOLD + "Page " + (page + 1)));
        }

        int min = page * 27;
        int max = min + 27;

        if(max > groups.size()) {
            max = min + (groups.size() - (page * 27));
        }

        int current = 9;
        for(int x = min; x < max; x++) {
            WarpGroup group = groups.get(x);

            String headId = group.getHeadId();
            if(headId.equals("")) headId = Constants.cyanWoolHead;
            ItemStack item = SledgehammerUtil.getSkull(headId, group.getName());

            ItemMeta meta = item.getItemMeta();

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.BLUE + "" + ChatColor.STRIKETHROUGH + "------------------");
            lore.add(ChatColor.DARK_GRAY + "Group: " + group.getName());
            lore.add(ChatColor.DARK_GRAY + "> " + ChatColor.GREEN + "Click to view.");
            lore.add(ChatColor.BLUE + "" + ChatColor.STRIKETHROUGH + "------------------");
            lore.add(ChatColor.GRAY + "ID: " + group.getId());
            meta.setLore(lore);
            item.setItemMeta(meta);

            inventory.setItem(current, item);
            current++;
        }
    }

    @Override
    public void onInventoryClick(InventoryClickEvent e) {
        e.setCancelled(true);
        if(e.getCurrentItem() == null) return;
        if(e.getCurrentItem().getItemMeta() == null) return;
        if(e.getCurrentItem().getItemMeta().getDisplayName() == null) return;

        GroupListWarpInventoryController controller = (GroupListWarpInventoryController) getController();

        if(e.getCurrentItem().getItemMeta().getDisplayName() == null) return;

        if(e.getSlot() == 45) {
            controller.close();
            GUIRegistry.register(new WarpSortInventoryController(getPlayer(), controller.getPayload()));
            return;
        }

        if(e.getSlot() == 40) {
            GUIRegistry.register(new WarpInventoryController(getController(), controller.getPayload()));
            return;
        }

        if(e.getSlot() == 46) {
            GUIRegistry.register(new PinnedWarpInventoryController(getPlayer(), controller.getPayload()));
            return;
        }

        if(e.getSlot() == 47 && controller.getPayload().isEditAccess()) {
            controller.close();
            GUIRegistry.register(new WarpConfigController(getPlayer()));
            return;
        }

        if(ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName()).equalsIgnoreCase("Previous Page")) {
            controller.openChild(controller.getChildByPage(page-1));
            return;
        }

        if(ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName()).equalsIgnoreCase("Next Page")) {
            controller.openChild(controller.getChildByPage(page+1));
            return;
        }

        if(ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName()).equalsIgnoreCase("Close")) {
            controller.close();
            return;
        }

        if(e.getSlot() > 8 && e.getSlot() < 36) {
            ItemMeta meta = e.getCurrentItem().getItemMeta();
            String id = "";
            List<String> lore = meta.getLore();
            for(String s : lore) {
                if(s.contains("ID:"))
                    id = ChatColor.stripColor(s).trim().replace("ID: ", "");
            }

            GUIRegistry.register(new GroupWarpInventoryController(getController(), controller.getPayload(), id));
            return;
        }
    }

    private ItemStack generateCompass() {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta m = compass.getItemMeta();

        m.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "All Warps");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "All Warps");
        m.setLore(lore);

        compass.setItemMeta(m);

        return compass;
    }

    public int getPage() {
        return page;
    }
}
