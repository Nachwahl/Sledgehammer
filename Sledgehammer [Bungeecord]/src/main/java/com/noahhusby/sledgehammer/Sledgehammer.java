package com.noahhusby.sledgehammer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.noahhusby.sledgehammer.commands.TpllCommand;
import com.noahhusby.sledgehammer.commands.WarpCommand;
import com.noahhusby.sledgehammer.datasets.OpenStreetMaps;
import com.noahhusby.sledgehammer.handlers.CommunicationHandler;
import com.noahhusby.sledgehammer.handlers.WarpHandler;
import com.noahhusby.sledgehammer.util.Warp;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.nio.file.Files;
import java.util.function.Predicate;

public class Sledgehammer extends Plugin implements Listener {
    private static File warpFile = null;
    private final File configFile = new File(getDataFolder(), "config.yml");
    public static Configuration configuration;

    @Override
    public void onEnable() {
        initConfig();

        warpFile = new File(getDataFolder(), "warps.json");
        loadWarpDB();

        if(!configuration.getString("warp-command").equals("")) {
            ProxyServer.getInstance().getPluginManager().registerCommand(this, new WarpCommand());
        }

        ProxyServer.getInstance().getPluginManager().registerCommand(this, new TpllCommand());
        ProxyServer.getInstance().registerChannel("sledgehammer:channel");
        ProxyServer.getInstance().getPluginManager().registerListener(this, this);

        OpenStreetMaps.getInstance();
    }

    @EventHandler
    public void onMessage(PluginMessageEvent e) {
        CommunicationHandler.onIncomingMessage(e);
    }


    private void initConfig() {
        createConfig();

        try {
            configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createConfig() {
        if (!getDataFolder().exists())
            getDataFolder().mkdir();

        File file = new File(getDataFolder(), "config.yml");

        if (!file.exists()) {
            try (InputStream in = getResourceAsStream("config.yml")) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void loadWarpDB() {
        if (warpFile.exists())
        {
            String json = null;
            try
            {
                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting()
                        .create();
                json = FileUtils.readFileToString(warpFile, "UTF-8");
                WarpHandler.setInstance(gson.fromJson(json, WarpHandler.class));
                WarpHandler.getInstance();
                saveWarpDB();
            }
            catch (Throwable e)
            {
                e.printStackTrace();
                System.err.println("\n" + json);
                System.out.println(e);
            }
            return;
        }

        WarpHandler.setInstance(new WarpHandler());
        WarpHandler.getInstance();
        saveWarpDB();
    }

    public static void saveWarpDB() {
        try
        {
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
            Predicate<String> nonnull = new Predicate<String>()
            {
                @Override
                public boolean test(String t)
                {
                    return t == null || t.isEmpty();
                }
            };
            FileUtils.writeStringToFile(warpFile, gson.toJson(WarpHandler.getInstance()), "UTF-8");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


}
