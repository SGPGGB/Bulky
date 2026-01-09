package de.sgpggb.bulky.guis;

import de.sgpggb.bulky.misc.Messages;
import de.sgpggb.pluginutilitieslib.logging.Logging;
import de.sgpggb.pluginutilitieslib.utils.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GUIHandler implements Listener {
    private final Map<UUID, CustomGUI> guis = new HashMap<>();
    private final Logging logger;

    public GUIHandler(Logging logger, JavaPlugin plugin) {
        this.logger = logger;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public CustomGUI getGUI(Player player) {
        if (guis.containsKey(player.getUniqueId()))
            return guis.get(player.getUniqueId());
        return null;
    }

    private void setGUI(CustomGUI gui) {
        guis.put(gui.getPlayer().getUniqueId(), gui);
    }

    private void removeGUI(Player player) {
        guis.remove(player.getUniqueId());
    }

    public void closeAll() {
        for (UUID uuid : guis.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null)
                continue;

            closeGUI(player);
            player.sendMessage(Messages.get(Messages.MSG.ERROR_CHEST_CLOSED));
        }
    }

    public void openGUI(CustomGUI gui) {
        setGUI(gui);
        gui.getPlayer().openInventory(gui.getInventory());
        logger.debug("Opened " + gui.getType() + " gui for player " + gui.getPlayer().getName());
    }

    public void closeGUI(Player player) {
        if (getGUI(player) == null)
            return;
        removeGUI(player);
        player.closeInventory();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player))
            return;
        CustomGUI gui = getGUI(player);
        if (gui == null)
            return;
        if (!gui.getInventory().equals(event.getInventory()))
            return;
        gui.click(event);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player))
            return;
        CustomGUI gui = getGUI(player);
        if (gui == null)
            return;
        if (!gui.getInventory().equals(event.getInventory()))
            return;
        gui.onInventoryClose(event);
        removeGUI(player);
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player))
            return;
        CustomGUI gui = getGUI(player);
        if (gui == null)
            return;
        if (!gui.getInventory().equals(event.getInventory()))
            return;
        gui.onInventoryOpen(event);
    }
}
