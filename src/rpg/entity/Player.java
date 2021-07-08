package rpg.entity;

import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.*;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.scoreboard.*;
import rpg.calculator.ExpCalculator;
import rpg.effect.Effect;
import rpg.effect.EffectType;
import rpg.gui.InventoryGUI;
import rpg.main.Main;
import rpg.move.MoveType;
import rpg.skill.Skill;
import rpg.skill.SkillAction;
import rpg.skill.SkillPreset;
import rpg.utils.*;
import rpg.vocation.Vocation;

import java.io.File;
import java.util.*;

public class Player extends Entity {

    private static final int listCol1 = 0x00ffdd, listCol2 = 0xcc77dd;
    private static final String listHeader = ColorUtil.addGradient("┌──────────────────┐", listCol1, listCol2);
    private static final String listFooter = ColorUtil.addGradient("└──────────────────┘", listCol1, listCol2);
    private static final HashMap<String, Player> playerMap = new HashMap<>();
    private static final char[] originalScoreboardTitle = "눈누난나".toCharArray();
    public static final int LIFE_BOSS_BAR = 0;
    public static final int SHIELD_BOSS_BAR = 1;
    public static final int MANA_BOSS_BAR = 2;
    public static final int ATTACK_BOSS_BAR = 3;
    public static final int EFFECT_BOSS_BAR = 4;
    private final Scoreboard scoreboard;
    private final BossBar[] bossBars = new BossBar[6];
    private final int[] bossBarVisibleTimes = new int[bossBars.length];
    private final int[] bossBarTasks = new int[bossBars.length];
    private InventoryGUI currentInventoryGUI;

    private long gold = 0;
    private MoveType moveType = MoveType.DEFAULT;
    private long latestSwapHand = 0;
    private long latestInteract = 0;
    private long latestDeath = 0;
    private Action lastInteractAction = null;

    private final List<Skill> skills = new ArrayList<>();
    private final Skill[] skillSlot = new Skill[4];

    private long playTime = 0;
    private String title = ChatColor.DARK_GRAY + "USER";
    private Vocation vocation = Vocation.ADVENTURER;
    private boolean isVisible = true;

    public static Collection<Player> getAllPlayers() {
        return playerMap.values();
    }

    public void saveData() {
        Map<String, Object> dataMap = new HashMap<>();
        Map<String, Integer> statMap = new HashMap<>();
        List<Map<String, Object>> skillList = new LinkedList<>();
        List<String> skillSlotList = new LinkedList<>();
        dataMap.put("level", level);
        dataMap.put("life", life);
        dataMap.put("mana", mana);
        dataMap.put("exp", exp);
        dataMap.put("maxExp", maxExp);
        dataMap.put("playTime", playTime);
        dataMap.put("vocation", vocation.name());
        dataMap.put("gold", gold);
        dataMap.put("moveType", moveType.name());
        statMap.put("strength", stat.strength);
        statMap.put("agility", stat.agility);
        statMap.put("vitality", stat.vitality);
        statMap.put("mentality", stat.mentality);
        statMap.put("sensibility", stat.sensibility);
        statMap.put("point", stat.point);
        dataMap.put("stat", statMap);
        for(Skill skill : skills) {
            Map<String, String> extras = new HashMap<>();
            Map<String, Object> skillMap = new HashMap<>();
            skillMap.put("name", skill.getName());
            skillMap.put("level", skill.getLevel());
            skillMap.put("expScale", skill.getExpScale());
            skillMap.put("cooldown", skill.getCooldown());
            skillMap.put("latest", skill.getLatest());
            for(String key : skill.getExtras().keySet()) {
                extras.put(key, skill.getExtra(key));
            }
            skillMap.put("extras", extras);
            skillList.add(skillMap);
        }
        for(Skill skill : skillSlot) skillSlotList.add(skill == null ? null : skill.getName());
        dataMap.put("skills", skillList);
        dataMap.put("skillSlot", skillSlotList);

        YamlUtil.saveYaml("players/" + uuid + ".yml", dataMap);
    }

    public boolean hasData() {
        Map<String, Object> dataMap = YamlUtil.readYaml("players/" + uuid + ".yml");
        return dataMap != null;
    }

    @SuppressWarnings("unchecked")
    public void loadData() {
        Map<String, Object> dataMap = YamlUtil.readYaml("players/" + uuid + ".yml");
        if(dataMap == null) return;
        try {
            level = Integer.parseInt(dataMap.getOrDefault("level", 1) + "");
            life = Double.parseDouble(dataMap.getOrDefault("life", life) + "");
            mana = Double.parseDouble(dataMap.getOrDefault("mana", mana) + "");
            playTime = Long.parseLong(dataMap.getOrDefault("playTime", 0) + "");
            maxExp = Long.parseLong(dataMap.getOrDefault("maxExp", DEFAULT_EXP) + "");
            exp = Long.parseLong(dataMap.getOrDefault("exp", 0) + "");
            vocation = Vocation.valueOf(dataMap.getOrDefault("vocation", Vocation.ADVENTURER.name()) + "");
            gold = Long.parseLong(dataMap.getOrDefault("gold", 0) + "");
            moveType = MoveType.valueOf(dataMap.getOrDefault("moveType", MoveType.DEFAULT.name()) + "");
            Object statObj = dataMap.getOrDefault("stat", new HashMap<>());
            Map<String, Integer> statMap = (Map<String, Integer>) statObj;
            stat.mentality = statMap.get("mentality");
            stat.agility = statMap.get("agility");
            stat.vitality = statMap.get("vitality");
            stat.strength = statMap.get("strength");
            stat.sensibility = statMap.get("sensibility");
            stat.point = statMap.get("point");
            Object skillListObj = dataMap.getOrDefault("skills", new LinkedList<Map<String, Object>>());
            List<Map<String, Object>> skillList = (List<Map<String, Object>>) skillListObj;
            for(Map<String, Object> skillMap : skillList) {
                Skill skill = Skill.createSkill(SkillPreset.getByName(skillMap.get("name") + ""));
                if(skill != null) {
                    skill.setLevel(Integer.parseInt(skillMap.getOrDefault("level", 1) + ""));
                    skill.setExpScale(Double.parseDouble(skillMap.getOrDefault("expScale", 0) + ""));
                    skill.setLatest(Long.parseLong(skillMap.getOrDefault("latest", 0) + ""));
                    skill.setCooldown(Double.parseDouble(skillMap.getOrDefault("cooldown", skill.getCooldown()) + ""));
                    Map<String, String> extras = (Map<String, String>) skillMap.getOrDefault("extras", new HashMap<>());
                    for(String key : extras.keySet()) {
                        skill.setExtra(key, extras.get(key));
                    }
                    learnSkill(skill);
                }
            }
            Object skillSlotListObj = dataMap.getOrDefault("skillSlot", new LinkedList<String>());
            List<String> skillSlotList = (List<String>) skillSlotListObj;
            int i = 0;
            for(String skillName : skillSlotList) {
                setSkillToSlot(skillName, i);
                i++;
            }
        } catch (Exception e) {
            saveData();
        }
    }

    public Player(org.bukkit.entity.Player player) {
        this(player.getUniqueId().toString());
    }

    public Player(String uuid) {
        this.uuid = uuid;
        this.scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();

        task = Bukkit.getScheduler().runTaskTimerAsynchronously(Main.getPlugin(), () -> {
            org.bukkit.entity.Player player = getMinecraftPlayer();
            if (player == null) return;
            if (!player.isOnline()) return;
            if (player.isDead()) return;
            ticks++;
            if (ticks % 20 == 0) playTime++;
            if (this.name == null) this.name = player.getName();

            updateBossBars();

            if (ticks % 6 == 0) {
                Objective obj = scoreboard.getObjective("statusGUI");
                if (obj != null) obj.unregister();
                registerScoreboardObj();
                for (org.bukkit.entity.Player p : Bukkit.getOnlinePlayers()) {
                    Player rp = Player.getPlayer(p);
                    if (rp == null) continue;
                    if (rp.isVisible())
                        player.showPlayer(Main.getPlugin(Main.class), rp.getMinecraftPlayer());
                    else
                        player.hidePlayer(Main.getPlugin(Main.class), rp.getMinecraftPlayer());

                    String uuidHash = rp.getUuid().hashCode() + "";
                    if (scoreboard.getTeam(uuidHash) == null) {
                        scoreboard.registerNewTeam(uuidHash);
                    }
                    Team privateTeam = scoreboard.getTeam(uuidHash);
                    if (privateTeam != null) {
                        List<String> remove = new LinkedList<>();
                        for (String s : privateTeam.getEntries()) {
                            if (!s.equals(rp.getMinecraftPlayer().getName())) remove.add(s);
                        }
                        for (String s : remove) privateTeam.removeEntry(s);
                        if (privateTeam.getEntries().size() == 0) {
                            privateTeam.addEntry(rp.getMinecraftPlayer().getName());
                        }
                        privateTeam.setColor(ChatColor.WHITE);
                        privateTeam.setPrefix(rp.getPrefix());
                    }
                }
            }
            update();
        }, 0, 1).getTaskId();

        playerMap.put(uuid, this);
    }

    public void resetPlayer() {
        Bukkit.getScheduler().cancelTask(task);
        playerMap.remove(uuid);
        new Player(uuid);
    }

    @Override
    public void update() {
        org.bukkit.entity.Player player = getMinecraftPlayer();
        if(player == null) return;
        if(player.isDead()) return;

        player.setPlayerListHeaderFooter(listHeader, listFooter);
        player.setArrowsInBody(0);
        player.setShieldBlockingDelay(0);

        if(ticks % 5 == 0) {
            player.setDisplayName(getDisplayName());
        }

        if(ticks % 20 == 0) {
            for(ItemStack is : player.getInventory().getContents()) {
                if(ItemUtil.isRpgItem(is)) ItemUtil.applyRpgItem(is);
            }
        }

        AttributeInstance maxHealthAtt = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        double maxHealth = maxHealthAtt == null ? 0 : maxHealthAtt.getBaseValue();

        long beforeLevel = level;
        int statGrow = 0;
        while(exp >= maxExp) {
            exp -= maxExp;
            level++;
            if(level % 10 == 0) {
                statGrow += 2;
                stat.vitality += 2;
                stat.sensibility += 2;
                stat.agility += 2;
                stat.mentality += 2;
                stat.strength += 2;
            }
            stat.point += 3;
            maxExp += ExpCalculator.getExpAddition(level);
        }
        player.setLevel(level);
        player.setExp((float) exp / maxExp);

        final String MINT = ColorUtil.fromRGB(0x00ff88);
        if(beforeLevel < level) { //level up
            player.sendTitle("",
                    ChatColor.YELLOW + "[ " +
                            MINT + "LEVEL UP! " + ChatColor.YELLOW + "]",
                    10, 30, 20);
            player.sendActionBar(ChatColor.YELLOW + "Lv." + beforeLevel +
                    ChatColor.RED + " → " + ChatColor.YELLOW + "Lv." + level);
            player.sendMessage(ChatColor.DARK_GRAY + "┌──────────┐");
            player.sendMessage(ChatColor.YELLOW + "   [ "+
                    MINT + "레벨업 하셨습니다! " + ChatColor.YELLOW + "]");
            player.sendMessage(ChatColor.YELLOW + "    Lv." + beforeLevel +
                    ChatColor.RED + " → " + ChatColor.YELLOW + "Lv." + level);
            player.sendMessage("    - " + ChatColor.GRAY + "스탯 포인트 " + ChatColor.RED + "+" + (3 * (level - beforeLevel)));
            if(statGrow > 0) {
                player.sendMessage("    - " + ChatColor.GRAY + "모든 스탯 " + ChatColor.RED + "+" + statGrow);
                player.playSound(getMinecraftPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 0.6f);
            }
            player.sendMessage(ChatColor.DARK_GRAY + "└──────────┘");
            player.playSound(getMinecraftPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        }

        updateAttributes();
        updateEffects();
        updateSkills();
        updateSkillSlot();
        if(vocation.getVocationEffect() != null) {
            vocation.getVocationEffect().run(this, ticks);
        }
        if(life <= maxLife * 0.2) {
            moveSpeed = Math.min(moveSpeed * 0.5, 100);
            if(life < maxLife * 0.05) {
                addEffect(new Effect(EffectType.BLINDNESS, 10, 1));
            }
        }

        life = Math.max(0, Math.min(maxLife, life));
        mana = Math.max(0, Math.min(maxMana, mana));

        player.setAbsorptionAmount(shieldAmount / maxLife * maxHealth);

        AttributeInstance moveSpeedAtt = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if(moveSpeedAtt != null) {
            double moveSpeed_ = moveSpeed;
            switch(moveType) {
                case HALF:
                    if(moveSpeed_ > 100)
                        moveSpeed_ = (moveSpeed_ - 100) * 0.5 + 100;
                    break;
                case SPRINT_ONLY:
                    if(!player.isSprinting() && moveSpeed_ > 100)
                        moveSpeed_ = 100;
                    break;
                case NEVER:
                    if(moveSpeed_ > 100)
                        moveSpeed_ = 100;
                    break;
                case BALANCE:
                    if(player.isSneaking()) {
                        moveSpeed_ = Math.min(moveSpeed_, 250);
                    }
                    else if(!player.isSprinting()) {
                        moveSpeed_ = Math.max(Math.min(Math.min(moveSpeed_, 200),
                                (moveSpeed_ - 100) * 0.2 + 100), moveSpeed_ > 150 ? 150 : moveSpeed_);
                    }
                default:
                    break;
            }
            moveSpeedAtt.setBaseValue(Math.min(0.6, 0.1 * moveSpeed_ / 100));
        }

        BossBar lifeBar = getBossBar(LIFE_BOSS_BAR);
        lifeBar.setProgress(life / maxLife);
        lifeBar.setColor(BarColor.GREEN);
        lifeBar.setTitle("생명력 " + ChatColor.GRAY + String.format("(%.1f/%.1f)", life, maxLife));
        showBossBar(LIFE_BOSS_BAR, 100, 0);

        BossBar manaBar = getBossBar(MANA_BOSS_BAR);
        manaBar.setProgress(mana / maxMana);
        manaBar.setColor(BarColor.BLUE);
        manaBar.setTitle("마나 " + ChatColor.GRAY + String.format("(%.1f/%.1f)", mana, maxMana));
        showBossBar(MANA_BOSS_BAR, 100, 0);

        if(!effects.isEmpty()) {
            BossBar effectBar = getBossBar(EFFECT_BOSS_BAR);
            Effect effect = effects.get((int) (ticks / 100 % effects.size()));
            for(Effect eff : effects) {
                if(eff.getDuration() < 3 && eff.getDuration() < effect.getDuration()) {
                    effect = eff;
                }
            }
            String hours = ((int)effect.getDuration()) / 3600 + "";
            String minutes = ((int)effect.getDuration()) / 60 % 60 + "";
            String seconds = ((int)effect.getDuration()) % 60 + "";
            if(hours.length() == 1) hours = "0" + hours;
            if(minutes.length() == 1) minutes = "0" + minutes;
            if(seconds.length() == 1) seconds = "0" + seconds;

            effectBar.setProgress(Math.max(0, Math.min(1, effect.getDuration() / effect.getMaxDuration())));
            effectBar.setColor(effect.getEffectType().isDebuff() ? BarColor.PURPLE : BarColor.PINK);
            effectBar.setTitle(ChatColor.WHITE + "Lv." + effect.getLevel() +
                    " " + effect.getEffectType().getName() +
                    ChatColor.GRAY + " [" + hours + ":" + minutes + ":" + seconds + "]");
            showBossBar(EFFECT_BOSS_BAR, 10, 0);
        }

        if(!player.isDead()) {
            if (life <= 0) {
                sync(() -> player.setHealth(0));
            } else player.setHealth(Math.max(1, maxHealth * life / maxLife));
        }
    }

    private void updateSkillSlot() {
        org.bukkit.entity.Player player = getMinecraftPlayer();
        if(player == null) return;
        Inventory inventory = player.getInventory();
        for(int i = 0; i < skillSlot.length; i++) {
            Skill skill = skillSlot[i];
            if(skill == null) {
                ItemStack skillItem = ItemUtil.setSkillItem(
                        ItemUtil.createItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "스킬 슬롯",
                                null, 1, false));
                inventory.setItem(i, skillItem);
                continue;
            }
            double cooldown = skill.getCooldown() - (new Date().getTime() - skill.getLatest()) / 1000.0;
            int spaceCnt = 21;
            for(char ch : skill.getName().toCharArray()) {
                spaceCnt -= ch < 128 ? 1 : 2;
            }
            if(spaceCnt < 3) spaceCnt = 3;
            StringBuilder space = new StringBuilder();
            for(int j = 0; j < spaceCnt; j++) space.append(' ');
            ItemStack skillItem = ItemUtil.setSkillItem(
                    ItemUtil.createItem(cooldown > 0 ? Material.BOOK :
                                    (skill.isActive() ? Material.WRITABLE_BOOK : Material.ENCHANTED_BOOK),
                            ChatColor.GRAY + " [" + ChatColor.GOLD + "Lv." + skill.getLevel() + ChatColor.GRAY + "] " +
                                    ChatColor.WHITE + skill.getName() + space +
                                    (skill.isPassiveSkill() ? ChatColor.AQUA + "PASSIVE" : ChatColor.RED + "ACTIVE"),
                            "  " + StringUtils.join(StringUtil.splitByLength(skill.getSkillAction()
                                    .getDescription(skill, this), 17), "\n  "),
                            Math.max(1, (int) Math.ceil(cooldown)), cooldown <= 0)
            );
            if(skill.getPreset().getSkillItemModifier() != null
                    && (!skill.isActive() || skill.isPassiveSkill())) {
                skillItem = skill.getPreset().getSkillItemModifier().modify(skillItem);
            }
            inventory.setItem(i, skillItem);
        }
    }

    private void updateSkills() {
        for(Skill skill : skills) {
            if(skill.isPassiveSkill()) {
                skill.setActive(true);
                skill.getSkillAction().onActive(skill, this);
            }
            else if(skill.isActive()) {
                skill.getSkillAction().onActive(skill, this);
                if(skill.getActiveTimeTicks() >= skill.getSkillAction().getFinishTimeTicks(skill, this)) {
                    skill.getSkillAction().onStop(skill, this);
                    skill.setLatest(new Date().getTime());
                    skill.setActive(false);
                    skill.setActiveTimeTicks(0);
                    continue;
                }
                skill.setActiveTimeTicks(skill.getActiveTimeTicks() + 1);
            }
        }
    }

    public void onRespawn() {
        for(int i = 0; i < 40; i++) {
            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                for(Effect effect : effects) removeEffect(effect.getEffectType());
                life = maxLife;
                mana = maxMana;
            }, i);
        }
    }

    public String getPrefix() {
        return ChatColor.GRAY + "[" + ChatColor.WHITE + title + ChatColor.GRAY + "]" +
                ChatColor.GRAY + "[" + ChatColor.WHITE + vocation.getName() + ChatColor.GRAY + "] " + ChatColor.WHITE;
    }

    @Override
    public String getDisplayName() {
        return getPrefix() + getName();
    }

    public void updateBossBars() {
        if(getMinecraftPlayer() == null) return;
        org.bukkit.entity.Player player = getMinecraftPlayer();
        for(int i = 0; i < bossBars.length; i++) {
            BossBar bar = bossBars[i];
            int time = bossBarVisibleTimes[i];

            if (bar == null) {
                bar = bossBars[i] = BossBarUtil.applyBossBar(i + "/rpg/" + uuid, "title",
                        BarColor.RED, BarStyle.SOLID);
                bar.setVisible(true);
            }
            if(ticks % 5 == 0)
                bar.removeAll();
            if(time > 0) {
                bar.addPlayer(player);
                bossBarVisibleTimes[i]--;
            }
        }
    }

    public void registerScoreboardObj() {
        org.bukkit.entity.Player player = getMinecraftPlayer();
        if(player == null) return;

        int seq = (int) ((ticks / 6) % (originalScoreboardTitle.length + 20));
        StringBuilder title = new StringBuilder(ColorUtil.fromRGB(0x8866ff));

        for(int i = 0; i < originalScoreboardTitle.length; i++) {
            if(i == seq) {
                title.append(ChatColor.MAGIC).append(originalScoreboardTitle[i] == ' ' ? ' ': "A");
            }
            else {
                if(i == seq + 1) {
                    title.append(ChatColor.RESET).append(ColorUtil.fromRGB(0x8866ff / 2));
                }
                title.append(originalScoreboardTitle[i]);
            }
        }

        title.append(ChatColor.RESET).append(ChatColor.GRAY)
                .append(" (").append(Bukkit.getOnlinePlayers().size()).append("명)");

        Objective obj = scoreboard.registerNewObjective("statusGUI", "dummy", title.toString(), RenderType.INTEGER);
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        String playTimeStr;
        if(playTime < 60) playTimeStr = playTime + "초";
        else if(playTime < 3600) playTimeStr = (playTime / 60) + "분";
        else playTimeStr = (playTime / 3600) + "시간";

        String[] data = {
                ChatColor.DARK_GRAY + "┌─── 기본 정보 ────",
                ChatColor.DARK_GRAY + "│ " + ChatColor.LIGHT_PURPLE + " 이름  " + ChatColor.WHITE + getName(),
                ChatColor.DARK_GRAY + "│ " + ChatColor.LIGHT_PURPLE + " 직업  " + ChatColor.WHITE + vocation.getName(),
                ChatColor.DARK_GRAY + "│ " + ChatColor.LIGHT_PURPLE + " 레벨  " + ChatColor.WHITE + level + "레벨" + ChatColor.GRAY + String.format(" (%.1f%%)", exp / (double)maxExp * 100.0),
                ChatColor.DARK_GRAY + "│ " + ChatColor.LIGHT_PURPLE + " 골드  " + ChatColor.WHITE + gold + "G",
                ChatColor.DARK_GRAY + "├─── 이동 정보 ────",
                ChatColor.DARK_GRAY + "│ " + ChatColor.DARK_PURPLE + " 이동모드  " + ChatColor.WHITE + moveType.getName(),
                ChatColor.DARK_GRAY + "├─── 부가 정보 ────",
                ChatColor.DARK_GRAY + "│ " + ChatColor.GRAY + " 지연  " + ChatColor.WHITE + PlayerUtil.getPlayerPing(player) + "ms",
                ChatColor.DARK_GRAY + "│ " + ChatColor.GRAY + " 플레이 시간  " + ChatColor.WHITE + playTimeStr,
                ChatColor.DARK_GRAY + "└────────────"
        };

        int size = data.length;
        for(int i = 0; i < size; i++) {
            obj.getScore(data[i]).setScore(size - i - 1);
        }

        if(player.getScoreboard() != scoreboard) {
            player.setScoreboard(scoreboard);
        }
    }

    public void openMenuGUI() {
        org.bukkit.entity.Player player = getMinecraftPlayer();
        if(player == null) return;
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
        InventoryGUI gui = new InventoryGUI(ChatColor.RED + "메뉴", 1);
        ItemStack playerHead = SkullHead.getPlayerHead(Bukkit.getOfflinePlayer(UUID.fromString(uuid)));
        if(playerHead.getItemMeta() != null) {
            ItemMeta meta = playerHead.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + "플레이어 정보창 열기");
            meta.setLore(Arrays.asList(
                    " ",
                    ChatColor.WHITE + "  정보창을 열어서 " + ChatColor.YELLOW + "능력치, 스탯,",
                    ChatColor.YELLOW + "  레벨 정보" + ChatColor.WHITE + " 등을 확인할 수 있습니다.",
                    " "));
            playerHead.setItemMeta(meta);
        }
        gui.setItem(1, 5, playerHead, event -> {
            if(event.isLeftClick()) {
                openStatusGUI();
            }
        });
        gui.openTo(player);
    }

    public void onHuntMonster(Monster monster) {
        org.bukkit.entity.Player player = getMinecraftPlayer();
        if(player == null) return;
        long gettingExp = ExpCalculator.getGettingExp(this, monster);
        player.sendActionBar(ColorUtil.fromRGB(0xcc33ff) + "[ " +
                ColorUtil.fromRGB(0x00ffcc) + monster.getName() +
                ColorUtil.fromRGB(0x00ab90) + "(을)를 처치하셨습니다!" + ColorUtil.fromRGB(0xcc33ff) + " ]");
        StringBuilder stick = new StringBuilder("──────────");
        int nameLength = ChatColor.stripColor(monster.getName()).length();
        for(double i = 0; i < nameLength; i += 1.2) stick.append("─");
        player.sendMessage(ChatColor.DARK_GRAY + "┌" + stick + "┐");
        player.sendMessage(ColorUtil.fromRGB(0xcc33ff) + "   [ " +
                ColorUtil.fromRGB(0x00ffcc) + monster.getName() +
                ColorUtil.fromRGB(0x00ab90) + "(을)를 처치하셨습니다!" + ColorUtil.fromRGB(0xcc33ff) + " ]");
        List<String> rewards = new LinkedList<>();
        rewards.add(gettingExp + " EXP");

        for(String reward : rewards) {
            player.sendMessage(ColorUtil.fromRGB(0xeeeeee) + "    - " + reward);
        }
        player.sendMessage(ChatColor.DARK_GRAY + "└" + stick + "┘");

        int delay = 0;
        if(exp + gettingExp >= maxExp) delay = 20;
        Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getPlugin(), () -> exp += gettingExp, delay);
    }

    public void openStatusGUI() {
        Bukkit.getScheduler().runTask(Main.getPlugin(), () -> {
            org.bukkit.entity.Player player = getMinecraftPlayer();
            if(player == null) return;
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            InventoryGUI gui = new InventoryGUI(ColorUtil.fromRGB(0xff4400) + "정보창", 6);
            ItemStack gray = ItemUtil.createItem(Material.GRAY_STAINED_GLASS_PANE,
                    " ", null, 1, false);
            ItemStack black = ItemUtil.createItem(Material.BLACK_STAINED_GLASS_PANE,
                    " ", null, 1, false);

            for(int i = 1; i <= 9; i++) {
                gui.setItem(1, i, gray.clone());
                gui.setItem(4, i, gray.clone());
                gui.setItem(6, i, gray.clone());
                gui.setItem(2, i, black.clone());
                gui.setItem(3, i, black.clone());
                gui.setItem(5, i, black.clone());
            }

            ItemStack nameInfo = ItemUtil.createItem(Material.NAME_TAG,
                    ColorUtil.fromRGB(0x00ff99) + "기본 정보",
                    " \n  " + ChatColor.AQUA + "이름  " + ChatColor.WHITE + player.getName() +
                            "\n  " + ChatColor.AQUA + "칭호  " + ChatColor.GRAY + "[" + ChatColor.WHITE + getTitle() + ChatColor.GRAY + "]" +
                    "\n  " + ChatColor.AQUA + "골드  " + ChatColor.WHITE + gold + "G",
                    1, true);
            gui.setItem(2, 3, nameInfo);

            ItemStack attributeInfo = ItemUtil.createItem(Material.NETHER_STAR,
                    ColorUtil.fromRGB(0xdd55ff) + "능력치 정보",
                    " \n  " + ChatColor.AQUA + rpg.attribute.Attribute.ATK.getDisplayName() +
                            "  " + ChatColor.WHITE + String.format("%.1f", atk) + rpg.attribute.Attribute.ATK.getSuffix() +
                            "\n  " + ChatColor.AQUA + rpg.attribute.Attribute.RANGE_ATK.getDisplayName() +
                            "  " + ChatColor.WHITE + String.format("%.1f", rangeAtk) + rpg.attribute.Attribute.RANGE_ATK.getSuffix() +
                            "\n  " + ChatColor.AQUA + rpg.attribute.Attribute.MAGIC_ATK.getDisplayName() +
                            "  " + ChatColor.WHITE + String.format("%.1f", magicAtk) + rpg.attribute.Attribute.MAGIC_ATK.getSuffix() +
                            "\n" + ColorUtil.fromRGB(0xff99ff) + "      -------------      \n  " +
                            ChatColor.AQUA + rpg.attribute.Attribute.DEFEND.getDisplayName() +
                            "  " + ChatColor.WHITE + String.format("%.1f", defend) + rpg.attribute.Attribute.DEFEND.getSuffix() +
                            "\n  " + ChatColor.AQUA + rpg.attribute.Attribute.RESISTANCE.getDisplayName() +
                            "  " + ChatColor.WHITE + String.format("%.1f", resistance) + rpg.attribute.Attribute.RESISTANCE.getSuffix() +
                            "\n  " + ChatColor.AQUA + rpg.attribute.Attribute.SHIELD_DEFEND.getDisplayName() +
                            "  " + ChatColor.WHITE + String.format("%.1f", shieldDefend) + rpg.attribute.Attribute.SHIELD_DEFEND.getSuffix() +
                            "\n" + ColorUtil.fromRGB(0xff99ff) + "      -------------      \n  " +
                            ChatColor.AQUA + rpg.attribute.Attribute.PENETRATE.getDisplayName() +
                            "  " + ChatColor.WHITE + String.format("%.1f", penetrate) + rpg.attribute.Attribute.PENETRATE.getSuffix() +
                            "\n  " + ChatColor.AQUA + rpg.attribute.Attribute.MOVE_SPEED.getDisplayName() +
                            "  " + ChatColor.WHITE + String.format("%.1f", moveSpeed) + rpg.attribute.Attribute.MOVE_SPEED.getSuffix() +
                            "\n  " + ChatColor.AQUA + rpg.attribute.Attribute.CRITICAL_CHANCE.getDisplayName() +
                            "  " + ChatColor.WHITE + String.format("%.1f", criticalChance) + rpg.attribute.Attribute.CRITICAL_CHANCE.getSuffix() +
                            "\n  " + ChatColor.AQUA + rpg.attribute.Attribute.CRITICAL_INCREASE.getDisplayName() +
                            "  " + ChatColor.WHITE + String.format("%.1f", criticalIncrease) + rpg.attribute.Attribute.CRITICAL_INCREASE.getSuffix() +
                            "\n  " + ChatColor.AQUA + rpg.attribute.Attribute.ATTACK_COOLDOWN.getDisplayName() +
                            "  " + ChatColor.WHITE + String.format("%.1f", attackCooldown) + rpg.attribute.Attribute.ATTACK_COOLDOWN.getSuffix(),
                    1, true);
            gui.setItem(2, 5, attributeInfo);

            ItemStack vocationInfo = ItemUtil.createItem(Material.DIAMOND_PICKAXE,
                    ColorUtil.fromRGB(0xffcc00) + "직업 정보",
                    " \n  " + ChatColor.AQUA + "직업  " + ChatColor.WHITE + getVocation().getName() +
                            "\n  " + ChatColor.AQUA + "직업 효과  " + ChatColor.WHITE +
                            StringUtils.join(StringUtil.splitByLength(getVocation().getDescription(), 15), "\n            "),
                    1, true);
            gui.setItem(2, 7, vocationInfo);

            ItemStack levelInfo = ItemUtil.createItem(Material.EXPERIENCE_BOTTLE,
                    ColorUtil.fromRGB(0xffee00) + "레벨 정보",
                    " \n  " + ChatColor.AQUA + "레벨  " + ChatColor.WHITE + level + "레벨" +
                            "\n  " + ChatColor.AQUA + "경험치  " +
                            ProgressBar.createBar(6, exp, maxExp, ColorUtil.fromRGB(0xffffdd), true),
                    1, true);
            gui.setItem(3, 4, levelInfo);

            StringBuilder effectLore = new StringBuilder(" ");
            for(Effect effect : effects) {
                String hours = ((int)effect.getDuration()) / 3600 + "";
                String minutes = ((int)effect.getDuration()) / 60 % 60 + "";
                String seconds = ((int)effect.getDuration()) % 60 + "";
                if(hours.length() == 1) hours = "0" + hours;
                if(minutes.length() == 1) minutes = "0" + minutes;
                if(seconds.length() == 1) seconds = "0" + seconds;
                effectLore.append("\n")
                        .append(effect.getEffectType().isDebuff() ? ColorUtil.fromRGB(0x990033) : ColorUtil.fromRGB(0x00ffff))
                        .append("┃ ").append(ChatColor.WHITE).append("Lv.").append(effect.getLevel())
                        .append(" ").append(effect.getEffectType().getName()).append(" ").append(ChatColor.GRAY).append("[")
                        .append(hours).append(":")
                        .append(minutes).append(":")
                        .append(seconds).append("]");
            }
            if(effects.isEmpty()) effectLore.append("\n").append(ChatColor.WHITE).append("없음");
            ItemStack effectInfo = ItemUtil.createItem(Material.SPLASH_POTION,
                    ColorUtil.fromRGB(0xff66ee) + "이상 효과", effectLore.toString(),
                    1, true);
            PotionMeta potionMeta = (PotionMeta) effectInfo.getItemMeta();
            potionMeta.setColor(Color.fromRGB(0xaa2299));
            effectInfo.setItemMeta(potionMeta);
            gui.setItem(3, 6, effectInfo);

            int[] statAmount = {stat.strength, stat.agility, stat.vitality, stat.sensibility, stat.mentality};
            String[] statName = {"근력", "민첩", "체력", "감각", "정신력"};
            Material[] statItemType = {Material.GOLDEN_AXE, Material.FEATHER,
                    Material.APPLE, Material.ENDER_EYE, Material.LIGHT_BLUE_DYE};

            for(int i = 0; i < 5; i++) {
                ItemStack statItem = ItemUtil.createItem(statItemType[i],
                        ChatColor.RED + "[스탯] " + ChatColor.WHITE + statName[i],
                        " \n  " + ColorUtil.fromRGB(0x00ff99) + statName[i] + "  " + ChatColor.WHITE + statAmount[i] +
                                "\n  " + ChatColor.GRAY + "스탯 포인트  " + ChatColor.WHITE + stat.point +
                                "\n\n  " + ColorUtil.fromRGB(0x777777) + "[좌클릭] 스탯 포인트 1 분배" +
                                "\n  " + ColorUtil.fromRGB(0x777777) + "[쉬프트+좌클릭] 스탯 포인트 10 분배",
                        1, true);
                final int finalI = i;
                gui.setItem(5, 3 + i, statItem, event -> {
                    if(event.isLeftClick()) {
                        int up = 1;
                        if(event.isShiftClick()) up = 10;
                        if(stat.point <= 0) return;
                        if(stat.point < up) up = stat.point;
                        stat.point -= up;
                        switch (finalI) {
                            case 0:
                                stat.strength += up;
                                break;
                                case 1:
                                stat.agility += up;
                                break;
                            case 2:
                                stat.vitality += up;
                                break;
                            case 3:
                                stat.sensibility += up;
                                break;
                            case 4:
                                stat.mentality += up;
                                break;
                        }
                        openStatusGUI();
                    }
                });
            }

            gui.openTo(player);
        });
    }

    public static void saveAllData() {
        for(Player player : playerMap.values()) {
            player.saveData();
        }
    }

    public static void loadAllData() {
        File file = new File(Main.getPlugin().getDataFolder(), "players");
        if(file.exists() && file.isDirectory()) {
            String[] uuidList = file.list();
            if(uuidList == null) return;
            for(String fileName : uuidList) {
                Player player = new Player(fileName.replace(".yml", ""));
                player.loadData();
            }
        }
    }

    public Skill getSkill(String name) {
        for(Skill skill : skills) {
            if(skill.getName().equals(name)) return skill;
        }
        return null;
    }

    public Skill getSkill(int index) {
        return skillSlot[index];
    }

    public void learnSkill(Skill skill) {
        if(getSkill(skill.getName()) != null) removeSkill(skill.getName());
        skills.add(skill);
    }

    public void setSkillToSlot(String skillName, int index) {
        Skill skill = getSkill(skillName);
        if(skill == null) return;
        skillSlot[index] = skill;
    }

    public void removeSkill(String name) {
        Skill find = null;
        for(Skill skill : skills) {
            if(skill.getName().equals(name)) find = skill;
        }
        if(find != null) {
            skills.remove(find);
            for(int i = 0; i < skillSlot.length; i++) {
                if(skillSlot[i] == find) skillSlot[i] = null;
            }
        }
    }

    public void removeSkillInSlot(int index) {
        skillSlot[index] = null;
    }

    @Override
    public void setShield(double shield, double duration) {
        super.setShield(shield, duration);
        
        BossBar bar = getBossBar(SHIELD_BOSS_BAR);
        bar.setTitle("방어막 " + ChatColor.GRAY + String.format("(%.1f/%.1f  %.1f초)", shield, shield, duration));
        bar.setColor(BarColor.WHITE);
        bar.setProgress(1);
        int[] i = {0};
        i[0] = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), () -> {
            double progress = (getShieldAmount() / shield) * (getShieldDuration() / duration);
            if(i[0] != getBossBarTaskId(SHIELD_BOSS_BAR) || progress <= 0) {
                if(progress <= 0) hideBossBar(SHIELD_BOSS_BAR);
                Bukkit.getScheduler().cancelTask(i[0]);
                return;
            }
            bar.setTitle("방어막 " + ChatColor.GRAY + String.format("(%.1f/%.1f  %.1f초)", getShieldAmount(), shield, getShieldDuration()));
            bar.setProgress(progress);
        }, 0, 1).getTaskId();
        showBossBar(SHIELD_BOSS_BAR, (int) (duration * 20), i[0]);
    }

    public org.bukkit.entity.Player getMinecraftPlayer() {
        return Bukkit.getPlayer(UUID.fromString(uuid));
    }

    public BossBar getBossBar(int index) {
        return bossBars[index];
    }

    public void showBossBar(int index, int ticks, int task) {
        bossBarVisibleTimes[index] = ticks;
        bossBarTasks[index] = task;
    }

    public void hideBossBar(int index) {
        bossBarVisibleTimes[index] = 0;
    }

    public int getBossBarTaskId(int index) {
        return bossBarTasks[index];
    }

    public int getBossBarVisibleTime(int index) {
        return bossBarVisibleTimes[index];
    }

    public Vocation getVocation() {
        return vocation;
    }

    public void setVocation(Vocation vocation) {
        this.vocation = vocation;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public static Player getPlayer(String uuid) {
        return playerMap.get(uuid);
    }

    public static Player getPlayer(org.bukkit.entity.Player p) {
        return getPlayer(p.getUniqueId().toString());
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public long getLatestSwapHand() {
        return latestSwapHand;
    }

    public void setLatestSwapHand(long latestSwapHand) {
        this.latestSwapHand = latestSwapHand;
    }

    public long getLatestInteract() {
        return latestInteract;
    }

    public void setLatestInteract(long latestInteract) {
        this.latestInteract = latestInteract;
    }

    public long getGold() {
        return gold;
    }

    public void setGold(long gold) {
        this.gold = gold;
    }

    public InventoryGUI getCurrentInventoryGUI() {
        return currentInventoryGUI;
    }

    public void setCurrentInventoryGUI(InventoryGUI currentInventoryGUI) {
        this.currentInventoryGUI = currentInventoryGUI;
    }

    public Action getLastInteractAction() {
        return lastInteractAction;
    }

    public void setLastInteractAction(Action lastInteractAction) {
        this.lastInteractAction = lastInteractAction;
    }

    public MoveType getMoveType() {
        return moveType;
    }

    public void setMoveType(MoveType moveType) {
        this.moveType = moveType;
    }

    public List<Skill> getSkills() {
        return skills;
    }

    public Skill[] getSkillSlot() {
        return skillSlot;
    }

    public void useSkill(String skillName) {
        Skill skill = getSkill(skillName);
        if(skill == null) return;
        org.bukkit.entity.Player player = getMinecraftPlayer();
        if(player == null) return;
        SkillAction action = skill.getSkillAction();
        if(skill.isActive()) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1f, 0f);
            player.sendActionBar(ChatColor.RED + "스킬이 이미 사용중입니다.");
        }
        else if(new Date().getTime() - skill.getLatest() < skill.getCooldown() * 1000) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1f, 0f);
            player.sendActionBar(ChatColor.RED + "스킬 재사용 대기시간이 지나지 않았습니다.");
        }
        else if(!action.canPayCost(skill, this)) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1f, 0f);
            player.sendActionBar(ChatColor.RED + action.getCannotPayCostReason(skill, this));
        }
        else {
            action.payCost(skill, this);
            skill.setActive(true);
            skill.setActiveTimeTicks(0);
            action.onStart(skill, this);
        }
    }

    public void showAttackBossbar(Entity victim, double damage, boolean isCritical) {
        BossBar bar = getBossBar(Player.ATTACK_BOSS_BAR);
        if(bar == null) return;
        int[] i = {0, 0};
        final boolean isShield = victim.getShieldAmount() > 0 && victim.getShieldDuration() > 0;
        final boolean isZeroLife = victim.getLife() <= 0;
        final int ticks = isZeroLife ? 20 : 200;
        i[0] = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), () -> {
            if(!(i[1] < ticks) || i[0] != getBossBarTaskId(Player.ATTACK_BOSS_BAR)) {
                Bukkit.getScheduler().cancelTask(i[0]);
                return;
            }

            double hp = isZeroLife ? 0 : Math.max(0, victim.getLife()), maxHp = victim.getMaxLife();
            bar.setTitle(String.format(
                    "%s%s " + ChatColor.GRAY + "%.1f / %.1f " + ChatColor.RED + "(-%.1f)",
                    isCritical ? "" + ChatColor.RED + ChatColor.BOLD + "크리티컬! " + ChatColor.RESET : "",
                    victim.getName(), hp, maxHp, damage));
            bar.setColor(isShield ? BarColor.WHITE : BarColor.RED);
            bar.setProgress(Math.max(0, Math.min(1, hp / maxHp)));

            i[1]++;
        }, 0, 1).getTaskId();
        showBossBar(Player.ATTACK_BOSS_BAR, ticks, i[0]);
    }

    public long getLatestDeath() {
        return latestDeath;
    }

    public void setLatestDeath(long latestDeath) {
        this.latestDeath = latestDeath;
    }
}
