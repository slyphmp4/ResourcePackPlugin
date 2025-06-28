package me.slyph.resourcepack.listeners;

import me.slyph.resourcepack.data.DataManager;
import me.slyph.resourcepack.main.Main;
import me.slyph.resourcepack.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

import java.util.List;
import java.util.UUID;

import static org.bukkit.event.player.PlayerResourcePackStatusEvent.Status;

public class ResourcePackListener implements Listener {

    private final Main plugin;
    private final DataManager dataManager;

    private String resourcePackUrl;
    private List<String> kickMessages;
    private List<String> successMessages;

    public ResourcePackListener(Main plugin, DataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        loadConfigValues();
    }

    private void loadConfigValues() {
        resourcePackUrl = plugin.getConfig().getString("ResourcePackLink", "");
        kickMessages = plugin.getConfig().getStringList("KickMessages");
        successMessages = plugin.getConfig().getStringList("SuccessMessages");

        if (kickMessages.isEmpty()) {
            kickMessages.add("&HFF0000Вы не установили наш ресурс-пак!");
        }
        if (successMessages.isEmpty()) {
            successMessages.add("&H00FF00Спасибо за установку нашего РП!");
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        boolean rpEnabled = plugin.getConfig().getBoolean("ResourcePackRequestEnabled", true);
        if (!rpEnabled) return;

        Player player = event.getPlayer();
        if (resourcePackUrl == null || resourcePackUrl.isEmpty()) {
            Bukkit.getLogger().warning("[ResourcePackPlugin] ResourcePackLink пустая!");
            return;
        }
        player.setResourcePack(resourcePackUrl);
    }

    @EventHandler
    public void onResourcePackStatus(PlayerResourcePackStatusEvent event) {
        Player player = event.getPlayer();
        Status status = event.getStatus();

        switch (status) {
            case DECLINED:
            case FAILED_DOWNLOAD:
                StringBuilder sb = new StringBuilder();
                for (String line : kickMessages) {
                    sb.append(ColorUtil.translateHexColorCodes(line)).append("\n");
                }
                player.kickPlayer(sb.toString());
                break;

            case SUCCESSFULLY_LOADED:
                UUID uuid = player.getUniqueId();
                String uuidStr = uuid.toString();
                if (!dataManager.hasInstalled(uuidStr)) {
                    for (String line : successMessages) {
                        player.sendMessage(ColorUtil.translateHexColorCodes(line));
                    }
                    dataManager.setInstalled(uuidStr);
                }
                break;

            default:
                break;
        }
    }
}
