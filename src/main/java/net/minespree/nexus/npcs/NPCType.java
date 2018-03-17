package net.minespree.nexus.npcs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minespree.babel.Babel;
import net.minespree.feather.FeatherPlugin;
import net.minespree.feather.data.update.UpdateBook;
import net.minespree.feather.player.NetworkPlayer;
import net.minespree.feather.util.ItemUtil;
import org.bukkit.Sound;

import java.util.List;

/**
 * @since 21/10/2017
 */
@Getter
@AllArgsConstructor
public enum NPCType {
    CHANGELOG("changelog", (npc, bukkit) -> {
        NetworkPlayer player = NetworkPlayer.of(bukkit);

        if (player == null) return;

        UpdateBook book = FeatherPlugin.get().getUpdateBook();

        if (book == null || book.getStack() == null) {
            return;
        }

        bukkit.playSound(bukkit.getLocation(), Sound.VILLAGER_YES, 1, 1);
        ItemUtil.openBook(book.getStack(), bukkit);
    });

    private String id;

    private NPCClickHandler clickHandler;

    public void setup(NexusNPC npc, List<String> babelMessages) {
        float height = ((float) babelMessages.size() * 0.25F);
        for (String message : babelMessages) {
            npc.addText(Babel.translate(message), height);
            height -= 0.25F;
        }

        npc.setClickHandler(clickHandler);
    }

    public static NPCType byId(String id) {
        id = id.toLowerCase().trim();

        for (NPCType type : values()) {
            if (type.id.equals(id)) {
                return type;
            }
        }

        return null;
    }
}
