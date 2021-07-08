package rpg.projectile;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import rpg.calculator.CollideCalculator;
import rpg.entity.Entity;
import rpg.main.Main;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;

public class AbstractProjectile {

    public static void launchIgnoreTicks(@Nonnull Location start, double speed, double minSpeed, double maxSpeed,
                                         double acceleration, double gravityAcceleration, double defaultGravity, double maxDistance,
                                         boolean canPassBlock, double radius, @Nonnull Update update, @Nonnull Entity launcher, int limitTicks) {
        if(start.getWorld() == null) return;
        Location[] locations = {null, start};
        Vector direction = start.getDirection();
        for(int i = 0; i <= limitTicks; i++) {
            Location before = locations[1];
            if(before.getY() <= 0 || start.distance(before) > maxDistance) {
                break;
            }
            Location current = before.clone().add(direction.clone().multiply(
                    Math.min(Math.max((speed + i * acceleration / 20.0), minSpeed), maxSpeed) / 20))
                    .subtract(0, i * gravityAcceleration / 20.0 + defaultGravity, 0);
            locations[0] = before;
            locations[1] = current;
            double distance = before.distance(current);
            Vector addition = current.clone()
                    .subtract(before).toVector().multiply(radius / distance);
            Location temp = before.clone();
            do {
                List<Entity> collideEntities = new LinkedList<>();
                for(LivingEntity le : start.getWorld().getNearbyLivingEntities(temp, 0.3)) {
                    if(le == launcher.getMinecraftEntity()) continue;
                    Entity rpgEntity = Entity.getEntity(le);
                    if(rpgEntity != null) {
                        collideEntities.add(rpgEntity);
                    }
                }
                if((CollideCalculator.isCollideHardnessBlock(temp, radius) && !canPassBlock) || !collideEntities.isEmpty()) {
                    if(!collideEntities.isEmpty()) update.onHit(temp.clone(), collideEntities);
                    update.onCollide(temp.clone());
                    i = limitTicks + 1;
                    break;
                }
                update.updateLocation(temp.clone());
                temp.add(addition);
            } while (temp.distance(current) > radius);
            update.updateTicks(current.clone(), i);
        }
    }

    public static void launch(@Nonnull Location start, double speed, double minSpeed, double maxSpeed,
                              double acceleration, double gravityAcceleration, double defaultGravity, double maxDistance,
                              boolean canPassBlock, double radius, @Nonnull Update update, @Nonnull Entity launcher) {
        if(start.getWorld() == null) return;
        Location[] locations = {null, start};
        Vector direction = start.getDirection();
        int[] i = {0, 0};
        i[0] = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), () -> {
            Location before = locations[1];
            if(before.getY() <= 0 || start.distance(before) > maxDistance) {
                Bukkit.getScheduler().cancelTask(i[0]);
                return;
            }
            Location current = before.clone().add(direction.clone().multiply(
                    Math.min(Math.max((speed + i[1] * acceleration / 20.0), minSpeed), maxSpeed) / 20))
                    .subtract(0, i[1] * gravityAcceleration / 20.0 + defaultGravity, 0);
            locations[0] = before;
            locations[1] = current;
            double distance = before.distance(current);
            Vector addition = current.clone()
                    .subtract(before).toVector().multiply(radius / distance);
            Location temp = before.clone();
            do {
                List<Entity> collideEntities = new LinkedList<>();
                for(LivingEntity le : start.getWorld().getNearbyLivingEntities(temp, 0.3)) {
                    if(le == launcher.getMinecraftEntity()) continue;
                    Entity rpgEntity = Entity.getEntity(le);
                    if(rpgEntity != null) {
                        collideEntities.add(rpgEntity);
                    }
                }
                if((CollideCalculator.isCollideHardnessBlock(temp, radius) && !canPassBlock) || !collideEntities.isEmpty()) {
                    if(!collideEntities.isEmpty()) update.onHit(temp.clone(), collideEntities);
                    update.onCollide(temp.clone());
                    Bukkit.getScheduler().cancelTask(i[0]);
                    break;
                }
                update.updateLocation(temp.clone());
                temp.add(addition);
            } while (temp.distance(current) > radius);
            update.updateTicks(current.clone(), i[1]);
            i[1]++;
        }, 0, 1).getTaskId();
    }

    public interface Update {
        void updateTicks(Location location, int ticks);
        void updateLocation(Location location);
        void onHit(Location location, List<Entity> hitEntities);
        void onCollide(Location location);
    }
}
