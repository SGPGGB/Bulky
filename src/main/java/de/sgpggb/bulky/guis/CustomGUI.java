package de.sgpggb.bulky.guis;

import de.sgpggb.pluginutilitieslib.utils.ChatUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.function.Consumer;

public abstract class CustomGUI {
    protected Player player;
    protected Inventory inventory;
    protected final String title;
    private final Consumer<InventoryClickEvent>[] actions;

    public CustomGUI(Player player, String title, int size) {
        this.player = player;
        this.title = title;
        this.inventory = Bukkit.createInventory(null, size, ChatUtil.mm(title));
        this.actions = new Consumer[size];
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    public abstract void initInventory();

    protected void addGUIItem(Material material, int slot, Component name, List<Component> lore, Consumer<InventoryClickEvent> action) {
        ItemStack item = new ItemStack(material, 1);
        this.addGUIItem(item, slot, name, lore, action);
    }

    protected void addGUIItem(ItemStack item, int slot, Component name, List<Component> lore, Consumer<InventoryClickEvent> action) {
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name);
        if (lore != null)
            meta.lore(lore);
        item.setItemMeta(meta);
        addGUIItem(item, slot, action);
    }

    protected void addGUIItem(ItemStack item, int slot) {
        addGUIItem(item, slot, null);
    }

    protected void addGUIItem(ItemStack item, int slot, Consumer<InventoryClickEvent> action) {
        inventory.setItem(slot, item);
        actions[slot] = action;
    }

    public abstract void onInventoryClose(InventoryCloseEvent event);

    public abstract void onInventoryOpen(InventoryOpenEvent event);

    public abstract void onBottomClick(InventoryClickEvent event);

    public abstract void onTopClick(InventoryClickEvent event);

    public abstract String getType();

    protected void playClickSound() {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
    }

    protected void playCancelSound() {
        player.playSound(player.getLocation(), Sound.BLOCK_LADDER_STEP, 0.5f, 1);
    }

    protected void playLevelUpSound() {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1);
    }

    public void click(InventoryClickEvent event) {
        //out of inventory
        if (event.getClickedInventory() == null)
            return;

        //run consumer in top inventory
        if (event.getClickedInventory() == event.getView().getTopInventory()) {
            Consumer<InventoryClickEvent> c = actions[event.getSlot()];
            if (c != null) {
                c.accept(event);
            } else
                onTopClick(event);
        }

        //bottom inventory
        if (event.getClickedInventory() == event.getView().getBottomInventory()) {
            onBottomClick(event);
        }
    }

    public Player getPlayer() {
        return player;
    }

    public String getTitle() {
        return title;
    }
}
