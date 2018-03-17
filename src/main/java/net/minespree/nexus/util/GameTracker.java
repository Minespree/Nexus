package net.minespree.nexus.util;

import com.google.common.collect.Lists;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minespree.feather.data.gamedata.GameRegistry;
import net.minespree.feather.db.mongo.MongoManager;
import net.minespree.feather.player.stats.persitent.PersistentStatistics;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @since 17/10/2017
 */
public class GameTracker {

    /**
     * Gets the last 9 (due to menus) games the player was in.
     * @param player The player.
     * @return {@link List<net.minespree.nexus.util.GameTracker.GameData>} of the games.
     */
    public static CompletableFuture<List<GameData>> getLastGames(Player player) {
        MongoCollection<Document> gamesRan = MongoManager.getInstance().getCollection("gamesran");
        return CompletableFuture.supplyAsync(() -> {
            List<GameData> datas = Lists.newArrayList();
            Bson projection = Filters.and(
                    Filters.or(
                            Filters.in("players", player.getUniqueId().toString()),
                            Filters.in("teams.$.players", player.getUniqueId().toString())
                    )
            );

            FindIterable<Document> docs = gamesRan.find(projection).sort(new Document("gameEnded", -1)).limit(9);
            for (Document dat : docs) {
                GameData data = new GameData(GameRegistry.Type.valueOf(dat.getString("gameType")),
                        dat.getString("winner") == null ? "Unknown" : dat.getString("winner"),
                        PersistentStatistics.getFrom(dat, "stats." + player.getUniqueId().toString(), Document.class),
                        dat.getLong("gameStart") == null ? System.currentTimeMillis() : dat.getLong("gameStart"),
                        dat.getLong("gameEnded") == null ? System.currentTimeMillis() : dat.getLong("gameEnded"));

                datas.add(data);
            }
            return datas;
        });
    }

    public static MaterialData from(GameRegistry.Type type) {
        switch (type) {
            case BLOCKWARS:
                return new MaterialData(Material.STAINED_CLAY, DyeColor.BLUE.getData());
            case SKYWARS:
                return new MaterialData(Material.GRASS);
            case THIMBLE:
                return new MaterialData(Material.WATER_BUCKET);
            default:
                return new MaterialData(Material.BARRIER);
        }
    }

    @Data
    @AllArgsConstructor
    public static class GameData {
        private GameRegistry.Type type;
        private String winner;
        private Map<String, Object> stats;
        private long timeStarted;
        private long timeFinished;
    }

}
