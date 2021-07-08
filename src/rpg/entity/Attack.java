package rpg.entity;

public class Attack {
    private boolean isCritical;
    private final boolean isRangeAttack;
    private final boolean isBlocking;
    private final double rangeForce;
    private final double damageIncrease;
    private double damage;
    private final Entity abuser, victim;

    public Attack(Entity abuser, Entity victim, double damage,
                  boolean isCritical, double damageIncrease,
                  boolean isRangeAttack, double rangeForce, boolean isBlocking) {
        this.isCritical = isCritical;
        this.isRangeAttack = isRangeAttack;
        this.isBlocking = isBlocking;
        this.rangeForce = rangeForce;
        this.damageIncrease = damageIncrease;
        this.damage = damage;
        this.abuser = abuser;
        this.victim = victim;
    }

    public boolean isCritical() {
        return isCritical;
    }

    public void setCritical(boolean critical) {
        isCritical = critical;
    }

    public boolean isRangeAttack() {
        return isRangeAttack;
    }

    public boolean isBlocking() {
        return isBlocking;
    }

    public double getRangeForce() {
        return rangeForce;
    }

    public double getDamageIncrease() {
        return damageIncrease;
    }

    public double getDamage() {
        return damage;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public Entity getAbuser() {
        return abuser;
    }

    public Entity getVictim() {
        return victim;
    }
}
