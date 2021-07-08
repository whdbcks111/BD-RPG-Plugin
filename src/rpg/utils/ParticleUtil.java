package rpg.utils;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;

public class ParticleUtil {
    public static void createParticle(Location location, Particle particleType, int count) {
        createParticle(location, particleType, count, 0, 0, 0, 0);
    }

    public static void createParticle(Location location, Particle particleType, int count, Object t) {
        createParticle(location, particleType, count, 0, 0, 0, 0, t);
    }

    public static void createColoredParticle(Location location, Color color, int count, float size) {
        createColoredParticle(location, color, count, size, 0, 0, 0, 0);
    }

    public static void createParticle(Location location, Particle particleType, int count, double speed) {
        createParticle(location, particleType, count, 0, 0, 0, speed);
    }

    public static void createParticle(Location location, Particle particleType, int count, double speed, Object t) {
        createParticle(location, particleType, count, 0, 0, 0, speed, t);
    }

    public static void createColoredParticle(Location location, Color color, int count, float size, double speed) {
        createColoredParticle(location, color, count, size, 0, 0, 0, speed);
    }

    public static void createParticle(Location location, Particle particleType, int count, double dx, double dy, double dz, double speed) {
        location.getWorld().spawnParticle(particleType, location, count, dx, dy, dz, speed, null, true);
    }

    public static void createColoredParticle(Location location, Color color, int count, float size, double dx, double dy, double dz, double speed) {
        location.getWorld().spawnParticle(Particle.REDSTONE, location, count, dx, dy, dz, speed, new Particle.DustOptions(color, size), true);
    }

    public static void createParticle(Location location, Particle particleType, int count, double dx, double dy, double dz, double speed, Object t) {
        location.getWorld().spawnParticle(particleType, location, count, dx, dy, dz, speed, t, true);
    }
}
