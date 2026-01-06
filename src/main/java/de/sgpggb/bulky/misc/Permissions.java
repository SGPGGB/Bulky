package de.sgpggb.bulky.misc;

import org.bukkit.command.CommandSender;

public enum Permissions {

    USER("bulky.user"),
    MOD("bulky.mod"),
    ADMIN("bulky.admin"),
    ;

    String permission;
    Permissions(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }

    public boolean check(CommandSender sender) {
        return sender.hasPermission(permission);
    }


}
