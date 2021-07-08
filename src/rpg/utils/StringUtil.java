package rpg.utils;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;

import java.util.LinkedList;
import java.util.List;

public class StringUtil {
    public static List<String> splitByLength(String source, int length) {
        source = source.replaceAll(ChatColor.RESET.toString(), ChatColor.GRAY.toString());
        StringBuilder builder = new StringBuilder();
        List<String> returns = new LinkedList<>();
        StringBuilder colors = new StringBuilder();
        String applyColors = null;
        char[] chars = source.toCharArray();
        boolean isColorChar = false;
        int count = 0;
        int i = 0;
        for(char ch : chars) {
            if(ch != '\n') builder.append(ch);
            if(isColorChar) {
                isColorChar = false;
                colors.append(ch);
            }
            else if(ch == ChatColor.COLOR_CHAR) {
                colors.append(ch);
                isColorChar = true;
            }
            else count += (ch < 128 ? 1 : 2);
            if(count >= (length * 2 - 1) || i == chars.length - 1 || ch == '\n') {
                count = 0;
                returns.add(ChatColor.GRAY + (applyColors == null ? "" : applyColors) + builder.toString().trim());
                builder.delete(0, builder.length());
                applyColors = colors.toString();
            }

            i++;
        }
        return returns;
    }
}
