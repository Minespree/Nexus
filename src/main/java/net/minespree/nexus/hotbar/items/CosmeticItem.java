package net.minespree.nexus.hotbar.items;

import net.minespree.babel.Babel;
import net.minespree.nexus.hotbar.HotbarItem;
import net.minespree.pirate.cosmetics.CosmeticManager;
import net.minespree.pirate.cosmetics.CosmeticMenu;
import net.minespree.wizard.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

public class CosmeticItem extends HotbarItem {

    public CosmeticItem() {
        super(new ItemBuilder(Material.ENDER_CHEST).displayName(Babel.translate("loot_name")), 7);
    }

    @Override
    public void interact(Player player, Action action) {
        CosmeticManager.getCosmeticManager().open(player, CosmeticMenu.MAIN);
    }
}
