package net.minespree.nexus.servers.games;

import net.minespree.babel.Babel;
import net.minespree.wizard.util.ItemBuilder;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class StaticGameRegistry implements GameRegistry {

    @Override
    public List<Game> getAvailableGames() {
        List<Game> games = new ArrayList<>();
        games.add(new Game("skywars", new ItemBuilder(Material.GRASS).displayName(Babel.translate("skywars")).lore(Babel.translateMulti("skywars_lore"))));
        games.add(new Game("blockwars", new ItemBuilder(Material.STAINED_CLAY).displayName(Babel.translate("blockwars")).lore(Babel.translateMulti("blockwars_lore"))));
        games.add(new Game("thimble", new ItemBuilder(Material.WATER_BUCKET).displayName(Babel.translate("thimble")).lore(Babel.translateMulti("thimble_lore"))));
        games.add(new Game("clash", new ItemBuilder(Material.BOOK).displayName(Babel.translate("clash")).lore(Babel.translateMulti("clash_lore"))));

        return games;
    }
}
