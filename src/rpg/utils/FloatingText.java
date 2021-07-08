package rpg.utils;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class FloatingText {
    private final ArmorStand armorStand;
    private static final ArrayList<ArmorStand> armorStands = new ArrayList<>();

    public FloatingText(Location location, String text) {
        this.armorStand = Objects.requireNonNull(location.getWorld())
                .spawn(location, ArmorStand.class, armorStand -> {
                    armorStand.setVisible(false);
                    armorStand.setGravity(false);
                    armorStand.setInvulnerable(true);
                    armorStand.setMarker(true);
                    armorStand.setSmall(true);
                    armorStand.setCustomName(text);
                    armorStand.setCustomNameVisible(true);
                });
        armorStands.add(armorStand);
    }

    public void setText(String text) {
        this.armorStand.setCustomName(text);
    }

    public ArmorStand getArmorStand() {
        return armorStand;
    }

    public static Collection<ArmorStand> getAllArmorStands() {
        return armorStands;
    }

    public void remove() {
        this.armorStand.remove();
        armorStands.remove(this.armorStand);
    }
}
