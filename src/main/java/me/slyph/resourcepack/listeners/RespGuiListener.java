package me.slyph.resourcepack.listeners;

import me.slyph.resourcepack.main.Main;
import me.slyph.resourcepack.utils.ColorUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class RespGuiListener implements Listener {

    private final Main plugin;
    private boolean reloading = false;
    private boolean reloadingWarned = false;

    private final Material[] ANIMATION_COLORS = {
            Material.LIME_STAINED_GLASS_PANE,
            Material.RED_STAINED_GLASS_PANE,
            Material.BLUE_STAINED_GLASS_PANE,
            Material.YELLOW_STAINED_GLASS_PANE,
            Material.MAGENTA_STAINED_GLASS_PANE
    };

    public RespGuiListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView() == null) return;

        String guiTitle = plugin.getConfig().getString("GuiTitle", "&H00FFFFСтатус RP-сайта");
        guiTitle = ColorUtil.translateHexColorCodes(guiTitle);
        if (!event.getView().getTitle().equals(guiTitle)) return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= event.getInventory().getSize()) return;

        switch (slot) {
            case 29:
                toggleRP(player);
                break;
            case 33:
                if (!reloading) {
                    startReloadAnimation(player, event.getInventory());
                } else {
                    if (!reloadingWarned) {
                        sendMsgList(player, "ReloadInProgress", "&cСейчас идёт перезагрузка, подождите...");
                        reloadingWarned = true;
                    }
                }
                break;
            default:
                break;
        }
    }

    private void toggleRP(Player player) {
        boolean current = plugin.getConfig().getBoolean("ResourcePackRequestEnabled", true);
        plugin.getConfig().set("ResourcePackRequestEnabled", !current);
        plugin.saveConfig();

        reopenGui(player);
    }

    private void startReloadAnimation(Player player, Inventory inv) {
        reloading = true;
        reloadingWarned = false;

        plugin.reloadConfig();
        sendMsgList(player, "ReloadStart", "&aКонфиг перезагружен, идёт анимация...");

        new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count >= ANIMATION_COLORS.length) {
                    finishAnimation(inv, player);
                    this.cancel();
                    return;
                }

                FileConfiguration cfg = plugin.getConfig();
                String path = "GuiItems.ReloadingItem";

                String itemName = cfg.getString(path + ".Name", "&eПерезагрузка... {count} / 5");
                itemName = ColorUtil.translateHexColorCodes(itemName);
                itemName = itemName.replace("{count}", String.valueOf(count + 1));

                List<String> loreList = cfg.getStringList(path + ".Lore");
                List<String> translatedLore = new ArrayList<>();
                for (String line : loreList) {
                    line = ColorUtil.translateHexColorCodes(line);
                    line = line.replace("{count}", String.valueOf(count + 1));
                    translatedLore.add(line);
                }

                Material mat = ANIMATION_COLORS[count];
                ItemStack animPane = new ItemStack(mat);
                ItemMeta meta = animPane.getItemMeta();
                meta.setDisplayName(itemName);
                if (!translatedLore.isEmpty()) {
                    meta.setLore(translatedLore);
                }
                animPane.setItemMeta(meta);

                inv.setItem(33, animPane);

                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);

                count++;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void finishAnimation(Inventory inv, Player player) {
        FileConfiguration cfg = plugin.getConfig();
        String path = "GuiItems.ReloadItem";

        String reloadName = cfg.getString(path + ".Name", "&eПерезагрузить конфиг");
        reloadName = ColorUtil.translateHexColorCodes(reloadName);

        List<String> loreList = cfg.getStringList(path + ".Lore");
        List<String> translatedLore = new ArrayList<>();
        for (String line : loreList) {
            translatedLore.add(ColorUtil.translateHexColorCodes(line));
        }

        ItemStack donePane = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta doneMeta = donePane.getItemMeta();
        doneMeta.setDisplayName(reloadName);
        if (!translatedLore.isEmpty()) {
            doneMeta.setLore(translatedLore);
        }
        donePane.setItemMeta(doneMeta);

        inv.setItem(33, donePane);

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);

        sendMsgList(player, "ReloadFinish", "&aПерезагрузка завершена!");

        reloading = false;
        reloadingWarned = false;
    }

    private void reopenGui(Player player) {
        player.closeInventory();
        player.performCommand("resp");
    }

    private void sendMsgList(Player player, String key, String def) {
        String path = "Messages." + key;
        List<String> lines = plugin.getConfig().getStringList(path);
        if (lines.isEmpty()) {
            lines.add(def);
        }
        for (String line : lines) {
            player.sendMessage(ColorUtil.translateHexColorCodes(line));
        }
    }
}
