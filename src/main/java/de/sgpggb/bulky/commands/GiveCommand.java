package de.sgpggb.bulky.commands;

import de.sgpggb.bulky.Bulky;
import de.sgpggb.bulky.misc.Messages;
import de.sgpggb.bulky.misc.Permissions;
import de.sgpggb.bulky.models.ChestContainer;
import de.sgpggb.pluginutilitieslib.cmd.CustomCommand;
import de.sgpggb.pluginutilitieslib.utils.CommandUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class GiveCommand extends CustomCommand {

    @Override
    protected boolean checkPermission(CommandSender sender) {
        return Permissions.ADMIN.check(sender);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Messages.get(Messages.MSG.ERROR_INGAME_ONLY));
            return;
        }
        player.give(ChestContainer.createBulky(new ItemStack(Material.STONE)));
        sender.sendMessage(Messages.get(Messages.MSG.COMMANDS_GIVE));
    }

    @Override
    public List<String> getCommandNames() {
        return List.of("give");
    }

    @Override
    public void printHelp(CommandSender sender) {
        sender.sendMessage(CommandUtils.helpSuggest(Bulky.getInstance().getPrefix(), "/bulky give", null, "Gives a Bulky Chest"));
    }


}