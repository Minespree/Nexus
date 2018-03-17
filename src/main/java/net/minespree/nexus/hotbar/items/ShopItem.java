package net.minespree.nexus.hotbar.items;

import lombok.Getter;
import net.minespree.babel.Babel;
import net.minespree.feather.data.gamedata.GameRegistry;
import net.minespree.feather.data.gamedata.kits.KitManager;
import net.minespree.feather.data.gamedata.perks.PerkHandler;
import net.minespree.nexus.Nexus;
import net.minespree.nexus.hotbar.HotbarItem;
import net.minespree.wizard.gui.PerPlayerInventoryGUI;
import net.minespree.wizard.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

public class ShopItem extends HotbarItem {

    @Getter
    private static PerPlayerInventoryGUI kitMain;

    public ShopItem() {
        super(new ItemBuilder(Material.GOLD_INGOT).displayName(Babel.translate("shop_name")), 1);

        ItemBuilder blockwars = new ItemBuilder(Material.STAINED_CLAY).durability((short) 14)
                .displayName(Babel.translate("blockwars_kits")).lore(Babel.translateMulti("blockwars_kits_lore"));
        ItemBuilder clash = new ItemBuilder(Material.BOOK)
                .displayName(Babel.translate("clash_decks")).lore(Babel.translateMulti("clash_decks_lore"));
        ItemBuilder skywars = new ItemBuilder(Material.IRON_SWORD).displayName(Babel.translate("skywars_kits"))
                .lore(Babel.translateMulti("skywars_kits_lore"));

        kitMain = new PerPlayerInventoryGUI(Babel.translate("kits_main"), 45, Nexus.getInstance());
        kitMain.setItem(20, blockwars::build, (player, type) -> KitManager.getInstance().open(player, GameRegistry.Type.BLOCKWARS));
        kitMain.setItem(22, clash::build, (player, type) -> PerkHandler.getInstance().open(player, GameRegistry.Type.CLASH));
        kitMain.setItem(24, skywars::build, (player, type) -> KitManager.getInstance().open(player, GameRegistry.Type.SKYWARS));

        ItemBuilder builder = new ItemBuilder(Material.BOOK).displayName(Babel.translate("go_back")).lore(Babel.translateMulti("go_back_lore"));
        for (PerPlayerInventoryGUI gui : KitManager.getInstance().getMenus().values()) {
            gui.setItem(40, builder::build, (player, type) -> kitMain.open(player));
        }
        for (PerPlayerInventoryGUI gui : PerkHandler.getInstance().getGuis().values()) {
            gui.setItem(40, builder::build, (player, type) -> kitMain.open(player));
        }
    }

    @Override
    public void interact(Player player, Action action) {
        kitMain.open(player);
    }
}
