package net.minespree.nexus.settings;

import net.minespree.feather.player.NetworkPlayer;
import net.minespree.feather.player.settings.PlayerSettingCallback;
import net.minespree.feather.settings.Setting;
import org.bukkit.entity.Player;

/**
 * @since 03/11/2017
 */
public class FlightCallback extends PlayerSettingCallback {
    @Override
    public void notifyChange(NetworkPlayer player, Setting element, Object oldValue, Object newValue) {
        Player bukkit = player.getPlayer();
        boolean value = (boolean) newValue;

        bukkit.setAllowFlight(value);
        bukkit.setFlying(value);
    }
}
