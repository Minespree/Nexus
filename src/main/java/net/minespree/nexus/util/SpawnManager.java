package net.minespree.nexus.util;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.mongodb.client.model.Filters;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import net.minespree.feather.db.mongo.MongoManager;
import net.minespree.feather.util.Scheduler;
import net.minespree.nexus.Nexus;
import net.minespree.wizard.executors.BukkitSyncExecutor;
import net.minespree.wizard.util.Area;
import net.minespree.wizard.util.SetupUtil;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.*;

public class SpawnManager {

    private String world;
    @Getter
    private SpawnLocation spawn;
    @Getter
    private List<Area> invisAreas = new ArrayList<>();
    private Area spawnArea;
    @Getter
    private Area lobbyArea;

    @Getter
    private Map<UUID, Area> inside = new HashMap<>();

    public void spawn(Player player) {
        player.teleport(spawn.toLocation());

        inside.put(player.getUniqueId(), spawnArea);

        hide(player);
    }

    public void leave(Player player) {
        inside.remove(player.getUniqueId());

        hide(player);
    }

    public void hide(Player player) {
        if(inside.containsKey(player.getUniqueId())) {
            Bukkit.getOnlinePlayers().forEach(p -> {
                if (!inside.containsKey(p.getUniqueId()))
                    player.showPlayer(p);
                else {
                    player.hidePlayer(p);
                    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(
                            new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, ((CraftPlayer) p).getHandle()));
                }
                p.hidePlayer(player);
                ((CraftPlayer) p).getHandle().playerConnection.sendPacket(
                        new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, ((CraftPlayer) player).getHandle()));
            });
        } else {
            Bukkit.getOnlinePlayers().forEach(p -> p.showPlayer(player));
        }
    }

    public void load() {
        ListenableFuture<Document> future = Scheduler.getPublicExecutor().submit(() ->
                MongoManager.getInstance().getCollection("nexus").find(Filters.eq("_id", "spawn")).first());
        Futures.addCallback(future, new FutureCallback<Document>() {
            @SuppressWarnings("unchecked")
            @Override
            public void onSuccess(Document document) {
                world = document.getString("world");
                spawn = locationFromDocument((Document) document.get("spawn"));
                int time = document.getInteger("time");

                Document pos1 = (Document) document.get("pos1");
                Document pos2 = (Document) document.get("pos2");
                spawnArea = new Area(pos1.getDouble("x"), pos2.getDouble("x"), pos1.getDouble("y"), pos2.getDouble("y"),
                        pos1.getDouble("z"), pos2.getDouble("z"));
                invisAreas.add(spawnArea);

                Document lobby1 = (Document) document.get("lobbyArea1");
                Document lobby2 = (Document) document.get("lobbyArea2");

                lobbyArea = new Area(lobby1.getDouble("x"), lobby2.getDouble("x"), lobby1.getDouble("y"), lobby2.getDouble("y"),
                        lobby1.getDouble("z"), lobby2.getDouble("z"));

                World w = Bukkit.createWorld(new WorldCreator(world));
                SetupUtil.setupWorld(w);
                w.setTime(time);
            }

            @Override
            public void onFailure(Throwable throwable) {
                throw new RuntimeException("Failed to load data");
            }
        }, BukkitSyncExecutor.create(Nexus.getInstance()));
    }

    private SpawnLocation locationFromDocument(Document document) {
        return new SpawnLocation(world, document.getDouble("x"), document.getDouble("y"), document.getDouble("z"),
                (float) ((double) document.getDouble("yaw")), (float) ((double) document.getDouble("pitch")));
    }

    @Data @RequiredArgsConstructor
    private class SpawnLocation {

        private final String world;
        private final double x, y, z;
        private final float yaw, pitch;

        Location toLocation() {
            return new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
        }

    }

}
