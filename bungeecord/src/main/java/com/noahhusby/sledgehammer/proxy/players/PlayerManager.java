/*
 * Copyright (c) 2020 Noah Husby
 * Sledgehammer [Bungeecord] - PlayerManager.java
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
 *  You should have received a copy of the GNU General Public License
 *  along with Sledgehammer.  If not, see <https://github.com/noahhusby/Sledgehammer/blob/master/LICENSE/>.
 */

package com.noahhusby.sledgehammer.proxy.players;

import com.google.common.collect.Maps;
import com.noahhusby.lib.data.storage.StorageList;
import com.noahhusby.sledgehammer.proxy.Sledgehammer;
import lombok.Getter;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.*;

public class PlayerManager implements Listener {
    private static PlayerManager instance = null;

    public static PlayerManager getInstance() {
        return instance == null ? instance = new PlayerManager() : instance;
    }

    @Getter private final Map<UUID, SledgehammerPlayer> players = Maps.newHashMap();
    @Getter private final StorageList<Attribute> attributes = new StorageList<>(Attribute.class);

    private PlayerManager() {
        Sledgehammer.addListener(this);
    }

    /**
     * Creates a new SledgehammerPlayer and sets attributes from storage upon player joining
     * @param e {@link PostLoginEvent}
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoinEvent(PostLoginEvent e) {
        onPlayerJoin(e.getPlayer());
    }

    /**
     * Creates a new SledgehammerPlayer and sets attributes from storage upon player joining
     * @param p {@link ProxiedPlayer}
     */
    private SledgehammerPlayer onPlayerJoin(ProxiedPlayer p) {
        SledgehammerPlayer newPlayer = new SledgehammerPlayer(p);

        Attribute attribute = null;
        for(Attribute a : attributes)
            if(a.getUuid().equals(p.getUniqueId())) attribute = a;

        if(attribute != null) {
            newPlayer.setAttributes(attribute.getAttributes());
        }

        players.put(newPlayer.getUniqueId(), newPlayer);
        return newPlayer;
    }

    /**
     * Removes the SledgehammerPlayer and saves the attributes to storage upon player leaving
     * @param e {@link PlayerDisconnectEvent}
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDisconnectEvent(PlayerDisconnectEvent e) {
        onPlayerDisconnect(e.getPlayer());
    }

    /**
     * Removes the SledgehammerPlayer and saves the attributes to storage upon player leaving
     * @param player {@link ProxiedPlayer}
     */
    private void onPlayerDisconnect(ProxiedPlayer player) {
        SledgehammerPlayer p = players.get(player.getUniqueId());

        if(p == null) return;

        Attribute attribute = null;
        for(Attribute a : attributes)
            if(a.getUuid().equals(player.getUniqueId())) attribute = a;

        if(attribute == null) {
            if(!p.getAttributes().isEmpty()) {
                attributes.add(new Attribute(p.getUniqueId(), p.getAttributes()));
            }
        } else {
            if(p.getAttributes().isEmpty()) {
                attributes.remove(attribute);
            }
        }

        attributes.save(true);
        players.remove(player.getUniqueId());
    }

    /**
     * Gets SledgehammerPlayer by player name
     * @param s Player name
     * @return {@link SledgehammerPlayer}
     */
    public SledgehammerPlayer getPlayer(String s) {
        ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(s);
        if(proxiedPlayer == null) return null;
        SledgehammerPlayer player = players.get(proxiedPlayer.getUniqueId());
        return player == null ? onPlayerJoin(proxiedPlayer) : player;
    }

    /**
     * Gets SledgehammerPlayer by command sender
     * @param s {@link CommandSender}
     * @return {@link SledgehammerPlayer}
     */
    public SledgehammerPlayer getPlayer(CommandSender s) {
        if(s == null) return null;
        if(!(s instanceof ProxiedPlayer)) return null;
        ProxiedPlayer proxiedPlayer = (ProxiedPlayer) s;
        SledgehammerPlayer player = players.get(proxiedPlayer.getUniqueId());
        return player == null ? onPlayerJoin(proxiedPlayer) : player;
    }
}
