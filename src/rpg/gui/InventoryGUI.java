package rpg.gui;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InventoryGUI {
    private final String title;
    private final Inventory inventory;
    private final HashMap<Integer, InventoryGUIClickEvent> clickEvents = new HashMap<>();
    private final InventoryGUICloseEvent closeEvent;

    public InventoryGUI(String title, int lines) {
        this(title, lines, null);
    }
    public InventoryGUI(String title, int lines, InventoryGUICloseEvent closeEvent) {
        this.title = title;
        this.inventory = Bukkit.createInventory(null, lines * 9, title);
        this.closeEvent = closeEvent;
    }

    public void setItem(int height, int width, ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if(meta != null) {
            for(ItemFlag flag : ItemFlag.values()) {
                meta.addItemFlags(flag);
            }
            itemStack.setItemMeta(meta);
        }
        inventory.setItem((height - 1) * 9 + (width - 1), itemStack);
    }

    public void setItem(int height, int width, ItemStack itemStack, InventoryGUIClickEvent event) {
        setItem(height, width, itemStack);
        setClickEvent(height, width, event);
    }

    public void openTo(Player player) {
        player.openInventory(inventory);
        rpg.entity.Player rpgPlayer = rpg.entity.Player.getPlayer(player);
        if(rpgPlayer != null) rpgPlayer.setCurrentInventoryGUI(this);
    }

    public ItemStack getItem(int height, int width) {
        return inventory.getItem((height - 1) * 9 + (width - 1));
    }

    public void setClickEvent(int height, int width, InventoryGUIClickEvent event) {
        clickEvents.put((height - 1) * 9 + (width - 1), event);
    }

    public InventoryGUIClickEvent getClickEvent(int slotId) {
        return clickEvents.get(slotId);
    }

    public InventoryGUIClickEvent getClickEvent(int height, int width) {
        return getClickEvent((height - 1) * 9 + (width - 1));
    }

    public String getTitle() {
        return title;
    }

    public boolean equalsTitle(String title) {
        return ChatColor.stripColor(this.title).equals(ChatColor.stripColor(title));
    }

    public Inventory getInventory() {
        return inventory;
    }

    public InventoryGUICloseEvent getCloseEvent() {
        return closeEvent;
    }
}
