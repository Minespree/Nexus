package net.minespree.nexus.hotbar;

import lombok.Getter;
import net.minespree.wizard.util.ItemBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

public abstract class HotbarItem {

    protected ItemBuilder builder;
    @Getter
    protected int slot;

    public HotbarItem(ItemBuilder builder, int slot) {
        this.builder = builder;
        this.slot = slot;
    }

    protected void set(Player player) {
        player.getInventory().setItem(slot, builder.build(player));
    }

    public abstract void interact(Player player, Action action);

}
