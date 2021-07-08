package rpg.projectile;

import org.bukkit.Bukkit;
import org.bukkit.entity.Projectile;
import rpg.main.Main;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class ProjectileManager {
    private final static List<Projectile> projectiles = new LinkedList<>();
    private final static List<Projectile> remove = new LinkedList<>();
    private final static HashMap<Projectile, ProjectileEvent> events = new HashMap<>();
    private static int task = 0;

    public static void registerProjectile(Projectile projectile, ProjectileEvent event) {
        if(!projectiles.contains(projectile)) {
            projectiles.add(projectile);
            events.put(projectile, event);
        }
    }

    public static void unregisterProjectile(Projectile projectile) {
        if(projectiles.contains(projectile))
            remove.add(projectile);
    }

    public static void registerManager() {
        if(task != 0) return;
        task = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), () -> {
            for(Projectile projectile : projectiles) {
                if(getEvent(projectile) != null) {
                    ProjectileEvent event = getEvent(projectile);
                    event.update(projectile);
                    if(projectile.isOnGround() || projectile.getLocation().getY() <= 0 || projectile.isDead())
                        remove.add(projectile);
                }
            }
            while(!remove.isEmpty()) {
                Projectile projectile = remove.remove(0);
                projectiles.remove(projectile);
                events.remove(projectile);
            }
        }, 0, 1).getTaskId();
    }

    public static ProjectileEvent getEvent(Projectile projectile) {
        return events.get(projectile);
    }

    public static List<Projectile> getProjectiles() {
        return projectiles;
    }
}
