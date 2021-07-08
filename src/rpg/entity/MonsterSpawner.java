package rpg.entity;

import org.bukkit.*;
import org.bukkit.entity.Mob;
import rpg.main.Main;
import rpg.utils.ParticleUtil;
import rpg.utils.YamlUtil;

import java.io.File;
import java.util.*;

public class MonsterSpawner {

    private final String name;
    private Location spawnLocation;
    private MonsterType monsterType;
    private String entityUUID;
    private long latestDead = 0;
    private double respawnTime = 20;
    private boolean findSpawn = false;
    private int findSpawnTime = 0;
    private final int taskId;
    private static final Map<String, MonsterSpawner> spawners = new HashMap<>();

    private MonsterSpawner(String name, MonsterType type, Location location) {
        this.name = name;
        this.monsterType = type;
        this.spawnLocation = location;
        this.taskId = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), () -> {
            World world = location.getWorld();
            if(world == null) return;
            for(int xx = -1; xx <= 1; xx++) {
                for(int zz = -1; zz <= 1; zz++) {
                    Chunk chunk = world.getChunkAt(location.clone().add(xx * 16, 0, zz * 16));
                    if(!chunk.isLoaded()) chunk.load();
                }
            }
            if((entityUUID == null || world.getEntity(UUID.fromString(entityUUID)) == null)
                    && new Date().getTime() - latestDead >= respawnTime * 1000) {
                Monster monster = Monster.spawnMonster(location, monsterType);
                ParticleUtil.createParticle(location, Particle.CLOUD, 10,
                        monster.getMinecraftMob().getWidth() * 0.5, monster.getMinecraftMob().getHeight(), monster.getMinecraftMob().getWidth() * 0.5, 0.01);
                entityUUID = monster.getUuid();
            }
            if(entityUUID != null && Monster.getMonster(entityUUID) != null) {
                Monster monster = Monster.getMonster(entityUUID);
                Mob mob = monster.getMinecraftMob();
                if(mob != null) {
                    if(mob.getLocation().distance(spawnLocation) > monster.getFollowRange()) findSpawn = true;
                    else if(mob.getLocation().distance(spawnLocation) > 10 && mob.getTarget() == null) findSpawn = true;
                    if(findSpawn) {
                        findSpawnTime++;
                        monster.setLastDamager(null);
                        mob.setTarget(null);
                        mob.getPathfinder().moveTo(spawnLocation);
                        if(mob.getLocation().distance(spawnLocation) < 5) {
                            findSpawn = false;
                        }
                        else if(findSpawnTime > 20 * 30) {
                            mob.teleport(spawnLocation);
                        }
                        else if(mob.getLocation().distance(spawnLocation) > monster.getFollowRange() && findSpawnTime > 20 * 10) {
                            mob.teleport(spawnLocation);
                        }
                    }
                    else if(findSpawnTime > 0) findSpawnTime = 0;
                }
            }
        }, 10, 10).getTaskId();
    }

    public static void saveAllData() {
        for(MonsterSpawner spawner : getMonsterSpawners()) {
            spawner.saveData();
        }
    }

    public void saveData() {
        Map<String, Object> dataMap = new HashMap<>();
        Map<String, Object> spawnLocationMap = new HashMap<>();
        dataMap.put("name", this.name);
        dataMap.put("entityUUID", this.entityUUID);
        dataMap.put("findSpawn", this.findSpawn);
        dataMap.put("findSpawnTime", this.findSpawnTime);
        dataMap.put("monsterType", this.monsterType.name());
        dataMap.put("latestDead", this.latestDead);
        dataMap.put("respawnTime", this.respawnTime);
        spawnLocationMap.put("world", spawnLocation.getWorld().getName());
        spawnLocationMap.put("x", spawnLocation.getX());
        spawnLocationMap.put("y", spawnLocation.getY());
        spawnLocationMap.put("z", spawnLocation.getZ());
        dataMap.put("spawnLocation", spawnLocationMap);
        YamlUtil.saveYaml("monster-spawners/" + name + ".yml", dataMap);
    }

    @SuppressWarnings("unchecked")
    public static void loadAllData() {
        File file = new File(Main.getPlugin().getDataFolder(), "monster-spawners");
        if(file.exists() && file.isDirectory()) {
            String[] list = file.list();
            if(list == null) return;
            for(String fileName : list) {
                Map<String, Object> dataMap = YamlUtil.readYaml("monster-spawners/" + fileName);
                if(dataMap == null) return;
                try {
                    String name = dataMap.get("name").toString();
                    MonsterType type = MonsterType.valueOf(dataMap.get("monsterType").toString());
                    HashMap<String, Object> spawnLocationMap = (HashMap<String, Object>) dataMap.get("spawnLocation");
                    Location spawnLocation = new Location(
                            Bukkit.getWorld(spawnLocationMap.get("world").toString()),
                            (Double) spawnLocationMap.get("x"),
                            (Double) spawnLocationMap.get("y"),
                            (Double) spawnLocationMap.get("z")
                    );
                    addMonsterSpawner(name, type, spawnLocation);
                    MonsterSpawner spawner = spawners.get(name);
                    spawner.entityUUID = dataMap.get("entityUUID").toString();
                    spawner.findSpawn= (Boolean) dataMap.get("findSpawn");
                    spawner.findSpawnTime = (Integer) dataMap.get("findSpawnTime");
                    spawner.latestDead = Long.parseLong(dataMap.get("latestDead").toString());
                    spawner.respawnTime = (Double) dataMap.get("respawnTime");
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void remove() {
        spawners.remove(name);
        Bukkit.getScheduler().cancelTask(taskId);
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public void setSpawnLocation(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
    }

    public MonsterType getMonsterType() {
        return monsterType;
    }

    public void setMonsterType(MonsterType monsterType) {
        this.monsterType = monsterType;
    }

    public String getName() {
        return name;
    }

    public String getEntityUUID() {
        return entityUUID;
    }

    public long getLatestDead() {
        return latestDead;
    }

    public void setLatestDead(long latestDead) {
        this.latestDead = latestDead;
    }

    public double getRespawnTime() {
        return respawnTime;
    }

    public void setRespawnTime(double respawnTime) {
        this.respawnTime = respawnTime;
    }

    public static Collection<MonsterSpawner> getMonsterSpawners() {
        return spawners.values();
    }

    public static boolean addMonsterSpawner(String name, MonsterType type, Location location) {
        if(name.matches("[\\\\|:*<>/]")) return false;
        if(spawners.get(name) != null) return false;
        MonsterSpawner spawner = new MonsterSpawner(name, type, location);
        spawners.put(name, spawner);
        return true;
    }

    public static boolean addMonsterSpawner(MonsterType type, Location location) {
        int i = 0;
        while(!addMonsterSpawner(type.name() + " (" + i + ")", type, location)) {
            i++;
        }
        return true;
    }

    public boolean isFindSpawn() {
        return findSpawn;
    }

    public interface Spawner {
        void initMonster(Monster monster);
    }
}
