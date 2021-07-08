package rpg.skill;

import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import rpg.entity.Attack;
import rpg.entity.Player;

public interface SkillAction {
    void onStart(Skill skill, Player rpgPlayer);
    void onActive(Skill skill, Player rpgPlayer);
    void onStop(Skill skill, Player rpgPlayer);
    boolean canPayCost(Skill skill, Player rpgPlayer);
    String getCannotPayCostReason(Skill skill, Player rpgPlayer);
    void payCost(Skill skill, Player rpgPlayer);
    int getFinishTimeTicks(Skill skill, Player rpgPlayer);
    String getDescription(Skill skill, Player rpgPlayer);
    default void onAttackWhileActive(Skill skill, Player rpgPlayer, Attack attackInfo) {}
    default void onInteractWhileActive(Skill skill, Player rpgPlayer, PlayerInteractEvent event) {}
    default void onInteractAtEntityWhileActive(Skill skill, Player rpgPlayer, PlayerInteractAtEntityEvent event) {}

}
