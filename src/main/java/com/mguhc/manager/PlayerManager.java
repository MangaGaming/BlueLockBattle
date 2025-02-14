package com.mguhc.manager;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerManager {

    private final List<Player> players = new ArrayList<>();
    private final Map<Player, Integer> killMap = new HashMap<>();

    public void addPlayer(Player p) {
        players.add(p);
    }

    public void removePlayer(Player p) {
        players.remove(p);
    }

    public boolean isInPlayers(Player p) {
        return players.contains(p);
    }

    public List<Player> getPlayers() {
        return players;
    }

    public int getKills(Player player) {
        return killMap.getOrDefault(player, 0);
    }

    public void addKill(Player player) {
        killMap.put(player, killMap.getOrDefault(player, 0) + 1);
    }
}