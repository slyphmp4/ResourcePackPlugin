package me.slyph.resourcepack.commands;

import me.slyph.resourcepack.main.Main;
import me.slyph.resourcepack.utils.ColorUtil;
import me.slyph.resourcepack.utils.SiteChecker;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class RespCommand implements CommandExecutor {

    private final Main plugin;

    public RespCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sendMsgList(sender, "NotPlayer", "&cКоманду может использовать только игрок!");
            return true;
        }
        Player player = (Player) sender;

        if (!player.hasPermission("resp.admin")) {
            sendMsgList(player, "NoPermission", "&cУ вас нет прав!");
            return true;
        }

        openRespGui(player);
        return true;
    }

    private void openRespGui(Player player) {
        String url = plugin.getConfig().getString("ResourcePackLink", "");
        boolean isSiteUp = SiteChecker.isSiteOnline(url);

        String guiTitle = plugin.getConfig().getString("GuiTitle", "&H00FFFFСтатус RP-сайта");
        guiTitle = ColorUtil.translateHexColorCodes(guiTitle);

        Inventory inv = Bukkit.createInventory(null, 45, guiTitle);

        if (isSiteUp) {
            inv.setItem(13, createGuiItem("SiteOnline", Material.GREEN_STAINED_GLASS_PANE));
        } else {
            inv.setItem(13, createGuiItem("SiteOffline", Material.RED_STAINED_GLASS_PANE));
        }

        boolean rpEnabled = plugin.getConfig().getBoolean("ResourcePackRequestEnabled", true);
        if (rpEnabled) {
            inv.setItem(29, createGuiItem("RPEnabled",  Material.LIME_GLAZED_TERRACOTTA));
        } else {
            inv.setItem(29, createGuiItem("RPDisabled", Material.RED_GLAZED_TERRACOTTA));
        }

        inv.setItem(33, createGuiItem("ReloadItem", Material.LIME_STAINED_GLASS_PANE));

        player.openInventory(inv);
    }

    private ItemStack createGuiItem(String key, Material material) {
        String path = "GuiItems." + key;

        String name = plugin.getConfig().getString(path + ".Name", "&fБезымянный предмет");
        name = ColorUtil.translateHexColorCodes(name);

        List<String> loreLines = plugin.getConfig().getStringList(path + ".Lore");
        List<String> translatedLore = new ArrayList<>();
        for (String line : loreLines) {
            translatedLore.add(ColorUtil.translateHexColorCodes(line));
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (!translatedLore.isEmpty()) {
            meta.setLore(translatedLore);
        }
        item.setItemMeta(meta);

        return item;
    }

    private void sendMsgList(CommandSender sender, String key, String def) {
        List<String> lines = plugin.getConfig().getStringList("Messages." + key);
        if (lines.isEmpty()) {
            lines.add(def);
        }
        for (String line : lines) {
            sender.sendMessage(ColorUtil.translateHexColorCodes(line));
        }
    }
}
