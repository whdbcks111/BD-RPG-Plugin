package rpg.utils;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.HashMap;

public class ActionBar {

    private static final HashMap<Player, Long> latestSent = new HashMap<>();

    public static void sendMessage(Player p, String msg, boolean doSaveSentTime) {
        if(doSaveSentTime) {
            latestSent.remove(p);
            latestSent.put(p, new Date().getTime());
        }
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(msg));
    }

    public static void sendMessage(Player p, String msg) {
        sendMessage(p, msg, true);
    }

    public static void broadcastMessage(String msg) {
        for(Player p : Bukkit.getOnlinePlayers()) {
            sendMessage(p, msg);
        }
    }

    public static long getLatestSentTime(Player p) {
        if(latestSent.get(p) == null) return 0;
        return latestSent.get(p);
    }
}
