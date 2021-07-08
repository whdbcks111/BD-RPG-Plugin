package rpg.utils;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.meta.FireworkMeta;

public class FireworkUtil {
    public static void spawnFirework(Location location
            , Color[] colors, Color[] fades, FireworkEffect.Type type, LivingEntity shooter) {
        if(location.getWorld() == null) return;
        location.getWorld()
                .spawn(location, Firework.class, firework -> {
                    FireworkMeta meta = firework.getFireworkMeta();
                    FireworkEffect effect = FireworkEffect.builder()
                            .with(type)
                            .withColor(colors)
                            .withFade(fades)
                            .trail(false)
                            .build();
                    meta.addEffect(effect);
                    firework.setSilent(true);
                    firework.setShooter(shooter);
                    firework.setFireworkMeta(meta);
                    firework.detonate();
                });
    }

    public static void spawnFirework(Location location
            , Color color, FireworkEffect.Type type, LivingEntity shooter) {
        spawnFirework(location, new Color[] {color}, new Color[] {color}, type, shooter);
    }

    public static void spawnFirework(Location location
            , Color color, Color fade, FireworkEffect.Type type, LivingEntity shooter) {
        spawnFirework(location, new Color[] {color}, new Color[] {fade}, type, shooter);
    }
}
