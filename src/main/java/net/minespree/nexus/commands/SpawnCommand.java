package net.minespree.nexus.commands;

import net.minespree.babel.Babel;
import net.minespree.feather.command.system.annotation.Command;
import net.minespree.feather.player.rank.Rank;
import net.minespree.nexus.Nexus;
import org.bukkit.entity.Player;

public class SpawnCommand {

    @Command(names = "spawn", requiredRank = Rank.MEMBER)
    public static void spawnCommand(Player player) {
        Nexus.getInstance().getSpawnManager().spawn(player);
    }

    @Command(names = "fly", requiredRank = Rank.DIAMOND)
    public static void flyCommand(Player player) {
        if (Nexus.getInstance().getKoTLHubMinigame().getPlayableArea().inside(player.getLocation())) {
            Babel.translate("kotl_no_fly").sendMessage(player);
            return;
        }

        if (player.getAllowFlight()) {
            player.setFlying(false);
            player.setAllowFlight(false);
            Babel.translate("fly_disabled").sendMessage(player);
        } else {
            player.setAllowFlight(true);
            Babel.translate("fly_enabled").sendMessage(player);
        }
    }

}
