package rpg.vocation;

import rpg.entity.Player;

public interface VocationEffect {
    void run(Player player, long ticks);
}
