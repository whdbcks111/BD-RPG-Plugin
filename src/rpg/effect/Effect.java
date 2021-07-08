package rpg.effect;

import rpg.entity.Entity;

public class Effect {

    private final EffectType effectType;
    private double maxDuration;
    private double duration;
    private int level;
    private Entity caster;

    public Effect(EffectType effectType, double duration, int level) {
        this(effectType, duration, level, null);
    }

    public Effect(EffectType effectType, double duration, int level, Entity caster) {
        this.effectType = effectType;
        this.maxDuration = this.duration = duration;
        this.level = level;
        this.caster = caster;
    }

    public EffectType getEffectType() {
        return effectType;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        if(duration > maxDuration) maxDuration = duration;
        this.duration = duration;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public double getMaxDuration() {
        return maxDuration;
    }

    public void setMaxDuration(double maxDuration) {
        this.maxDuration = maxDuration;
    }

    public Entity getCaster() {
        return caster;
    }

    public void setCaster(Entity caster) {
        this.caster = caster;
    }
}
