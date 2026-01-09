package de.sgpggb.bulky.misc;

import de.sgpggb.bulky.Bulky;
import de.sgpggb.pluginutilitieslib.utils.ChatUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.FileConfiguration;

public class Messages {

    public static void init(FileConfiguration c) {
        for (MSG msg : MSG.values()) {
            c.addDefault(msg.getConfig(), msg.getDef());
        }
    }

    public static Component get(MSG msg, Placeholder... placeholders) {
        String raw = Bulky.getInstance().getConfig().getString(msg.getConfig());

        if (placeholders != null) {
            for (Placeholder p : placeholders) {
                raw = raw.replace(p.key(), p.value());
            }
        }

        return ChatUtil.mm(Bulky.getInstance().getPrefix() + raw);
    }

    public record Placeholder(String key, String value) {}

    public enum MSG {
        ERROR_INGAME_ONLY("messages.error.ingame_only", "<red>Ingame only!"),
        ERROR_CHEST_CLOSED("messages.error.chest_closed", "<red>Chest was forced to close by a reload!"),

        COMMANDS_GIVE("messages.commands.give", "<green>You received one BulkyChest!"),
        COMMANDS_RELOAD("messages.commands.reload", "<green>Plugin reloaded!"),
        COMMANDS_VERSION("messages.commands.version", "<green>Version: <version>!"),

        ERROR_CHEST_NOT_EMPTY("messages.chest.not_empty", "<red>Cant change material, when chest not empty!"),
        ERROR_BULKY_CONNECT("messages.chest.bulky_connect", "<red>BulkyChests may not connect to other chests!"),
        ERROR_CHEST_NEXT_TO_BULKY("messages.chest.chest_next_to_bulky", "<red>You can't place a chest next to a BulkyChest!"),
        ERROR_BREAK_UNKNOWN("messages.chest.break_unknown", "<red>Something went wrong while breaking the chest!"),
        ERROR_BREAK_LOCKED("messages.chest.break_locked", "<red>This chest is currently locked!"),
        ERROR_BREAK_NOT_EMPTY("messages.chest.break_not_empty", "<red>Your Bulky is not empty!"),
        ERROR_OPEN_UNKNOWN("messages.chest.open_unknown", "<red>Something went wrong while opening the chest!"),
        ERROR_OPEN_LOCKED("messages.chest.open_locked", "<red>This chest is already in use!"),
        ERROR_UPGRADE_NO_MONEY("messages.chest.upgrade.no_money", "<red>You don't have enough money!"),
        ERROR_UPGRADE_NO_UPGRADES_LEFT("messages.chest.cant_upgrade", "<red>You cant upgrade this chest!"),
        ERROR_UPGRADE_UNKNOWN("messages.chest.upgrade_unknown", "<red>Something went wrong while upgrading the chest!"),

        CHEST_MATERIAL_CHANGED("messages.chest.material_changed", "<red>Changed material to <material>!"),
        ;

        String config;
        String def;

        MSG(String config,  String def) {
            this.config = config;
            this.def = def;
        }

        public String getConfig() {
            return config;
        }

        public String getDef() {
            return def;
        }
    }
}
