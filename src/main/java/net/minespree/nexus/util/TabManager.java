package net.minespree.nexus.util;

import com.google.common.collect.Maps;
import net.citizensnpcs.api.npc.NPC;
import net.minespree.feather.player.NetworkPlayer;
import net.minespree.feather.player.rank.Rank;
import net.minespree.nexus.Nexus;
import net.minespree.nexus.npcs.NexusNPC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * @since 09/09/2017
 */
public class TabManager {

    private Map<UUID, Scoreboard> scoreboardMap = Maps.newConcurrentMap();

    public TabManager() {
    }

    public void update() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            Scoreboard scoreboard = scoreboardMap.computeIfAbsent(player.getUniqueId(), uuid -> Bukkit.getScoreboardManager().getNewScoreboard());

            for (Player other : Bukkit.getOnlinePlayers()) {
                NetworkPlayer data = NetworkPlayer.of(other);

                Team team = scoreboard.getTeam(other.getName());
                if (team == null) {
                    team = scoreboard.registerNewTeam(other.getName());
                }

                String prefix = ChatColor.GRAY.toString();
                if (data.hasNick()) {
                    prefix = data.getNickedRank().getColoredTag();
                } else if (data.getPrefix() != null && !data.getPrefix().isEmpty()) {
                    prefix = data.getPrefix();
                    if (prefix.length() > 16) prefix = prefix.substring(0, 14) + " ";
                } else if (data.getRank().has(Rank.IRON)) {
                    prefix = data.getRank().getColoredTag();
                }
                team.setPrefix(prefix);
                team.setSuffix(ChatColor.RESET.toString());

                String name = data.hasNick() ? data.getNick() : other.getName();
                if (!team.hasEntry(name)) {
                    team.addEntry(name);
                }
            }

            Team npcTeam = scoreboard.getTeam("npcs");
            if (npcTeam == null) {
                npcTeam = scoreboard.registerNewTeam("npcs");
                npcTeam.setPrefix(ChatColor.DARK_GRAY.toString());
                npcTeam.setNameTagVisibility(NameTagVisibility.NEVER);
            }

            for (NexusNPC npc : Nexus.getInstance().getNpcSystem().getNpcs()) {
                if (npc.getEntity().getType() == EntityType.PLAYER) {
                    Player npcP = (Player) npc.getEntity();

                    if (!npcTeam.hasEntry(npcP.getName())) {
                        npcTeam.addEntry(npcP.getName());
                    }
                }
            }

            player.setScoreboard(scoreboard);
        });
    }

}
