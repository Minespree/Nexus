package net.minespree.nexus.games.infinite;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.FutureCallback;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import lombok.Getter;
import net.minespree.babel.Babel;
import net.minespree.babel.BabelMessage;
import net.minespree.feather.data.gamedata.GameRegistry;
import net.minespree.feather.db.mongo.MongoManager;
import net.minespree.feather.player.NetworkPlayer;
import net.minespree.feather.player.PlayerManager;
import net.minespree.feather.player.stats.persitent.PersistentStatistics;
import net.minespree.feather.settings.FeatherSettings;
import net.minespree.feather.util.Callback;
import net.minespree.feather.util.Scheduler;
import net.minespree.nexus.Nexus;
import net.minespree.nexus.games.InfiniteHubMinigame;
import net.minespree.nexus.settings.NexusSettings;
import net.minespree.wizard.floatingtext.types.PublicFloatingText;
import net.minespree.wizard.util.Area;
import net.minespree.wizard.util.MessageUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class KoTLHubMinigame extends InfiniteHubMinigame {

    private static final BabelMessage afkMessage = Babel.translate("kotl_afk");

    private Location kingLoc;
    private Location spawnLoc;
    private Location hologramLoc;
    @Getter
    private Area playableArea;
    @Getter
    private HashSet<Player> activePlayers = new HashSet<>();
    private ConcurrentHashMap<UUID, LadderPlayerData> playerData = new ConcurrentHashMap<>();
    @Getter
    private boolean active = false;
    private int clock = 0;

    private List<PublicFloatingText> holograms = Lists.newArrayList();

    public void initialize() {
        FileConfiguration config = Nexus.getInstance().getConfig();
        World defaultWorld = Bukkit.getWorlds().get(0);

        playableArea = new Area(
                config.getDouble("Spawns.KoTL.Pos1.x"),
                config.getDouble("Spawns.KoTL.Pos2.x"),
                config.getDouble("Spawns.KoTL.Pos1.y"),
                config.getDouble("Spawns.KoTL.Pos2.y"),
                config.getDouble("Spawns.KoTL.Pos1.z"),
                config.getDouble("Spawns.KoTL.Pos2.z")
        );

        kingLoc = new Location(
                defaultWorld,
                config.getDouble("Spawns.KoTL.King.x"),
                config.getDouble("Spawns.KoTL.King.y"),
                config.getDouble("Spawns.KoTL.King.z")
        );

        spawnLoc = new Location(
                defaultWorld,
                config.getDouble("Spawns.KoTL.Outside.x"),
                config.getDouble("Spawns.KoTL.Outside.y"),
                config.getDouble("Spawns.KoTL.Outside.z")
        );

        hologramLoc = new Location(
                defaultWorld,
                config.getDouble("Spawns.KoTL.Leaderboard.x"),
                config.getDouble("Spawns.KoTL.Leaderboard.y"),
                config.getDouble("Spawns.KoTL.Leaderboard.z")
        );

//        PublicFloatingText title = new PublicFloatingText(hologramLoc);
//        title.setText(Babel.translate("top_10_kotl_players"));
//        holograms.add(title);
//        AtomicBoolean first = new AtomicBoolean(true);
//        for (int i = 1; i < 11; i++) {
//            double add = first.get() ? 0.3 : 0.25;
//            if (first.get()) {
//                first.set(false);
//            }
//            PublicFloatingText pos = new PublicFloatingText(hologramLoc.subtract(0, add, 0));
//            pos.setText(Babel.translate("kotl_board_position"), i, ChatColor.GRAY + "N/A");
//            holograms.add(title);
//        }
//
//        new BukkitRunnable() {
//            @Override
//            public void run() {
//                getLeaderboard(pairs -> {
//                    if (pairs == null) return;
//                    int index = 1;
//                    for (Pair<NetworkPlayer, Integer> pair : pairs) {
//                        String name = pair.getKey().getRank().getSecondaryColor() + pair.getKey().getLastKnownUsername();
//                        holograms.get(index++).setText(Babel.translate("kotl_board_position"), index, name);
//                    }
//                });
//            }
//        }.runTaskTimer(Nexus.getInstance(), 0L, 80L);
    }

    public void tick() {
        if (activePlayers.size() >= 2) {
            if (!active) {
                //Activate KoTL
                active = true;
                clock = 0;
                if (!playerData.isEmpty()) {
                    playerData.values().forEach(LadderPlayerData::cleanForKOTL);
                }
            }

        } else {
            if (active) {
                //Disable KoTL
                clock = 0;
                active = false;

                if (!activePlayers.isEmpty()) {
                    Iterator<Player> iterator = activePlayers.iterator();

                    while (iterator.hasNext()) {
                        Player player = iterator.next();

                        getData(player).restorePreviousState();
                        getData(player).cancelAFkTimer();
                        getData(player).saveStats();

                        playerData.remove(player.getUniqueId());

                        iterator.remove();
                    }
                }
            }
        }

        if (active) {
            clock++;

            Player king = getKing();

            if (clock == 20) {
                clock = 0;

                if (king != null) {
                    getData(king).increasePoints();
                    MessageUtil.sendActionBar(king, Babel.translate(
                            "kotl_point_gain", String.valueOf(getData(king).getPoints())));
                }
            }
        }
    }

    public void enter(Player player) {
        if (!activePlayers.contains(player)) {
            activePlayers.add(player);
            playerData.put(player.getUniqueId(), new LadderPlayerData(player));
        }
    }

    public void leave(Player player) {
        if (activePlayers.contains(player)) {
            activePlayers.remove(player);

            LadderPlayerData data = getData(player);

            data.restorePreviousState();
            data.cancelAFkTimer();
            data.saveStats();

            if (data.getOriginalPoints() != data.getPoints()) {
                Babel.translate("total_points_kotl").sendMessage(player, getData(player).points); // Points is total points.
            }

            playerData.remove(player.getUniqueId());
        }
    }

    public void resetAfk(Player player) {
        if (activePlayers.contains(player)) {
            getData(player).resetAfkTimer();
        }
    }

    private LadderPlayerData getData(Player player) {
        return playerData.get(player.getUniqueId());
    }

    private boolean matchKingLoc(Location loc) {
        return loc.getBlockX() == kingLoc.getBlockX() && loc.getBlockY() == kingLoc.getBlockY() && loc.getBlockZ() == kingLoc.getBlockZ();
    }

    @Nullable
    private Player getKing() {
        Optional<?> matchedPlayer = activePlayers
                .stream()
                .filter(player -> matchKingLoc(player.getLocation()))
                .findFirst();

        return matchedPlayer.map(o -> (Player) o).orElse(null);
    }

    private void getLeaderboard(Callback<List<Pair<NetworkPlayer, Integer>>> boardCallback) {
        String key = PersistentStatistics.constructKey(GameRegistry.Type.KOTL, PersistentStatistics.PersistableData.INTEGERS, "points");
        Scheduler.run(() -> MongoManager.getInstance().getCollection("players").find(Filters.exists(key, true)).limit(10).sort(new Document(key, -1)), new FutureCallback<FindIterable<Document>>() {
            @Override
            public void onSuccess(@Nullable FindIterable<Document> documents) {
                if (documents == null) {
                    onFailure(new IllegalStateException("no data"));
                    return;
                }
                List<Pair<NetworkPlayer, Integer>> list = Lists.newArrayList();
                for (Document cur : documents) {
                    Integer num = PersistentStatistics.getFrom(cur, key, Integer.class);
                    String id = cur.getString("_id");
                    if (id == null || num == null) {
                        continue;
                    }
                    UUID uuid = UUID.fromString(id);
                    boolean shouldRemove = false;
                    NetworkPlayer np;
                    Player p;
                    if ((p = Bukkit.getPlayer(uuid)) != null) {
                        np = NetworkPlayer.of(p);
                    } else {
                        np = PlayerManager.getInstance().getPlayer(uuid, true);
                        shouldRemove = true;
                    }
                    Pair<NetworkPlayer, Integer> pair = Pair.of(np, num);
                    list.add(pair);
                    if (shouldRemove) {
                        PlayerManager.getInstance().removePlayer(uuid);
                    }
                }

                boardCallback.call(list);
            }

            @Override
            public void onFailure(Throwable throwable) {
                boardCallback.call(Collections.emptyList());
            }
        });
    }

    private class LadderPlayerData {

        private BukkitTask afkTimer = null;
        @Getter
        private Player player;
        @Getter
        private ItemStack[] inventoryContents;
        @Getter
        private int originalPoints = 0;
        @Getter
        private int points = 0;
        private boolean hasSpeed = false;

        LadderPlayerData(Player player) {
            this.player = player;

            NetworkPlayer np = NetworkPlayer.of(player);
            PersistentStatistics statistics = np.getPersistentStats();

            statistics.getValue(PersistentStatistics.constructKey(GameRegistry.Type.KOTL, PersistentStatistics.PersistableData.INTEGERS, "points"), o -> {
                if (o instanceof Integer) {
                    originalPoints = (Integer) o;
                    points += originalPoints;
                }
            });

            inventoryContents = player.getInventory().getContents();

            if (active) {
                cleanForKOTL();
            }

            resetAfkTimer();
        }

        void cleanForKOTL() {
            ItemStack[] armor = player.getInventory().getArmorContents();
            inventoryContents = player.getInventory().getContents();

            player.getInventory().clear();
            player.getInventory().setArmorContents(armor);
            player.updateInventory();
            player.setAllowFlight(false);
            player.setFlying(false);

            if (player.hasPotionEffect(PotionEffectType.SPEED)) {
                hasSpeed = true;
                player.removePotionEffect(PotionEffectType.SPEED);
            }
        }

        void resetAfkTimer() {
            if (afkTimer != null) {
                afkTimer.cancel();
            }

            afkTimer = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!active || !activePlayers.contains(player)) {
                        cancel();

                        return;
                    }

                    player.teleport(spawnLoc);
                    afkMessage.sendMessage(player);
                }
            }.runTaskLater(Nexus.getInstance(), 400);
        }

        void cancelAFkTimer() {
            afkTimer.cancel();
            afkTimer = null;
        }

        void increasePoints() {
            points++;
        }

        void saveStats() {
            NetworkPlayer np = NetworkPlayer.of(player);
            PersistentStatistics stats = np.getPersistentStats();
            stats.getIntegerStatistics(GameRegistry.Type.KOTL).increment("points", points - originalPoints); // Increment doesn't require to know previous value, magic of mongo!
            stats.persist();
        }

        void restorePreviousState() {
            player.getInventory().setContents(inventoryContents);
            player.updateInventory();

            if (hasSpeed) {
                NexusSettings.applySetting(NetworkPlayer.of(player), FeatherSettings.HUB_SPEED);
            }
        }
    }
}
