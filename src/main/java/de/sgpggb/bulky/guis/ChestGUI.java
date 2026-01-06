package de.sgpggb.bulky.guis;

import de.sgpggb.bulky.Bulky;
import de.sgpggb.bulky.misc.ItemBuilder;
import de.sgpggb.bulky.models.Manager;
import de.sgpggb.bulky.misc.Utils;
import de.sgpggb.bulky.models.ChestContainer;
import de.sgpggb.pluginutilitieslib.logging.Logging;
import de.sgpggb.pluginutilitieslib.utils.ChatUtil;
import de.sgpggb.pluginutilitieslib.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

public class ChestGUI extends CustomGUI {

    Manager manager = Bulky.getInstance().getManager();
    FileConfiguration config = Bulky.getInstance().getConfig();
    Logging log = Bulky.getInstance().getLog();
    String prefix = Bulky.getInstance().getPrefix();

    private final ChestContainer container;
    private int bulkOption = -1;

    public ChestGUI(Player player, ChestContainer container) {
        super(player, Bulky.getInstance().getConfig().getString("chest.gui.title"), 54);
        this.container = container;
        initInventory();
        manager.lock(container.getChest());
    }

    private final ItemStack moveOneStack = new ItemBuilder().playerHead(config.getString("chest.gui.moveOneStack.texture"))
        .displayName(ChatUtil.mm(config.getString("chest.gui.moveOneStack.name")))
        .lore(config.getStringList("chest.gui.moveOneStack.lore").stream().map(ChatUtil::mm).toList())
        .build();

    private final ItemStack moveNineStacks = new ItemBuilder().playerHead(config.getString("chest.gui.moveNineStacks.texture"))
        .displayName(ChatUtil.mm(config.getString("chest.gui.moveNineStacks.name")))
        .lore(config.getStringList("chest.gui.moveNineStacks.lore").stream().map(ChatUtil::mm).toList())
        .build();

    private final ItemStack moveTwentySevenStacks = new ItemBuilder().playerHead(config.getString("chest.gui.moveTwentySevenStacks.texture"))
        .displayName(ChatUtil.mm(config.getString("chest.gui.moveTwentySevenStacks.name")))
        .lore(config.getStringList("chest.gui.moveTwentySevenStacks.lore").stream().map(ChatUtil::mm).toList())
        .build();

    private final ItemStack moveItemsToChestButton = new ItemBuilder().playerHead(config.getString("chest.gui.moveItemsToChestButton.texture"))
        .displayName(ChatUtil.mm(config.getString("chest.gui.moveItemsToChestButton.name")))
        .lore(config.getStringList("chest.gui.moveItemsToChestButton.lore").stream().map(ChatUtil::mm).toList())
        .build();

    /**
     * creates the gui
     */
    @Override
    public void initInventory() {
        double percent = ((double) container.getAmount() / container.getMaxAmount()) * 100;
        String percentFormatted = String.format("%.2f", percent);

        //if not set, get first bulk option
        if (bulkOption == -1)
            bulkOption = manager.getNextBulk(bulkOption);

        //needs to be here for dynamic replacement
        ItemStack informationButton = new ItemBuilder().playerHead(config.getString("chest.gui.informationButton.texture"))
            .displayName(
                ChatUtil.mm(config.getString("chest.gui.informationButton.name")
                    .replace("<stored>", Utils.formatNumber(container.getAmount()))
                    .replace("<max>", Utils.formatNumber(container.getMaxAmount()))
                    .replace("<percent>", percentFormatted)))
            .lore(config.getStringList("chest.gui.informationButton.lore").stream().map(e ->
                ChatUtil.mm(e
                    .replace("<stored>", Utils.formatNumber(container.getAmount()))
                    .replace("<max>", Utils.formatNumber(container.getMaxAmount()))
                    .replace("<percent>", percentFormatted)))
                .toList())
            .build();

        ItemStack changeMaterialButton = new ItemBuilder(container.getItemStack().clone())
            .displayName(ChatUtil.mm(config.getString("chest.gui.changeMaterialButton.name")))
            .lore(config.getStringList("chest.gui.changeMaterialButton.lore").stream().map(ChatUtil::mm).toList())
            .build();

        ItemStack upgradeButton = new ItemBuilder().playerHead(config.getString("chest.gui.upgradeButton.texture"))
            .displayName(
                ChatUtil.mm(config.getString("chest.gui.upgradeButton.name")
                    .replace("<bulk>", "" + bulkOption)
                    .replace("<upgrades>", "" + container.getUpgrades())
                    .replace("<maxupgrades>", (manager.getMaxUpgrades() == -1 ? "∞" : "" + manager.getMaxUpgrades()))))
            .lore(config.getStringList("chest.gui.upgradeButton.lore").stream().map(l ->
                ChatUtil.mm(l
                    .replace("<bulk>", "" + bulkOption)
                    .replace("<upgrades>", "" + container.getUpgrades())
                    .replace("<maxupgrades>", (manager.getMaxUpgrades() == -1 ? "∞" : "" + manager.getMaxUpgrades()))))
                .toList())
            .build();

        inventory.clear();
        ItemStack item = container.getItemStack().clone();

        long toShow = container.getAmount();

        //fill row 1-4 with items
        for (int i = 0; i<4*9; i++) {
            if (toShow == 0)
                continue;

            int reduce = item.getMaxStackSize();
            if (toShow < item.getMaxStackSize())
                reduce = Math.toIntExact(toShow);

            //check inv space
            item.setAmount(reduce);
            addGUIItem(item, i);

            /*addGUIItem(item, i, e -> {
                playClickSound();
                removeItems(reduce);
            });*/
            toShow -= reduce;
        }

        //1 stacks
        addGUIItem(moveOneStack, 6*9-9, e -> {
            e.setCancelled(true);
            container.removeItems(item.getMaxStackSize(), true);
            playClickSound();
            initInventory();
        });

        //9 stacks
        addGUIItem(moveNineStacks, 6*9-8, e -> {
            e.setCancelled(true);
            container.removeItems( 9 * item.getMaxStackSize(), true);
            playClickSound();
            initInventory();
        });

        //27 stacks
        addGUIItem(moveTwentySevenStacks, 6*9-7, e -> {
            e.setCancelled(true);
            container.removeItems(27 * item.getMaxStackSize(), true);
            playClickSound();
            initInventory();
        });

        //move items to container
        addGUIItem(moveItemsToChestButton, 6*9-5, e -> {
            e.setCancelled(true);
            int added = container.addItems(Utils.getAmountOfItem(player, container.getItemStack()));
            playClickSound();

            if (added == 0)
                return;

            Utils.removeItemsFromInventory(player.getInventory(), container.getItemStack(), added);
            initInventory();
        });

        //change button
        addGUIItem(changeMaterialButton, 6*9-1, e -> {
            e.setCancelled(true);

            if (e.getCursor().isEmpty()) {
                playCancelSound();
                return;
            }

            if (container.getAmount() != 0) {
                player.sendMessage(ChatUtil.mm(prefix + "<red>Cant change material, when chest not empty!"));
                playCancelSound();
                return;
            }

            ItemStack is = e.getCursor().clone();
            is.setAmount(1);
            container.setItemStack(is);
            player.sendMessage(ChatUtil.mm(prefix + "<green>Changed material to " + container.getItemStack().getType().name()));
            playClickSound();
            initInventory();
        });

        //information button
        addGUIItem(informationButton, 6*9 - 2, e -> {
            e.setCancelled(true);
            playCancelSound();
        });

        //upgrade button
        if (Bukkit.getPluginManager().getPlugin("SGPGGBEconomySpigot") == null)
            return;

        addGUIItem(upgradeButton, 6*9 - 3, e -> {
            e.setCancelled(true);
            playClickSound();

            if (e.isRightClick()) {
                this.bulkOption = manager.getNextBulk(this.bulkOption);
                initInventory();
                return;
            }

            int amount = container.canUpgrade(this.bulkOption);
            if (amount == 0) {
                player.sendMessage(ChatUtil.mm(prefix + "<red>No upgrades left"));
                playCancelSound();
                return;
            }

            Bulky.getInstance().getGuiHandler().openGUI(new ConfirmGUI(player, container, amount, amount * manager.getUpgradePrice()));
        });
    }

    @Override
    public void onInventoryClose(InventoryCloseEvent event) {
        manager.unlock(container.getChest());
        container.getChest().close();
    }

    @Override
    public void onInventoryOpen(InventoryOpenEvent event) {
        container.getChest().open();
    }

    /**
     * when there is no action on given item
     * @param event the event
     */
    @Override
    public void onTopClick(InventoryClickEvent event) {
        int slot = event.getSlot();
        ItemStack clickedItem = event.getCurrentItem();
        ItemStack cursor = event.getCursor();
        ItemStack item = container.getItemStack().clone();
        event.setCancelled(true);

        //returns if slot is not first 4 rows
        if (!(slot >= 0 && slot < 36)) {
            return;
        }

        //shiftclick on stored item
        if (event.isShiftClick() && Utils.isSameItem(clickedItem, item)) {
            container.removeItems(clickedItem.getAmount(), true);
            initInventory();
            playClickSound();
            return;
        }

        //take items with empty cursor
        if (cursor.isEmpty()) {
            if (!Utils.isSameItem(clickedItem, item)) {
                return;
            }

            int a;
            if (event.isLeftClick()) {
                //give player the amount on cursor
                a = container.removeItems(clickedItem.getAmount(), false);
            } else if (event.isRightClick()) {
                // right click is halving the stack
                a = container.removeItems(clickedItem.getAmount() / 2, false);
            } else
                a = 0;
            item.setAmount(a);
            event.getView().setCursor(item);


            initInventory();
            playClickSound();
            return;
        }

        //take items with items on cursor
        if (!Utils.isSameItem(cursor, item)) {
            return;
        }

        int cursorAmount = cursor.getAmount();
        long containerBefore = container.getAmount();

        //merge cursor and container items
        if (Utils.isSameItem(clickedItem, item) && clickedItem.getAmount() < clickedItem.getMaxStackSize()) {
            int space = clickedItem.getMaxStackSize() - clickedItem.getAmount();
            int add = Math.min(space, cursorAmount);

            int added = container.addItems(add);
            cursor.setAmount(cursorAmount - add);

            if (cursor.getAmount() <= 0) {
                event.getView().setCursor(null);
            } else {
                event.getView().setCursor(cursor);
            }
            playClickSound();
            initInventory();
            return;
        }

        //store complete cursor
        int added = container.addItems(cursorAmount);
        event.getView().getCursor().setAmount(event.getView().getCursor().getAmount() - added);
        playClickSound();
        initInventory();
    }



    /**
     * handles bottom inventory clicks
     * @param event the event
     */
    @Override
    public void onBottomClick(InventoryClickEvent event) {
        //cancel this action for every item
        if (event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY))
            event.setCancelled(true);

        //if (event.getCurrentItem().getType() != container.getMaterial())
        //    return;

        //cancel everything container material related
        //event.setCancelled(true);

        //playClickSound();

        ItemStack item = container.getItemStack().clone();

        if (event.getCurrentItem() == null)
            return;

        //cancel shift clicks for non container items
        if (event.isShiftClick() && !Utils.isSameItem(event.getCurrentItem(), item)) {
            event.setCancelled(true);
            return;
        }

        //shift click for container items
        if (event.isShiftClick() && event.isLeftClick() && Utils.isSameItem(event.getCurrentItem(), item)) {
            int added = container.addItems(event.getCurrentItem().getAmount());
            event.getCurrentItem().setAmount(event.getCurrentItem().getAmount() - added);
            playClickSound();
            initInventory();
            log.debug("player " + player.getName() + " shift clicked " + added + "x " + item.getType().name());
        }
    }

    @Override
    public String getType() {
        return "bulky_main";
    }
}
