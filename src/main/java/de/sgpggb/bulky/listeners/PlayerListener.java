package de.sgpggb.bulky.listeners;

import de.sgpggb.bulky.Bulky;
import de.sgpggb.bulky.models.Manager;
import de.sgpggb.bulky.models.ChestContainer;
import de.sgpggb.bulky.guis.ChestGUI;
import de.sgpggb.pluginutilitieslib.logging.Logging;
import de.sgpggb.pluginutilitieslib.utils.ChatUtil;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {

    Manager manager = Bulky.getInstance().getManager();
    Logging log = Bulky.getInstance().getLog();
    String prefix = Bulky.getInstance().getPrefix();

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();

        if (!manager.isBulkyItem(item))
            return;

        Block block = event.getBlockPlaced();

        if (!(block.getState() instanceof Chest chest))
            return;

        item.getPersistentDataContainer().copyTo(chest.getPersistentDataContainer(), true);
        chest.update();
    }


    @EventHandler
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

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Block block = event.getClickedBlock();
        if (block == null || !(block.getState() instanceof Chest chest))
            return;

        if (!manager.isBulkyBlock(chest))
            return;

        event.setCancelled(true);

        Player player = event.getPlayer();
        ChestContainer chestContainer = manager.getOrCreate(chest, player);
        if (!chestContainer.validate()) {
            player.sendMessage(ChatUtil.mm(prefix +"<red>Something went wrong while opening the chest!"));
            return;
        }

        if (manager.isLocked(chest)) {
            player.sendMessage(ChatUtil.mm(prefix + "<red>This chest is already in use!"));
            player.playSound(player, Sound.BLOCK_CHEST_LOCKED, 1,1 );
            return;
        }

        Bulky.getInstance().getGuiHandler().openGUI(new ChestGUI(player, chestContainer));
    }
}
