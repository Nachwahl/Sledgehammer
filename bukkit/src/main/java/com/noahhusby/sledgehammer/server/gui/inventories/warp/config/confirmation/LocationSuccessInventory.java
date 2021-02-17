package com.noahhusby.sledgehammer.server.gui.inventories.warp.config.confirmation;

import com.noahhusby.sledgehammer.common.warps.Warp;
import com.noahhusby.sledgehammer.server.Constants;
import com.noahhusby.sledgehammer.server.SledgehammerUtil;
import com.noahhusby.sledgehammer.server.data.warp.WarpConfigPayload;
import com.noahhusby.sledgehammer.server.gui.inventories.general.GUIChild;
import com.noahhusby.sledgehammer.server.gui.inventories.general.GUIRegistry;
import com.noahhusby.sledgehammer.server.gui.inventories.warp.config.manage.ManageWarpInventoryController;
import org.bukkit.ChatColor;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class LocationSuccessInventory extends GUIChild {
    private final WarpConfigPayload payload;
    private final Warp warp;
    public LocationSuccessInventory(WarpConfigPayload payload, Warp warp) {
        this.payload = payload;
        this.warp = warp;
    }

    @Override
    public void init() {
        for(int x = 0; x < 27; x++) {
            ItemStack skull = SledgehammerUtil.getSkull(Constants.purpleExclamationMark, ChatColor.GREEN + "" + ChatColor.BOLD +
                    "Successfully updated warp location!");

            ItemMeta meta = skull.getItemMeta();
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.BLUE + "Click to continue");
            meta.setLore(lore);

            skull.setItemMeta(meta);

            inventory.setItem(x, skull);
        }
    }

    @Override
    public void onInventoryClick(InventoryClickEvent e) {
        e.setCancelled(true);
        GUIRegistry.register(new ManageWarpInventoryController(getPlayer(), payload, warp));
    }
}
