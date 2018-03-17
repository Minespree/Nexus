package net.minespree.nexus.npcs;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import net.citizensnpcs.api.npc.MetadataStore;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.minespree.babel.BabelStringMessageType;
import net.minespree.wizard.floatingtext.types.PublicFloatingText;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * @since 21/10/2017
 */
@Getter
public class NexusNPC {
    private net.citizensnpcs.api.npc.NPC npc;
    private Location location;

    private List<PublicFloatingText> texts = Lists.newArrayList();
    @Setter
    private NPCClickHandler clickHandler;

    public NexusNPC(String name, EntityType type, Location location) {
        this.location = location;

        npc = getRegistry().createNPC(type, name + "NexusNPC");
        npc.spawn(location);
    }

    public void lookAt(Location location) {
        npc.faceLocation(location);
    }

    public void setSkin(String value, String signature) {
        try {
            NPCSystem.changeSkin(npc, npc.getName(), value, signature);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public PublicFloatingText addText(BabelStringMessageType babel, float yOffset, Object... params) {
        Location textLocation = location.clone().add(0, yOffset - 0.5F, 0);

        PublicFloatingText line = new PublicFloatingText(textLocation);
        line.setText(babel, params);

        texts.add(line);
        return line;
    }

    public boolean removeText(int index) {
        PublicFloatingText text = texts.remove(index);

        if (text != null) {
            text.remove();
            return true;
        }

        return false;
    }

    public boolean setText(int index, BabelStringMessageType babel, Object... params) {
        PublicFloatingText text = texts.get(index);

        if (text == null) {
            return false;
        }

        text.setText(babel, params);

        return true;
    }

    public boolean onClick(Player player) {
        if (clickHandler != null) {
            clickHandler.onClick(this, player);
            return true;
        }

        return false;
    }

    public PublicFloatingText getText(int index) {
        return texts.get(index);
    }

    public void destroy() {
        npc.destroy();
    }

    public void setItemInHand(ItemStack itemStack) {
        getEquipment().setItemInHand(itemStack);
    }

    public MetadataStore data() {
        return npc.data();
    }

    public LivingEntity getEntity() {
        return (LivingEntity) npc.getEntity();
    }

    public EntityEquipment getEquipment() {
        return getEntity().getEquipment();
    }

    private static NPCRegistry getRegistry() {
        return NPCSystem.registry;
    }


}
