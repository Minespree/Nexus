package net.minespree.nexus.npcs;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.FutureCallback;
import com.mojang.authlib.properties.Property;
import com.mongodb.client.model.Filters;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.MetadataStore;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.npc.skin.Skin;
import net.citizensnpcs.npc.skin.SkinnableEntity;
import net.minespree.babel.Babel;
import net.minespree.babel.StaticBabelMessage;
import net.minespree.feather.data.gamedata.GameRegistry;
import net.minespree.feather.db.mongo.MongoManager;
import net.minespree.feather.util.BsonGenerics;
import net.minespree.feather.util.Scheduler;
import net.minespree.nexus.Nexus;
import net.minespree.wizard.util.Area;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @since 23/08/2017
 */
@SuppressWarnings("unchecked")
public class NPCSystem implements Listener {
    public static NPCRegistry registry;

    private Map<GameRegistry.Type, NexusNPC> gameNpcs = Maps.newHashMap();
    private Set<NexusNPC> npcs = Sets.newHashSet();

    public NPCSystem() {
        registry = CitizensAPI.getNPCRegistry();

        Scheduler.run(() -> MongoManager.getInstance().getCollection("nexus").find(Filters.eq("_id", "npcdata")).first(), new FutureCallback<Document>() {
            @Override
            public void onSuccess(@Nullable Document document) {
                if (document == null) {
                    onFailure(new IllegalStateException("No NPC data found"));
                    return;
                }

                List<Object> npcObjects = (List<Object>) document.get("npcs");

                npcObjects.stream().map(o -> (Document) o).forEach(npcDocument -> {
                    Document locationDoc = (Document) npcDocument.get("location");
                    Document skinDoc = (Document) npcDocument.get("skin");
                    String skin = skinDoc.getString("value");
                    String signature = skinDoc.getString("signature");
                    boolean lookAtCenter = npcDocument.getBoolean("lookAtCenter");
                    boolean enabled = npcDocument.getBoolean("enabled");

                    if (!enabled) {
                        return;
                    }

                    Location lookAt = lookAtCenter ? Bukkit.getWorlds().get(0).getSpawnLocation() : null;
                    ItemStack stack = npcDocument.containsKey("itemInHand") ? new ItemStack(Material.valueOf(npcDocument.getString("itemInHand")), 1) : null;
                    Location bukkitLocation = BsonGenerics.DOCUMENT_TO_LOCATION.apply(locationDoc, Bukkit.getWorlds().get(0));

                    String gameTypeName = npcDocument.getString("gameType");
                    String npcName = gameTypeName != null ? gameTypeName : npcDocument.getString("type");

                    Nexus.getInstance().getSpawnManager().getInvisAreas().add(
                        new Area(bukkitLocation.clone().add(1.5, 5, 1.5), bukkitLocation.clone().subtract(1.5, 5, 1.5))
                    );

                    NexusNPC npc = spawnNPC(npcName, bukkitLocation, lookAt, skin, signature, stack);

                    if (gameTypeName != null) {
                        GameRegistry.Type gameType = GameRegistry.Type.valueOf(npcDocument.getString("gameType"));
                        String gameName = npcDocument.containsKey("gameName") ? npcDocument.getString("gameName") : null;

                        List<Object> news = npcDocument.containsKey("news") ? (List<Object>) npcDocument.get("news") : Collections.emptyList();

                        setupGameNPC(npc, gameType, gameName, news);
                    } else {
                        String typeValue = npcDocument.getString("type");
                        NPCType type = NPCType.byId(typeValue);

                        if (type == null) {
                            return;
                        }

                        List<String> messages = (List<String>) npcDocument.get("messages");

                        type.setup(npc, messages);
                    }
                });
            }

            @Override
            public void onFailure(Throwable ignored) {}
        });

        Nexus.getInstance().getServer().getPluginManager().registerEvents(this, Nexus.getInstance());

        new HologramGameUpdater().runTaskTimer(Nexus.getInstance(), 0L, 20L);
    }

    public Collection<NexusNPC> getNpcs() {
        return npcs;
    }

    public NexusNPC spawnNPC(String name, Location location, Location lookAt, String skin, String signature, ItemStack itemInHand) {
        NexusNPC npc = new NexusNPC(name, EntityType.PLAYER, location);

        if (skin != null && signature != null) {
            npc.setSkin(skin, signature);
        }

        MetadataStore data = npc.data();

        data.set(NPC.COLLIDABLE_METADATA, false);
        data.set(NPC.NAMEPLATE_VISIBLE_METADATA, false);

        if (lookAt != null) {
            npc.lookAt(lookAt);
        }

        if (itemInHand != null) {
            npc.setItemInHand(itemInHand);
        }

        npcs.add(npc);

        return npc;
    }

    private final NPCClickHandler gameClickHandler = (npc, player) -> {
        GameRegistry.Type gameType = npc.data().get("GameType");
        String gameName = npc.data().get("GameName");

        if (gameType == null) {
            return;
        }

        Nexus.getInstance().getStatusUpdater().selectAndJoinServer(player, gameName != null ? gameName : gameType.name().toLowerCase());
        // FeatherPlugin.get().getQueueJoiner().joinQueue(player.getUniqueId(), gameType.name().toLowerCase());
    };

    private void setupGameNPC(NexusNPC npc, GameRegistry.Type gameType, String gameName, List<Object> news) {
        npc.data().set("GameType", gameType);
        npc.data().set("GameName", gameName);
        npc.setClickHandler(gameClickHandler);

        if(gameType != null) {
            gameNpcs.put(gameType, npc);
        }

        npc.addText(Babel.translate("click_to_play"), -0.25F);
        npc.addText(Babel.translate("currently_playing"), 0.0F, Nexus.getInstance().getStatusUpdater().getPlayersInGame(gameType.name().toLowerCase()));
        npc.addText(Babel.translate(gameType.name().toLowerCase()), 0.25F);

        if (!news.isEmpty()) {
            float offset = 0.50F;
            float add = 0.25F;

            for (Object line : news) {
                String lineText = (String) line;

                offset += add;

                StaticBabelMessage message = Babel.messageStatic(ChatColor.translateAlternateColorCodes('&', lineText));
                npc.addText(message, offset);
            }
        }
    }

    public static void changeSkin(NPC npc, String npcName, String texture, String signature) throws NoSuchFieldException, IllegalAccessException {
        try {
            Method method = Skin.class.getDeclaredMethod("setNPCSkinData", SkinnableEntity.class, String.class, UUID.class, Property.class);
            method.setAccessible(true);

            method.invoke(null, npc.getEntity(), npcName, UUID.nameUUIDFromBytes(npcName.getBytes()), new Property("textures", texture, signature));
        } catch (NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        npcs.forEach(NexusNPC::destroy);
    }

    @EventHandler
    public void onGameNPCClick(PlayerInteractEntityEvent event) {
        if (event.getRightClicked().getType() != EntityType.PLAYER) {
            return;
        }

        Entity entity = event.getRightClicked();

        checkHitEvent(event.getPlayer(), entity);
    }

    @EventHandler
    public void onGameNPCHit(EntityDamageByEntityEvent event) {
        if (event.getEntity().getType() != EntityType.PLAYER || !(event.getDamager() instanceof Player)) {
            return;
        }

        Entity entity = event.getEntity();
        Player player = (Player) event.getDamager();

        checkHitEvent(player, entity);
    }

    private void checkHitEvent(Player player, Entity entity) {
        if (!registry.isNPC(entity)) {
            return;
        }

        NPC npc = registry.getNPC(entity);
        NexusNPC nexusNPC = getNPC(npc);

        if (nexusNPC == null) {
            return;
        }

        nexusNPC.onClick(player);
    }

    public NexusNPC getNPC(NPC citizens) {
        return npcs.stream()
                .filter(npc -> npc.getNpc().equals(citizens))
                .findAny()
                .orElse(null);
    }

    private class HologramGameUpdater extends BukkitRunnable {
        @Override
        public void run() {
            gameNpcs.forEach((type, npc) -> {
                int playerCount = Nexus.getInstance().getStatusUpdater().getPlayersInGame(type.name().toLowerCase());

                npc.setText(1, Babel.translate("currently_playing"), playerCount);
            });
        }
    }

}
