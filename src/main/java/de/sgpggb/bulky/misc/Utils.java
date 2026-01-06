package de.sgpggb.bulky.misc;

import de.sgpggb.bulky.Bulky;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.NumberFormat;
import java.util.Objects;

public class Utils {

    /**
     * returns the amount of items which fits in the players inventory
     * @param player the player
     * @param item the itemstack
     * @return the amount of items
     */
    public static int getFreeSpaceForItem(Player player, ItemStack item) {
        int maxStack = item.getMaxStackSize();
        int free = 0;

        for (ItemStack slot : player.getInventory().getStorageContents()) {

            //empty slot -> add whole stack
            if (slot == null || slot.getType().isAir()) {
                free += maxStack;
                continue;
            }

            //just same items
            if (!Utils.isSameItem(slot, item))
                continue;

            int spaceInSlot = maxStack - slot.getAmount();
            if (spaceInSlot > 0) {
                free += spaceInSlot;
            }
        }

        return free;
    }

    /**
     * returns the amount of items in players inventory
     * @param player the player
     * @param is the searched item
     * @return the amount of items
     */
    public static int getAmountOfItem(Player player, ItemStack is) {
        int amount = 0;

        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (item == null || item.getType().isAir())
                continue;

            if (!Utils.isSameItem(item, is))
                continue;

            amount += item.getAmount();
        }

        return amount;
    }

    /**
     * removes the given amount of the items of player
     * @param inventory the inventory
     * @param is the itemstack to remove
     * @param amount the amount of items to remove
     * @return true, if all items are removed from player
     */
    public static boolean removeItemsFromInventory(Inventory inventory, ItemStack is, int amount) {
        if (amount <= 0 || is == null || is.getType().isAir()) {
            return false;
        }

        int toRemove = amount;
        ItemStack[] contents = inventory.getStorageContents();

        for (int slot = 0; slot < contents.length && toRemove > 0; slot++) {
            ItemStack item = contents[slot];
            if (item == null)
                continue;

            if (!Utils.isSameItem(item, is))
                continue;

            int stackAmount = item.getAmount();

            //remove whole slot
            if (stackAmount <= toRemove) {
                toRemove -= stackAmount;
                contents[slot] = null;
            }
            //reduce slot
            else {
                item.setAmount(stackAmount - toRemove);
                contents[slot] = item;
                toRemove = 0;
            }
        }

        inventory.setStorageContents(contents);
        return toRemove == 0;
    }

    private static String formatBigNumber(long value) {
        if (value < 1000)
            return String.valueOf(value);

        int exp = (int) (Math.log10(value) / 3); // 1=K, 2=M, 3=B, ...
        double scaled = value / Math.pow(1000, exp);

        return String.format("%.2f%c", scaled, "KMGTPE".charAt(exp - 1));
    }


    private static String formatGerman(long value) {
        if (value >= 1_000_000_000_000_000_000L) {
            return String.format("%.2f Tr", value / 1_000_000_000_000_000_000.0); // Trillion
        } else if (value >= 1_000_000_000_000_000L) {
            return String.format("%.2f Brd", value / 1_000_000_000_000_000.0); // Billiarde
        } else if (value >= 1_000_000_000_000L) {
            return String.format("%.2f Bio", value / 1_000_000_000_000.0); // Billion
        } else if (value >= 1_000_000_000L) {
            return String.format("%.2f Mrd", value / 1_000_000_000.0); // Milliarde
        } else if (value >= 1_000_000L) {
            return String.format("%.2f Mio", value / 1_000_000.0);
        } else if (value >= 1_000L) {
            return String.format("%.2f Tsd", value / 1_000.0);
        }

        return String.format("%,d", value);
    }


    public static String formatNumber(long value) {
        String mode = Bulky.getInstance().getConfig().getString("options.numberFormat");
        return switch (mode) {
            case "short" -> formatBigNumber(value);
            case "german" -> formatGerman(value);
            case "long"    -> NumberFormat.getInstance().format(value);
            default        -> String.valueOf(value);
        };
    }

    public static boolean isSameItem(ItemStack a, ItemStack b) {
        if (a == null || b == null)
            return false;
        if (a.getType() != b.getType())
            return false;

        if (a.isSimilar(b))
            return true;

        ItemMeta metaA = a.getItemMeta();
        ItemMeta metaB = b.getItemMeta();

        return Objects.equals(metaA, metaB);
    }

    public static void playClickSound(Player player) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
    }

    public static void playCancelSound(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_LADDER_STEP, 0.5f, 1);
    }

    public static void playLevelUpSound(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1);
    }
}
