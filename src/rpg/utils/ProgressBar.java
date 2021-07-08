package rpg.utils;

import org.bukkit.ChatColor;

public class ProgressBar {
    public static String createBar(int length, double value, double max,
                                   String color, ProgressSuffixType suffixType, String shape) {
        if(shape == null) shape = "■";
        StringBuilder result = new StringBuilder(ChatColor.WHITE.toString() + color);
        max = Math.max(0, max);
        value = Math.min(max, Math.max(0, value));

        double scale = value / max;
        double valueLength = scale * length;

        String suffix = "";
        if(suffixType == ProgressSuffixType.PERCENT) {
            suffix = String.format(" %.1f", scale * 100) + "%";
        }
        else if(suffixType == ProgressSuffixType.VALUE_BY_DOUBLE) {
            suffix = String.format(" %.1f/%.1f", value, max);
        }
        else if(suffixType == ProgressSuffixType.VALUE_BY_INT) {
            suffix = String.format(" %d/%d", (int)value, (int)max);
        }

        for(int i = 0; i < (int)valueLength; i++) {
            result.append(shape);
        }
        result.append(ChatColor.GRAY);
        for(int i = (int)valueLength; i < length; i++) {
            result.append(shape);
        }
        result.append(ChatColor.WHITE).append(suffix);
        return result.toString();
    }

    public static String createBar(int length, double value, double max, ChatColor color, ProgressSuffixType suffixType) {
        return createBar(length, value, max, color.toString(), suffixType, null);
    }

    public static String createBar(int length, double value, double max, String color) {
        return createBar(length, value, max, color, false);
    }

    public static String createBar(int length, double value, double max, ChatColor color) {
        return createBar(length, value, max, color.toString(), false);
    }

    public static String createBar(int length, double value, double max) {
        return createBar(length, value, max, ChatColor.WHITE + "", false);
    }

    public static String createBar(int length, double value, double max, boolean showPercent) {
        return createBar(length, value, max, ChatColor.WHITE + "", showPercent);
    }

    public static String createBar(int length, double value, double max, String color, boolean showPercent) {
        return createBar(length, value, max, color, showPercent ? ProgressSuffixType.PERCENT : ProgressSuffixType.NONE, null);
    }

    public static String createBar(int length, double value, double max, ChatColor color, boolean showPercent) {
        return createBar(length, value, max, color.toString(),
                showPercent ? ProgressSuffixType.PERCENT : ProgressSuffixType.NONE, null);
    }
}
