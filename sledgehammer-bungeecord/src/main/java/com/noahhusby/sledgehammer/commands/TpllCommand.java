/*
 * Copyright (c) 2020 Noah Husby
 * Sledgehammer [Bungeecord] - TpllCommand.java
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

package com.noahhusby.sledgehammer.commands;

import com.noahhusby.sledgehammer.SledgehammerUtil;
import com.noahhusby.sledgehammer.chat.ChatConstants;
import com.noahhusby.sledgehammer.commands.data.Command;
import com.noahhusby.sledgehammer.datasets.OpenStreetMaps;
import com.noahhusby.sledgehammer.network.P2S.P2SLocationPacket;
import com.noahhusby.sledgehammer.chat.ChatHelper;
import com.noahhusby.sledgehammer.chat.TextElement;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class TpllCommand extends Command {

    public TpllCommand() {
        super("tpll", "sledgehammer.tpll");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(ChatConstants.issueByPlayer);
            return;
        }

        if(!hasGeneralPermission(sender)) {
            sender.sendMessage(ChatConstants.noPermission);
            return;
        }

        if(args.length==0) {
            if(hasPermissionAdmin(sender)) {
                sender.sendMessage(ChatHelper.makeTitleTextComponent(new TextElement("Usage: /tpll [target player] <lat> <lon>", ChatColor.RED)));
                return;
            }
            sender.sendMessage(ChatHelper.makeTitleTextComponent(new TextElement("Usage: /tpll <lat> <lon>", ChatColor.RED)));
            return;
        }

        String parsedSender;
        String[] parseArgs;

        if(args.length == 3 && hasPermissionAdmin(sender)) {
            parseArgs = new String[]{args[1], args[2]};
            parsedSender = args[0];
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(parsedSender);
            if(player == null) {
                sender.sendMessage(ChatHelper.makeTitleTextComponent(new TextElement(parsedSender, ChatColor.RED),
                        new TextElement(" could not be found on the network!", ChatColor.RED)));
                return;
            }
        } else {
            parsedSender = sender.getName();
            parseArgs = args;
        }

        String[] splitCoords = parseArgs[0].split(",");
        String alt = null;
        if(splitCoords.length==2&&parseArgs.length<3) { // lat and long in single arg
            if(parseArgs.length>1) alt = parseArgs[1];
            parseArgs = splitCoords;
        } else if(parseArgs.length==3) {
            alt = parseArgs[2];
        }
        if(parseArgs[0].endsWith(","))
            parseArgs[0] = parseArgs[0].substring(0, parseArgs[0].length() - 1);
        if(parseArgs.length>1&&parseArgs[1].endsWith(","))
            parseArgs[1] = parseArgs[1].substring(0, parseArgs[1].length() - 1);
        if(parseArgs.length!=2&&parseArgs.length!=3) {
            if(hasPermissionAdmin(sender)) {
                sender.sendMessage(ChatHelper.makeTitleTextComponent(new TextElement("Usage: /tpll [target player] <lat> <lon>", ChatColor.RED)));
                return;
            }
            sender.sendMessage(ChatHelper.makeTitleTextComponent(new TextElement("Usage: /tpll <lat> <lon>", ChatColor.RED)));
            return;
        }

        double lon;
        double lat;

        try {
            lat = Double.parseDouble(parseArgs[0]);
            lon = Double.parseDouble(parseArgs[1]);
        } catch(Exception e) {
            if(hasPermissionAdmin(sender)) {
                sender.sendMessage(ChatHelper.makeTitleTextComponent(new TextElement("Usage: /tpll [target player] <lat> <lon>", ChatColor.RED)));
                return;
            }
            sender.sendMessage(ChatHelper.makeTitleTextComponent(new TextElement("Usage: /tpll <lat> <lon>", ChatColor.RED)));
            return;
        }

        ServerInfo server = OpenStreetMaps.getInstance().getServerFromLocation(lon, lat);

        if (server == null) {
            sender.sendMessage(ChatHelper.makeTitleTextComponent(new TextElement("That location could not be found, or is not available on this server!", ChatColor.RED)));
            return;
        }

        if(!hasPermissionAdmin(sender) && !(hasSpecificNode(sender, server.getName()) || hasSpecificNode(sender, "all"))) {
            sender.sendMessage(ChatHelper.makeTitleTextComponent(new TextElement("You don't have permission to tpll to ", ChatColor.RED),
                    new TextElement(server.getName(), ChatColor.DARK_RED)));
            return;
        }

        if (SledgehammerUtil.getServerFromPlayerName(parsedSender) != server) {
            if(!parsedSender.equals(sender.getName())) {
                ProxyServer.getInstance().getPlayer(parsedSender).sendMessage(
                        ChatHelper.makeTitleTextComponent(new TextElement("You were summoned to ", ChatColor.GRAY),
                                new TextElement(server.getName(), ChatColor.RED), new TextElement(" by ", ChatColor.GRAY),
                                new TextElement(sender.getName(), ChatColor.DARK_RED)));
            } else {
                sender.sendMessage(ChatHelper.makeTitleTextComponent(new TextElement("Sending you to ", ChatColor.GRAY), new TextElement(server.getName(), ChatColor.RED)));
            }
            ProxyServer.getInstance().getPlayer(parsedSender).connect(server);
        }

        if(!parsedSender.equals(sender.getName())) {
            ProxyServer.getInstance().getPlayer(parsedSender).sendMessage(
                    ChatHelper.makeTitleTextComponent(new TextElement("Teleporting to ", ChatColor.GRAY),
                            new TextElement(lat+", "+lon, ChatColor.RED)));
        } else {
            sender.sendMessage(ChatHelper.makeTitleTextComponent(new TextElement("Teleporting to ", ChatColor.GRAY),
                            new TextElement(lat+", "+lon, ChatColor.RED)));
        }
        getNetworkManager().sendPacket(new P2SLocationPacket(parsedSender, server.getName(), String.valueOf(lat), String.valueOf(lon)));
    }
}
