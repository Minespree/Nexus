package net.minespree.nexus.settings;

import net.minespree.feather.player.NetworkPlayer;
import net.minespree.feather.player.settings.PlayerSettingCallback;
import net.minespree.feather.settings.Setting;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * @since 03/11/2017
 */
public class SpeedCallback extends PlayerSettingCallback {
    private static final PotionEffect SPEED_EFFECT = new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1);

    @Override
    public void notifyChange(NetworkPlayer player, Setting element, Object oldValue, Object newValue) {
        Player bukkit = player.getPlayer();

        if ((boolean) newValue) {
            bukkit.addPotionEffect(SPEED_EFFECT);
        } else {
            bukkit.removePotionEffect(PotionEffectType.SPEED);
        }
    }

    /*
     *
        if (np.getRank().has(Rank.DIAMOND) && np.getSettings().isSelected("HUB_FLY")) {
            player.setAllowFlight(true);
            player.setFlying(true);
        }

        if (np.getRank().has(Rank.IRON) && np.getSettings().isSelected("HUB_SPEED")) {
            applySpeed(player);
        }
    }
     */
}
