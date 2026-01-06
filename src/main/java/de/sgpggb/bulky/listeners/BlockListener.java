package de.sgpggb.bulky.listeners;

import de.sgpggb.bulky.Bulky;
import de.sgpggb.bulky.misc.Utils;
import de.sgpggb.bulky.models.ChestContainer;
import de.sgpggb.bulky.models.Manager;
import de.sgpggb.pluginutilitieslib.logging.Logging;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ConcurrentHashMap;

public class BlockListener implements Listener {

    Logging log = Bulky.getInstance().getLog();
    Manager manager = Bulky.getInstance().getManager();

    private final ConcurrentHashMap<Location, Object> lockMap = new ConcurrentHashMap<>();

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onItemMoveEvent(InventoryMoveItemEvent event) {

        Inventory destInv = event.getDestination();
        if (destInv == null || destInv.getLocation() == null)
            return;

        InventoryHolder destHolder = destInv.getHolder();
        if (!(destHolder instanceof Chest chest))
            return;

        BlockState state = chest.getBlock().getState();
        if (!(state instanceof Chest chestState))
            return;

        if (!manager.isBulkyBlock(chest))
            return;

        ItemStack moving = event.getItem();
        if (moving == null || moving.getType() == Material.AIR)
            return;

        ChestContainer container = manager.getOrCreate(chestState, null);

        if (container == null || !container.isItemOk(moving))
            return;

        Inventory sourceInv = event.getSource();
        if (sourceInv == null || sourceInv.getLocation() == null)
            return;

        final Location sourceLoc = sourceInv.getLocation();
        final Location targetLoc = destInv.getLocation();

        Object sourceLock = lockMap.computeIfAbsent(sourceLoc, loc -> new Object());
        Object targetLock = lockMap.computeIfAbsent(targetLoc, loc -> new Object());

        synchronized (sourceLock) {
            synchronized (targetLock) {
                event.setCancelled(true);

                //delay 1 tick
                Bukkit.getScheduler().scheduleSyncDelayedTask(
                    Bulky.getInstance(),
                    () -> {
                        try {
                            Inventory currentSource = event.getSource();
                            Inventory currentDest = event.getDestination();
                            if (currentSource == null || currentDest == null)
                                return;

                            InventoryHolder currentDestHolder = currentDest.getHolder();
                            if (!(currentDestHolder instanceof Chest currentChest))
                                return;

                            BlockState currentState = currentChest.getBlock().getState();
                            if (!(currentState instanceof Chest currentChestState))
                                return;

                            if (!manager.isBulkyBlock(currentChest))
                                return;

                            ChestContainer currentContainer = manager.getOrCreate(currentChestState, null);

                            if (currentContainer == null || !currentContainer.validate())
                                return;

                            ItemStack eventItem = event.getItem();
                            if (eventItem == null || eventItem.getType() == Material.AIR)
                                return;

                            if (!currentContainer.isItemOk(eventItem))
                                return;

                            int slot = -1;
                            for (int i = 0; i < currentSource.getSize(); i++) {
                                ItemStack stack = currentSource.getItem(i);
                                if (stack == null || stack.getType() == Material.AIR)
                                    continue;

                                if (Utils.isSameItem(stack, eventItem) && stack.getAmount() > 0) {
                                    slot = i;
                                    break;
                                }
                            }

                            if (slot == -1)
                                return;

                            ItemStack stackInSlot = currentSource.getItem(slot);
                            if (stackInSlot == null || stackInSlot.getAmount() <= 0)
                                return;

                            int actuallyAdded = currentContainer.addItems(1);
                            if (actuallyAdded <= 0)
                                return;

                            int newAmount = stackInSlot.getAmount() - actuallyAdded;
                            if (newAmount > 0) {
                                stackInSlot.setAmount(newAmount);
                                currentSource.setItem(slot, stackInSlot);
                            } else {
                                currentSource.setItem(slot, null);
                            }

                        } finally {
                            lockMap.remove(sourceLoc);
                            lockMap.remove(targetLoc);
                        }
                    },
                    1L
                );
            }
        }
    }
}
