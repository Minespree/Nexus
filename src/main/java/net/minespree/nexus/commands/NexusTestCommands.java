package net.minespree.nexus.commands;

import com.github.kevinsawicki.timeago.TimeAgo;
import net.minespree.babel.Babel;
import net.minespree.babel.BabelMessage;
import net.minespree.feather.command.system.annotation.Command;
import net.minespree.feather.player.rank.Rank;
import net.minespree.feather.util.TimeUtils;
import net.minespree.nexus.Nexus;
import net.minespree.nexus.util.GameTracker;
import net.minespree.wizard.gui.PerPlayerInventoryGUI;
import net.minespree.wizard.util.Chat;
import net.minespree.wizard.util.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @since 17/10/2017
 */
public class NexusTestCommands {

    private static TimeAgo timeAgo = new TimeAgo();

    private static int[] mappings = new int[]{9,10,11,12,13,14,15,16,17};

    @Command(names = {"gametracker"}, requiredRank = Rank.ADMIN, hideFromHelp = true, async = true)
    public static void tracker(Player player) {
        GameTracker.getLastGames(player).whenCompleteAsync((gameData, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
                return;
            }

            PerPlayerInventoryGUI gui = new PerPlayerInventoryGUI(player1 -> Babel.translate("game_tracker").toString(player1), 27, Nexus.getInstance());
            int index = 0;
            for (GameTracker.GameData data : gameData) {
                BabelMessage msg = Babel.translate(data.getType().name().toLowerCase());

                MaterialData mat = GameTracker.from(data.getType());
                ItemBuilder builder = new ItemBuilder(mat.toItemStack(1));
                builder.displayName(Babel.translate(data.getType().name().toLowerCase()));
                List<String> lore = new ArrayList<>();
                String name = msg.toString(player);
                name = ChatColor.stripColor(name);
                lore.add(Chat.GRAY + Chat.ITALIC + "You played this " + name + " game");
                lore.add(Chat.GRAY + Chat.ITALIC + timeAgo.timeAgo(data.getTimeFinished()) + ".");
                lore.add(" ");
                lore.add(Chat.GOLD + "Winner: " + Chat.GRAY + data.getWinner());
                lore.add(" ");
                lore.add(Chat.GOLD + "Your Statistics: ");
                data.getStats().forEach((s, o) -> {
                    if ("timePlayed".equalsIgnoreCase(s)) {
                        o = TimeUtils.formatTime((int) (((long) o) / 1000));
                    } else if ("win".equalsIgnoreCase(s)) {
                        return;
                    }
                    lore.add("  " + Chat.DARK_GRAY + Chat.SMALL_DOT + " " + Chat.GOLD + Babel.translate(s.toLowerCase() + "_stats").toString(player) + " " + Chat.WHITE + o.toString());
                });
                lore.add(" ");
                lore.add(Chat.YELLOW + "Click for more information!");
                builder.lore(lore);

                int slot = mappings[index];
                gui.setItem(slot, builder::build, (player12, clickType) -> player12.sendMessage("TODO"));
                index++;
            }

            gui.open(player);
        }).thenApply(s -> Collections.emptyList());
    }

}
