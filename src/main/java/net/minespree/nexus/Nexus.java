package net.minespree.nexus;

import lombok.Getter;
import net.citizensnpcs.api.CitizensAPI;
import net.minespree.feather.command.system.CommandManager;
import net.minespree.feather.data.gamedata.kits.KitManager;
import net.minespree.feather.db.redis.RedisManager;
import net.minespree.feather.internal.jedis.JedisPool;
import net.minespree.feather.player.PlayerManager;
import net.minespree.nexus.commands.NexusTestCommands;
import net.minespree.nexus.commands.SpawnCommand;
import net.minespree.nexus.games.infinite.KoTLHubMinigame;
import net.minespree.nexus.hotbar.Hotbar;
import net.minespree.nexus.npcs.NPCSystem;
import net.minespree.nexus.servers.JoinServerGUI;
import net.minespree.nexus.servers.ServerStatusUpdater;
import net.minespree.nexus.servers.games.GameRegistry;
import net.minespree.nexus.servers.games.StaticGameRegistry;
import net.minespree.nexus.settings.NexusSettings;
import net.minespree.nexus.util.SpawnManager;
import net.minespree.nexus.util.TabManager;
import net.minespree.nexus.util.VanishNickNotifier;
import net.minespree.pirate.cosmetics.CosmeticManager;
import net.minespree.pirate.cosmetics.CosmeticPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class Nexus extends JavaPlugin {

    @Getter
    private static Nexus instance;

    private JedisPool jedisPool;
    @Getter
    private ServerStatusUpdater statusUpdater;
    @Getter
    private GameRegistry gameRegistry = new StaticGameRegistry();
    @Getter
    private JoinServerGUI joinServerGUI;
    @Getter
    private Hotbar hotbar;
    @Getter
    private NPCSystem npcSystem;
    @Getter
    private SpawnManager spawnManager;
    @Getter
    private TabManager tabManager;
    @Getter
    private KoTLHubMinigame koTLHubMinigame;

    @Override
    public void onLoad() {
        PlayerManager.getInstance().setFactory(CosmeticPlayer::new);
    }


    @Override
    public void onEnable() {
        instance = this;

        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            saveDefaultConfig();
        }

        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerOutgoingPluginChannel(this, "Dominion");

        // Plugin startup logic
        //jedisPool = new JedisPool();
        //System.out.println(jedisPool.getNumActive());

        CosmeticManager.getCosmeticManager().load();
        jedisPool = RedisManager.getInstance().getPool();

        statusUpdater = new ServerStatusUpdater(this);
        getServer().getScheduler().runTaskTimerAsynchronously(this, statusUpdater, 0L, 60L);

        joinServerGUI = new JoinServerGUI(this);
        getServer().getScheduler().runTaskTimer(this, joinServerGUI::populateItems, 0L, 20L);

        getServer().getScheduler().runTaskTimer(this, new VanishNickNotifier(), 0L, 40L);

        KitManager.getInstance().load();

        NexusSettings.registerCallbacks();
        hotbar = new Hotbar();
        spawnManager = new SpawnManager();
        spawnManager.load();

        tabManager = new TabManager();

        koTLHubMinigame = new KoTLHubMinigame();
        koTLHubMinigame.start();


        CommandManager.getInstance().registerClass(SpawnCommand.class);
        CommandManager.getInstance().registerClass(NexusTestCommands.class);

        getServer().getPluginManager().registerEvents(new NexusListener(), this);

        if (Bukkit.getPluginManager().getPlugin("Citizens") != null) {
            CitizensAPI.getNPCRegistry().deregisterAll();

            npcSystem = new NPCSystem();
        } else {
            throw new IllegalStateException("citizens is not available");
        }
    }

    @Override
    public void onDisable() {
        // Shutdown logic
        if (npcSystem != null) {
            this.npcSystem.stop();
        }
        Bukkit.getWorlds().forEach(world -> world.getEntities().stream().filter(e -> !(e instanceof Player)).forEach(Entity::remove));
    }

    public JedisPool getJedisPool() {
        return jedisPool;
    }
}
