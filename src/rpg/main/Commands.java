package rpg.main;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_16_R2.NBTBase;
import net.minecraft.server.v1_16_R2.NBTTagCompound;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.inventory.ItemStack;
import rpg.effect.Effect;
import rpg.effect.EffectType;
import rpg.entity.MonsterSpawner;
import rpg.entity.MonsterType;
import rpg.music.Music;
import rpg.skill.Skill;
import rpg.skill.SkillPreset;
import rpg.utils.EntityUtil;
import rpg.utils.FileStream;
import rpg.utils.SkullHead;
import rpg.vocation.Vocation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

public class Commands {

    public static boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if(!(sender instanceof Player)) return false;
        Player player = (Player) sender;
        rpg.entity.Player rpgPlayer = rpg.entity.Player.getPlayer(player);
        if(label.equals("rpg")) {
            if(args.length == 0) {

            }
            else {
                if(args.length == 2 && args[0].equals("exp") && args[1].matches("^[0-9]+$")
                        && player.isOp() && rpgPlayer != null) {
                    long exp = Long.parseLong(args[1]);
                    rpgPlayer.setExp(rpgPlayer.getExp() + exp);
                }

                if(args.length == 2 && args[0].equals("vocation") && rpgPlayer != null) {
                    try {
                        rpgPlayer.setVocation(Vocation.valueOf(args[1]));
                        player.sendMessage(rpgPlayer.getVocation().getName());
                    }
                    catch (IllegalArgumentException ignored) {}
                }

                if(args.length == 1 && args[0].equals("reset") && rpgPlayer != null) {
                    rpgPlayer.resetPlayer();
                    player.sendMessage("reset.");
                }

                if(args.length == 2 && args[0].equals("spawn") && rpgPlayer != null) {
                    try {
                        rpg.entity.Monster.spawnMonster(player.getLocation(), MonsterType.valueOf(args[1]));
                    }
                    catch (IllegalArgumentException ignored) {}
                }

                if(args.length == 2 && args[0].equals("spawner") && rpgPlayer != null) {
                    try {
                        MonsterType type = MonsterType.valueOf(args[1]);
                        MonsterSpawner.addMonsterSpawner(type, player.getLocation());
                    }
                    catch (IllegalArgumentException ignored) {}
                }

                if(args.length == 1 && args[0].equals("heal") && rpgPlayer != null && player.isOp()) {
                    rpgPlayer.setLife(rpgPlayer.getMaxLife());
                    rpgPlayer.setMana(rpgPlayer.getMaxMana());
                }

                if(args.length == 1 && args[0].equals("reset-all") && rpgPlayer != null && player.isOp()) {
                    List<rpg.entity.Player> resets = new LinkedList<>(rpg.entity.Player.getAllPlayers());
                    for(rpg.entity.Player rp : resets) rp.resetPlayer();
                    player.sendMessage("reset all players.");
                }

                if(args.length == 2 && args[0].equals("skill") && args[1].equals("all") && rpgPlayer != null) {
                    for(SkillPreset preset : SkillPreset.values()) {
                        Skill skill = Skill.createSkill(preset);
                        rpgPlayer.learnSkill(skill);
                        if(!skill.isPassiveSkill())
                            rpgPlayer.setSkillToSlot(skill.getName(), (int) (Math.random() * 4));
                        player.sendMessage(skill.getName());
                    }
                }

                else if(args.length == 2 && args[0].equals("skill") && rpgPlayer != null) {
                    try {
                        Skill skill = Skill.createSkill(SkillPreset.valueOf(args[1]));
                        rpgPlayer.learnSkill(skill);
                        player.sendMessage(skill.getName());
                    }
                    catch (IllegalArgumentException ignored) {}
                }

                if(args.length == 4 && args[0].equals("effect") && rpgPlayer != null
                        && args[2].matches("^[0-9]+$") && args[3].matches("^[0-9]+(\\.[0-9]+)?$")) {
                    try {
                        EffectType effectType = EffectType.valueOf(args[1]);
                        Effect effect = new Effect(effectType, Double.parseDouble(args[3]), Integer.parseInt(args[2]));
                        rpgPlayer.addEffect(effect);
                    }
                    catch (IllegalArgumentException ignored) {}
                }

                if(args.length == 1 && args[0].equals("save")) {
                    rpgPlayer.saveData();
                    player.sendMessage("data saved");
                }

                if(args.length == 1 && args[0].equals("load")) {
                    rpgPlayer.loadData();
                    player.sendMessage("data loaded");
                }
            }
        }
        return false;
    }

    public static List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String alias, String[] args) {

        return new ArrayList<>();
    }

}
