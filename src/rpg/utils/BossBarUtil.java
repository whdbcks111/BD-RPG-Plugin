package rpg.utils;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import rpg.main.Main;

import java.util.Objects;

public class BossBarUtil {
    public static BossBar applyBossBar(String key, String title, BarColor color, BarStyle style, BarFlag... flags) {
        NamespacedKey namespacedKey = new NamespacedKey(Main.getPlugin(), key);
        if(Bukkit.getServer().getBossBar(namespacedKey) != null) {
            BossBar bb = Bukkit.getServer().getBossBar(namespacedKey);
            if(bb != null) bb.removeAll();
            Bukkit.getServer().removeBossBar(namespacedKey);
        }
        return Bukkit.getServer().createBossBar(namespacedKey, title, color, style, flags);
    }
}
