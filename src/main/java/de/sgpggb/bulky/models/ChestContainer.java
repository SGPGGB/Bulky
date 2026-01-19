package de.sgpggb.bulky.models;

import de.sgpggb.bulky.Bulky;
import de.sgpggb.bulky.guis.ChestGUI;
import de.sgpggb.bulky.misc.Constants;
import de.sgpggb.bulky.misc.Messages;
import de.sgpggb.bulky.misc.Utils;
import de.sgpggb.pluginutilitieslib.logging.Logging;
import de.sgpggb.pluginutilitieslib.utils.ChatUtil;
import de.sgpggb.pluginutilitieslib.utils.ItemUtils;
import de.sgpggb.sgpggbeconomyspigot.EconomyAPI;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class ChestContainer {

    Logging log = Bulky.getInstance().getLog();
    Manager manager = Bulky.getInstance().getManager();

    Chest chest;
    long amount;
    int upgrades;
    ItemStack itemStack;

    //runtime variables
    Player player;

    /**
     *
     * @param chest
     */
    public ChestContainer(Chest chest) {
        this.chest = chest;
        this.amount = chest.getPersistentDataContainer().get(Constants.bulkyChestStorageAmount, PersistentDataType.LONG);
        this.upgrades = chest.getPersistentDataContainer().get(Constants.bulkyChestStorageUpgrades, PersistentDataType.INTEGER);
        String item = chest.getPersistentDataContainer().get(Constants.bulkyChestStorageItem, PersistentDataType.STRING);

        if (item == null) {
            this.itemStack = null;
        } else {
            try {
                this.itemStack = ItemUtils.convertStringToItemStack(item);
            } catch (IllegalArgumentException ex) {
                this.itemStack = null;
            }
        }

        if (this.itemStack == null)
            this.itemStack = new ItemStack(Material.STONE);

        log.debug("created chestcontainer at " + chest.getLocation());
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return this.player;
    }

    public boolean isItemOk(ItemStack is) {
        return Utils.isSameItem(is, this.itemStack);
    }

    /**
     * removes items from the container and sends it to the players inventory.
     * @param remove the amount to remove
     * @param giveItems true when items should be sent to player
     * @return the amount the player has received
     */
    public int removeItems(int remove, boolean giveItems) {
        int toRemove = (int) Math.min(amount, remove);

        //check player inv
        if (player != null)
            toRemove = Math.min(toRemove, Utils.getFreeSpaceForItem(player, itemStack));

        if (toRemove <= 0)
            return 0;


        if (giveItems && player != null) {
            ItemStack output = this.itemStack.clone();
            output.setAmount(toRemove);
            player.getInventory().addItem(output);
        }

        setAmount(amount - toRemove);
        return toRemove;
    }

    /**
     * Fügt eine Anzahl Items zum Container hinzu.
     * @param add die Anzahl der Items, die hinzugefügt werden soll
     * @return die Anzahl, die tatsächlich hinzugefügt wurde
     */
    public int addItems(int add) {
        long freeSpace = getMaxAmount() - amount;

        int toAdd = (int) Math.min(add, freeSpace);

        if (toAdd <= 0)
            return 0;

        setAmount(amount + toAdd);
        return toAdd;
    }

    public static ItemStack createBulky(ItemStack is) {
        return createBulky(is, 0, 1);
    }

    public static ItemStack createBulky(ItemStack is, long amount, int upgrades) {
        ItemStack chest = new ItemStack(Material.CHEST);
        ItemMeta meta = chest.getItemMeta();
        meta.displayName(ChatUtil.mm(Bulky.getInstance().getConfig().getString("chest.name")));
        meta.lore(List.of(ChatUtil.mm(""), ChatUtil.mm("")));
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        setPDC(pdc, is, amount, upgrades);
        chest.setItemMeta(meta);
        return chest;
    }

    private static void setPDC(PersistentDataContainer pdc, ItemStack is, long amount, int upgrades) {
        pdc.set(Constants.bulkyChestKey, PersistentDataType.BOOLEAN, true);
        pdc.set(Constants.bulkyChestStorageItem, PersistentDataType.STRING, ItemUtils.convertItemStackToString(is));
        pdc.set(Constants.bulkyChestStorageAmount, PersistentDataType.LONG, amount);
        pdc.set(Constants.bulkyChestStorageUpgrades, PersistentDataType.INTEGER, upgrades);
    }

    /**
     * checks if the chest can be upgraded
     * @param amount the amount of upgrades
     * @return the amount of purchasable upgrades.
     */
    public int canUpgrade(int amount) {

        //ignore if max upgrades set to unlimited
        if (manager.getMaxUpgrades() != -1) {
            //reached max level
            if (this.upgrades >= manager.getMaxUpgrades())
                return 0;

            //cap bulk on max upgrades
            amount = Math.min(amount, manager.getMaxUpgrades() - this.upgrades);
        }


        //check max capacity
        long added = (long) manager.getAddCapacity() * amount;
        long space = manager.getMaxCapacity() - this.amount;

        if (space <= 0)
            return 0;

        //reduce bulk
        if (added > space)
            amount = (int) (space / manager.getAddCapacity());

        return Math.max(amount, 0);
    }

    /**
     * increasing tier of chest
     * @param p the player
     */
    public void doUpgrade(Player p, int bulk) {
        bulk = canUpgrade(bulk);
        if (bulk == 0) {
            p.sendMessage(Messages.get(Messages.MSG.ERROR_UPGRADE_NO_UPGRADES_LEFT));
            return;
        }

        int totalPrice = manager.getUpgradePrice() * bulk;

        //check money
        if (!EconomyAPI.hasEnough(p.getUniqueId(), totalPrice)) {
            p.sendMessage(Messages.get(Messages.MSG.ERROR_UPGRADE_NO_MONEY));
            return;
        }

        //remove money
        final int amount = bulk;
        EconomyAPI.take(p.getUniqueId(), totalPrice, "", "", new EconomyAPI.EconomyFuture() {
                @Override
                protected void onSuccess() {
                    setUpgrades(getUpgrades() + amount);

                    Utils.playLevelUpSound(p);

                    log.info(p.getName() + " purchased #" + amount + " upgrades for " + totalPrice);

                    //reopen the gui
                    Bulky.getInstance().getGuiHandler().openGUI(new ChestGUI(player, manager.getOrCreate(chest)));
                }

                @Override
                protected void onFailure() {
                    p.sendMessage(Messages.get(Messages.MSG.ERROR_UPGRADE_UNKNOWN));

                    Utils.playCancelSound(p);
                }

                @Override
                protected void onError() {
                    p.sendMessage(Messages.get(Messages.MSG.ERROR_UPGRADE_UNKNOWN));

                    Utils.playCancelSound(p);
                }
        });

    }



    /**
     * validates the chest
     * @return
     */
    public boolean validate() {
        if (chest == null)
            return false;
        if (!chest.getPersistentDataContainer().has(Constants.bulkyChestKey))
            return false;
        if (!chest.getPersistentDataContainer().has(Constants.bulkyChestStorageItem))
            return false;
        if (!chest.getPersistentDataContainer().has(Constants.bulkyChestStorageAmount))
            return false;
        if (!chest.getPersistentDataContainer().has(Constants.bulkyChestStorageUpgrades))
            return false;
        if (amount < -1)
            return false;
        if (upgrades < -1)
            return false;
        if (itemStack == null || itemStack.isEmpty())
            return false;
        return true;
    }

    public Chest getChest() {
        return chest;
    }

    public long getAmount() {
        return amount;
    }

    private void setAmount(long amount) {
        this.amount = amount;
        setPDC(chest.getPersistentDataContainer(), itemStack, amount, upgrades);
        chest.update();
    }

    public void setItemStack(ItemStack is) {
        this.itemStack = is;
        if (amount != 0)
            throw new IllegalStateException("amount must be 0 when changing material!");
        setPDC(chest.getPersistentDataContainer(), itemStack, amount, upgrades);
        chest.update();
    }

    /**
     * returns the max storable amount of items
     * @return
     */
    public long getMaxAmount() {
        return Math.min((long) upgrades * manager.getAddCapacity(), manager.getMaxCapacity());
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public int getUpgrades() {
        return upgrades;
    }

    public void setUpgrades(int upgrades) {
        this.upgrades = upgrades;
        setPDC(chest.getPersistentDataContainer(), itemStack, amount, upgrades);
        chest.update();
    }
}
