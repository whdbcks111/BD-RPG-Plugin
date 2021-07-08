package rpg.skill;

import java.util.HashMap;

public class Skill {
    private final SkillPreset preset;
    private final String name;
    private int level = 1;
    private final int maxLevel;
    private double expScale;
    private double cooldown;
    private long latest;
    private long activeTimeTicks = 0;
    private boolean isActive = false;
    private final boolean isPassiveSkill;
    private final SkillAction skillAction;
    private final HashMap<String, String> extras = new HashMap<>();

    private Skill(SkillPreset preset, String name, int maxLevel,
                  double defaultCooldown, boolean isPassiveSkill, SkillAction skillAction) {
        this.preset = preset;
        this.name = name;
        this.maxLevel = maxLevel;
        this.cooldown = defaultCooldown;
        this.isPassiveSkill = isPassiveSkill;
        this.skillAction = skillAction;
    }

    public static Skill createSkill(SkillPreset preset) {
        if(preset == null) return null;
        return new Skill(preset, preset.getName(), preset.getMaxLevel(), preset.getDefaultCooldown(),
                preset.isPassiveSkill(), preset.getSkillAction());
    }

    public SkillPreset getPreset() {
        return preset;
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public double getExpScale() {
        return expScale;
    }

    public void setExpScale(double expScale) {
        this.expScale = expScale;
    }

    public double getCooldown() {
        return cooldown;
    }

    public void setCooldown(double cooldown) {
        this.cooldown = cooldown;
    }

    public long getLatest() {
        return latest;
    }

    public void setLatest(long latest) {
        this.latest = latest;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public SkillAction getSkillAction() {
        return skillAction;
    }

    public void setExtra(String key, String value) {
        extras.put(key, value);
    }

    public HashMap<String, String> getExtras() {
        return extras;
    }

    public String getExtra(String key) {
        return extras.get(key);
    }

    public long getActiveTimeTicks() {
        return activeTimeTicks;
    }

    public void setActiveTimeTicks(long activeTimeTicks) {
        this.activeTimeTicks = activeTimeTicks;
    }

    public boolean isPassiveSkill() {
        return isPassiveSkill;
    }
}
