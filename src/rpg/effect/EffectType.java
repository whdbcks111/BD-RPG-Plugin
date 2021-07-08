package rpg.effect;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import rpg.entity.Entity;
import rpg.main.Main;
import rpg.utils.ParticleUtil;

public enum EffectType {

    FIRE("화염", true, new EffectAction() {
        @Override
        public void onRemove(Effect effect, Entity rpgEntity, long ticks) {
            LivingEntity entity = rpgEntity.getMinecraftEntity();
            if(entity == null) return;
            entity.setFireTicks(0);
        }

        @Override
        public void update(Effect effect, Entity rpgEntity, long ticks) {
            LivingEntity entity = rpgEntity.getMinecraftEntity();
            if(entity == null) return;
            double damage = (50 * effect.getLevel()) / 20.0 * 15;
            if(rpgEntity.getTicks() % 15 == 0)
                rpgEntity.damagePhysic(damage, 20 * effect.getLevel());
            if(effect.getCaster() != null) {
                rpgEntity.setLastDamager(effect.getCaster());
            }
            if(effect.getMaxDuration() - effect.getDuration() < 0.1) {
                entity.setFireTicks((int) (effect.getDuration() * 20));
            }
            else if(entity.getFireTicks() > 0 && entity.getFireTicks() < (int) (effect.getDuration() * 20)) {
                entity.setFireTicks((int) (effect.getDuration() * 20));
            }
            else if(entity.getFireTicks() > effect.getDuration() * 20) {
                effect.setDuration(entity.getFireTicks() / 20.0);
            }
            else if(entity.getFireTicks() <= 0) effect.setDuration(0);
            if(effect.getDuration() <= 0.1) entity.setFireTicks(0);
        }
    }),
    SLOWNESS("둔화", true, (effect, rpgEntity, ticks) -> {
        LivingEntity entity = rpgEntity.getMinecraftEntity();
        if(entity != null && entity.isOnGround() && !entity.getLocation().add(0, -1, 0).getBlock().isPassable()) {
            Block block = entity.getLocation().add(0, -1, 0).getBlock();
            if(rpgEntity.getTicks() % 10 == 0)
                ParticleUtil.createParticle(entity.getLocation(), Particle.BLOCK_CRACK, 5,
                        0.1, block.getBlockData());
        }
        rpgEntity.setMoveSpeed(rpgEntity.getMoveSpeed() - effect.getLevel() * 10);
    }),
    BLOOD("출혈", true, (effect, rpgEntity, ticks) -> {
        LivingEntity entity = rpgEntity.getMinecraftEntity();
        if(entity != null) {
            if(rpgEntity.getTicks() % 15 == 0) {
                ParticleUtil.createParticle(entity.getLocation().add(0, entity.getHeight() * 0.3, 0), Particle.BLOCK_CRACK, 5,
                        0.1, Material.REDSTONE_BLOCK.createBlockData());
                Bukkit.getScheduler().runTask(Main.getPlugin(), () -> entity.damage(1));
                double damage = (rpgEntity.getLife() * 0.01 + 20) * effect.getLevel() / 20.0 * 15;
                rpgEntity.setLife(rpgEntity.getLife() - damage);
                if(effect.getCaster() != null) rpgEntity.setLastDamager(effect.getCaster());
            }
            ParticleUtil.createColoredParticle(entity.getLocation(), Color.RED, 5, 0.6f);
        }
    }),
    BLINDNESS("실명", true, new EffectAction() {
        @Override
        public void update(Effect effect, Entity rpgEntity, long ticks) {
            LivingEntity entity = rpgEntity.getMinecraftEntity();
            if(entity != null) {
                Entity.sync(() -> {
                    entity.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 15 * 10,
                            0, false, false));
                });
                if(entity instanceof Mob) {
                    Mob mob = (Mob) entity;
                    if(mob.getTarget() != null && mob.getTarget().getLocation().distance(mob.getLocation()) > 5) {
                        mob.setAware(false);
                    }
                }
            }
        }
        @Override
        public void onRemove(Effect effect, Entity rpgEntity, long ticks) {
            LivingEntity entity = rpgEntity.getMinecraftEntity();
            if(entity != null) {
                Entity.sync(() -> {
                    entity.removePotionEffect(PotionEffectType.BLINDNESS);
                });
            }
        }
    }),
    REGENERATION("생명력 재생", false, (effect, rpgEntity, ticks) -> {
        LivingEntity entity = rpgEntity.getMinecraftEntity();
        if(entity != null) {
            Entity.sync(() -> {
                entity.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, (int) (effect.getDuration() * 20),
                        effect.getLevel() - 1, false, false));
            });
            if(entity.hasPotionEffect(PotionEffectType.REGENERATION)) {
                PotionEffect regenEff = entity.getPotionEffect(PotionEffectType.REGENERATION);
                if(regenEff != null && regenEff.getDuration() > effect.getDuration() * 20) {
                    effect.setDuration(regenEff.getDuration() / 20.0);
                }
            }
        }
        double amount = (effect.getLevel() * 30 - 5) / 20.0;
        rpgEntity.setLife(rpgEntity.getLife() + amount);
    })
    ;


    private final String name;
    private final boolean isDebuff;
    private final EffectAction action;

    EffectType(String name, boolean isDebuff, EffectAction action) {
        this.name = name;
        this.isDebuff = isDebuff;
        this.action = action;
    }

    public String getName() {
        return name;
    }

    public boolean isDebuff() {
        return isDebuff;
    }

    public EffectAction getAction() {
        return action;
    }
}
