package net.minespree.nexus.servers.games;

import net.minespree.wizard.util.ItemBuilder;

public class Game {
    /**
     * The ID for this specific game title. It is used to identify registered servers and for localization.
     */
    private final String id;
    /**
     * The item to display in the inventory.
     */
    private final ItemBuilder item;

    public Game(String id, ItemBuilder item) {
        this.id = id;
        this.item = item;
    }

    public String getId() {
        return id;
    }

    public ItemBuilder getItem() {
        return item;
    }
}
