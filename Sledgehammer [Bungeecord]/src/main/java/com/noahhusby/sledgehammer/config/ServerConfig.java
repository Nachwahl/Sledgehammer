package com.noahhusby.sledgehammer.config;

import com.google.gson.annotations.Expose;
import com.noahhusby.sledgehammer.SledgehammerUtil;
import com.noahhusby.sledgehammer.config.types.Server;
import com.noahhusby.sledgehammer.datasets.Location;
import com.noahhusby.sledgehammer.network.P2S.P2SInitializationPacket;
import com.noahhusby.sledgehammer.network.P2S.P2SLocationPacket;
import com.noahhusby.sledgehammer.network.SledgehammerNetworkManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ServerConfig {
    private static ServerConfig instance;

    public static ServerConfig getInstance() {
        return instance;
    }

    public static void setInstance(ServerConfig instance) {
        ServerConfig.instance = instance;
    }

    public ServerConfig() { }

    @Expose(serialize = true, deserialize = true)
    public List<Server> servers = new ArrayList<>();

    public List<ServerInfo> bungeeServers;

    public List<Server> getServers() {
        return servers;
    }

    public void onServerJoin(ServerConnectedEvent e) {
        Server s = getServer(e.getServer().getInfo().getName());
        if(s != null) {
            if(!s.isInitialized()) {
                SledgehammerNetworkManager.getInstance().sendPacket(new P2SInitializationPacket(e.getPlayer().getName(), e.getServer().getInfo().getName()));
            }
        }
    }

    public void initializeServer(ServerInfo serverInfo, JSONObject data) {
        Server s = getServer(serverInfo.getName());
        if(s != null) {
            s.initialize((String) data.get("version"), (String) data.get("tpllmode"));
            pushServer(s);
        }
    }

    public List<ServerInfo> getBungeeServers() {
        if(bungeeServers == null) {
            bungeeServers = new ArrayList<>();
            Map<String, ServerInfo> serversTemp = ProxyServer.getInstance().getServers();
            for(Map.Entry<String, ServerInfo> s : serversTemp.entrySet()) {
                bungeeServers.add(s.getValue());
            }
        }
        return bungeeServers;
    }

    public void pushServer(Server server) {
        List<Server> remove = new ArrayList<>();
        for(Server s : servers) {
            if(s.name.toLowerCase().equals(server.name.toLowerCase())) {
                remove.add(s);
            }
        }

        for(Server s : remove) {
            servers.remove(s);
        }

        servers.add(server);
        ConfigHandler.getInstance().saveServerDB();
    }


    public Server getServer(String name) {
        for(Server s : servers) {
            if(s.name.toLowerCase().equals(name.toLowerCase())) {
                return s;
            }
        }
        return null;
    }

    public List<Location> getLocationsFromServer(String server) {
        for(Server s : servers) {
            if(s.name.toLowerCase().equals(server.toLowerCase())) {
                return s.locations;
            }
        }

        return null;
    }
}
