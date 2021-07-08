package rpg.effect;

import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import rpg.entity.Attack;
import rpg.entity.Entity;
import rpg.entity.Player;

public interface EffectAction {
    void update(Effect effect, Entity rpgEntity, long ticks);
    default void onRemove(Effect effect, Entity rpgEntity, long ticks) { }
    default void onAttack(Effect effect, Entity rpgEntity, Attack attackInfo) { }
    default void onInteract(Effect effect, Player rpgPlayer, PlayerInteractEvent event) { }
    default void onInteractAtEntity(Effect effect, Player rpgPlayer, PlayerInteractAtEntityEvent event) { }
}
