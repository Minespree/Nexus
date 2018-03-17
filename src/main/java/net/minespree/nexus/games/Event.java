package net.minespree.nexus.games;

public abstract class Event extends HubMinigame {

    public abstract void end();

    public void endGame() {
        task.cancel();
        end();
    }

}
