package net.minespree.nexus.hotbar.items;

import net.minespree.babel.Babel;
import net.minespree.nexus.Nexus;
import net.minespree.nexus.hotbar.HotbarItem;
import net.minespree.wizard.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

public class CompassItem extends HotbarItem {
    public CompassItem() {
        super(new ItemBuilder(Material.COMPASS).displayName(Babel.translate("join_game_name")), 0);
    }

    public void interact(Player player, Action action) {
        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            Nexus.getInstance().getJoinServerGUI().open(player);
        }
    }
}
