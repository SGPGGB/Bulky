package de.sgpggb.bulky.guis;

import de.sgpggb.bulky.Bulky;
import de.sgpggb.bulky.misc.ItemBuilder;
import de.sgpggb.bulky.models.ChestContainer;
import de.sgpggb.bulky.models.Manager;
import de.sgpggb.pluginutilitieslib.logging.Logging;
import de.sgpggb.pluginutilitieslib.utils.ChatUtil;
import de.sgpggb.sgpggbeconomyspigot.EconomyAPI;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class ConfirmGUI extends CustomGUI {

    Manager manager = Bulky.getInstance().getManager();
    FileConfiguration config = Bulky.getInstance().getConfig();
    Logging log = Bulky.getInstance().getLog();
    String prefix = Bulky.getInstance().getPrefix();

    ChestContainer container;
    int bulk;
    int cost;

    public ConfirmGUI(Player player, ChestContainer container, int bulk, int cost) {
        super(player, "Purchase upgrades.", 6 * 9);
        this.container = container;
        this.bulk = bulk;
        this.cost = cost;
        initInventory();
    }

    @Override
    public void initInventory() {
        //item with text
        addGUIItem(new ItemBuilder().playerHead(config.getString("upgrades.gui.info.texture"))
                .displayName(ChatUtil.mm(config.getString("upgrades.gui.info.name")
                    .replace("<upgrades>", "" + bulk)
                    .replace("<money>", EconomyAPI.formatAmount(cost))))
                .lore(config.getStringList("upgrades.gui.info.lore").stream().map(l ->
                    ChatUtil.mm(l
                        .replace("<upgrades>", "" + bulk)
                        .replace("<money>", EconomyAPI.formatAmount(cost))))
                    .toList())
                .build(), 13,
            e -> {
                e.setCancelled(true);
            }
        );

        //yes
        addGUIItem(new ItemBuilder().playerHead(config.getString("upgrades.gui.yes.texture"))
                .displayName(ChatUtil.mm(config.getString("upgrades.gui.yes.name")
                    .replace("<upgrades>", "" + bulk)
                    .replace("<money>", EconomyAPI.formatAmount(cost))))
                .lore(config.getStringList("upgrades.gui.yes.lore").stream().map(l ->
                        ChatUtil.mm(l
                            .replace("<upgrades>", "" + bulk)
                            .replace("<money>", EconomyAPI.formatAmount(cost))))
                    .toList())
                .build(), 29,
            e -> {
                e.setCancelled(true);
                container.doUpgrade(player, bulk);
            }
        );

        //no
        addGUIItem(new ItemBuilder().playerHead(config.getString("upgrades.gui.no.texture"))
                .displayName(ChatUtil.mm(config.getString("upgrades.gui.no.name")))
                .lore(config.getStringList("upgrades.gui.no.lore").stream().map(ChatUtil::mm).toList())
                .build(), 33,
            e -> {
                e.setCancelled(true);
                playClickSound();
                Bulky.getInstance().getGuiHandler().openGUI(new ChestGUI(player, container));
            });
    }

    @Override
    public void onInventoryClose(InventoryCloseEvent event) {
        manager.unlock(container.getChest());
        container.getChest().close();
    }

    @Override
    public void onInventoryOpen(InventoryOpenEvent event) {

    }

    @Override
    public void onTopClick(InventoryClickEvent event) {
        event.setCancelled(true);
    }

    @Override
    public void onBottomClick(InventoryClickEvent event) {
        event.setCancelled(true);
    }

    @Override
    public String getType() {
        return "bulky_upgrade";
    }
}
