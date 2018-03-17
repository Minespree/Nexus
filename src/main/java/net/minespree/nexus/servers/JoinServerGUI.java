package net.minespree.nexus.servers;

import net.minespree.babel.Babel;
import net.minespree.feather.FeatherPlugin;
import net.minespree.nexus.Nexus;
import net.minespree.nexus.hotbar.items.ShopItem;
import net.minespree.nexus.servers.games.Game;
import net.minespree.wizard.gui.PerPlayerInventoryGUI;
import net.minespree.wizard.util.ItemBuilder;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class JoinServerGUI {
    private final PerPlayerInventoryGUI gui;
    private final Nexus plugin;

    private int[] beta_mapped_slots = new int[]{10,12,14,16};
    private int[] mapped_slots = new int[]{11,12,13,14,15,20,21,22,23,24,29,30,31,32,33};

    private short[] blockwarsColors = new short[]{DyeColor.RED.getDyeData(), DyeColor.BLUE.getDyeData(), DyeColor.GREEN.getDyeData(), DyeColor.YELLOW.getDyeData()};

    private int swapColorIndex = 0;

    public JoinServerGUI(Nexus plugin) {
        this.gui = new PerPlayerInventoryGUI(p -> Babel.translate("join_game_title").toString(p), 36, plugin);
        this.plugin = plugin;
    }

    public void populateItems() {
        int slot = 0;
        for (Game game : plugin.getGameRegistry().getAvailableGames()) {
            swapColorIndex++;
            if (swapColorIndex > 3) {
                swapColorIndex = 0;
            }
            ItemBuilder builder = game.getItem();
            if (game.getId().equalsIgnoreCase("blockwars")) {
                builder.durability(blockwarsColors[swapColorIndex]);
            }
            builder.lore(Babel.translate("item_playing"), Nexus.getInstance().getStatusUpdater().getPlayersInGame(game.getId().toLowerCase()));
            builder.lore(Babel.translate("click_to_play"));

            this.gui.setItem(beta_mapped_slots[slot++], builder::build, (player, type) -> {
                Nexus.getInstance().getStatusUpdater().selectAndJoinServer(player, game.getId());
                // FeatherPlugin.get().getQueueJoiner().joinQueue(player.getUniqueId(), game.getId());
            });
        }

        this.gui.setItem(28, player -> new ItemBuilder(Material.EMERALD).displayName(Babel.translate("shop_name")).build(player), (player, clickType) -> Babel.translate("shop_link_message").sendMessage(player));
        this.gui.setItem(30, player -> new ItemBuilder(Material.GOLD_INGOT).displayName(Babel.translate("game_kit_shop")).build(player), (player, clickType) -> ShopItem.getKitMain().open(player));
        this.gui.setItem(32, player -> new ItemBuilder(Material.BOOK_AND_QUILL).displayName(Babel.translate("social_links")).build(player), (player, clickType) -> Babel.translateMulti("social_link_messages").sendMessage(player));
        this.gui.setItem(34, player -> new ItemBuilder(Material.WATCH).displayName(Babel.translate("soontm")).build(player), (player, clickType) -> Babel.translate("available_soon"));
    }

    public void open(Player player) {
        gui.open(player);
    }
}
