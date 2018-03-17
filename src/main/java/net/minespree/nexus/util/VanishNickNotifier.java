package net.minespree.nexus.util;

import net.minespree.feather.player.NetworkPlayer;
import net.minespree.feather.player.rank.Rank;
import net.minespree.wizard.util.Chat;
import net.minespree.wizard.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * @since 17/09/2017
 */
public class VanishNickNotifier implements Runnable {
    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            NetworkPlayer np = NetworkPlayer.of(player);
            if (!np.getRank().has(Rank.YOUTUBE)) continue;

            String text = Chat.DARK_GRAY + Chat.SMALL_ARROWS_RIGHT + Chat.YELLOW + "";
            if (np.hasNick()) {
                text += " NICKED ";
            } /* if (np.isVanished()) {
                text += " VANISHED"
            } */

            text += Chat.DARK_GRAY + Chat.SMALL_ARROWS_LEFT;

            if (np.hasNick()) {
                MessageUtil.sendActionBar(player, text);
            }
        }
    }
}
