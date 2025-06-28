package me.slyph.resourcepack.main;

import me.slyph.resourcepack.commands.RespCommand;
import me.slyph.resourcepack.data.DataManager;
import me.slyph.resourcepack.listeners.ResourcePackListener;
import me.slyph.resourcepack.listeners.RespGuiListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class Main extends JavaPlugin {

    private DataManager dataManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        dataManager = new DataManager(this);
        dataManager.loadData();

        getServer().getPluginManager().registerEvents(new ResourcePackListener(this, dataManager), this);
        getServer().getPluginManager().registerEvents(new RespGuiListener(this), this);

        getCommand("resp").setExecutor(new RespCommand(this));

        getLogger().info("ResourcePackPlugin включён!");
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.saveData();
        }

        List<String> lines = getConfig().getStringList("Messages.PluginDisabled");
        if (lines.isEmpty()) {
            lines.add("&eПлагин выключен!"); // дефолт, если пусто
        }
        for (String line : lines) {
            getLogger().info(line.replaceAll("&", "§")); // или использовать ColorUtil, но в логах RGB не работает
        }

        getLogger().info("ResourcePackPlugin выключен!");
    }

    public DataManager getDataManager() {
        return dataManager;
    }
}
