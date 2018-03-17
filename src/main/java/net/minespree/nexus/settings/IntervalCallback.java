package net.minespree.nexus.settings;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import net.minespree.feather.player.NetworkPlayer;
import net.minespree.feather.player.settings.PlayerSettingCallback;
import net.minespree.feather.settings.Setting;

import java.util.Map;
import java.util.UUID;

/**
 * @since 03/11/2017
 */
public class IntervalCallback extends PlayerSettingCallback {
    private Map<UUID, Long> intervals;

    public IntervalCallback() {
        intervals = Maps.newHashMap();
    }

    @Override
    public void notifyChange(NetworkPlayer player, Setting element, Object oldValue, Object newValue) {
        intervals.put(player.getUuid(), System.currentTimeMillis() + element.getInterval());
    }

    public long getRemaining(NetworkPlayer player) {
        Preconditions.checkNotNull(player);

        Long value = intervals.get(player.getUuid());

        if (value == null) return 0L;

        return value - System.currentTimeMillis();
    }

    public void remove(UUID uuid) {
        intervals.remove(uuid);
    }
}
