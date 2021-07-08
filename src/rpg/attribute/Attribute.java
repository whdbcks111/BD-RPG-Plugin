package rpg.attribute;

public enum Attribute {

    ATK("근거리 공격력", "atk",
            (rpgEntity, value) -> rpgEntity.setAtk(rpgEntity.getAtk() + value)),
    EXP_ABSORB("경험치 흡수량", "expAbsorb",
            (rpgEntity, value) -> rpgEntity.setExpAbsorb(rpgEntity.getExpAbsorb() + value)),
    MAX_LIFE("최대 생명력", "maxLife",
            (rpgEntity, value) -> rpgEntity.setMaxLife(rpgEntity.getMaxLife() + value)),
    LIFE_REGEN("생명력 재생량", "lifeRegen", "/초",
            (rpgEntity, value) -> rpgEntity.setLifeRegen(rpgEntity.getLifeRegen() + value)),
    MANA_REGEN("마나 재생량", "manaRegen", "/초",
            (rpgEntity, value) -> rpgEntity.setManaRegen(rpgEntity.getManaRegen() + value)),
    MAX_MANA("최대 마나", "maxMana",
            (rpgEntity, value) -> rpgEntity.setMaxMana(rpgEntity.getMaxMana() + value)),
    CRITICAL_CHANCE("치명타 확률", "criticalChance", "%",
            (rpgEntity, value) -> rpgEntity.setCriticalChance(rpgEntity.getCriticalChance() + value)),
    CRITICAL_INCREASE("치명타 피해 증가량", "criticalIncrease", "%",
            (rpgEntity, value) -> rpgEntity.setCriticalIncrease(rpgEntity.getCriticalIncrease() + value)),
    ATTACK_COOLDOWN("공격 쿨타임", "attackCooldown", "초",
            (rpgEntity, value) -> rpgEntity.setAttackCooldown(rpgEntity.getAttackCooldown() + value)),
    RANGE_ATK("원거리 공격력", "rangeAtk",
            (rpgEntity, value) -> rpgEntity.setRangeAtk(rpgEntity.getRangeAtk() + value)),
    DEFEND("방어력", "defend",
            (rpgEntity, value) -> rpgEntity.setDefend(rpgEntity.getDefend() + value)),
    RESISTANCE("마법 저항력", "resistance",
            (rpgEntity, value) -> rpgEntity.setResistance(rpgEntity.getResistance() + value)),
    PENETRATE("방어력 관통력", "penetrate",
            (rpgEntity, value) -> rpgEntity.setPenetrate(rpgEntity.getPenetrate() + value)),
    MAGIC_ATK("마법 공격력", "magicAtk",
            (rpgEntity, value) -> rpgEntity.setMagicAtk(rpgEntity.getMagicAtk() + value)),
    MOVE_SPEED("이동 속도", "moveSpeed",
            (rpgEntity, value) -> rpgEntity.setMoveSpeed(rpgEntity.getMoveSpeed() + value)),
    SHIELD_DEFEND("방패 방어력", "shieldDefend",
            (rpgEntity, value) -> rpgEntity.setShieldDefend(rpgEntity.getShieldDefend() + value));

    private final String displayName, suffix, attributeName;
    private final AttributeApplyAction applyAction;
    Attribute(String displayName, String attributeName, String suffix, AttributeApplyAction applyAction) {
        this.displayName = displayName;
        this.suffix = suffix;
        this.attributeName = attributeName;
        this.applyAction = applyAction;
    }
    Attribute(String displayName, String attributeName, AttributeApplyAction applyAction) {
        this(displayName, attributeName, "", applyAction);
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSuffix() {
        return suffix;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public static Attribute getByAttributeName(String attributeName) {
         for(Attribute attribute : Attribute.values()) {
             if(attribute.getAttributeName().equals(attributeName)) return attribute;
         }
         return null;
    }

    public AttributeApplyAction getApplyAction() {
        return applyAction;
    }
}
