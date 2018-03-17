package net.minespree.nexus.hotbar;

import lombok.Getter;
import net.minespree.nexus.Nexus;
import net.minespree.nexus.hotbar.items.CompassItem;
import net.minespree.nexus.hotbar.items.CosmeticItem;
import net.minespree.nexus.hotbar.items.SettingsItem;
import net.minespree.nexus.hotbar.items.ShopItem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;

public class Hotbar implements Listener { // TODO: possibly loading in hotbar items idk yet

    @Getter
    private final static int GADGET_SLOT = 4;

    private Map<Integer, HotbarItem> items = new HashMap<>();

    public Hotbar() {
        Bukkit.getPluginManager().registerEvents(this, Nexus.getInstance());

        addItem(new CompassItem());
        addItem(new ShopItem());
        addItem(new CosmeticItem());
        addItem(new SettingsItem());
    }

    void addItem(HotbarItem item) {
        items.put(item.getSlot(), item);
    }

    public void set(Player player) {
        items.forEach((slot, item) -> item.set(player));
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        int heldSlot = event.getPlayer().getInventory().getHeldItemSlot();
        if ((event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) && items.containsKey(heldSlot)) {
            event.setCancelled(true);
            items.get(heldSlot).interact(event.getPlayer(), event.getAction());
        }
    }

}
