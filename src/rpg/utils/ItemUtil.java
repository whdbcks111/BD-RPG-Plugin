package rpg.utils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_16_R2.NBTTagCompound;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R2.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import rpg.attribute.Attribute;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ItemUtil {

    public static interface ItemModifier {
        ItemStack modify(ItemStack item);
    }

    public static ItemStack createItem(Material material, String displayName, String lore, int amount, boolean isGlowing) {
        ItemStack is = new ItemStack(material, amount);
        ItemMeta meta = is.getItemMeta();
        if(meta != null) {
            meta.setDisplayName(displayName);
            if(lore != null) {
                meta.setLore(Arrays.asList(lore.split("\\n")));
            }
            is.setItemMeta(meta);
        }
        if(isGlowing) {
            is.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
            is.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        return is;
    }

    public static NBTTagCompound getNBTTag(ItemStack is) {
        net.minecraft.server.v1_16_R2.ItemStack itemStack = CraftItemStack.asNMSCopy(is);
        return itemStack.getTag() != null ? itemStack.getTag() : new NBTTagCompound();
    }

    public static ItemStack getItemByNBT(ItemStack is, NBTTagCompound nbt) {
        net.minecraft.server.v1_16_R2.ItemStack itemStack = CraftItemStack.asNMSCopy(is);
        itemStack.setTag(nbt);
        return CraftItemStack.asCraftMirror(itemStack);
    }

    public static ItemStack setSkillItem(ItemStack is) {
        NBTTagCompound nbt = getNBTTag(is);
        nbt.setBoolean("isSkillItem", true);
        return getItemByNBT(is, nbt);
    }

    public static boolean isSkillItem(ItemStack is) {
        NBTTagCompound nbt = getNBTTag(is);
        return nbt.getBoolean("isSkillItem");
    }

    public static ItemStack setSlotType(ItemStack is, EquipmentSlot slot) {
        NBTTagCompound nbt = getNBTTag(is);
        nbt.setString("slotType", slot.name());
        return getItemByNBT(is, nbt);
    }

    public static boolean isRpgItem(ItemStack is) {
        NBTTagCompound nbt = getNBTTag(is);
        if(nbt.get("attributes") != null) return true;
        if(!nbt.getString("description").isEmpty()) return true;
        return nbt.get("extras") != null;
    }

    public static void applyRpgItem(ItemStack is) {
        List<String> lore = new LinkedList<>();
        EquipmentSlot slotType = ItemUtil.getSlotType(is);
        lore.add(" ");
        if(slotType != null) {
            String slotTypeName;
            switch (slotType) {
                case HEAD: slotTypeName = "머리"; break;
                case FEET: slotTypeName = "발"; break;
                case HAND: slotTypeName = "주로 사용하는 손"; break;
                case LEGS: slotTypeName = "다리"; break;
                case CHEST: slotTypeName = "상체"; break;
                case OFF_HAND: slotTypeName = "다른 손"; break;
                default: slotTypeName = null;
            }
            lore.add(ColorUtil.fromRGB(0xff3355) + "착용부위  " + ColorUtil.fromRGB(0xff5577) +
                    slotTypeName);
        }
        boolean hasAttribute = false;
        for(Attribute attribute : Attribute.values()) {
            Double value = ItemUtil.getAttributeDouble(is, attribute);
            if(value == null) continue;
            if(!hasAttribute)
                lore.add(ChatColor.DARK_GRAY + "┌──────────┐");
            hasAttribute = true;
            lore.add(ColorUtil.fromRGB(0x00aa80) + "  " + attribute.getDisplayName() +
                    ColorUtil.fromRGB(0x00ffcc) + "  +" + String.format("%.1f", value) + attribute.getSuffix());
        }
        if(hasAttribute)
            lore.add(ChatColor.DARK_GRAY + "└──────────┘");
        String desc = ChatColor.stripColor(ItemUtil.getDescription(is));
        if(desc != null) {
            lore.addAll(StringUtil.splitByLength(desc, 14));
        }
        lore.add(" ");
        ItemMeta meta = is.getItemMeta();
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.values());
        is.setItemMeta(meta);
    }

    public static EquipmentSlot getSlotType(ItemStack is) {
        NBTTagCompound nbt = getNBTTag(is);
        try {
            return EquipmentSlot.valueOf(nbt.getString("slotType"));
        }
        catch (IllegalArgumentException ignored) {}
        return null;
    }

    public static String getDescription(ItemStack is) {
        NBTTagCompound nbt = getNBTTag(is);
        if(!nbt.getString("description").isEmpty()) return nbt.getString("description");
        return null;
    }

    public static ItemStack getDescription(ItemStack is, String description) {
        NBTTagCompound nbt = getNBTTag(is);
        nbt.setString("description", description);
        return getItemByNBT(is, nbt);
    }

    public static ItemStack setAttribute(ItemStack is, Attribute attribute, double value) {
        NBTTagCompound nbt = getNBTTag(is);
        if(nbt.get("attributes") == null) nbt.set("attributes", new NBTTagCompound());
        NBTTagCompound attributes = (NBTTagCompound) nbt.get("attributes");
        if (attributes != null) attributes.setDouble(attribute.getAttributeName(), value);
        return getItemByNBT(is, nbt);
    }

    public static Double getAttributeDouble(ItemStack is, Attribute attribute) {
        NBTTagCompound nbt = getNBTTag(is);
        if(nbt.get("attributes") == null) nbt.set("attributes", new NBTTagCompound());
        NBTTagCompound attributes = (NBTTagCompound) nbt.get("attributes");
        if (attributes != null && attributes.hasKey(attribute.getAttributeName())) {
            return attributes.getDouble(attribute.getAttributeName());
        }
        return null;
    }

    public static ItemStack setExtra(ItemStack is, String key, String value) {
        NBTTagCompound nbt = getNBTTag(is);
        if(nbt.get("extra") == null) nbt.set("extra", new NBTTagCompound());
        NBTTagCompound extras = (NBTTagCompound) nbt.get("extra");
        if (extras != null) extras.setString(key, value);
        return getItemByNBT(is, nbt);
    }

    public static ItemStack setExtra(ItemStack is, String key, long value) {
        NBTTagCompound nbt = getNBTTag(is);
        if(nbt.get("extra") == null) nbt.set("extra", new NBTTagCompound());
        NBTTagCompound extras = (NBTTagCompound) nbt.get("extra");
        if (extras != null) extras.setLong(key, value);
        return getItemByNBT(is, nbt);
    }

    public static ItemStack setExtra(ItemStack is, String key, double value) {
        NBTTagCompound nbt = getNBTTag(is);
        if(nbt.get("extra") == null) nbt.set("extra", new NBTTagCompound());
        NBTTagCompound extras = (NBTTagCompound) nbt.get("extra");
        if (extras != null) extras.setDouble(key, value);
        return getItemByNBT(is, nbt);
    }

    public static String getExtraString(ItemStack is, String key) {
        NBTTagCompound nbt = getNBTTag(is);
        if(nbt.get("extra") == null) nbt.set("extra", new NBTTagCompound());
        NBTTagCompound extras = (NBTTagCompound) nbt.get("extra");
        if (extras != null && extras.hasKey(key)) {
            return extras.getString(key);
        }
        return null;
    }

    public static Long getExtraLong(ItemStack is, String key) {
        NBTTagCompound nbt = getNBTTag(is);
        if(nbt.get("extra") == null) nbt.set("extra", new NBTTagCompound());
        NBTTagCompound extras = (NBTTagCompound) nbt.get("extra");
        if (extras != null && extras.hasKey(key)) {
            return extras.getLong(key);
        }
        return null;
    }

    public static Double getExtraDouble(ItemStack is, String key) {
        NBTTagCompound nbt = getNBTTag(is);
        if(nbt.get("extra") == null) nbt.set("extra", new NBTTagCompound());
        NBTTagCompound extras = (NBTTagCompound) nbt.get("extra");
        if (extras != null && extras.hasKey(key)) {
            return extras.getDouble(key);
        }
        return null;
    }
}
