package rpg.calculator;

import rpg.entity.Entity;
import rpg.entity.Monster;
import rpg.entity.Player;

public class ExpCalculator {
    public static long getMaxExp(int level) {
        long maxExp = Entity.DEFAULT_EXP;
        for(int i = 1; i < level; i++) {
            maxExp += getExpAddition(i + 1);
        }
        return maxExp;
    }

    public static long getAllExp(int beginLevel, int endLevel) {
        if(beginLevel > endLevel) return 0;
        if(beginLevel < 1) beginLevel = 1;
        long maxExp = getMaxExp(beginLevel);
        long allExp = maxExp;
        for(int i = beginLevel; i < endLevel; i++) {
            maxExp += getExpAddition(i + 1);
            allExp += maxExp;
        }
        return allExp;
    }

    public static long getGettingExp(Player player, Monster monster) {
        double absorb = player.getExpAbsorb();
        if(monster.getLevel() > player.getLevel() + 5) absorb *= 2;
        return (long) (getMaxExp(monster.getLevel() + 1) * absorb / 100);
    }

    public static long getExpAddition(int afterLevel) {
        return (long) (100 + Math.floor(Math.pow(afterLevel * 5, 1.2)));
    }
}
