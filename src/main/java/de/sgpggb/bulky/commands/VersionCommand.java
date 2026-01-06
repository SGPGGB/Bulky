package de.sgpggb.bulky.commands;

import de.sgpggb.bulky.Bulky;
import de.sgpggb.bulky.misc.Permissions;
import de.sgpggb.pluginutilitieslib.cmd.CustomCommand;
import de.sgpggb.pluginutilitieslib.utils.ChatUtil;
import de.sgpggb.pluginutilitieslib.utils.CommandUtils;
import org.bukkit.command.CommandSender;

import java.util.List;

public class VersionCommand extends CustomCommand {


    @Override
    protected boolean checkPermission(CommandSender sender) {
        return Permissions.ADMIN.check(sender);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage(ChatUtil.mm(Bulky.getInstance().getPrefix() + "<green>Version: " + Bulky.getInstance().getPluginMeta().getVersion()));
    }

    @Override
    public List<String> getCommandNames() {
        return List.of("version");
    }

    @Override
    public void printHelp(CommandSender sender) {
        sender.sendMessage(CommandUtils.helpRun(Bulky.getInstance().getPrefix(), "/bulky version", null, "Shows the plugin version"));
    }
}
