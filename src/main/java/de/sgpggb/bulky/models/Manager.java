package de.sgpggb.bulky.models;

import de.sgpggb.bulky.Bulky;
import de.sgpggb.bulky.misc.Constants;
import de.sgpggb.pluginutilitieslib.logging.Logging;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Manager {

    private Logging log = Bulky.getInstance().getLog();
    private FileConfiguration config = Bulky.getInstance().getConfig();

    private int upgradePrice;
    private int addCapacity;
    private int maxUpgrades;
    private long maxCapacity;
    private List<Integer> bulkOptions;

    public void reload() {
        this.upgradePrice = config.getInt("upgrades.price");
        this.addCapacity = config.getInt("upgrades.addCapacity");
        this.maxUpgrades = config.getInt("upgrades.maxUpgrades");
        this.maxCapacity = config.getLong("upgrades.maxCapacity");
        this.bulkOptions = config.getIntegerList("upgrades.bulkOptions");

        Bulky.getInstance().getGuiHandler().closeAll();
        containers.clear();
        lockedChests.clear();
    }

    /*
        additional lock, to be sure, and don't be fooled by minecraft shenanigans
     */
    private final Set<Location> lockedChests = ConcurrentHashMap.newKeySet();

    private final HashMap<Location, ChestContainer> containers = new HashMap<>();

    /**
     * locks the given chest
     * @param chest the chest
     */
    public void lock(Chest chest) {
        lockedChests.add(chest.getLocation());
    }

    /**
     * checks if the given chest is currently locked
     * @param chest the chest
     * @return true if chest is locked
     */
    public boolean isLocked(Chest chest) {
        return lockedChests.contains(chest.getLocation());
    }

    /**
     * unlocks the given chest
     * @param chest the chest
     */
    public void unlock(Chest chest) {
        lockedChests.remove(chest.getLocation());
    }

    /**
     * unlocks all locked chests
     */
    public void unlockAll() {
        for (Location loc : lockedChests) {
            if (!(loc.getBlock().getState() instanceof Chest chest))
                continue;
            unlock(chest);
        }
        lockedChests.clear();
    }

    /**
     * checks if given itemstack is a bulky chest
     * @param itemStack the itemstack
     * @return true, if it is a bulky chest
     */
    public boolean isBulkyItem(ItemStack itemStack) {
        if (!itemStack.getType().equals(Material.CHEST))
            return false;

        ItemMeta meta = itemStack.getItemMeta();
        return meta.getPersistentDataContainer().has(Constants.bulkyChestKey);
    }

    /**
     * checks if given block is a bulky chest
     * @param block the block
     * @return true, if it is a bulky chest
     */
    public boolean isBulkyBlock(Block block) {
        if (!block.getType().equals(Material.CHEST))
            return false;
        if (!(block.getState() instanceof Chest chest))
            return false;
        return isBulkyBlock(chest);
    }

    /**
     * checks if given chest is a bulky chest
     * @param chest the chest
     * @return true, if it is a bulky chest
     */
    public boolean isBulkyBlock(Chest chest) {
        return chest.getPersistentDataContainer().has(Constants.bulkyChestKey);
    }

    /**
     *
     * @return the amount of storage which will added with one upgrade
     */
    public int getAddCapacity() {
        return addCapacity;
    }

    /**
     *
     * @return the max amount of upgrades
     */
    public int getMaxUpgrades() {
        return maxUpgrades;
    }

    /**
     *
     * @return all bulk options as a list
     */
    public List<Integer> getBulkOptions() {
        return bulkOptions;
    }

    /**
     *
     * @param current the current bulk option
     * @return the next bulk option
     */
    public int getNextBulk(int current) {
        int index = bulkOptions.indexOf(current);

        if (index == -1)  // fallback, wenn index nicht gefunden wird
            return bulkOptions.getFirst();

        index++;

        //return first is reached last
        if (index >= bulkOptions.size())
            return bulkOptions.getFirst();

        return bulkOptions.get(index);
    }

    /**
     *
     * @return the price per upgrade
     */
    public int getUpgradePrice() {
        return upgradePrice;
    }

    /**
     *
     * @return the overall max capacity
     */
    public long getMaxCapacity() {
        return maxCapacity;
    }

    /**
     * returns or creates a chest container object
     * @param chest the chest
     * @return the chest container object
     */
    public ChestContainer getOrCreate(Chest chest) {
        return getOrCreate(chest.getLocation());
    }

    /**
     * returns or creates a chest container object
     * @param location the location of the chest
     * @return the chest container object
     */
    public ChestContainer getOrCreate(Location location) {
        if (getContainer(location) != null)
            return getContainer(location);

        if (!(location.getBlock().getState() instanceof Chest chest))
            return null;

        ChestContainer container = new ChestContainer(chest);
        addContainer(container);
        return container;
    }

    public ChestContainer getContainer(Location location) {
        return containers.getOrDefault(location, null);
    }

    public void addContainer(ChestContainer container) {
        containers.put(container.getChest().getLocation(), container);
    }

    public void removeContainer(ChestContainer container) {
        containers.remove(container.getChest().getLocation());
    }

    public HashMap<Location, ChestContainer> getContainers() {
        return containers;
    }
}
