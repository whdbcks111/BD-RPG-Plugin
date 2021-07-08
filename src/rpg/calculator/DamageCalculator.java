package rpg.calculator;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Projectile;
import org.bukkit.util.Vector;

public class DamageCalculator {
    public static double getProjectileForce(Projectile p) {
        Vector vel = p.getVelocity();
        double force = Math.sqrt(Math.pow(vel.getX(), 2) + Math.pow(vel.getY(), 2) + Math.pow(vel.getZ(), 2)) / 2.9;
        LivingEntity abuser = (LivingEntity) p.getShooter();
        if(abuser instanceof Mob) force *= 1.8;
        return force;
    }
}
