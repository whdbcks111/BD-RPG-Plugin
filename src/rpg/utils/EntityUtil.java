package rpg.utils;

import net.minecraft.server.v1_16_R2.EntityInsentient;
import net.minecraft.server.v1_16_R2.NBTTagCompound;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class EntityUtil {

    public static NBTTagCompound getEntityNBT(Entity entity) {
        net.minecraft.server.v1_16_R2.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        return nmsEntity.save(new NBTTagCompound());
    }

    public static void setEntityNBT(Entity entity, NBTTagCompound nbt) {
        net.minecraft.server.v1_16_R2.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        nmsEntity.load(nbt);
    }

}
