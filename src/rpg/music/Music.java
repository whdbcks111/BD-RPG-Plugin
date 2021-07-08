package rpg.music;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import rpg.main.Main;

import java.util.*;

public class Music {

    private final List<Part> parts = new LinkedList<>();
    private final List<Part> currents = new LinkedList<>();
    private final Set<String> listeners = new LinkedHashSet<>();
    private boolean playing = false;
    private int totalPlayTimeTicks = 0;
    private double wait = 0;
    private int task = 0;
    private double speed = 1;

    public Music(String... list) {
        try {
            for (String s : list) {
                String[] spl = s.split(" ");
                if (spl.length != 5) continue;
                String soundName = spl[0].toUpperCase();
                if(soundName.startsWith("#")) soundName = soundName.substring(1);
                else soundName = "BLOCK_NOTE_BLOCK_" + soundName;
                String pitchName = spl[1];
                int octave = Integer.parseInt(spl[2]);
                float volume = (float) (Double.parseDouble(spl[3]) / 100.0);
                int time = (int)(Double.parseDouble(spl[4]) * 20.0);
                Pitch pitch = null;
                for(Pitch p : Pitch.values()) {
                    if(p.getName().equals(pitchName) && p.getOctave() == octave) {
                        pitch = p;
                        break;
                    }
                }
                if(pitch == null) return;

                Sound sound = Sound.valueOf(soundName);
                addPart(new Part(sound, pitch, time, volume));
            }
        } catch (IllegalArgumentException ignored) {}
    }

    public void addListener(Player player) {
        listeners.add(player.getUniqueId().toString());
    }

    public void addPart(Part part) {
        parts.add(part);
    }

    public Set<String> getListeners() {
        return listeners;
    }

    public List<Part> getParts() {
        return parts;
    }

    public void playMusic() {
        playing = true;
        totalPlayTimeTicks = 0;
        wait = 0;
        currents.clear();
        currents.addAll(parts);
        task = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), () -> {
            if(currents.isEmpty() || !playing) {
                Bukkit.getScheduler().cancelTask(task);
                return;
            }
            if(wait <= 0) {
                Part part = currents.remove(0);
                double time = part.getTimeTicks() / speed - 1;
                for(String uuid : listeners) {
                    Player player = Bukkit.getPlayer(UUID.fromString(uuid));
                    if(player != null) {
                        player.playSound(player.getLocation(), part.getSound(), part.getVolume(), part.getPitch().getPitch());
                    }
                }
                while(time < 0 && !currents.isEmpty()) {
                    part = currents.remove(0);
                    time = part.getTimeTicks() / speed - 1;
                    for(String uuid : listeners) {
                        Player player = Bukkit.getPlayer(UUID.fromString(uuid));
                        if(player != null) {
                            player.playSound(player.getLocation(), part.getSound(), part.getVolume(), part.getPitch().getPitch());
                        }
                    }
                }
                wait += time;
            }
            else wait--;
            totalPlayTimeTicks++;
        }, 0, 1).getTaskId();
    }

    public void stopMusic() {
        playing = false;
    }

    public boolean isPlaying() {
        return playing;
    }

    public int getTotalPlayTimeTicks() {
        return totalPlayTimeTicks;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public static class Part {
        private final Sound sound;
        private final Pitch pitch;
        private final int timeTicks;
        private final float volume;

        public Part(Sound sound, Pitch pitch, int timeTicks, float volume) {
            this.sound = sound;
            this.pitch = pitch;
            this.timeTicks = timeTicks;
            this.volume = volume;
        }

        public Sound getSound() {
            return sound;
        }

        public Pitch getPitch() {
            return pitch;
        }

        public int getTimeTicks() {
            return timeTicks;
        }

        public float getVolume() {
            return volume;
        }
    }
}
