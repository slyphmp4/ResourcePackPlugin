package me.slyph.resourcepack.data;

import me.slyph.resourcepack.main.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DataManager {

    private final Main plugin;
    private File dataFile;
    private FileConfiguration dataConfig;
    private Set<String> installedPlayers;

    public DataManager(Main plugin) {
        this.plugin = plugin;
    }

    public void loadData() {
        dataFile = new File(plugin.getDataFolder(), "players.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("Не удалось создать players.yml!");
                e.printStackTrace();
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        List<String> list = dataConfig.getStringList("installedPlayers");
        installedPlayers = new HashSet<>(list);
    }

    public void saveData() {
        dataConfig.set("installedPlayers", installedPlayers.stream().toList());
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Не удалось сохранить players.yml!");
            e.printStackTrace();
        }
    }

    public boolean hasInstalled(String uuidStr) {
        return installedPlayers.contains(uuidStr);
    }

    public void setInstalled(String uuidStr) {
        installedPlayers.add(uuidStr);
    }
}
