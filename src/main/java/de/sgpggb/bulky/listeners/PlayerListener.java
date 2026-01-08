package de.sgpggb.bulky.listeners;

import de.sgpggb.bulky.Bulky;
import de.sgpggb.bulky.models.Manager;
import de.sgpggb.bulky.models.ChestContainer;
import de.sgpggb.bulky.guis.ChestGUI;
import de.sgpggb.pluginutilitieslib.logging.Logging;
import de.sgpggb.pluginutilitieslib.utils.ChatUtil;
import de.sgpggb.pluginutilitieslib.utils.ItemUtils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {

    Manager manager = Bulky.getInstance().getManager();
    Logging log = Bulky.getInstance().getLog();
    String prefix = Bulky.getInstance().getPrefix();

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBulkyPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();

        if (!manager.isBulkyItem(item))
            return;

        Block block = event.getBlockPlaced();

        if (!(block.getState() instanceof Chest chest))
            return;

        item.getPersistentDataContainer().copyTo(chest.getPersistentDataContainer(), true);
        chest.update();
    }

    @EventHandler(ignoreCancelled = true)
    public void onChestPlace(BlockPlaceEvent e) {
        Block placed = e.getBlockPlaced();
        Block clicked = e.getBlockAgainst();

        if (placed.getType() != Material.CHEST)
            return;

        boolean placingBulkyChest = manager.isBulkyItem(e.getItemInHand());

        for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {
            Block neighbor = placed.getRelative(face);

            if (neighbor.getType() != Material.CHEST)
                continue;

            boolean neighborIsBulky = manager.isBulkyBlock(neighbor);

            if (placingBulkyChest) {
                e.setCancelled(true);
                e.getPlayer().sendMessage(ChatUtil.mm(prefix + "<red>BulkyChests may not connect to other chests!"));
                return;
            }

            if (neighborIsBulky) {
                e.setCancelled(true);
                e.getPlayer().sendMessage(ChatUtil.mm(prefix + "<red>You can't place a chest next to a BulkyChest!"));
                return;
            }
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!(block.getState() instanceof Chest chest))
            return;

        if (!manager.isBulkyBlock(chest))
            return;

        Player player = event.getPlayer();
        ChestContainer chestContainer = manager.getOrCreate(chest, player);
        if (!chestContainer.validate()) {
            player.sendMessage(ChatUtil.mm(prefix + "<red>Something went wrong while breaking the chest!"));
            event.setCancelled(true);
            return;
        }

        if (manager.isLocked(chest)) {
            player.sendMessage(ChatUtil.mm(prefix + "<red>This chest is currently locked!"));
            event.setCancelled(true);
            return;
        }

        long amount = chestContainer.getAmount();
        int upgrades = chestContainer.getUpgrades();

        if (amount != 0) {
            player.sendMessage(ChatUtil.mm(prefix + "<red>Your Bulky is not empty!"));
            event.setCancelled(true);
            return;
        }

        if (chestContainer.getItemStack() == null)
            throw new IllegalStateException("item stack is null!");

        event.setDropItems(false);
        block.getWorld().dropItemNaturally(block.getLocation(), ChestContainer.createBulky(chestContainer.getItemStack(), amount, upgrades));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        if (event.getHand() != EquipmentSlot.HAND)
            return;

        Block block = event.getClickedBlock();
        if (block == null || !(block.getState() instanceof Chest chest))
            return;

        if (!manager.isBulkyBlock(chest))
            return;

        Player player = event.getPlayer();
        ItemStack handItem = event.getItem();

        if (player.isSneaking() && ItemUtils.empty(handItem)) {
            open(player, chest);
            event.setCancelled(true);
        }

        if (!player.isSneaking()) {
            open(player, chest);
            event.setCancelled(true);
        }
    }

    private void open(Player player, Chest chest) {
        ChestContainer chestContainer = manager.getOrCreate(chest, player);
        if (!chestContainer.validate()) {
            player.sendMessage(ChatUtil.mm(prefix + "<red>Something went wrong while opening the chest!"));
            return;
        }

        if (manager.isLocked(chest)) {
            player.sendMessage(ChatUtil.mm(prefix + "<red>This chest is already in use!"));
            player.playSound(player, Sound.BLOCK_CHEST_LOCKED, 1, 1);
            return;
        }

        Bulky.getInstance().getGuiHandler().openGUI(new ChestGUI(player, chestContainer));
    }
}
