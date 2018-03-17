package net.minespree.nexus.servers;

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import lombok.NonNull;
import net.minespree.feather.internal.jedis.Jedis;
import net.minespree.myers.bukkit.MyersPlugin;
import net.minespree.myers.common.Server;
import net.minespree.nexus.Nexus;
import net.minespree.nexus.servers.data.LobbyStatus;
import net.minespree.nexus.servers.games.Game;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ServerStatusUpdater implements Runnable {
    private static final long SHORT_TIMEOUT = 15 * 1000; // 15s -> ms
    private final Map<String, List<LobbyInfo>> availableServers = new ConcurrentHashMap<>();
    private final Nexus plugin;
    private final Gson gson = new Gson();

    public ServerStatusUpdater(Nexus plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        Map<String, Server> myersServers = MyersPlugin.getPlugin().getServerManager().getAllServers();
        Set<String> games = plugin.getGameRegistry().getAvailableGames().stream().map(Game::getId).collect(Collectors.toSet());
        try (Jedis jedis = plugin.getJedisPool().getResource()) {
            // get all enabled lobbies
            for (String game : games) {
                Map<String, String> servers = jedis.hgetAll("instance-statuses:" + game);
                List<LobbyInfo> lobbies = new ArrayList<>();
                for (Map.Entry<String, String> entry : servers.entrySet()) {
                    // If Myers doesn't know this server exists, we can't send players to it
                    if (!myersServers.containsKey(entry.getKey())) continue;

                    LobbyStatus status = gson.fromJson(entry.getValue(), LobbyStatus.class);
                    if (System.currentTimeMillis() >= status.getTimestamp() + SHORT_TIMEOUT) {
                        // DNR?
                        continue;
                    }
                    lobbies.add(new LobbyInfo(entry.getKey(), status));
                }

                lobbies.sort(null);
                availableServers.put(game, Collections.unmodifiableList(lobbies));
            }
        }
    }

    public void selectAndJoinServer(Player player, String game) {
        ByteArrayDataOutput o = ByteStreams.newDataOutput();
        o.writeUTF("JoinNext");
        o.writeUTF(game);
        player.sendPluginMessage(Nexus.getInstance(), "Dominion", o.toByteArray());
    }


    public List<LobbyInfo> getLobbiesForGame(@NonNull String game) {
        return availableServers.getOrDefault(game, ImmutableList.of());
    }

    public int getPlayersInGame(String game) {
        return getLobbiesForGame(game).stream()
                .mapToInt(l -> l.getStatus().getPlayersOnline())
                .sum();
    }
}
