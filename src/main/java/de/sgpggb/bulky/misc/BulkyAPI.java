package de.sgpggb.bulky.misc;

import de.sgpggb.bulky.Bulky;
import de.sgpggb.bulky.models.ChestContainer;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

public class BulkyAPI {

    /**
     * Checks if the given block is a Bulky chest.
     *
     * @param block the block to check
     * @return true if the block is managed as a Bulky chest, otherwise false
     */
    public static boolean isBulkyChest(Block block) {
        return Bulky.getInstance().getManager().isBulkyBlock(block);
    }

    /**
     * Checks if the given chest is a Bulky chest.
     *
     * @param chest the chest to check
     * @return true if the chest is managed as a Bulky chest, otherwise false
     */
    public static boolean isBulkyChest(Chest chest) {
        return Bulky.getInstance().getManager().isBulkyBlock(chest);
    }

    /**
     * Returns how much free space is left in the given Bulky chest.
     * If the chest is not a Bulky chest or no container exists, 0 is returned.
     *
     * @param chest the chest to query
     * @return free space (max amount - stored amount), or 0 if not available
     */
    public static double getFreeSpace(Chest chest) {
        if (!isBulkyChest(chest))
            return 0;
        ChestContainer container = Bulky.getInstance().getManager().getOrCreate(chest.getLocation());
        if (container == null)
            return 0;
        return container.getMaxAmount() - container.getAmount();
    }

    /**
     * Returns the currently stored amount in the given Bulky chest.
     * If the chest is not a Bulky chest or no container exists, 0 is returned.
     *
     * @param chest the chest to query
     * @return stored amount, or 0 if not available
     */
    public static double getStoredAmount(Chest chest) {
        if (!isBulkyChest(chest))
            return 0;
        ChestContainer container = Bulky.getInstance().getManager().getOrCreate(chest.getLocation());
        if (container == null)
            return 0;
        return container.getAmount();
    }

    /**
     * Returns the maximum capacity (amount) of the given Bulky chest.
     * If the chest is not a Bulky chest or no container exists, 0 is returned.
     *
     * @param chest the chest to query
     * @return max capacity, or 0 if not available
     */
    public static double getMaxAmount(Chest chest) {
        if (!isBulkyChest(chest))
            return 0;
        ChestContainer container = Bulky.getInstance().getManager().getOrCreate(chest.getLocation());
        if (container == null)
            return 0;
        return container.getMaxAmount();
    }

    /**
     * Adds items to the given Bulky chest.
     * Returns 0 if:
     *   the chest is not a Bulky chest
     *   no container exists
     *   the chest is locked
     *   the provided item does not match the chest's stored item
     *
     * @param chest the target chest
     * @param itemStack the item type to add
     * @param amount the amount to add
     * @return the amount that was actually added (can be lower than requested), or 0 on failure
     */
    public static int addAmount(Chest chest, ItemStack itemStack, int amount) {
        if (!isBulkyChest(chest))
            return 0;
        ChestContainer container = Bulky.getInstance().getManager().getOrCreate(chest.getLocation());
        if (container == null)
            return 0;
        if (Bulky.getInstance().getManager().isLocked(chest))
            return 0;
        if (Utils.isSameItem(itemStack, container.getItemStack()))
            return 0;
        return container.addItems(amount);
    }

    /**
     * Removes items from the given Bulky chest.
     * Returns 0 if:
     *   the chest is not a Bulky chest
     *   no container exists
     *   the provided item does not match the chest's stored item
     *
     * @param chest the target chest
     * @param itemStack the item type to remove
     * @param amount the amount to remove
     * @return the amount that was actually removed (can be lower than requested), or 0 on failure
     */
    public static int removeAmount(Chest chest, ItemStack itemStack, int amount) {
        if (!isBulkyChest(chest))
            return 0;
        ChestContainer container = Bulky.getInstance().getManager().getOrCreate(chest.getLocation());
        if (container == null)
            return 0;
        if (Utils.isSameItem(itemStack, container.getItemStack()))
            return 0;
        return container.removeItems(amount, false);
    }

    /**
     * Checks whether the given item matches the item stored in the Bulky chest.
     *
     * @param chest the chest to compare against
     * @param itemStack the item to check
     * @return true if the item matches the stored item, otherwise false
     */
    public static boolean isSameItem(Chest chest, ItemStack itemStack) {
        if (!isBulkyChest(chest))
            return false;
        ChestContainer container = Bulky.getInstance().getManager().getOrCreate(chest.getLocation());
        if (container == null)
            return false;
        return Utils.isSameItem(itemStack, container.getItemStack());
    }

    /**
     * Returns the item stored in the given Bulky chest.
     *
     * @param chest the chest to query
     * @return the stored item, or null if not a Bulky chest or not available
     */
    public static ItemStack getStoredItem(Chest chest) {
        if (!isBulkyChest(chest))
            return null;
        ChestContainer container = Bulky.getInstance().getManager().getOrCreate(chest.getLocation());
        if (container == null)
            return null;
        return container.getItemStack();
    }

    /**
     * Creates the Bulky Chest ItemStack.
     *
     * @param itemStack the base item
     * @return the Bulky item representation
     */
    public static ItemStack getBulkyItem(ItemStack itemStack) {
        return ChestContainer.createBulky(itemStack);
    }

}
