package de.sgpggb.bulky;

import de.sgpggb.bulky.commands.GiveCommand;
import de.sgpggb.bulky.commands.ReloadCommand;
import de.sgpggb.bulky.commands.VersionCommand;
import de.sgpggb.bulky.listeners.BlockListener;
import de.sgpggb.bulky.listeners.CraftListener;
import de.sgpggb.bulky.listeners.PlayerListener;
import de.sgpggb.bulky.guis.GUIHandler;
import de.sgpggb.bulky.misc.Messages;
import de.sgpggb.bulky.models.Manager;
import de.sgpggb.pluginutilitieslib.CustomJavaPlugin;
import de.sgpggb.pluginutilitieslib.cmd.CustomCommandHandler;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public final class Bulky extends CustomJavaPlugin {

    private static Bulky instance;
    Manager manager;
    GUIHandler guiHandler;
    CustomCommandHandler cmdHandler;
    String prefix = "<white>[<red>B<gray>y<white>] ";

    public static Bulky getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
        APIVERSION_MAJOR_REQ = 5;
        APIVERSION_MINOR_REQ = 0;
    }

    @Override
    public void onEnable() {
        instance = this;

        if (!this.initLib())
            return;

        prefix = getConfig().getString("prefix");

        manager = new Manager();
        manager.reload();

        guiHandler = new GUIHandler(getLog(), this);

        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new BlockListener(), this);
        getServer().getPluginManager().registerEvents(new CraftListener(), this);

        cmdHandler = new CustomCommandHandler("bulky", getLog(), getPrefix());
        cmdHandler.registerCmd(new VersionCommand());
        cmdHandler.registerCmd(new ReloadCommand());
        cmdHandler.registerCmd(new GiveCommand());
        getCommand("bulky").setExecutor(cmdHandler);
    }

    @Override
    public void initConfig() {
        FileConfiguration c = getConfig();
        c.addDefault("debug", false);
        c.addDefault("prefix", "<white>[<red>B<gray><white>] <white>");

        c.addDefault("chest.name", "<red>Bulky");
        c.addDefault("chest.price", 10000);

        c.addDefault("chest.gui.title", "Bulky");

        c.addDefault("chest.gui.moveOneStack.texture", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODgyZmFmOWE1ODRjNGQ2NzZkNzMwYjIzZjg5NDJiYjk5N2ZhM2RhZDQ2ZDRmNjVlMjg4YzM5ZWI0NzFjZTcifX19");
        c.addDefault("chest.gui.moveOneStack.name", "<green>Move 1 Stack");
        c.addDefault("chest.gui.moveOneStack.lore", List.of("<gray>Moves <green>1 stack <gray>into your inventory."));

        c.addDefault("chest.gui.moveNineStacks.texture", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDFiNjJkYjVjMGEzZmExZWY0NDFiZjcwNDRmNTExYmU1OGJlZGY5YjY3MzE4NTNlNTBjZTkwY2Q0NGZiNjkifX19");
        c.addDefault("chest.gui.moveNineStacks.name", "<green>Move 9 Stacks");
        c.addDefault("chest.gui.moveNineStacks.lore", List.of("<gray>Moves <green>9 stacks <gray>into your inventory."));

        c.addDefault("chest.gui.moveTwentySevenStacks.texture", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmU5YWU3YTRiZTY1ZmNiYWVlNjUxODEzODlhMmY3ZDQ3ZTJlMzI2ZGI1OWVhM2ViNzg5YTkyYzg1ZWE0NiJ9fX0=");
        c.addDefault("chest.gui.moveTwentySevenStacks.name", "<green>Move 27 Stacks");
        c.addDefault("chest.gui.moveTwentySevenStacks.lore", List.of("<gray>Moves <green>27 stacks <gray>into your inventory."));

        c.addDefault("chest.gui.upgradeButton.texture", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWEyZDg5MWM2YWU5ZjZiYWEwNDBkNzM2YWI4NGQ0ODM0NGJiNmI3MGQ3ZjFhMjgwZGQxMmNiYWM0ZDc3NyJ9fX0=");
        c.addDefault("chest.gui.upgradeButton.name", "<gold>Buy an upgrade");
        c.addDefault("chest.gui.upgradeButton.lore", List.of("<gray>Click to purchase an upgrade<gray>.", "<gray>Bought <upgrades> of <maxupgrades> Upgrades.", "<gray>Rightclick to bulk purchase. Currently set to <bulk>"));

        c.addDefault("chest.gui.moveItemsToChestButton.texture", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmNjYmY5ODgzZGQzNTlmZGYyMzg1YzkwYTQ1OWQ3Mzc3NjUzODJlYzQxMTdiMDQ4OTVhYzRkYzRiNjBmYyJ9fX0=");
        c.addDefault("chest.gui.moveItemsToChestButton.name", "<yellow>Move All Items");
        c.addDefault("chest.gui.moveItemsToChestButton.lore", List.of("<gray>Moves all items into the chest."));

        c.addDefault("chest.gui.changeMaterialButton.texture", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDc2MWVhNzE5MTMxMzdlYjk4NmVkODYxZDFhOWRhMWM3MDAyMmFiNWFjZDU3Yjg4NDk5YWI4MGNjNGUwYmQyNCJ9fX0=");
        c.addDefault("chest.gui.changeMaterialButton.name", "<yellow>Changes material to store");
        c.addDefault("chest.gui.changeMaterialButton.lore", List.of("<gray>Click to change material to", "<gray>the item on the cursor!"));

        c.addDefault("chest.gui.informationButton.texture", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDAxYWZlOTczYzU0ODJmZGM3MWU2YWExMDY5ODgzM2M3OWM0MzdmMjEzMDhlYTlhMWEwOTU3NDZlYzI3NGEwZiJ9fX0=");
        c.addDefault("chest.gui.informationButton.name", "<aqua>Information");
        c.addDefault("chest.gui.informationButton.lore", List.of("<gray>Stored: <white><stored>", "<gray>Max: <white><max>", "<gray>Percent: <white><percent> %"));

        c.addDefault("upgrades.price", 10000);
        c.addDefault("upgrades.addCapacity", 3456);
        c.addDefault("upgrades.maxUpgrades", -1);
        c.addDefault("upgrades.maxCapacity", 100_000_000L);
        c.addDefault("upgrades.bulkOptions", List.of(1, 5, 10, 50, 100));

        c.addDefault("upgrades.gui.title", "Upgrades");
        
        c.addDefault("upgrades.gui.info.texture", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTEyNDNmN2U3MzhhMTI4MTc2YzUxNzUwMjY1MjRmMGU3NjhkZGU1MzBkZWRjMDU0Nzk3NDJjY2JiZTg5N2U1OCJ9fX0=");
        c.addDefault("upgrades.gui.info.name", "<green>Do you want to purchase <upgrades> upgrades!");
        c.addDefault("upgrades.gui.info.lore", List.of("<gray>Click accept to buy it", "It will cost you <money> money!"));

        c.addDefault("upgrades.gui.yes.texture", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzZiYWNjMmRhNjk4OGIzNGUyYTI1MmYxZDFmODRmZTQ3MDNmZDhhOWRkMjFkMGU1NWU1YzJkMWI0NTE0NzE3OCJ9fX0=");
        c.addDefault("upgrades.gui.yes.name", "<yellow>Accept");
        c.addDefault("upgrades.gui.yes.lore", List.of("<gray>Click to buy <upgrades> upgrades for <money> money!"));

        c.addDefault("upgrades.gui.no.texture", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWRjZTU4MjI4MmRiYTE5NTNlODcxOGViMGE3MmU0Nzc0MzE2ZTg4ZGEzMjE1MDY4ZjY5MWFhNGNhMzQyYTcxZiJ9fX0=");
        c.addDefault("upgrades.gui.no.name", "<yellow>Deny");
        c.addDefault("upgrades.gui.no.lore", List.of("<gray>Click to return to chest."));

        Messages.init(c);

        c.addDefault("options.numberFormat", "short");

        c.options().copyDefaults(true);
        saveConfig();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void reload() {
        reloadConfig();
        super.initLib();
        prefix = getConfig().getString("prefix");
        manager.reload();
        guiHandler.closeAll();
    }

    public String getPrefix() {
        return prefix;
    }

    public Manager getManager() {
        return manager;
    }

    public GUIHandler getGuiHandler() {
        return guiHandler;
    }
}
