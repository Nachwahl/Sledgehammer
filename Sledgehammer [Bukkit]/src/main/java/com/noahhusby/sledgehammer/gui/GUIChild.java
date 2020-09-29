/*
 * Copyright (c) 2020 Noah Husby
 * sledgehammer - GUIChild.java
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

package com.noahhusby.sledgehammer.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public abstract class GUIChild implements IGUIChild {
    protected Inventory inventory;
    protected GUIController controller;
    protected Player player;

    public void initFromController(GUIController controller, Player player, Inventory inventory) {
        this.inventory = Bukkit.createInventory(null, inventory.getSize());
        this.controller = controller;
        this.player = player;
        init();
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    protected Player getPlayer() {
        return player;
    }

    protected GUIController getController() {
        return controller;
    }
}
