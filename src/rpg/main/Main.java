package rpg.main;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import rpg.entity.Monster;
import rpg.entity.MonsterSpawner;
import rpg.projectile.ProjectileManager;

import java.util.List;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        initEvents();
        initGameRules();
        rpg.entity.Player.loadAllData();
        MonsterSpawner.loadAllData();
        ProjectileManager.registerManager();
        Bukkit.getScheduler().runTaskTimerAsynchronously(
                this, () -> {
                    rpg.entity.Player.saveAllData();
                    Runtime.getRuntime().gc();
                }, 20 * 60 * 5, 20 * 60 * 5);
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for(Player player : Bukkit.getOnlinePlayers()) {
                if(rpg.entity.Player.getPlayer(player) == null) {
                    rpg.entity.Player rpgPlayer = new rpg.entity.Player(player);

                    player.sendMessage();
                }
            }
            for(World world : Bukkit.getWorlds()) {
                for(LivingEntity livingEntity : world.getLivingEntities()) {
                    if(Monster.getMonster(livingEntity) == null
                            && livingEntity instanceof Mob) {
                        AttributeInstance maxHealthAtt = livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                        double maxHealth = maxHealthAtt == null ? 0 : maxHealthAtt.getBaseValue();
                        Monster monster = new Monster((Mob) livingEntity);
                        int minLevel = 1 + (int)(maxHealth / 3.5);
                        double levelRange = 2 + maxHealth / 10.5;
                        int level = minLevel + (int)(Math.random() * levelRange);
                        monster.setLevel(level);
                        monster.stat.strength += level / 2;
                        monster.stat.vitality += (level + 1) / 2;
                        monster.setLife(Double.MAX_VALUE);
                    }
                }
            }
        }, 0, 1);
    }

    @Override
    public void onDisable() {
        rpg.entity.Player.saveAllData();
        MonsterSpawner.saveAllData();
    }

    public void initGameRules() {
        for(World world : Bukkit.getWorlds()) {
            world.setGameRule(GameRule.DO_MOB_LOOT, false);
            world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
            world.setGameRule(GameRule.MOB_GRIEFING, false);
            world.setGameRule(GameRule.DO_ENTITY_DROPS, false);
            world.setGameRule(GameRule.COMMAND_BLOCK_OUTPUT, false);
            world.setGameRule(GameRule.FALL_DAMAGE, true);
            world.setGameRule(GameRule.DO_FIRE_TICK, false);
            world.setGameRule(GameRule.NATURAL_REGENERATION, false);
            world.setGameRule(GameRule.KEEP_INVENTORY, true);
            world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false);
            world.setGameRule(GameRule.SPAWN_RADIUS, 0);
            world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
        }
    }

    public void initEvents() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new Events(), this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return Commands.onCommand(sender, command, label, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        return Commands.onTabComplete(sender, command, alias, args);
    }

    public static Plugin getPlugin() {
        return Main.getPlugin(Main.class);
    }
}
