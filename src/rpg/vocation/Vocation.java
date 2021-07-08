package rpg.vocation;


public enum Vocation {
    ADVENTURER("모험가", (player, ticks) -> {
        player.setMoveSpeed(player.getMoveSpeed() * 1.05);
    }, "이동속도가 5% 증가합니다."),
    WARRIOR("전사", (player, ticks) -> {
        player.setAtk(player.getAtk() * 1.1);
        player.setDefend(player.getDefend() * 1.1);
    }, "방어력과 근거리 공격력이 10% 증가합니다."),
    WIZARD("마법사", (player, ticks) -> {
        player.setMagicAtk(player.getMagicAtk() * 1.5);
        player.setMaxMana(player.getMaxMana() * 1.2);
    }, "마법 공격력이 50% 증가하고, 최대 마나량이 20% 증가합니다."),
    ASSASSIN("암살자", (player, ticks) -> {
        player.setMoveSpeed(player.getMoveSpeed() * 1.15);
        player.setAtk(player.getAtk() * 1.20);
        player.setDefend(player.getDefend() * 0.9);
    }, "이동속도가 15% 증가하며, 근거리 공격력이 20% 증가, 방어력이 10% 감소합니다."),
    BERSERKER("광전사", (player, ticks) -> {
        player.setAttackCooldown(player.getAttackCooldown() / 1.15);
        player.setMaxLife(player.getMaxLife() * 1.1);
    }, "공격속도가 15% 증가하며, 최대 생명력이 10% 증가합니다."),
    ARCHER("궁수", (player, ticks) -> {
        player.setMoveSpeed(player.getMoveSpeed() * 1.05);
        player.setRangeAtk(player.getRangeAtk() * 1.2);
    }, "원거리 공격력이 20% 증가하며, 이동속도가 5% 증가합니다.");

    private final String name;
    private final VocationEffect vocationEffect;
    private final String description;

    Vocation(String name, VocationEffect vocationEffect, String description) {
        this.name = name;
        this.vocationEffect = vocationEffect;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public VocationEffect getVocationEffect() {
        return vocationEffect;
    }

    public String getDescription() {
        return description;
    }
}
