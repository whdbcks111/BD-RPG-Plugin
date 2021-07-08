package rpg.utils;

import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Color;

public class ColorUtil {
    public static Color mixColor(Color col1, Color col2, double ratio) {
        return Color.fromRGB(
                (int)(col1.getRed() * (1 - ratio)) + (int)(col2.getRed() * ratio),
                (int)(col1.getGreen() * (1 - ratio)) + (int)(col2.getGreen() * ratio),
                (int)(col1.getBlue() * (1 - ratio)) + (int)(col2.getBlue() * ratio));
    }

    public static String fromRGB(Color color) {
        int rgb = color.asRGB();
        StringBuilder hex = new StringBuilder();
        for(int i = 0; i < 6; i++) {
            hex.insert(0, "0123456789abcdef".split("")[rgb % 16]);
            rgb /= 16;
        }
        return "§x§" + StringUtils.join(hex.toString().split(""), "§");
    }

    public static String fromRGB(int rgb) {
        return fromRGB(Color.fromRGB(rgb));
    }

    public static String fromRGB(String rgb) {
        if(rgb.matches("#[0-9a-fA-F]{6}"))
            return fromRGB(Color.fromRGB(Integer.parseInt(rgb.substring(1), 16)));
        return ChatColor.WHITE.toString();
    }

    public static String addGradient(String source, int... rgb) {
        StringBuilder builder = new StringBuilder();
        char[] str = source.toCharArray();
        for(int i = 0; i < str.length; i++) {
            double colorLen = (str.length - 1) / (double)(rgb.length - 1);
            int index1 = (int)((i / ((double)str.length - 1)) * (rgb.length - 1));
            int color1 = rgb[index1];
            int color2 = rgb.length - 1 == index1 ? rgb[index1] : rgb[index1 + 1];
            double ratio = (i / colorLen) % 1;
            builder.append(fromRGB(mixColor(Color.fromRGB(color1), Color.fromRGB(color2), ratio)));
            builder.append(str[i]);
        }
        return builder.toString();
    }
}
