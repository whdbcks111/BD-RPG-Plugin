package rpg.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Base64;
import java.util.UUID;

public class SkullHead {
    public static ItemStack getPlayerHead(OfflinePlayer player) {
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
        if (skullMeta != null) {
            skullMeta.setOwningPlayer(player);
        }
        itemStack.setItemMeta(skullMeta);
        return itemStack;
    }

    public static ItemStack getCustomHead(Format format) {
        return getCustomHead(format.getURL());
    }

    public static ItemStack getCustomHead(String base64) {

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        if (base64.isEmpty()) return head;

        SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);

        profile.getProperties().put("textures", new Property("textures", base64));

        try {
            Method mtd = skullMeta.getClass().getDeclaredMethod("setProfile", GameProfile.class);
            mtd.setAccessible(true);
            mtd.invoke(skullMeta, profile);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException ex) {
            ex.printStackTrace();
        }

        head.setItemMeta(skullMeta);
        return head;
    }

    public enum Format {

        CRYSTAL_BLOCK("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh" +
                "0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGY3NDY3YzVmNzM4YzY0MTI0N" +
                "mMwOWY4Y2U3OTFlMzM5YTg2ZTgxZGU2MjA0OWI0MWY0OTI4ODgxNzJmYTcyNiJ9fX0="),
        HEALTH("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY" +
                "3JhZnQubmV0L3RleHR1cmUvMWIxNWNlODIzNzcwZDlhMjY5YzFlYmY1ODNkM2U0OTM" +
                "yNzQ3YTEzZWY0MzYxM2NkNGY3NWY4MDRjYTQifX19"),
        EYE("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3Jh" +
                "ZnQubmV0L3RleHR1cmUvMmQyN2YxMzBjMWFjZGQ3ODRjZWVlMmI3NWZiMTgxZmE1MmZmM" +
                "2E3NTAyNDU3NGViNGFmN2ZhZjFhZTc1YmMifX19"),
        MAGIC_BLOCK("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY" +
                "3JhZnQubmV0L3RleHR1cmUvZDkzNmJiMWNjNGFiNmVjY2U2NWI2NDI5ODM5NGZhZmM1ZmUzZj" +
                "c4NzZkN2M5NDFkMDVhOTI5NGZhMzkyYjdjIn19fQ==")
        ;

        private final String url;
        Format(String url) {
            this.url = url;
        }

        public String getURL() {
            return url;
        }
    }
}
