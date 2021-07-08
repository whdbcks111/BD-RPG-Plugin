package rpg.entity;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import rpg.attribute.Attribute;
import rpg.effect.Effect;
import rpg.effect.EffectType;
import rpg.main.Main;
import rpg.utils.ColorUtil;
import rpg.utils.ItemUtil;
import rpg.utils.ParticleUtil;

import java.util.Date;

public enum MonsterType {

    SKULL_SOLDIER("해골 병사", EntityType.SKELETON, monster -> {
        monster.setName("해골 병사");
        monster.setLevel(15 + (int)(Math.random() * 2));
        monster.stat.strength = 3 * 10 + 2;
        monster.stat.vitality = 3 * 5 + 2;
        monster.stat.agility = 2;
        monster.stat.sensibility = 2;
        monster.getMinecraftMob().getEquipment().setItemInMainHand(new ItemStack(Material.STONE_SWORD));
        monster.getMinecraftMob().getEquipment().setHelmet(new ItemStack(Material.LEATHER_HELMET));
        monster.setTendency(Monster.Tendency.HOSTILE);
    }, monster -> {
        monster.setAtk(monster.getAtk() * 1.5 + 200);
        monster.setMoveSpeed(monster.getMoveSpeed() * 0.8);
        Mob mob = monster.getMinecraftMob();
        if(mob == null) return;
    }),
    WOLF("늑대", EntityType.WOLF, monster -> {
        monster.setName("늑대");
        monster.setLevel(5 + (int)(Math.random() * 3));
        for(int i = 1; i < monster.getLevel(); i++) {
            monster.stat.strength += 2;
            monster.stat.agility++;
        }
    }, monster -> {
        monster.setAttackCooldown(monster.getAttackCooldown() + 0.2);
        Mob mob = monster.getMinecraftMob();
        if(mob == null) return;
        if(monster.getTicks() % 160 == 0 && mob.getTarget() != null) {
            int count = 1 + (int)(Math.random() * 3);
            for(int k = 0; k < count; k++) {
                Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                    if(mob.getTarget() == null) return;
                    double dist = mob.getTarget().getLocation().distance(mob.getLocation());
                    if (dist < 8) {
                        mob.getWorld().playSound(mob.getLocation(), Sound.ENTITY_WOLF_GROWL, 1, 1);
                        mob.getWorld().playSound(mob.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1, 1);
                        Vector vel = mob.getTarget().getLocation().subtract(mob.getLocation())
                                .multiply(1 / dist).toVector().multiply(0.7);
                        mob.setVelocity(vel);
                        Location loc = mob.getEyeLocation();
                        int j = 0;
                        for (float pitch = -30; pitch < 30; pitch += 2) {
                            float finalPitch = pitch;
                            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                                Location temp = loc.clone();
                                temp.setYaw(temp.getYaw() + 90);
                                temp.setPitch(0);
                                Vector side = temp.getDirection().multiply(0.3);
                                temp = loc.clone();
                                temp.setPitch(finalPitch);
                                temp.subtract(side);
                                for (int i = 0; i < 3; i++) {
                                    ParticleUtil.createColoredParticle(temp.clone().subtract(loc.getDirection().multiply(1.5))
                                            .add(temp.getDirection().multiply(3)), Color.RED, 1, 0.7f, 0);
                                    temp.add(side);
                                }
                            }, j / 3);
                            j++;
                        }
                        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                            if (mob.getTarget() == null) return;
                            double distance = mob.getTarget().getLocation().distance(mob.getLocation());
                            if (distance < 2) {
                                Entity rpgEntity = Entity.getEntity(mob.getTarget());
                                if (rpgEntity != null) rpgEntity.addEffect(new Effect(EffectType.BLOOD, 4, 1, monster));
                            }
                        }, 10);
                    }
                }, (long) Math.pow(k * 10, 1.1));
            }
        }
    }),
    HOWLING("하울링", EntityType.WOLF,
            monster -> {
        monster.setTendency(Monster.Tendency.HOSTILE);
        monster.setBoss(true);
        monster.setName("하울링");
        monster.setLevel(20 + (int)(Math.random() * 3));
        monster.stat.vitality += 10;
        monster.stat.strength += 10;
        for(int i = 1; i < monster.getLevel(); i += 2) {
            monster.stat.strength += 2;
            monster.stat.vitality += 4;
        }
    },
            monster -> {
        monster.setAttackCooldown(monster.getAttackCooldown() + 0.2);
        Mob mob = monster.getMinecraftMob();
        if(mob == null) return;
        if(monster.getExtra("latestHowl") == null || monster.getLife() > monster.getMaxLife() * 0.7) {
            monster.setExtra("latestHowl", 0L);
        }
        long latestHowl = (Long)monster.getExtra("latestHowl");
        if(new Date().getTime() - latestHowl > 1000 * 60 * 5 && monster.getLife() < monster.getMaxLife() * 0.5) {
            monster.setExtra("latestHowl", new Date().getTime());
            Location loc = mob.getLocation().clone().add(0, 0.4, 0);
            for(int i = 0; i < 3; i++) {
                Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> mob.getWorld()
                        .playSound(mob.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1, 1), i * 6);
                for (float yaw = 0; yaw < 360; yaw += 5) {
                    float finalYaw = yaw;
                    int finalI = i;
                    Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                        loc.setYaw(finalYaw);
                        ParticleUtil.createColoredParticle(loc.clone()
                                        .add(loc.getDirection().multiply(1.2 + (finalYaw + finalI * 50) % 360.0 / 360.0 + finalI * 0.5)),
                                Color.fromRGB(0xff5500), 1, 1);
                    }, (int) (yaw / 360.0f * 10) + i * 6);
                }
            }
            monster.addEffect(new Effect(EffectType.SLOWNESS, 6, 100));
            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                mob.getWorld().playSound(mob.getLocation(), Sound.ENTITY_WOLF_HOWL, SoundCategory.HOSTILE, 1.1f, 1);
                for(int i = 0; i < 3; i++) {
                    Location location = mob.getLocation().clone().add(0, 1, 0);
                    location.setPitch(0);
                    final double ratio = Math.random();
                    final boolean negative = Math.random() < 0.5;
                    for(float yaw = 0; yaw < 360; yaw += 2.7f) {
                        location.setYaw(yaw);
                        Location target = location.clone()
                                .add(0, (location.getDirection().getX() * ratio
                                        + location.getDirection().getZ() * (1 - ratio)) * (negative ? -1 : 1), 0)
                                .add(location.getDirection().multiply(3 + (i * 1.7)));
                        float finalYaw = yaw;
                        int finalI = i;
                        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                            mob.getWorld().playSound(mob.getLocation(), Sound.ITEM_FIRECHARGE_USE, 0.05f, 0.05f);
                            if(Math.random() < 0.3)
                                ParticleUtil.createParticle(target, Particle.SMOKE_NORMAL, 1,
                                        Math.random(), Math.random(), Math.random(), 0);
                            ParticleUtil.createColoredParticle(target,
                                    ColorUtil.mixColor(Color.fromRGB(0x2211ff), Color.ORANGE,
                                            Math.abs((finalYaw + finalI * 40) % 360.0 - 180.0) / 180.0),
                                    1, 1.3f, 0);
                        }, (int)(yaw / 360.0f * 15) + i * 10);
                    }
                }
                for(LivingEntity le : mob.getWorld().getNearbyLivingEntities(mob.getLocation(), 6)) {
                    Entity rpgEntity = Entity.getEntity(le);
                    if(rpgEntity == null) continue;
                    if(le == mob) continue;
                    Vector vel = le.getVelocity();
                    vel.add(le.getLocation().subtract(mob.getLocation()).toVector()
                            .multiply(2 / Math.max(0.01, le.getLocation().distance(mob.getLocation()))));
                    le.setVelocity(vel);
                    rpgEntity.addEffect(new Effect(EffectType.SLOWNESS, 10, 1, monster));
                    rpgEntity.addEffect(new Effect(EffectType.FIRE, 4, 1, monster));
                }
            }, 25);
            for(int i = 0; i < 20; i++) {
                Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                    if (mob.getTarget() == null) return;
                    double dist = mob.getTarget().getLocation().distance(mob.getLocation());
                    if (dist == 0) dist = 1;
                    Vector vel = mob.getTarget().getLocation().subtract(mob.getLocation())
                            .multiply(0.8 / dist).toVector();
                    mob.setVelocity(vel);
                }, 30 + i);
            }
        }
        else if(monster.getLife() < monster.getMaxLife() * 0.5 && monster.getTicks() % 40 == 0) {
            Location loc = mob.getLocation().add(0, 0.5, 0);
            monster.setLife(monster.getLife() + (monster.getMaxLife() - monster.getLife()) * 0.03);
            mob.getWorld().playSound(mob.getLocation(), Sound.ITEM_FIRECHARGE_USE, 0.3f, 0.3f);
            for (float yaw = 0; yaw < 360; yaw += 5) {
                loc.setYaw(yaw);
                ParticleUtil.createColoredParticle(loc.clone()
                                .add(loc.getDirection().multiply(0.8 + yaw / 360.0 * 0.7)),
                        Color.fromRGB(0xff3300), 1, 0.6f);
            }
        }
        if(new Date().getTime() - latestHowl > 1000 * 10 && monster.getTicks() % 150 == 0 && mob.getTarget() != null) {
            int count = 1 + (int)(Math.random() * 3);
            for(int k = 0; k < count; k++) {
                Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                    double dist = mob.getTarget().getLocation().distance(mob.getLocation());
                    if(dist == 0) dist = 1;
                    if (dist < 8) {
                        mob.getWorld().playSound(mob.getLocation(), Sound.ENTITY_WOLF_GROWL, 1, 1);
                        mob.getWorld().playSound(mob.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1, 1);
                        Vector vel = mob.getTarget().getLocation().subtract(mob.getLocation())
                                .multiply(1 / dist).toVector().multiply(1.2);
                        mob.setVelocity(vel);
                        Location loc = mob.getEyeLocation();
                        int j = 0;
                        for (float pitch = -30; pitch < 30; pitch += 2) {
                            float finalPitch = pitch;
                            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                                Location temp = loc.clone();
                                temp.setYaw(temp.getYaw() + 90);
                                temp.setPitch(0);
                                Vector side = temp.getDirection().multiply(0.6);
                                temp = loc.clone();
                                temp.setPitch(finalPitch);
                                temp.subtract(side);
                                for (int i = 0; i < 3; i++) {
                                    ParticleUtil.createColoredParticle(temp.clone().subtract(loc.getDirection().multiply(1.5))
                                            .add(temp.getDirection().multiply(3)), Color.fromRGB(0xaa2200), 1, 1.2f, 0);
                                    temp.add(side);
                                }
                            }, j / 3);
                            j++;
                        }
                        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                            if (mob.getTarget() == null || mob.getTarget() == mob) return;
                            double distance = mob.getTarget().getLocation().distance(mob.getLocation());
                            if (distance < 2) {
                                Entity rpgEntity = Entity.getEntity(mob.getTarget());
                                if (rpgEntity != null) rpgEntity.addEffect(new Effect(EffectType.BLOOD, 4, 3, monster));
                            }
                        }, 5);
                    }
                }, (long) Math.pow(k * 10, 1.1));
            }
        }
    })
    ;

    private final String name;
    private final MonsterSpawner.Spawner spawner;
    private final Monster.Pattern pattern;
    private final EntityType entityType;

    MonsterType(String name, EntityType entityType, MonsterSpawner.Spawner spawner, Monster.Pattern pattern) {
        this.name = name;
        this.entityType = entityType;
        this.spawner = spawner;
        this.pattern = pattern;
    }

    public Monster.Pattern getPattern() {
        return pattern;
    }

    public String getName() {
        return name;
    }

    public MonsterSpawner.Spawner getSpawner() {
        return spawner;
    }

    public EntityType getEntityType() {
        return entityType;
    }
}
