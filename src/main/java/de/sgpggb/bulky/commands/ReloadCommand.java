package de.sgpggb.bulky.commands;

import de.sgpggb.bulky.Bulky;
import de.sgpggb.bulky.misc.Permissions;
import de.sgpggb.pluginutilitieslib.cmd.CustomCommand;
import de.sgpggb.pluginutilitieslib.utils.ChatUtil;
import de.sgpggb.pluginutilitieslib.utils.CommandUtils;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ReloadCommand extends CustomCommand {

    @Override
    protected boolean checkPermission(CommandSender sender) {
        return Permissions.ADMIN.check(sender);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Bulky.getInstance().reload();
        sender.sendMessage(ChatUtil.mm("<green>Plugin reloaded!"));
    }

    @Override
    public List<String> getCommandNames() {
        return List.of("reload");
    }

    @Override
    public void printHelp(CommandSender sender) {
        sender.sendMessage(CommandUtils.helpSuggest(Bulky.getInstance().getPrefix(), "/bulky reload", null, "Reloads the plugin"));
    }
}
