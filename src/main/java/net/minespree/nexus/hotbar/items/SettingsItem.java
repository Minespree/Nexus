package net.minespree.nexus.hotbar.items;

import net.minespree.babel.Babel;
import net.minespree.feather.player.NetworkPlayer;
import net.minespree.feather.repository.types.BooleanType;
import net.minespree.feather.repository.types.EnumType;
import net.minespree.feather.repository.types.util.Toggleable;
import net.minespree.feather.settings.FeatherSettings;
import net.minespree.feather.settings.MessageVisibility;
import net.minespree.feather.settings.Setting;
import net.minespree.feather.settings.Visibility;
import net.minespree.nexus.Nexus;
import net.minespree.nexus.hotbar.HotbarItem;
import net.minespree.nexus.settings.NexusSettings;
import net.minespree.wizard.gui.PerPlayerInventoryGUI;
import net.minespree.wizard.util.ItemBuilder;
import net.minespree.wizard.util.SkullUtil;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class SettingsItem extends HotbarItem {
    private static final BiConsumer<Player, ClickType> NO_CLICK = (player, clickType) -> {};
    private static final ItemStack NO_ITEM = new ItemStack(Material.AIR);

    private PerPlayerInventoryGUI gui;

    public SettingsItem() {
        super(new ItemBuilder(Material.SKULL_ITEM).durability((short) SkullType.PLAYER.ordinal()).displayName(Babel.translate("player_settings_name")), 8);

        gui = new PerPlayerInventoryGUI(Babel.translate("settings"), 54, Nexus.getInstance());

        setItem(FeatherSettings.VISIBILITY, Material.EYE_OF_ENDER, 1);
        setItem(FeatherSettings.MESSAGES, Material.BOOK_AND_QUILL, 3);
        setItem(FeatherSettings.FRIEND_REQUESTS, Material.SKULL_ITEM, 4, (short) 3);
        setItem(FeatherSettings.PARTY_REQUESTS, Material.DIAMOND, 5);
        setItem(FeatherSettings.UPDATE_LOG, Material.MAP, 7);
        setItem(FeatherSettings.NEXT_GAME, Material.SIGN, 28);
        setItem(FeatherSettings.COLOR_BLIND, Material.WOOL, 30, (short) 14);
        setItem(FeatherSettings.HUB_SPEED, Material.GOLD_BOOTS, 32);
        setItem(FeatherSettings.HUB_FLY, Material.FEATHER, 34);
    }

    private void setItem(Setting setting, Material material, int slot) {
        setItem(setting, material, slot, (short) 0);
    }

    private void setItem(Setting setting, Material material, int slot, short data) {
        gui.setItem(slot, player -> new ItemBuilder(material, 1, data).displayName(Babel.translate(setting.getBabelName())).lore(Babel.translateMulti(setting.getBabelDescription())).build(), NO_CLICK);
        gui.setItem(slot + 9, getItemConsumer(setting), getClickConsumer(setting, slot + 9));
    }

    private Function<Player, ItemStack> getItemConsumer(Setting setting) {
        return bukkit -> {
            NetworkPlayer player = NetworkPlayer.of(bukkit);

            if (player == null) return NO_ITEM;

            Object value = player.getSettings().getValue(setting);

            String babelName;
            short dataValue;

            if (setting.getType() instanceof BooleanType) {
                boolean bool = (boolean) value;

                babelName = bool ? "setting_select" : "setting_unselect";
                dataValue = bool ? (short) 10 : 8;
            } else if (setting.getType() instanceof EnumType) {
                String id = setting.getId();

                // TODO Generalize code for future use
                if (id.equals(FeatherSettings.VISIBILITY.getId())) {
                    Visibility visibility = (Visibility) value;

                    babelName = visibility.getBabel();
                    dataValue = visibility.getData();
                } else if (id.equals(FeatherSettings.MESSAGES.getId())) {
                    MessageVisibility message = (MessageVisibility) value;

                    babelName = message.getBabel();
                    dataValue = message.getData();
                } else {
                    return NO_ITEM;
                }
            } else {
                return NO_ITEM;
            }

            return new ItemBuilder(Material.INK_SACK, 1, dataValue).displayName(Babel.translate(babelName)).build();
        };
    }

    private BiConsumer<Player, ClickType> getClickConsumer(Setting setting, int slot) {
        return (bukkit, clickType) -> {
            NetworkPlayer player = NetworkPlayer.of(bukkit);

            if (player == null) return;

            if (!setting.canUse(player)) {
                player.sendMessage(Babel.translate("exclusive_rank").toString(setting.getRequiredRank().getColoredTag()));
                return;
            }

            long remaining = NexusSettings.INTERVAL_CALLBACK.getRemaining(player);

            if (remaining > 0L) {
                String time = String.format("%.2f", remaining / 1000D);

                player.sendMessage(Babel.translate("settings_interval").toString(time));
                return;
            }

            Object previous = player.getSettings().getValue(setting);
            Object next = null;

            if (setting.getType() instanceof Toggleable) {
                next = ((Toggleable) setting.getType()).getNextState(previous);
            }

            if (next == null) {
                // There's no way we can change this setting automatically
                return;
            }

            player.setSetting(setting, next);

            // Update the menu
            gui.refresh(bukkit, slot);
        };
    }

    public void set(Player player) {
        ItemStack item = builder.build(player);
        item.setItemMeta(SkullUtil.setSkull(player, (SkullMeta) item.getItemMeta()));
        player.getInventory().setItem(slot, item);
        player.updateInventory();
    }

    @Override
    public void interact(Player player, Action action) {
        gui.open(player);
    }
}
