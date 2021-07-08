package rpg.projectile;

import org.bukkit.entity.Projectile;
import rpg.entity.Attack;
import rpg.entity.Entity;

public interface ProjectileEvent {
    void update(Projectile projectile);
    void onBeforeHit(Projectile projectile, Entity abuser, Entity victim);
    void onHit(Projectile projectile, Attack attackInfo);
}
