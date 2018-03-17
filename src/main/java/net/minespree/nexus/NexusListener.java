package net.minespree.nexus;

import net.minespree.feather.achievements.impl.FeatherAchievements;
import net.minespree.feather.experience.ExperienceChangeEvent;
import net.minespree.feather.player.NetworkPlayer;
import net.minespree.feather.player.nick.NickChangeEvent;
import net.minespree.nexus.games.infinite.KoTLHubMinigame;
import net.minespree.nexus.settings.NexusSettings;
import net.minespree.nexus.util.SpawnManager;
import net.minespree.pirate.cosmetics.CosmeticManager;
import net.minespree.pirate.cosmetics.types.gadgets.events.GadgetActionEvent;
import net.minespree.wizard.util.Area;
import net.minespree.wizard.util.SetupUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;

import java.util.Arrays;

public class NexusListener implements Listener {

    private final KoTLHubMinigame koTL;
    private SpawnManager spawnManager;

    NexusListener() {
        spawnManager = Nexus.getInstance().getSpawnManager();
        koTL = Nexus.getInstance().getKoTLHubMinigame();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player bukkit = event.getPlayer();
        NetworkPlayer player = NetworkPlayer.of(bukkit);

        spawnManager.spawn(bukkit);

        SetupUtil.setupPlayer(bukkit);
        Nexus.getInstance().getHotbar().set(bukkit);

        CosmeticManager.getCosmeticManager().join(bukkit);

        Nexus.getInstance().getTabManager().update();

        if (player == null) {
            return;
        }

        NexusSettings.onJoin(player);

        bukkit.setLevel(player.getLevel());
        bukkit.setExp(player.getNeededZeroToOne());

        player.setAchievement(FeatherAchievements.FIRST_JOIN, true);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        spawnManager.leave(player);
        koTL.leave(player);
        NexusSettings.INTERVAL_CALLBACK.remove(player.getUniqueId());
    }

    @EventHandler
    public void onExpChange(ExperienceChangeEvent event) {
        NetworkPlayer player = event.getPlayer();
        Player bukkit = player.getPlayer();

        if (bukkit == null) return;

        bukkit.setLevel(bukkit.getLevel());
        bukkit.setExp(player.getNeededZeroToOne());
    }

    @EventHandler
    public void onGadgetAction(GadgetActionEvent event) {
        if (event.getActionType() == GadgetActionEvent.ActionType.BLOCK_CHANGE) {
            event.getBlocks().removeIf(block -> koTL.getPlayableArea().inside(block.getLocation()));
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        koTL.resetAfk(event.getPlayer());

        if (event.getTo().distance(event.getFrom()) > 0.0) {
            if (spawnManager.getInside().containsKey(event.getPlayer().getUniqueId())) {
                if (!spawnManager.getInside().get(event.getPlayer().getUniqueId()).inside(event.getPlayer().getLocation())) {
                    spawnManager.leave(event.getPlayer());
                }
            } else {
                for (Area area : spawnManager.getInvisAreas()) {
                    if (area.inside(event.getPlayer().getLocation())) {
                        spawnManager.getInside().put(event.getPlayer().getUniqueId(), area);
                        spawnManager.hide(event.getPlayer());
                    }
                }

                if (!spawnManager.getLobbyArea().inside(event.getPlayer().getLocation())) {
                    spawnManager.spawn(event.getPlayer());
                }

                if (koTL.getPlayableArea().inside(event.getPlayer().getLocation())) {
                    koTL.enter(event.getPlayer());
                    koTL.resetAfk(event.getPlayer());
                } else {
                    koTL.leave(event.getPlayer());
                }
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            if ((koTL.isActive()) && koTL.getActivePlayers().containsAll(
                    Arrays.asList(event.getDamager(), event.getEntity()))) {
                event.setDamage(0);
                event.setCancelled(false);

                return;
            }
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        koTL.resetAfk(event.getPlayer());
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.PHYSICAL) {
            event.setCancelled(true);
        }
        event.setUseInteractedBlock(Event.Result.DENY);
    }

    @EventHandler
    public void on(NickChangeEvent event) {
        Nexus.getInstance().getTabManager().update();
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        if (event.toWeatherState()) event.setCancelled(true);
    }

    @EventHandler
    public void onLeavesDecay(LeavesDecayEvent event) {
        event.setCancelled(true);
    }

}
