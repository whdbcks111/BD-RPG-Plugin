package rpg.main;

import com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent;
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import rpg.entity.Attack;
import rpg.calculator.DamageCalculator;
import rpg.effect.Effect;
import rpg.entity.MonsterSpawner;
import rpg.entity.Player;
import rpg.gui.InventoryGUI;
import rpg.move.MoveType;
import rpg.projectile.ProjectileManager;
import rpg.skill.Skill;
import rpg.skill.SkillAction;
import rpg.utils.ActionBar;
import rpg.utils.ColorUtil;
import rpg.utils.ItemUtil;

import java.util.Arrays;
import java.util.Date;

public class Events implements Listener {

    @EventHandler
    public void onTame(EntityTameEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onKnockback(EntityKnockbackByEntityEvent event) {
    }

    @EventHandler
    public void onSwapHand(PlayerSwapHandItemsEvent event) {
        org.bukkit.entity.Player player = event.getPlayer();
        Player rpgPlayer = Player.getPlayer(player);
        if(ItemUtil.isSkillItem(event.getOffHandItem())
                && player.getInventory().getHeldItemSlot() < rpgPlayer.getSkillSlot().length) event.setCancelled(true);
        if(player.isSneaking()) {
            event.setCancelled(true);
            rpgPlayer.openMenuGUI();
        }
    }

    @EventHandler
    public void onInteractAtEntity(PlayerInteractAtEntityEvent event) {
        org.bukkit.entity.Player player = event.getPlayer();
        Player rpgPlayer = Player.getPlayer(player);
        if(rpgPlayer != null) {
            for(Skill skill : rpgPlayer.getSkills()) {
                if(skill.isActive()) {
                    SkillAction action = skill.getSkillAction();
                    action.onInteractAtEntityWhileActive( skill, rpgPlayer, event);
                }
            }
        }
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        org.bukkit.entity.Player player = event.getPlayer();
        Player rpgPlayer = Player.getPlayer(player);
        if(rpgPlayer == null) return;
        if(ItemUtil.isSkillItem(event.getItemDrop().getItemStack())
                && player.getInventory().getHeldItemSlot() < rpgPlayer.getSkillSlot().length) event.setCancelled(true);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        org.bukkit.entity.Player player = event.getPlayer();
        Player rpgPlayer = Player.getPlayer(player);
        if(rpgPlayer != null) {
            if(new Date().getTime() - rpgPlayer.getLatestInteract() < 50) {
                return;
            }
            for(Skill skill : rpgPlayer.getSkills()) {
                if(skill.isActive()) {
                    SkillAction action = skill.getSkillAction();
                    action.onInteractWhileActive(skill, rpgPlayer, event);
                }
            }
            if(player.isSneaking()
                    && new Date().getTime() - rpgPlayer.getLatestInteract() < 300
                    && (rpgPlayer.getLastInteractAction() == Action.LEFT_CLICK_AIR
                      || rpgPlayer.getLastInteractAction() == Action.LEFT_CLICK_BLOCK)
                    && (event.getAction() == Action.LEFT_CLICK_AIR
                      || event.getAction() == Action.LEFT_CLICK_BLOCK)) {
                MoveType[] types = {MoveType.DEFAULT, MoveType.SPRINT_ONLY, MoveType.HALF, MoveType.BALANCE, MoveType.NEVER, MoveType.DEFAULT};
                MoveType applyType = types[Arrays.asList(types).indexOf(rpgPlayer.getMoveType()) + 1];
                rpgPlayer.setMoveType(applyType);
                player.sendTitle("", ChatColor.GRAY + applyType.getDescription(), 10, 20, 20);
            }
            rpgPlayer.setLastInteractAction(event.getAction());
            rpgPlayer.setLatestInteract(new Date().getTime());
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        org.bukkit.entity.Player player = (org.bukkit.entity.Player) event.getPlayer();
        Player rpgPlayer = Player.getPlayer(player);
        if(rpgPlayer != null && rpgPlayer.getCurrentInventoryGUI() != null
                && rpgPlayer.getCurrentInventoryGUI().equalsTitle(event.getView().getTitle())
                && rpgPlayer.getCurrentInventoryGUI().getCloseEvent() != null) {
            rpgPlayer.getCurrentInventoryGUI().getCloseEvent().onClose(event);
        }
    }

    @EventHandler
    public void onSlotChange(PlayerItemHeldEvent event) {
        org.bukkit.entity.Player player = event.getPlayer();
        Player rpgPlayer = Player.getPlayer(player);
        if(rpgPlayer != null) {
            if(event.getNewSlot() < rpgPlayer.getSkillSlot().length) {
                event.setCancelled(true);
                if(new Date().getTime() - rpgPlayer.getLatestDeath() >= 500) {
                    Skill skill = rpgPlayer.getSkill(event.getNewSlot());
                    if (skill == null) return;
                    rpgPlayer.useSkill(skill.getName());
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        org.bukkit.entity.Player player = (org.bukkit.entity.Player) event.getWhoClicked();
        Player rpgPlayer = Player.getPlayer(player);
        if(rpgPlayer != null) {
            if(ItemUtil.isSkillItem(event.getCurrentItem())
                    && event.getSlot() < rpgPlayer.getSkillSlot().length) event.setCancelled(true);
            if(rpgPlayer.getCurrentInventoryGUI() != null
                    && rpgPlayer.getCurrentInventoryGUI()
                    .equalsTitle(event.getView().getTitle())) {
                event.setCancelled(true);
                InventoryGUI gui = rpgPlayer.getCurrentInventoryGUI();
                if (gui.getClickEvent(event.getRawSlot()) != null
                        && event.getClickedInventory() == event.getView().getTopInventory()) {
                    gui.getClickEvent(event.getRawSlot()).onClick(event);
                }
            }
        }
    }

    @EventHandler
    public void onJump(PlayerJumpEvent event) {
        org.bukkit.entity.Player player = event.getPlayer();
        Player rpgPlayer = Player.getPlayer(player);
        if(rpgPlayer == null) return;
        if(rpgPlayer.getMoveSpeed() <= 5) event.setCancelled(true);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        rpg.entity.Monster monster = rpg.entity.Monster.getMonster(entity);
        if(monster != null && monster.getLastDamager() != null) {
            rpg.entity.Entity damager = monster.getLastDamager();
            if(damager instanceof Player) {
                Player rpgPlayer = (Player) damager;
                org.bukkit.entity.Player player = rpgPlayer.getMinecraftPlayer();
                rpgPlayer.onHuntMonster(monster);
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        event.setFormat("%1$s : %2$s");
        org.bukkit.entity.Player player = event.getPlayer();
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if(!(event.getEntity() instanceof LivingEntity)) return;
        LivingEntity le = (LivingEntity) event.getEntity();
        rpg.entity.Entity rpgEntity = rpg.entity.Entity.getEntity(le);
        if(rpgEntity == null) return;
        if(event instanceof EntityDamageByEntityEvent) return;
        if(event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK) return;
        rpgEntity.damagePhysic(event.getDamage() * 40
                / (event.getCause() == EntityDamageEvent.DamageCause.MAGIC ? 20 : 1), 0);
        event.setDamage(0);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        org.bukkit.entity.Player player = event.getEntity();
        Player rpgPlayer = Player.getPlayer(player);
        rpgPlayer.setLatestDeath(new Date().getTime());
        if(rpgPlayer.getLastDamager() instanceof Player && rpgPlayer.getLastDamager() != rpgPlayer
                && new Date().getTime() - rpgPlayer.getLatestAttacked() < 1000 * 60) {
            String killMessage = "   " +
                    ChatColor.RED + rpgPlayer.getName() + ChatColor.GRAY + "님이 " +
                    ChatColor.RED + rpgPlayer.getLastDamager().getName() + ChatColor.GRAY + "님에게 살해당했습니다.";
            for(Player rp : Player.getAllPlayers()) {
                if(rp.getMinecraftPlayer() != null) rp.getMinecraftPlayer().sendMessage(killMessage);
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        org.bukkit.entity.Player player = event.getPlayer();
        Player rpgPlayer = Player.getPlayer(player);
        if(rpgPlayer != null) {
            rpgPlayer.setLife(rpgPlayer.getMaxLife());
            rpgPlayer.onRespawn();
            player.getInventory().setHeldItemSlot(rpgPlayer.getSkillSlot().length);
        }
    }

    @EventHandler
    public void onExplosion(ExplosionPrimeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityRemove(EntityRemoveFromWorldEvent event) {
        Entity entity = event.getEntity();
        if(entity instanceof Mob) {
            Mob mob = (Mob) entity;
            rpg.entity.Monster monster = rpg.entity.Monster.getMonster(mob);
            if(monster != null) {
                for(MonsterSpawner spawner : MonsterSpawner.getMonsterSpawners()) {
                    if(spawner.getEntityUUID().equalsIgnoreCase(monster.getUuid())) {
                        spawner.setLatestDead(new Date().getTime());
                        break;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntityHit(EntityDamageByEntityEvent event) {
        LivingEntity a = null; //abuser
        if(!(event.getEntity() instanceof LivingEntity)) return;
        LivingEntity v = (LivingEntity) event.getEntity(); //victim
        if(v instanceof ArmorStand) return;

        boolean isBlocking = event.getDamage(EntityDamageEvent.DamageModifier.BLOCKING) != 0;
        boolean isRangeAttack = false;
        double rangeForce = 0;
        Projectile p = null;

        if(event.getDamager() instanceof Firework) {
            event.setDamage(0);
            event.setCancelled(true);
            return;
        }

        if(event.getDamager() instanceof Projectile) {
            p = (Projectile)event.getDamager();
            if(p.getShooter() != null) {
                isRangeAttack = true;
                rangeForce = DamageCalculator.getProjectileForce(p);
                a = (LivingEntity) p.getShooter();
            }
            else return;
        }
        if(a == null) {
            if (!(event.getDamager() instanceof LivingEntity)) return;
            a = (LivingEntity) event.getDamager();
        }

        rpg.entity.Entity abuser = rpg.entity.Entity.getEntity(a);
        rpg.entity.Entity victim = rpg.entity.Entity.getEntity(v);

        if(abuser == null || victim == null) return;

        if(p != null && ProjectileManager.getEvent(p) != null) {
            ProjectileManager.getEvent(p).onBeforeHit(p, abuser, victim);
        }

        if(new Date().getTime() - abuser.getLatestAttack() < abuser.getAttackCooldown() * 1000) {
            if(a instanceof org.bukkit.entity.Player)
                ActionBar.sendMessage((org.bukkit.entity.Player) a, ChatColor.YELLOW + "공격 시간이 지나지 않았습니다!");
            event.setCancelled(true);
            return;
        }

        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> v.setNoDamageTicks(2), 1);

        boolean isCritical = Math.random() * 100 < abuser.getCriticalChance() && !isBlocking;
        double increase = isCritical ? 1 + abuser.getCriticalIncrease() / 100 : 1;
        double damage = isBlocking ? 0 : victim.damagePhysic(
                (isRangeAttack ? abuser.getRangeAtk() * rangeForce : abuser.getAtk()) * increase,
                abuser.getPenetrate());
        abuser.setLatestAttack(new Date().getTime());
        Attack attackInfo = new Attack(abuser, victim, damage, isCritical, increase, isRangeAttack, rangeForce, isBlocking);

        //effect
        for(Effect effect : abuser.getEffects()) {
            effect.getEffectType().getAction().onAttack(effect, abuser, attackInfo);
        }
        if(abuser instanceof Player) {
            Player rpgAbuser = (Player) abuser;
            for(Skill skill : rpgAbuser.getSkills()) {
                if(skill.isActive()) {
                    SkillAction action = skill.getSkillAction();
                    action.onAttackWhileActive(skill, rpgAbuser, attackInfo);
                }
            }
        }
        if(ProjectileManager.getEvent(p) != null) {
            ProjectileManager.getEvent(p).onHit(p, attackInfo);
        }

        event.setDamage(0);
        victim.setLastDamager(abuser);
        damage = attackInfo.getDamage();
        isCritical = attackInfo.isCritical();

        if(a instanceof org.bukkit.entity.Player) {
            Player rpgPlayer = (Player) abuser;
            String name = victim.getName();

            if(isCritical) {
                Sound criticalSound = Sound.ENTITY_BLAZE_HURT;
                a.getWorld().playSound(a.getLocation(), criticalSound, 0.7f, 1);
                a.getWorld().playSound(v.getLocation(), criticalSound, 0.7f, 1);
            }

            rpgPlayer.showAttackBossbar(victim, damage, isCritical);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.setJoinMessage(ChatColor.GRAY + "  [" + ChatColor.AQUA + "+" + ChatColor.GRAY + "] " + ChatColor.WHITE +
                event.getPlayer().getName() +
                ChatColor.GRAY + " (" + Bukkit.getOnlinePlayers().size() + "명)");
        if(!event.getPlayer().hasPlayedBefore()) {
            Bukkit.broadcastMessage(" \n  " + ChatColor.YELLOW + event.getPlayer().getName() +
                    ChatColor.GREEN + "님이 처음 들어오셨습니다. 환영해주세요!\n ");
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.setQuitMessage(ChatColor.GRAY + "  [" + ChatColor.RED + "-" + ChatColor.GRAY + "] " + ChatColor.WHITE +
                event.getPlayer().getName() +
                        ChatColor.GRAY + " (" + (Bukkit.getOnlinePlayers().size() - 1) + "명)");
    }

}