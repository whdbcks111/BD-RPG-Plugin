package rpg.calculator;

import org.bukkit.Location;
import org.bukkit.block.Block;

public class CollideCalculator {

    public static boolean isCollideHardnessBlock(Location location, double range) {
        for(double xx = -0.5; xx <= 0.5; xx += 0.5) {
            for(double yy = -0.5; yy <= 0.5; yy += 0.5) {
                for(double zz = -0.5; zz <= 0.5; zz += 0.5) {
                     Block block = location.clone().add(xx * range, yy * range, zz * range).getBlock();
                     if(block.getType().isSolid()) return true;
                }
            }
        }
        return false;
    }

}
