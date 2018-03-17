package net.minespree.nexus.settings;

import com.google.common.collect.Maps;
import net.minespree.feather.FeatherPlugin;
import net.minespree.feather.data.update.UpdateBook;
import net.minespree.feather.player.NetworkPlayer;
import net.minespree.feather.player.settings.PlayerSettingCallback;
import net.minespree.feather.player.settings.PlayerSettingManager;
import net.minespree.feather.player.settings.PlayerSettings;
import net.minespree.feather.repository.RepoCallbackManager;
import net.minespree.feather.settings.FeatherSettings;
import net.minespree.feather.settings.Setting;
import net.minespree.feather.util.ItemUtil;
import net.minespree.nexus.npcs.NPCType;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * @since 03/11/2017
 */
public class NexusSettings {
    public static final IntervalCallback INTERVAL_CALLBACK = new IntervalCallback();

    private static final Map<Setting, PlayerSettingCallback> callbacks = Maps.newHashMap();

    public static void registerCallbacks() {
        callbacks.put(FeatherSettings.VISIBILITY, new VisibilityCallback());
        callbacks.put(FeatherSettings.HUB_SPEED, new SpeedCallback());
        callbacks.put(FeatherSettings.HUB_FLY, new FlightCallback());

        RepoCallbackManager<Setting> manager = PlayerSettings.getCallbackManager();

        callbacks.forEach(manager::addCallback);

        manager.addGlobalCallback(INTERVAL_CALLBACK);
    }

    public static void onJoin(NetworkPlayer player) {
        PlayerSettingManager manager = player.getSettings();

        callbacks.forEach((setting, callback) -> {
            Object current = manager.getValue(setting);

            callback.notifyChange(player, setting, null, current);

        });

        boolean bookEnabled = (boolean) manager.getValue(FeatherSettings.UPDATE_LOG);

        if (bookEnabled) {
            openUpdateBook(player);
        }
    }

    private static void openUpdateBook(NetworkPlayer player) {
        Player bukkit = player.getPlayer();
        UpdateBook book = FeatherPlugin.get().getUpdateBook();

        if (book == null || book.getStack() == null) {
            return;
        }

        int playerVersion = player.getUpdateVersion();
        int current = FeatherPlugin.get().getUpdateBook().getVersion();

        if (playerVersion >= current) {
            return;
        }

        player.setUpdateVersion(current);
        ItemUtil.openBook(book.getStack(), bukkit);
    }

    public static void applySetting(NetworkPlayer player, Setting setting) {
        PlayerSettingCallback callback = getCallback(setting);

        if (player == null || callback == null) return;

        callback.notifyChange(player, setting, null, player.getSettings().getValue(setting));
    }

    public static PlayerSettingCallback getCallback(Setting setting) {
        return callbacks.get(setting);
    }
}
