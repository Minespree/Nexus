package net.minespree.nexus.games;

import net.minespree.nexus.Nexus;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

abstract class HubMinigame {

    protected BukkitTask task;

    public void start() {
        initialize();
        startTimer();
    }

    public abstract void initialize();

    public abstract void tick();

    private void startTimer() {
        task = Bukkit.getScheduler().runTaskTimer(Nexus.getInstance(), this::tick, 1L, 1L);
    }

}
