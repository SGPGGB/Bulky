package de.sgpggb.bulky.listeners;

import de.sgpggb.bulky.Bulky;
import de.sgpggb.bulky.misc.Messages;
import de.sgpggb.bulky.misc.Permissions;
import de.sgpggb.bulky.models.ChestContainer;
import de.sgpggb.cashnuggets.NuggetAPI;
import de.sgpggb.pluginutilitieslib.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapelessRecipe;

public class CraftListener implements Listener {

    ShapelessRecipe bulkyRecipe;
    private final NamespacedKey bulkyCraftKey = new NamespacedKey(Bulky.getInstance(), "bulkyCraftKey");

    public void reload() {
        Bukkit.removeRecipe(bulkyCraftKey);

        bulkyRecipe = new ShapelessRecipe(bulkyCraftKey, ChestContainer.createBulky(new ItemStack(Material.STONE)));
        bulkyRecipe.addIngredient(new RecipeChoice.ExactChoice(NuggetAPI.createNugget(Bulky.getInstance().getConfig().getInt("chest.craftPrice"))));
        bulkyRecipe.addIngredient(Material.CHEST);

        Bukkit.addRecipe(bulkyRecipe);
    }

    @EventHandler
    public void onBulkyCraft(PrepareItemCraftEvent e) {
        if (!(e.getRecipe() instanceof ShapelessRecipe sr))
            return;

        if (!sr.getKey().equals(bulkyCraftKey))
            return;

        //set result to null for custom output
        e.getInventory().setResult(null);

        if (!Permissions.CRAFTING.check(e.getView().getPlayer())) {
            e.getView().getPlayer().sendMessage(Messages.get(Messages.MSG.ERROR_NO_PERMISSION));
            return;
        }

        boolean nuggetOk = false;

        for (ItemStack is : e.getInventory().getMatrix()) {
            if (ItemUtils.empty(is))
                continue;

            if (NuggetAPI.isNugget(is) &&
                NuggetAPI.getValue(is) == Bulky.getInstance().getConfig().getInt("chest.craftPrice")) {
                nuggetOk = true;
            }
        }

        if (!nuggetOk) {
            return;
        }

        e.getInventory().setResult(bulkyRecipe.getResult());
    }


}
