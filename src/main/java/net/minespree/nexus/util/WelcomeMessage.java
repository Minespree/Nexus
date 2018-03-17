package net.minespree.nexus.util;

import com.google.common.collect.ImmutableList;
import lombok.experimental.UtilityClass;
import net.minespree.babel.Babel;
import net.minespree.wizard.util.Chat;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Random;

@UtilityClass
public class WelcomeMessage {
    private static final List<Language> languages = ImmutableList.<Language>builder()
            .add(new Language("English", "&eHello, &6&l%player%&r&e!"))
            .add(new Language("Spanish", "&e¡Hola &6&l%player%&r&e!"))
            .add(new Language("French", "&eBonjour, &6&l%player%&r&e!"))
            .add(new Language("Pirate", "&eAhoy matey, &6&l%player%&r&e!"))
            .add(new Language("German", "&eHallo, &6&l%player%&r&e!"))
            .add(new Language("Chinese", "&e你好, &6&l%player%&r&e!"))
            .add(new Language("Japanese", "&e今日は, &6&l%player%&r&e!"))
            .add(new Language("Russian", "&eЗдравствуйте, &6&l%player%&r&e!"))
            .add(new Language("Esperanto", "&eSaluton &6&l%player%&r&e!"))
            .build();
    private static final Random random = new Random();

    public static void sendWelcome(Player player) {
        Language inLanguage = languages.get(random.nextInt(languages.size()));

        player.sendMessage("");
        player.sendMessage(Chat.center(inLanguage.get(player)));
        player.sendMessage(Chat.center(Babel.translate("welcome_to_minespree").toString(player)));
        player.sendMessage(Chat.center(Babel.translate("now_you_know_hello_in").toString(player, inLanguage)));
        player.sendMessage("");
        Babel.translateMulti("alpha_message").toString(player).forEach(message -> {
            player.sendMessage(Chat.center(message));
        });
    }

    private static class Language {
        private final String name;
        private final String format;

        private Language(String name, String format) {
            this.name = name;
            this.format = format;
        }

        public String get(Player player) {
            return ChatColor.translateAlternateColorCodes('&', format.replace("%player%", player.getName()));
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
