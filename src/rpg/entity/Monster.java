package rpg.entity;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.util.Vector;
import rpg.main.Main;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

public class Monster extends Entity {

    private static final HashMap<String, Monster> monsterMap = new HashMap<>();
    private MonsterType monsterType;
    private Pattern pattern;
    private boolean isBoss = false;
    private Tendency tendency = Tendency.FENCE;
    private double followRange = 100;
    private double hostileRange = 10;

    public Monster(Mob mob) {
        this(mob.getUniqueId().toString());
    }

    public Monster(String uuid) {
        this.uuid = uuid;

        task = Bukkit.getScheduler().runTaskTimerAsynchronously(Main.getPlugin(), () -> {
            Mob entity = (Mob) getMinecraftEntity();
            if(entity == null) return;
            if(entity.isDead()) {
                monsterMap.remove(uuid);
                Bukkit.getScheduler().cancelTask(task);
                return;
            }
            ticks++;
            if(this.name == null) {
                char[] typeName = getMinecraftEntity().getType().getKey().getKey().toCharArray();
                if('a' <= typeName[0] && typeName[0] <= 'z') typeName[0] += 'A' - 'a';
                for(int i = 0; i < typeName.length; i++) {
                    if(typeName[i] == '_') typeName[i] = ' ';
                    if(i > 0 && typeName[i - 1] == ' '
                            && 'a' <= typeName[i] && typeName[i] <= 'z') typeName[i] += 'A' - 'a';
                }
                this.name = String.valueOf(typeName);
            }

            update();

        }, 1, 1).getTaskId();
        monsterMap.put(uuid, this);
    }

    public Mob getMinecraftMob() {
        return (Mob) getMinecraftEntity();
    }

    @Override
    void update() {
        Mob entity = getMinecraftMob();
        if(entity == null) return;

        entity.setArrowsInBody(0);
        entity.setRemoveWhenFarAway(false);

        if(ticks % 5 == 0) {
            if(monsterType == null) {
                if(entity.getCustomName() != null && entity.getCustomName().contains(ChatColor.COLOR_CHAR + "T")) {
                    String encodedTypeStr = entity.getCustomName()
                            .substring(entity.getCustomName().indexOf(ChatColor.COLOR_CHAR + "T") + 2)
                            .replaceAll(ChatColor.COLOR_CHAR + "", "");
                    StringBuilder typeStr = new StringBuilder();
                    for(char ch : encodedTypeStr.toCharArray()) {
                        typeStr.append((char)(ch - '가'));
                    }
                    try {
                        MonsterType type = MonsterType.valueOf(typeStr.toString());
                        type.getSpawner().initMonster(this);
                        setPattern(type.getPattern());
                        setMonsterType(type);
                        setLife(Double.MAX_VALUE);
                    }
                    catch (IllegalArgumentException ignored) {}
                }
                entity.setCustomName(getDisplayName());
            }
            else {
                StringBuilder customName = new StringBuilder(getDisplayName() + ChatColor.COLOR_CHAR + "T");
                for(char ch : monsterType.name().toCharArray()) {
                    customName.append(ChatColor.COLOR_CHAR);
                    customName.append((char)(ch + '가'));
                }
                entity.setCustomName(customName.toString());
            }
            entity.setCustomNameVisible(true);
        }

        if(tendency == Tendency.HOSTILE && (entity.getTarget() == null ||
                entity.getTarget().getLocation().distance(entity.getLocation()) > hostileRange)) {
            sync(() -> {
                Collection<org.bukkit.entity.Player> list = entity.getWorld()
                        .getNearbyPlayers(entity.getLocation(), hostileRange);
                if(!list.isEmpty()) {
                    entity.setTarget(list.stream().findFirst().get());
                }
            });
        }

        if(tendency != Tendency.PEACEFUL && lastDamager != null
                && new Date().getTime() - latestAttacked < 1000 * 10 && entity.getTarget() == null
                && lastDamager.getMinecraftEntity().getLocation().distance(getMinecraftMob().getLocation()) < followRange) {
            entity.setTarget(lastDamager.getMinecraftEntity());
        }

        if(entity.getTarget() != null
                && entity.getTarget().getLocation().distance(entity.getLocation()) > followRange) {
            entity.setTarget(null);
            sync(() -> {
                entity.setTarget(null);
                entity.getPathfinder().stopPathfinding();
            });
        }

        if(entity.getTarget() != null && entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE) == null) {
            sync(() -> {
                entity.getPathfinder().moveTo(entity.getTarget());
            });
        }

        if(new Date().getTime() - latestAttack >= attackCooldown * 1000
                && entity.getTarget() != null
                && entity.getTarget().getLocation().distance(entity.getLocation()) < 1.9 + Math.random() * 0.3) {
            Vector direction = entity.getTarget().getLocation().subtract(entity.getEyeLocation())
                    .toVector().multiply(1.0 / entity.getTarget().getLocation().distance(entity.getEyeLocation()));
            Location loc1 = entity.getEyeLocation().setDirection(direction);
            Location loc2 = entity.getEyeLocation();
            float yawDist = Math.abs(loc1.getYaw() - loc2.getYaw());
            float pitchDist = Math.abs(loc1.getPitch() - loc2.getPitch());
            if((pitchDist < 60 || pitchDist > 300) && (yawDist < 60 || yawDist > 300)) {
                if(entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE) == null) {
                    sync(() -> {
                        if(entity.getTarget() != null)
                            entity.getTarget().damage(atk, entity);
                    });
                }
                else sync(() -> {
                    if(entity.getTarget() != null)
                        entity.attack(entity.getTarget());
                });
            }
        }

        updateAttributes();
        updateEffects();
        if(pattern == null && monsterType != null && monsterType.getPattern() != null)
            pattern = monsterType.getPattern();
        if(pattern != null) pattern.update(this);

        life = Math.max(0, Math.min(maxLife, life));
        mana = Math.max(0, Math.min(maxMana, mana));

        AttributeInstance moveSpeedAtt = entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if(moveSpeedAtt != null) {
            moveSpeedAtt.setBaseValue(Math.min(2, 0.3 * moveSpeed / 100));
        }

        AttributeInstance maxHealthAtt = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        double maxHealth = maxHealthAtt == null ? 0 : maxHealthAtt.getBaseValue();
        if(life <= 0) {
            sync(() -> entity.setHealth(0));
            Bukkit.getScheduler().cancelTask(task);
        }
        else {
            entity.setHealth(maxHealth);
        }

    }

    @Override
    String getDisplayName() {
        return ChatColor.WHITE + "[" + tendency.getName() + ChatColor.WHITE +
                "] Lv." + getLevel() + " " + (isBoss ? ChatColor.RED : ChatColor.GRAY) + name;
    }

    @Nonnull
    public static Monster spawnMonster(Location location, MonsterType type) {
        World world = location.getWorld();
        if(world == null) return null;
        org.bukkit.entity.Entity mob = world.spawnEntity(location, type.getEntityType());
        if(!(mob instanceof Mob)) return null;
        Monster monster = new Monster((Mob)mob);
        type.getSpawner().initMonster(monster);
        monster.setPattern(type.getPattern());
        monster.setMonsterType(type);
        monster.setLife(Double.MAX_VALUE);
        return monster;
    }

    public static Monster getMonster(LivingEntity le) {
        return getMonster(le.getUniqueId().toString());
    }

    public static Monster getMonster(String uuid) {
        return monsterMap.get(uuid);
    }

    public interface Pattern {
        void update(Monster monster);
    }

    public boolean isBoss() {
        return isBoss;
    }

    public void setBoss(boolean boss) {
        isBoss = boss;
    }

    public Collection<Monster> getAllMonsters() {
        return monsterMap.values();
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public MonsterType getMonsterType() {
        return monsterType;
    }

    public void setMonsterType(MonsterType monsterType) {
        this.monsterType = monsterType;
    }

    public Tendency getTendency() {
        return tendency;
    }

    public void setTendency(Tendency tendency) {
        this.tendency = tendency;
    }

    public double getFollowRange() {
        return followRange;
    }

    public void setFollowRange(double followRange) {
        this.followRange = followRange;
    }

    public double getHostileRange() {
        return hostileRange;
    }

    public void setHostileRange(double hostileRange) {
        this.hostileRange = hostileRange;
    }

    public static enum Tendency {
        HOSTILE(ChatColor.RED + "적대"), 
        FENCE(ChatColor.GOLD + "중립"),
        PEACEFUL(ChatColor.AQUA + "친화");

        private final String name;

        Tendency(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
