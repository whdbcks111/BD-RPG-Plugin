package rpg.utils;

import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class PlayerUtil {
    public static int getPlayerPing(Player player) {
        return ((CraftPlayer) player).getHandle().ping;
    }
}
