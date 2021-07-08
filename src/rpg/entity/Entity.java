package rpg.entity;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import rpg.attribute.Attribute;
import rpg.attribute.Stat;
import rpg.effect.Effect;
import rpg.effect.EffectType;
import rpg.main.Main;
import rpg.utils.ItemUtil;

import java.util.*;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public abstract class Entity {
    protected String uuid;
    protected long ticks = 0;
    protected int task;

    protected String name = null;
    protected int level = 1;
    protected long exp = 0, maxExp = DEFAULT_EXP;
    protected double expAbsorb = DEFAULT_EXP_ABSORB;
    protected final List<Effect> effects = new LinkedList<>();
    protected final List<Effect> removeEffectTask = new LinkedList<>();
    protected final List<Effect> addEffectTask = new LinkedList<>();
    protected final HashMap<String, Object> extras = new HashMap<>();

    public Stat stat = new Stat();

    protected double life = DEFAULT_LIFE;
    protected double maxLife = DEFAULT_LIFE;
    protected double lifeRegen = DEFAULT_LIFE_REGEN;

    protected double mana = DEFAULT_MANA;
    protected double maxMana = DEFAULT_MANA;
    protected double manaRegen = DEFAULT_MANA_REGEN;

    protected double atk = DEFAULT_ATK;
    protected double magicAtk = DEFAULT_MAGIC_ATK;
    protected double rangeAtk = DEFAULT_RANGE_ATK;

    protected double shieldAmount = 0;
    protected double shieldDuration = 0;
    protected double shieldDefend = DEFAULT_SHIELD_DEFEND;
    protected double defend = DEFAULT_DEFEND;
    protected double penetrate = DEFAULT_PENETRATE;
    protected double resistance = DEFAULT_RESISTANCE;

    protected double criticalChance = DEFAULT_CRITICAL_CHANCE;
    protected double criticalIncrease = DEFAULT_CRITICAL_INCREASE;

    protected double moveSpeed = DEFAULT_MOVE_SPEED;
    protected double attackCooldown = DEFAULT_ATTACK_COOLDOWN;

    protected long latestAttack = 0;
    protected long latestAttacked = 0;

    protected Entity lastDamager = null;

    public static final long DEFAULT_EXP = 50;
    public static final double DEFAULT_EXP_ABSORB = 20;
    public static final double DEFAULT_LIFE = 500;
    public static final double DEFAULT_LIFE_REGEN = 5;
    public static final double DEFAULT_MANA = 1000;
    public static final double DEFAULT_MANA_REGEN = 5;
    public static final double DEFAULT_ATK = 75;
    public static final double DEFAULT_MAGIC_ATK = 0;
    public static final double DEFAULT_RANGE_ATK = 158;
    public static final double DEFAULT_CRITICAL_CHANCE = 5;
    public static final double DEFAULT_CRITICAL_INCREASE = 50;
    public static final double DEFAULT_PENETRATE = 0;
    public static final double DEFAULT_RESISTANCE = 0;
    public static final double DEFAULT_DEFEND = 10;
    public static final double DEFAULT_MOVE_SPEED = 100;
    public static final double DEFAULT_ATTACK_COOLDOWN = 0.7;
    public static final double DEFAULT_SHIELD_DEFEND = 0;

    abstract void update();

    protected void updateEffects() {
        LivingEntity entity = getMinecraftEntity();
        if(entity == null) return;
        if(entity.getFireTicks() > 0 && !hasEffect(EffectType.FIRE)) {
            addEffect(new Effect(EffectType.FIRE, entity.getFireTicks() / 20.0, 1));
        }
        for(Effect effect : effects) {
            effect.getEffectType().getAction().update(effect, this, ticks);
            effect.setDuration(effect.getDuration() - 0.05);
            if(effect.getDuration() <= 0) removeEffectTask.add(effect);
        }
        while(!removeEffectTask.isEmpty()) {
            Effect eff = removeEffectTask.remove(0);
            eff.getEffectType().getAction().onRemove(eff, this, ticks);
            effects.remove(eff);
        }
        while(!addEffectTask.isEmpty()) {
            effects.add(addEffectTask.remove(0));
        }
    }

    public void addEffect(Effect targetEffect) {
        for(Effect effect : effects) {
            if(effect.getEffectType() == targetEffect.getEffectType()) {
                if(effect.getLevel() < targetEffect.getLevel()) {
                    effect.setDuration(targetEffect.getDuration());
                    effect.setLevel(targetEffect.getLevel());
                    return;
                }
                else if(effect.getLevel() == targetEffect.getLevel()
                        && effect.getDuration() < targetEffect.getDuration()) {
                    effect.setDuration(targetEffect.getDuration());
                    return;
                }
                else if(effect.getLevel() > targetEffect.getLevel()) return;
            }
        }
        addEffectTask.add(targetEffect);
    }

    public void removeEffect(EffectType effectType, int minLevel, int maxLevel) {
        for(Effect effect : effects) {
            if(effect.getEffectType() == effectType
                    && minLevel <= effect.getLevel() && effect.getLevel() < maxLevel) {
                removeEffectTask.add(effect);
            }
        }
    }

    public void removeEffect(EffectType effectType) {
        for(Effect effect : effects) {
            if(effect.getEffectType() == effectType) {
                removeEffectTask.add(effect);
            }
        }
    }

    public boolean hasEffect(EffectType effectType, int minLevel, int maxLevel) {
        for(Effect effect : effects) {
            if(effect.getEffectType() == effectType
                    && minLevel <= effect.getLevel() && effect.getLevel() < maxLevel) {
                return true;
            }
        }
        return false;
    }

    public boolean hasEffect(EffectType effectType) {
        for(Effect effect : effects) {
            if(effect.getEffectType() == effectType) {
                return true;
            }
        }
        return false;
    }

    abstract String getDisplayName();

    protected void updateSlotAttributes() {
        LivingEntity entity = getMinecraftEntity();
        EntityEquipment equipment = entity.getEquipment();
        if(equipment == null) return;
        for(EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack is = equipment.getItem(slot);
            EquipmentSlot slotType = ItemUtil.getSlotType(is);
            if(slotType != null && slotType != slot) continue;
            for(Attribute attribute : Attribute.values()) {
                Double value = ItemUtil.getAttributeDouble(is, attribute);
                if(value == null) continue;
                attribute.getApplyAction().apply(this, value);
            }
        }
    }

    protected void updateAttributes() {
        atk = DEFAULT_ATK + stat.strength * 6;
        penetrate = DEFAULT_PENETRATE + stat.strength * 0.6;
        moveSpeed = DEFAULT_MOVE_SPEED + stat.agility * 1.2;
        lifeRegen = DEFAULT_LIFE_REGEN + stat.vitality * 0.2 + level * 0.1;
        manaRegen = DEFAULT_MANA_REGEN + stat.mentality * 0.2;
        criticalChance = Math.min(40, DEFAULT_CRITICAL_CHANCE + stat.sensibility * 0.5);
        criticalIncrease = DEFAULT_CRITICAL_INCREASE + stat.sensibility * 0.5;
        defend = DEFAULT_DEFEND + stat.vitality * 0.3;
        attackCooldown = Math.max(0.4, DEFAULT_ATTACK_COOLDOWN - stat.agility * 0.007);
        resistance = DEFAULT_RESISTANCE + stat.vitality * 0.5;
        magicAtk = DEFAULT_MAGIC_ATK + stat.mentality * 10.5;
        rangeAtk = DEFAULT_RANGE_ATK + stat.strength * 7.6;
        maxLife = DEFAULT_LIFE + stat.vitality * 60 + level * 50;
        maxMana = DEFAULT_MANA + stat.mentality * 35 + level * 10;

        updateSlotAttributes();

        attackCooldown = Math.max(attackCooldown, 0);
        moveSpeed = Math.max(moveSpeed, 0);
        atk = Math.max(atk, 0);
        penetrate = Math.max(penetrate, 0);
        lifeRegen = Math.max(lifeRegen, 0);
        manaRegen = Math.max(manaRegen, 0);
        criticalIncrease = Math.max(criticalIncrease, 0);
        criticalChance = Math.max(criticalChance, 0);
        defend = Math.max(defend, 0);
        resistance = Math.max(resistance, 0);
        rangeAtk = Math.max(rangeAtk, 0);
        magicAtk = Math.max(magicAtk, 0);
        maxLife = Math.max(maxLife, 1);
        maxMana = Math.max(maxMana, 1);

        if(shieldDuration > 0) {
            shieldDuration -= 0.05;
            if (shieldAmount <= 0) shieldDuration = 0;
        }
        else if(shieldAmount > 0){
            shieldAmount = 0;
        }

        life += lifeRegen * 0.05;
        mana += manaRegen * 0.05;
    }

    public static void sync(Runnable runnable) {
        Bukkit.getScheduler().runTask(Main.getPlugin(), runnable);
    }

    public LivingEntity getMinecraftEntity() {
        org.bukkit.entity.Entity e = Bukkit.getEntity(UUID.fromString(uuid));
        if(e instanceof LivingEntity) return (LivingEntity) e;
        return null;
    }

    public static Entity getEntity(LivingEntity entity) {
        if(Monster.getMonster(entity) != null) return Monster.getMonster(entity);
        return Player.getPlayer(entity.getUniqueId().toString());
    }

    public String getUuid() {
        return uuid;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double damagePhysic(double atk, double penetrate) {
        double damage = Math.max(0, atk - Math.max(0, defend - penetrate));
        if(getShieldAmount() > 0 && getShieldDuration() > 0) {
            double amount = Math.min(damage, getShieldAmount());
            setShieldAmount(getShieldAmount() - amount);
            damage -= amount;
        }
        setLife(getLife() - damage);
        return damage;
    }

    public double damagePhysic(double atk, double penetrate, Entity attacker) {
        setLastDamager(attacker);
        return damagePhysic(atk, penetrate);
    }

    public double damageMagic(double atk, Entity attacker) {
        setLastDamager(attacker);
        return damageMagic(atk);
    }

    public double damage(double atk, Entity attacker) {
        setLastDamager(attacker);
        return damage(atk);
    }

    public double damageMagic(double atk) {
        double damage = Math.max(0, atk - resistance);
        if(getShieldAmount() > 0 && getShieldDuration() > 0) {
            double amount = Math.min(damage, getShieldAmount());
            setShieldAmount(getShieldAmount() - amount);
            damage -= amount;
        }
        setLife(getLife() - damage);
        return damage;
    }

    public double damage(double atk) {
        double damage = atk;
        if(getShieldAmount() > 0 && getShieldDuration() > 0) {
            double amount = Math.min(damage, getShieldAmount());
            setShieldAmount(getShieldAmount() - amount);
            damage -= amount;
        }
        setLife(getLife() - damage);
        return damage;
    }

    public double getLife() {
        return life;
    }

    public void setLife(double life) {
        this.life = life;
    }

    public double getMaxLife() {
        return maxLife;
    }

    public void setMaxLife(double maxLife) {
        this.maxLife = maxLife;
    }

    public double getAtk() {
        return atk;
    }

    public void setAtk(double atk) {
        this.atk = atk;
    }

    public void setExtra(String key, Object value) {
        extras.put(key, value);
    }

    public HashMap<String, Object> getExtras() {
        return extras;
    }

    public Object getExtra(String key) {
        return extras.get(key);
    }

    public double getLifeRegen() {
        return lifeRegen;
    }

    public void setLifeRegen(double lifeRegen) {
        this.lifeRegen = lifeRegen;
    }

    public long getExp() {
        return exp;
    }

    public void setExp(long exp) {
        this.exp = exp;
    }

    public long getMaxExp() {
        return maxExp;
    }

    public void setMaxExp(long maxExp) {
        this.maxExp = maxExp;
    }

    public double getExpAbsorb() {
        return expAbsorb;
    }

    public void setExpAbsorb(double expAbsorb) {
        this.expAbsorb = expAbsorb;
    }

    public double getMana() {
        return mana;
    }

    public void setMana(double mana) {
        this.mana = mana;
    }

    public double getMaxMana() {
        return maxMana;
    }

    public void setMaxMana(double maxMana) {
        this.maxMana = maxMana;
    }

    public double getManaRegen() {
        return manaRegen;
    }

    public void setManaRegen(double manaRegen) {
        this.manaRegen = manaRegen;
    }

    public long getTicks() {
        return ticks;
    }

    public double getMagicAtk() {
        return magicAtk;
    }

    public void setMagicAtk(double magicAtk) {
        this.magicAtk = magicAtk;
    }

    public double getRangeAtk() {
        return rangeAtk;
    }

    public void setRangeAtk(double rangeAtk) {
        this.rangeAtk = rangeAtk;
    }

    public double getDefend() {
        return defend;
    }

    public void setDefend(double defend) {
        this.defend = defend;
    }

    public double getPenetrate() {
        return penetrate;
    }

    public void setPenetrate(double penetrate) {
        this.penetrate = penetrate;
    }

    public double getResistance() {
        return resistance;
    }

    public void setResistance(double resistance) {
        this.resistance = resistance;
    }

    public double getCriticalChance() {
        return criticalChance;
    }

    public void setCriticalChance(double criticalChance) {
        this.criticalChance = criticalChance;
    }

    public double getCriticalIncrease() {
        return criticalIncrease;
    }

    public void setCriticalIncrease(double criticalIncrease) {
        this.criticalIncrease = criticalIncrease;
    }

    public double getMoveSpeed() {
        return moveSpeed;
    }

    public void setMoveSpeed(double moveSpeed) {
        this.moveSpeed = moveSpeed;
    }

    public double getAttackCooldown() {
        return attackCooldown;
    }

    public void setAttackCooldown(double attackCooldown) {
        this.attackCooldown = attackCooldown;
    }

    public void setShieldAmount(double shieldAmount) {
        this.shieldAmount = shieldAmount;
    }

    public void setShieldDuration(double shieldDuration) {
        this.shieldDuration = shieldDuration;
    }

    public double getShieldAmount() {
        return shieldAmount;
    }

    public void setShield(double shield, double duration) {
        this.shieldAmount = shield;
        this.shieldDuration = duration;
    }

    public Entity getLastDamager() {
        return lastDamager;
    }

    public void setLastDamager(Entity lastDamager) {
        this.lastDamager = lastDamager;
        this.latestAttacked = new Date().getTime();
    }

    public long getLatestAttacked() {
        return latestAttacked;
    }

    public void setLatestAttacked(long latestAttacked) {
        this.latestAttacked = latestAttacked;
    }

    public double getShieldDuration() {
        return shieldDuration;
    }

    public double getShieldDefend() {
        return shieldDefend;
    }

    public void setShieldDefend(double shieldDefend) {
        this.shieldDefend = shieldDefend;
    }

    public long getLatestAttack() {
        return latestAttack;
    }

    public void setLatestAttack(long latestAttack) {
        this.latestAttack = latestAttack;
    }

    public List<Effect> getEffects() {
        return effects;
    }
}
