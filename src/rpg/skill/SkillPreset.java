package rpg.skill;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.util.Vector;
import rpg.entity.Attack;
import rpg.calculator.CollideCalculator;
import rpg.effect.Effect;
import rpg.effect.EffectType;
import rpg.entity.Entity;
import rpg.entity.Player;
import rpg.main.Main;
import rpg.projectile.AbstractProjectile;
import rpg.projectile.ProjectileEvent;
import rpg.projectile.ProjectileManager;
import rpg.utils.ColorUtil;
import rpg.utils.FireworkUtil;
import rpg.utils.ItemUtil;
import rpg.utils.ParticleUtil;

import java.util.Date;
import java.util.List;

public enum SkillPreset {

    SPELL_SHIELD("보호막", 1, 25, item -> {
        item.setType(Material.SUNFLOWER);
        return item;
    }, new SkillAction() {
        @Override
        public void onStart(Skill skill, Player rpgPlayer) {
            org.bukkit.entity.Player player = rpgPlayer.getMinecraftPlayer();
            if(player != null) {
                player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 1);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
            }
            rpgPlayer.setShield(700 + rpgPlayer.getLevel() * 10, 7);
        }
        @Override
        public void onActive(Skill skill, Player rpgPlayer) {
            org.bukkit.entity.Player player = rpgPlayer.getMinecraftPlayer();
            if(player == null) return;
            Location location = player.getLocation();
            location.setPitch(0);
            for(int i = 0; i < 360; i += 30) {
                location.setYaw(i);
                ParticleUtil.createColoredParticle(location.clone().add(0, 0.7, 0)
                        .add(location.getDirection().multiply(0.7)), Color.YELLOW, 1, 0.6f);
            }
        }
        @Override
        public void onStop(Skill skill, Player rpgPlayer) { }
        @Override
        public boolean canPayCost(Skill skill, Player rpgPlayer) {
            return rpgPlayer.getMana() >= 140;
        }

        @Override
        public String getCannotPayCostReason(Skill skill, Player rpgPlayer) {
            return "마나가 부족합니다!";
        }

        @Override
        public void payCost(Skill skill, Player rpgPlayer) {
            rpgPlayer.setMana(rpgPlayer.getMana() - 140);
        }
        @Override
        public int getFinishTimeTicks(Skill skill, Player rpgPlayer) {
            return 7 * 20;
        }
        @Override
        public String getDescription(Skill skill, Player rpgPlayer) {
            return "7초동안 " + ChatColor.AQUA + (700 + rpgPlayer.getLevel() * 10) + ChatColor.RESET + "의 피해를 흡수하는 보호막을 생성합니다.";
        }
    }, false),
    SMITE("강타", 10, 10, item -> {
        item.setType(Material.GOLDEN_SWORD);
        return item;
    }, new SkillAction() {
        @Override
        public void onStart(Skill skill, Player rpgPlayer) {
            org.bukkit.entity.Player player = rpgPlayer.getMinecraftPlayer();
            if(player != null) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.9f);
                player.sendMessage(org.bukkit.ChatColor.DARK_GRAY + "┌──────────────┐");
                player.sendMessage(ChatColor.RED + "   강타" + ChatColor.WHITE + "가 발동될 준비가 되었습니다!");
                player.sendMessage(org.bukkit.ChatColor.DARK_GRAY + "└──────────────┘");
            }
        }
        @Override
        public void onActive(Skill skill, Player rpgPlayer) { }
        @Override
        public void onStop(Skill skill, Player rpgPlayer) {
            org.bukkit.entity.Player player = rpgPlayer.getMinecraftPlayer();
            if(player != null &&
                    skill.getActiveTimeTicks() == skill.getSkillAction().getFinishTimeTicks(skill, rpgPlayer)) {
                player.playSound(player.getLocation(), Sound.ITEM_BOTTLE_FILL_DRAGONBREATH, 1, 1);
                player.sendMessage(org.bukkit.ChatColor.DARK_GRAY + "┌──────────┐");
                player.sendMessage("   강타가 취소되었습니다.");
                player.sendMessage(org.bukkit.ChatColor.DARK_GRAY + "└──────────┘");
            }
        }
        @Override
        public boolean canPayCost(Skill skill, Player rpgPlayer) {
            return rpgPlayer.getMana() >= 100 + skill.getLevel() * 13;
        }
        @Override
        public String getCannotPayCostReason(Skill skill, Player rpgPlayer) {
            return "마나가 부족합니다.";
        }
        @Override
        public void payCost(Skill skill, Player rpgPlayer) {
            rpgPlayer.setMana(rpgPlayer.getMana() - 100 + skill.getLevel() * 13);
        }
        @Override
        public int getFinishTimeTicks(Skill skill, Player rpgPlayer) {
            return 10 * 20;
        }
        @Override
        public String getDescription(Skill skill, Player rpgPlayer) {
            return "스킬을 발동하고 나서 " + ChatColor.GOLD + "10초" + ChatColor.RESET + " 안에 적을 근거리 공격하면 " +
                    ChatColor.RED + "근거리 공격력의 1.5배 + 100" + ChatColor.RESET + "의 공격력으로 적을 공격합니다.";
        }
        @Override
        public void onAttackWhileActive(Skill skill, Player rpgPlayer, Attack attackInfo) {
            if(!attackInfo.isRangeAttack()) {
                FireworkUtil.spawnFirework(attackInfo.getVictim().getMinecraftEntity().getLocation(),
                        Color.fromRGB(0xff9900), FireworkEffect.Type.BALL, rpgPlayer.getMinecraftPlayer());
                double damage = attackInfo.getVictim().damagePhysic(rpgPlayer.getAtk() * 0.5 + 100,
                        rpgPlayer.getPenetrate() + rpgPlayer.getAtk(), rpgPlayer);
                attackInfo.setDamage(attackInfo.getDamage() + damage);
                skill.setActiveTimeTicks(skill.getSkillAction().getFinishTimeTicks(skill, rpgPlayer) + 1);
            }
        }
    }, false),
    MANA_CHARGE("마나 차지", 10, 15, item -> {
        item.setType(Material.DIAMOND_SHOVEL);
        return item;
    }, new SkillAction() {
        @Override
        public void onStart(Skill skill, Player rpgPlayer) {
            org.bukkit.entity.Player player = rpgPlayer.getMinecraftPlayer();
            int count = Math.min(9, 4 + skill.getLevel());
            if(player != null) {
                player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_CHAIN, 1, 0.9f);
                player.sendMessage(org.bukkit.ChatColor.DARK_GRAY + "┌──────────────────────────┐");
                player.sendMessage(ChatColor.YELLOW + "   좌클릭" + ChatColor.WHITE + String.format("으로 마력 탄환을 10초동안 최대 %d개 까지 발사하세요.", count));
                player.sendMessage(org.bukkit.ChatColor.DARK_GRAY + "└──────────────────────────┘");
                StringBuilder bullets = new StringBuilder();
                for(int i = 0; i < count; i++) {
                    bullets.append(" ◆ ");
                }
                player.sendActionBar(bullets.toString());
            }
            skill.setExtra("latestShoot", "0");
            skill.setExtra("count", count + "");
            skill.setExtra("maxCount", count + "");
        }
        @Override
        public void onActive(Skill skill, Player rpgPlayer) {
            org.bukkit.entity.Player player = rpgPlayer.getMinecraftPlayer();
            if(player != null && player.isSneaking()) {
                rpgPlayer.setMoveSpeed(-200);
            }
        }
        @Override
        public void onStop(Skill skill, Player rpgPlayer) {
            org.bukkit.entity.Player player = rpgPlayer.getMinecraftPlayer();
            if(player != null) {
                if(skill.getActiveTimeTicks() == skill.getSkillAction().getFinishTimeTicks(skill, rpgPlayer)) {
                    player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_CHAIN, 1, 0.9f);
                    player.sendMessage(org.bukkit.ChatColor.DARK_GRAY + "┌──────────┐");
                    player.sendMessage(ChatColor.GOLD + "   지속시간이 끝났습니다.");
                    player.sendMessage(org.bukkit.ChatColor.DARK_GRAY + "└──────────┘");
                }
                else {
                    player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_CHAIN, 1, 0.9f);
                    player.sendMessage(org.bukkit.ChatColor.DARK_GRAY + "┌────────────┐");
                    player.sendMessage(ChatColor.GOLD + "   탄환을 모두 사용했습니다.");
                    player.sendMessage(org.bukkit.ChatColor.DARK_GRAY + "└────────────┘");
                }
            }
        }
        @Override
        public boolean canPayCost(Skill skill, Player rpgPlayer) {
            return rpgPlayer.getMana() >= 200 + skill.getLevel() * 10;
        }
        @Override
        public String getCannotPayCostReason(Skill skill, Player rpgPlayer) {
            return "마나가 부족합니다.";
        }
        @Override
        public void payCost(Skill skill, Player rpgPlayer) {
            rpgPlayer.setMana(rpgPlayer.getMana() - (200 + skill.getLevel() * 10));
        }
        @Override
        public int getFinishTimeTicks(Skill skill, Player rpgPlayer) {
            return 10 * 20;
        }
        @Override
        public String getDescription(Skill skill, Player rpgPlayer) {
            return String.format("스킬을 발동하고 나서 " + ChatColor.GOLD + "10초" + ChatColor.RESET + "의 지속시간 동안 좌클릭" +
                    "으로 " + ChatColor.BLUE + "최대 %d개" + ChatColor.RESET + "의 마나 탄환을 발사합니다. " +
                    "탄환은 멀리 갈수록 대미지가 " + ChatColor.RED + "최소 %.0f" + ChatColor.RESET + "에서 " +
                    ChatColor.RED + "%.0f + 마법 공격력의 90%%" + ChatColor.RESET + "까지 " +
                    "늘어나며, 마지막 탄환은 " + ChatColor.RED + "130%%" + ChatColor.RESET + "의 피해를 입힙니다.",
                    5, 350.0, 600.0 + skill.getLevel() * 50.0);
        }
        @Override
        public void onInteractWhileActive(Skill skill, Player rpgPlayer, PlayerInteractEvent event) {
            org.bukkit.entity.Player player = rpgPlayer.getMinecraftPlayer();
            long latestShoot = skill.getExtra("latestShoot") == null ? 0 : Long.parseLong(skill.getExtra("latestShoot"));
            int count = skill.getExtra("count") == null ? 5 : Integer.parseInt(skill.getExtra("count"));
            int maxCount = skill.getExtra("maxCount") == null ? 5 : Integer.parseInt(skill.getExtra("maxCount"));
            if(new Date().getTime() - latestShoot < 700) return;
            if(player == null) return;
            if(event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
                skill.setExtra("latestShoot", new Date().getTime() + "");
                count--;
                skill.setExtra("count", count + "");
                StringBuilder bullets = new StringBuilder();
                for(int i = 0; i < maxCount; i++) {
                    if(i == count) bullets.append(ChatColor.RED);
                    if(i < count) {
                        bullets.append(" ◆ ");
                    }
                    else {
                        bullets.append(" ◇ ");
                    }
                }
                player.sendActionBar(bullets.toString());
                if(count <= 0) skill.setActiveTimeTicks(skill.getSkillAction().getFinishTimeTicks(skill, rpgPlayer) + 1);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1, count == 0 ? 1f : 1.5f);
                Vector direction = player.getEyeLocation().getDirection();
                Location eyeLocation = player.getEyeLocation();
                boolean[] collide = {false};
                Location selfLoc = player.getLocation();
                selfLoc.setPitch(selfLoc.getPitch() - 10);
                player.teleport(selfLoc);
                for(double i = 0.6; i < 50.0; i += 0.3) {
                    final Location target = eyeLocation.clone().add(direction.clone().multiply(i));
                    final double minDamage = 300, maxDamage = 650 + skill.getLevel() * 50.0 + rpgPlayer.getMagicAtk() * 0.9;
                    final double damage = (minDamage + Math.min(maxDamage - minDamage, i * (maxDamage - minDamage) / 50.0) * (count == 0 ? 1.3 : 1));
                    final double finalI = i;
                    int finalCount = count;
                    Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                        if(collide[0]) return;
                        ParticleUtil.createColoredParticle(target,
                                Color.fromRGB(Math.min(0xff, (int)(finalI * 10 + (finalCount == 0 ? 0x55 : 0x00))),
                                        0x00, 0xff - (finalCount == 0 ? 0x55 : 0x00)),
                                1, 1.5f, 0.1);
                        ParticleUtil.createParticle(target, Particle.END_ROD, 1);
                        if(CollideCalculator.isCollideHardnessBlock(target, 0.4)) collide[0] = true;
                        for(LivingEntity entity : player.getWorld().getNearbyLivingEntities(target, 0.3)) {
                            if(entity == player) continue;
                            rpg.entity.Entity rpgEntity = rpg.entity.Entity.getEntity(entity);
                            if(rpgEntity == null) continue;
                            entity.damage(0.1);
                            Vector vel = direction.clone().multiply(0.3);
                            vel.setY(0.1);
                            entity.setVelocity(entity.getVelocity().add(vel));
                            rpgPlayer.showAttackBossbar(rpgEntity, damage, false);
                            rpgEntity.damageMagic(damage, rpgPlayer);
                            collide[0] = true;
                        }
                    }, (int)(i / 3));
                }
            }
        }
    }, false),
    FIRE_BALL("파이어볼", 10, 15, item -> {
        item.setType(Material.FIRE_CHARGE);
        return item;
    }, new SkillAction() {
        @Override
        public void onStart(Skill skill, Player rpgPlayer) {
            org.bukkit.entity.Player player = rpgPlayer.getMinecraftPlayer();
            int count = Math.min(4, 1 + skill.getLevel() / 2);
            if(player != null) {
                player.getWorld().playSound(player.getLocation(), Sound.BLOCK_FIRE_AMBIENT, 0.5f, 1);
                player.sendMessage(org.bukkit.ChatColor.DARK_GRAY + "┌──────────────────────────┐");
                player.sendMessage(ChatColor.YELLOW + "   좌클릭" + ChatColor.WHITE +
                        String.format("으로 화염 구체를 60초동안 최대 %d개 까지 발사하세요.", count));
                player.sendMessage(org.bukkit.ChatColor.DARK_GRAY + "└──────────────────────────┘");
                StringBuilder bullets = new StringBuilder(ChatColor.GOLD.toString());
                for(int i = 0; i < count; i++) {
                    bullets.append(" ◆ ");
                }
                player.sendActionBar(bullets.toString());
            }
            skill.setExtra("latestShoot", "0");
            skill.setExtra("rotate", "0");
            skill.setExtra("count", count + "");
            skill.setExtra("maxCount", count + "");
        }
        @Override
        public void onActive(Skill skill, Player rpgPlayer) {
            Bukkit.getScheduler().runTask(Main.getPlugin(), () -> {
                org.bukkit.entity.Player player = rpgPlayer.getMinecraftPlayer();
                if(player != null) {
                    float rotate = Float.parseFloat(skill.getExtra("rotate"));
                    int count = Integer.parseInt(skill.getExtra("count"));
                    float r = rotate;

                    for(int i = 0; i < count; i++) {
                        Location dl = player.getLocation();
                        dl.setPitch(0);
                        dl.setYaw(r);
                        Vector direction = dl.getDirection();
                        r += 360.0f / count;
                        Location loc = player.getLocation().add(direction.clone().multiply(1)).add(0, 0.5, 0);
                        if(rpgPlayer.getTicks() % 15 == 0)
                            player.getWorld().playSound(loc, Sound.BLOCK_FIRE_AMBIENT, 0.2f, 1);
                        ParticleUtil.createColoredParticle(loc, Color.ORANGE, 1, 0.7f, 0);
                        ParticleUtil.createColoredParticle(loc, Color.RED, 1, 0.9f, 0);
                        ParticleUtil.createColoredParticle(loc, Color.YELLOW, 1, 0.5f, 0);
                    }

                    rotate += 10;
                    skill.setExtra("rotate", (rotate % 360) + "");

                    final Vector direction = player.getEyeLocation().getDirection();
                    final Location eyeLocation = player.getEyeLocation();
                    final Location start = eyeLocation.clone().add(direction.clone().multiply(0.5));

                    AbstractProjectile.launchIgnoreTicks(start, 30, 25, 30,
                            0.01, 0.3, 0.3, Double.MAX_VALUE, false,
                            0.2, new AbstractProjectile.Update() {
                                @Override
                                public void updateTicks(Location location, int ticks) { }
                                @Override
                                public void updateLocation(Location location) {
                                    player.spawnParticle(Particle.REDSTONE, location, 1, 0, 0, 0, 0,
                                            new Particle.DustOptions(Color.fromRGB(0xff5555), 0.2f));
                                }
                                @Override
                                public void onHit(Location location, List<Entity> hitEntities) { }
                                @Override
                                public void onCollide(Location location) {
                                    for(double x = -0.3; x <= 0.3; x += 0.1) {
                                        for(double y = -0.3; y <= 0.3; y += 0.1) {
                                            for(double z = -0.3; z <= 0.3; z += 0.1) {
                                                if(Math.sqrt(x * x + y * y + z * z) <= 0.3) {
                                                    player.spawnParticle(Particle.REDSTONE, location.clone().add(x, y, z),
                                                            1, 0, 0, 0, 0,
                                                            new Particle.DustOptions(Color.fromRGB(0xff5555), 0.4f));
                                                }
                                            }
                                        }
                                    }
                                }
                            }, rpgPlayer, 200);
                }
            });
        }
        @Override
        public void onInteractWhileActive(Skill skill, Player rpgPlayer, PlayerInteractEvent event) {
            org.bukkit.entity.Player player = rpgPlayer.getMinecraftPlayer();
            long latestShoot = Long.parseLong(skill.getExtra("latestShoot"));
            int count = Integer.parseInt(skill.getExtra("count"));
            int maxCount = Integer.parseInt(skill.getExtra("maxCount"));
            if(new Date().getTime() - latestShoot < 500) return;
            if(player == null) return;
            double damage = 100 + Math.pow(skill.getLevel() * 120, 1.2) + rpgPlayer.getMagicAtk();
            if(event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
                skill.setExtra("latestShoot", new Date().getTime() + "");
                count--;
                skill.setExtra("count", count + "");
                StringBuilder bullets = new StringBuilder(ChatColor.GOLD.toString());
                for (int i = 0; i < maxCount; i++) {
                    if (i == count) bullets.append(ChatColor.DARK_RED);
                    if (i < count) {
                        bullets.append(" ◆ ");
                    } else {
                        bullets.append(" ◇ ");
                    }
                }
                player.sendActionBar(bullets.toString());
                if (count <= 0)
                    skill.setActiveTimeTicks(skill.getSkillAction().getFinishTimeTicks(skill, rpgPlayer) + 1);
                player.getWorld().playSound(player.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1, count == 0 ? 1f : 1.5f);
                final Location eyeLocation = player.getEyeLocation();
                final Vector direction = player.getEyeLocation().getDirection();
                final Location start = eyeLocation.clone().add(direction.clone().multiply(0.5));
                AbstractProjectile.launch(start, 30, 25, 30,
                        0.01, 0.3, 0.3, Double.MAX_VALUE, false,
                        0.2, new AbstractProjectile.Update() {
                            @Override
                            public void updateTicks(Location location, int ticks) {
                                if(rpgPlayer.getTicks() % 18 == 0)
                                    player.getWorld().playSound(location, Sound.BLOCK_FIRE_AMBIENT, 1, 0.5f);
                            }
                            @Override
                            public void updateLocation(Location location) {
                                if(Math.random() < 0.4)
                                    ParticleUtil.createParticle(location, Particle.SMOKE_NORMAL, 1, 0.1);
                                if(Math.random() < 0.05)
                                    ParticleUtil.createParticle(location, Particle.LAVA, 1, 0.0001);
                                if(Math.random() < 0.2)
                                    ParticleUtil.createParticle(location, Particle.FLAME, 2, 0.01);
                                ParticleUtil.createColoredParticle(location, Color.fromRGB(0xff9900), 1, 1.0f, 0);
                                ParticleUtil.createColoredParticle(location, Color.fromRGB(0xffff00), 1, 0.5f, 0);
                                ParticleUtil.createColoredParticle(location, Color.fromRGB(0xff3300), 1, 0.8f, 0);
                            }
                            @Override
                            public void onHit(Location location, List<Entity> hitEntities) {
                                for(Entity rpgEntity : hitEntities) {
                                    LivingEntity le = rpgEntity.getMinecraftEntity();
                                    if(le == null) continue;
                                    le.damage(0.01);
                                    Vector vel = le.getVelocity();
                                    vel.add(le.getLocation().subtract(location).toVector().clone().multiply(1.2));
                                    le.setVelocity(vel);
                                    double realDamage = rpgEntity.damageMagic(damage, rpgPlayer);
                                    rpgEntity.addEffect(new Effect(EffectType.FIRE, 6 + skill.getLevel(), skill.getLevel(), rpgPlayer));
                                    rpgEntity.addEffect(new Effect(EffectType.SLOWNESS, 7, 1, rpgPlayer));
                                    rpgPlayer.showAttackBossbar(rpgEntity, realDamage, false);
                                }
                            }
                            @Override
                            public void onCollide(Location location) {
                                FireworkUtil.spawnFirework(location,
                                        new Color[]{Color.fromRGB(0xff3300), Color.fromRGB(0xff6600), Color.fromRGB(0xffaa00)},
                                        new Color[]{Color.fromRGB(0xff9900)},
                                        FireworkEffect.Type.BURST, player);
                                for(LivingEntity le : player.getWorld().getNearbyLivingEntities(location, 2)) {
                                    Entity rpgEntity = Entity.getEntity(le);
                                    if(rpgEntity != null) {
                                        le.damage(0.01);
                                        Vector vel = le.getVelocity();
                                        vel.add(le.getLocation().toVector().subtract(location.toVector()).multiply(0.5));
                                        le.setVelocity(vel);
                                        if(le != player) {
                                            double realDamage = rpgEntity.damageMagic(damage / 5, rpgPlayer);
                                            if(rpgPlayer.getBossBarVisibleTime(Player.ATTACK_BOSS_BAR) <= 0)
                                                rpgPlayer.showAttackBossbar(rpgEntity, realDamage, false);
                                            rpgEntity.addEffect(new Effect(EffectType.FIRE, 6, skill.getLevel() / 2 + 1, rpgPlayer));
                                        }
                                    }
                                }
                            }
                        }, rpgPlayer);
            }
        }
        @Override
        public void onStop(Skill skill, Player rpgPlayer) {
            org.bukkit.entity.Player player = rpgPlayer.getMinecraftPlayer();
            if(player != null) {
                if(skill.getActiveTimeTicks() == skill.getSkillAction().getFinishTimeTicks(skill, rpgPlayer)) {
                    player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 0.9f);
                    player.sendMessage(org.bukkit.ChatColor.DARK_GRAY + "┌──────────┐");
                    player.sendMessage(ChatColor.GOLD + "   지속시간이 끝났습니다.");
                    player.sendMessage(org.bukkit.ChatColor.DARK_GRAY + "└──────────┘");
                }
                else {
                    player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 0.9f);
                    player.sendMessage(org.bukkit.ChatColor.DARK_GRAY + "┌─────────────┐");
                    player.sendMessage(ChatColor.GOLD + "   화염 구체를 모두 발사했습니다.");
                    player.sendMessage(org.bukkit.ChatColor.DARK_GRAY + "└─────────────┘");
                }
            }
        }
        @Override
        public boolean canPayCost(Skill skill, Player rpgPlayer) {
            return rpgPlayer.getMana() >= 400 + skill.getLevel() * 10;
        }
        @Override
        public String getCannotPayCostReason(Skill skill, Player rpgPlayer) {
            return "마나가 부족합니다.";
        }
        @Override
        public void payCost(Skill skill, Player rpgPlayer) {
            rpgPlayer.setMana(rpgPlayer.getMana() - (400 + skill.getLevel() * 10));
        }
        @Override
        public int getFinishTimeTicks(Skill skill, Player rpgPlayer) {
            return 60 * 20;
        }
        @Override
        public String getDescription(Skill skill, Player rpgPlayer) {
            return String.format("스킬을 발동하고 나서 " + ChatColor.GOLD + "60초" + ChatColor.RESET + "의 지속시간 동안 좌클릭" +
                    "으로 " + ChatColor.BLUE + "최대 %d개" + ChatColor.RESET + "의 화염 구체를 발사합니다. " +
                    "화염 구체를 적에게 맞힐 시 " + ChatColor.RED + "%.1f의 대미지" + ChatColor.RESET + "를 주고, " +
                            ChatColor.DARK_AQUA + "화염과 둔화 효과" + ChatColor.RESET + "를 부여하며," +
                            " 터진 위치 주변에 " + ChatColor.GOLD + "화염 피해" + ChatColor.RESET + "를 줍니다.",
                    Math.min(4, 1 + skill.getLevel() / 2), 100 + Math.pow(skill.getLevel() * 120, 1.2) + rpgPlayer.getMagicAtk());
        }
    }, false),
    SCAMPER("질주", 10, 10, item -> {
        item.setType(Material.LEATHER_BOOTS);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        meta.setColor(Color.fromRGB(0x9999ff));
        item.setItemMeta(meta);
        return item;
    }, new SkillAction() {
        @Override
        public void onStart(Skill skill, Player rpgPlayer) {
            org.bukkit.entity.Player player = rpgPlayer.getMinecraftPlayer();
            if(player == null) return;
            player.getWorld().playSound(player.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_1, 1, 1);
            ParticleUtil.createParticle(player.getLocation(), Particle.CLOUD, 10, 0.1);
        }
        @Override
        public void onActive(Skill skill, Player rpgPlayer) {
            org.bukkit.entity.Player player = rpgPlayer.getMinecraftPlayer();
            if(player == null) return;
            rpgPlayer.setMoveSpeed(rpgPlayer.getMoveSpeed() * 1.3);
            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                ParticleUtil.createColoredParticle(player.getLocation(), Color.WHITE, 2, 1, 0.1, 0.1, 0.1, 0.1);
            }, 1);
        }
        @Override
        public void onStop(Skill skill, Player rpgPlayer) { }
        @Override
        public boolean canPayCost(Skill skill, Player rpgPlayer) {
            return rpgPlayer.getMana() >= 200;
        }
        @Override
        public String getCannotPayCostReason(Skill skill, Player rpgPlayer) {
            return "마나가 부족합니다.";
        }
        @Override
        public void payCost(Skill skill, Player rpgPlayer) {
            rpgPlayer.setMana(rpgPlayer.getMana() - 200);
        }
        @Override
        public int getFinishTimeTicks(Skill skill, Player rpgPlayer) {
            return (10 + skill.getLevel()) * 20;
        }
        @Override
        public String getDescription(Skill skill, Player rpgPlayer) {
            return (skill.getLevel() + 10) + "초" + ChatColor.RESET + " 동안 이동속도가 " +
                    ChatColor.GOLD + "30%" + ChatColor.RESET + " 빨라집니다.";
        }
    }, false),
    SPIN_LOP("회전베기", 10, 13, item -> {
        item.setType(Material.NETHERITE_AXE);
        return item;
    }, new SkillAction() {
        @Override
        public void onStart(Skill skill, Player rpgPlayer) {
            org.bukkit.entity.Player player = rpgPlayer.getMinecraftPlayer();
            if(player == null) return;
            skill.setExtra("yaw", player.getLocation().getYaw() + "");
            skill.setExtra("originalYaw", player.getLocation().getYaw() + "");
            skill.setExtra("rotate", "0");
            skill.setExtra("start", null);
        }
        @Override
        public void onActive(Skill skill, Player rpgPlayer) {
            org.bukkit.entity.Player player = rpgPlayer.getMinecraftPlayer();
            if(player == null) return;
            float yaw = skill.getExtra("yaw") == null ?
                    player.getLocation().getYaw() : Float.parseFloat(skill.getExtra("yaw"));
            float rotate = skill.getExtra("rotate") == null ?
                    0 : Float.parseFloat(skill.getExtra("rotate"));
            float ticks = skill.getSkillAction().getFinishTimeTicks(skill, rpgPlayer);
            Location horizontal = player.getLocation();
            horizontal.setPitch(0);
            horizontal.setY(horizontal.getY() + 0.9);
            player.getWorld().playSound(player.getLocation(), Sound.ITEM_TRIDENT_THROW, 1, 0.7f);
            if(skill.getExtra("start") == null) {
                Bukkit.getScheduler().runTask(Main.getPlugin(), () -> {
                    for(LivingEntity entity : player.getWorld().getNearbyLivingEntities(horizontal, 4, 0.5)) {
                        if(entity == player) continue;
                        if(entity.getLocation().distance(horizontal) <= 3) {
                            Entity rpgEntity = Entity.getEntity(entity);
                            if(rpgEntity != null) {
                                double damage = rpgPlayer.getAtk() * 0.7 + 100;
                                rpgPlayer.setAtk(damage);
                                rpgPlayer.setLatestAttack(0);
                                entity.damage(1, player);
                                if (entity.getLocation().distance(horizontal) > 1.8) {
                                    rpgEntity.addEffect(new Effect(EffectType.BLOOD, 5, skill.getLevel(), rpgPlayer));
                                    rpgEntity.addEffect(new Effect(EffectType.SLOWNESS, 8, (int) (skill.getLevel() * 1.5 + 1), rpgPlayer));
                                }
                            }
                        }
                    }
                });
            }
            for(float i = yaw; i < yaw + 360 / ticks; i += 3) {
                float ii = rotate + i - yaw;
                if(ii > 350) return;
                horizontal.setYaw(i);
                Vector direction = horizontal.getDirection();
                double start_j = Math.max(1.7, 2.4 - Math.pow(rotate / 360, 2));
                if(i % 30 < 3) {
                    for(double j = 0.3; j < start_j; j += 0.1) {
                        Location loc = horizontal.clone().add(direction.clone().multiply(j));
                        ParticleUtil.createColoredParticle(loc, Color.fromRGB(0x443300), 1, 0.8f);
                    }
                }
                for(double j = start_j; j < 3; j += 0.15) {
                    Color col = ColorUtil.mixColor(Color.fromRGB(0x444444), Color.fromRGB(0xff0066),
                            Math.min(1.0, Math.max(0.0,
                                    (j - start_j) / (3 - start_j)
                            )));
                    Location loc = horizontal.clone().add(direction.clone().multiply(j));
                    ParticleUtil.createColoredParticle(loc, col, 1, 1.1f);
                }
                ParticleUtil.createParticle(horizontal.clone().add(direction.clone().multiply(3)), Particle.SMOKE_NORMAL, 1, 0);
            }
            rotate += 360 / ticks;
            yaw += 360 / ticks;
            Location loc = player.getLocation();
            loc.setYaw(yaw);
            float finalYaw = yaw;
            Bukkit.getScheduler().runTask(Main.getPlugin(), () -> {
                Location tel = player.getLocation();
                tel.setYaw(finalYaw);
                player.teleport(tel);
            });
            skill.setExtra("yaw", yaw + "");
            skill.setExtra("rotate", rotate + "");
        }
        @Override
        public void onStop(Skill skill, Player rpgPlayer) {
            org.bukkit.entity.Player player = rpgPlayer.getMinecraftPlayer();
            if(player == null) return;
            float yaw = skill.getExtra("originalYaw") == null ?
                    player.getLocation().getYaw() : Float.parseFloat(skill.getExtra("originalYaw"));
            Bukkit.getScheduler().runTask(Main.getPlugin(), () -> {
                Location loc = player.getLocation();
                loc.setYaw(yaw);
                player.teleport(loc);
            });
        }
        @Override
        public boolean canPayCost(Skill skill, Player rpgPlayer) {
            return rpgPlayer.getMana() >= 90 + skill.getLevel() * 13;
        }
        @Override
        public String getCannotPayCostReason(Skill skill, Player rpgPlayer) {
            return "마나가 부족합니다.";
        }
        @Override
        public void payCost(Skill skill, Player rpgPlayer) {
            rpgPlayer.setMana(rpgPlayer.getMana() - 90 + skill.getLevel() * 13);
        }
        @Override
        public int getFinishTimeTicks(Skill skill, Player rpgPlayer) {
            return 8;
        }
        @Override
        public String getDescription(Skill skill, Player rpgPlayer) {
            return "반경 3m를 회전하면서 벱니다. 베인 적들에게는 " + ChatColor.RED + "100 + 근거리 공격력의 70%" +
                    ChatColor.RESET + "를 피해로 주고, " + ChatColor.DARK_AQUA + "출혈과 둔화 효과" + ChatColor.RESET + "를 부여합니다.";
        }
    }, false),
    MULTI_SHOT("멀티샷", 10, 7, item -> {
        item.setType(Material.TIPPED_ARROW);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        meta.setColor(Color.fromRGB(0x00ff99));
        item.setItemMeta(meta);
        return item;
    }, new SkillAction() {
        @Override
        public void onStart(Skill skill, Player rpgPlayer) {
            org.bukkit.entity.Player player = rpgPlayer.getMinecraftPlayer();
            if(player == null) return;
            Location eyeLoc = player.getEyeLocation();
            int i = 0;
            for(double angle = -20; angle <= 20; angle += 10) {
                double finalAngle = angle;
                Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                    Location temp = eyeLoc.clone();
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1, 1);
                    temp.setYaw((float) (temp.getYaw() - finalAngle));
                    Vector direction = temp.getDirection();
                    Projectile projectile = player.launchProjectile(Arrow.class, direction.clone().multiply(3.5));
                    ProjectileManager.registerProjectile(projectile, new ProjectileEvent() {
                        @Override
                        public void update(Projectile projectile) {
                            ParticleUtil.createColoredParticle(projectile.getLocation(), Color.fromRGB(0x00ff99), 1, 1, 0);
                            if(projectile.isOnGround()) {
                                FireworkUtil.spawnFirework(projectile.getLocation(),
                                        new Color[]{Color.fromRGB(0x00ff99), Color.YELLOW}, new Color[]{Color.YELLOW, Color.WHITE},
                                        FireworkEffect.Type.BALL, player);
                                projectile.remove();
                            }
                        }
                        @Override
                        public void onBeforeHit(Projectile projectile, Entity abuser, Entity victim) {
                            abuser.setLatestAttack(0);
                            abuser.setRangeAtk(abuser.getRangeAtk() * (0.5 + skill.getLevel() * 0.1));
                        }
                        @Override
                        public void onHit(Projectile projectile, Attack attackInfo) {
                            LivingEntity le = attackInfo.getVictim().getMinecraftEntity();
                            if(le != null) FireworkUtil.spawnFirework(le.getLocation(),
                                    new Color[]{Color.fromRGB(0x00ff99), Color.YELLOW}, new Color[]{Color.YELLOW, Color.WHITE},
                                    FireworkEffect.Type.BALL, player);
                        }
                    });
                }, i++ * 2);
            }
        }
        @Override
        public void onActive(Skill skill, Player rpgPlayer) { }
        @Override
        public void onStop(Skill skill, Player rpgPlayer) { }
        @Override
        public boolean canPayCost(Skill skill, Player rpgPlayer) {
            return rpgPlayer.getMana() >= 100 + skill.getLevel() * 15;
        }
        @Override
        public String getCannotPayCostReason(Skill skill, Player rpgPlayer) {
            return "마나가 부족합니다.";
        }
        @Override
        public void payCost(Skill skill, Player rpgPlayer) {
            rpgPlayer.setMana(rpgPlayer.getMana() - (100 + skill.getLevel() * 15));
        }
        @Override
        public int getFinishTimeTicks(Skill skill, Player rpgPlayer) {
            return 0;
        }
        @Override
        public String getDescription(Skill skill, Player rpgPlayer) {
            return String.format("보는 방향으로 " + ChatColor.GOLD + "화살 5개" + ChatColor.RESET +
                    "가 방사형으로 거의 동시에 날아갑니다. " +
                    "각각의 화살은 " + ChatColor.RED + "원거리 공격력의 %.1f%%의 피해" + ChatColor.RESET + "를 줍니다.",
                    50.0 + skill.getLevel() * 10);
        }
    }, false),
    NATURAL_HEALING("자연 치유력", 10, 0, item -> {
        item.setType(Material.APPLE);
        return item;
    }, new SkillAction() {
        @Override
        public void onStart(Skill skill, Player rpgPlayer) { }
        @Override
        public void onActive(Skill skill, Player rpgPlayer) {
            if(new Date().getTime() - rpgPlayer.getLatestAttacked() >= 1000 * 10 && rpgPlayer.getTicks() % 100 == 0) {
                double heal = skill.getLevel() * 10 + 20.0;
                rpgPlayer.setLife(rpgPlayer.getLife() + heal);
            }
        }
        @Override
        public void onStop(Skill skill, Player rpgPlayer) { }
        @Override
        public boolean canPayCost(Skill skill, Player rpgPlayer) { return false; }
        @Override
        public String getCannotPayCostReason(Skill skill, Player rpgPlayer) { return null; }
        @Override
        public void payCost(Skill skill, Player rpgPlayer) { }
        @Override
        public int getFinishTimeTicks(Skill skill, Player rpgPlayer) { return 0; }
        @Override
        public String getDescription(Skill skill, Player rpgPlayer) {
            return String.format(ChatColor.GOLD + "10초" + ChatColor.RESET + "동안 공격을 받지 않으면" +
                    ChatColor.BLUE + " 5초" + ChatColor.RESET + "마다 생명력이 " +
                    ChatColor.AQUA + "%d" + ChatColor.RESET + "씩 오릅니다.", skill.getLevel() * 10 + 20);
        }
    }, true)
    ;

    private final String name;
    private final int maxLevel;
    private final double defaultCooldown;
    private final ItemUtil.ItemModifier skillItemModifier;
    private final SkillAction skillAction;
    private final boolean isPassiveSkill;

    SkillPreset(String name, int maxLevel, double defaultCooldown, ItemUtil.ItemModifier skillItemModifier, SkillAction skillAction, boolean isPassiveSkill) {
        this.name = name;
        this.maxLevel = maxLevel;
        this.defaultCooldown = defaultCooldown;
        this.skillItemModifier = skillItemModifier;
        this.skillAction = skillAction;
        this.isPassiveSkill = isPassiveSkill;
    }

    public ItemUtil.ItemModifier getSkillItemModifier() {
        return skillItemModifier;
    }

    public String getName() {
        return name;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public double getDefaultCooldown() {
        return defaultCooldown;
    }

    public SkillAction getSkillAction() {
        return skillAction;
    }

    public static SkillPreset getByName(String skillName) {
        for(SkillPreset preset : SkillPreset.values()) {
            if(preset.getName().equals(skillName)) return preset;
        }
        return null;
    }

    public boolean isPassiveSkill() {
        return isPassiveSkill;
    }
}
