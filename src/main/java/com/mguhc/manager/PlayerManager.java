package com.mguhc.manager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class PlayerManager {

    private final List<UUID> players = new ArrayList<>();
    private final Map<UUID, Integer> killMap = new HashMap<>();

    public void addPlayer(Player p) {
        players.add(p.getUniqueId());
    }

    public void removePlayer(Player p) {
        players.remove(p.getUniqueId());
    }

    public boolean isInPlayers(Player p) {
        return players.contains(p.getUniqueId());
    }

    public List<Player> getPlayers() {
        List<Player> ps = new ArrayList<>();
        for (UUID uuid : players) {
            ps.add(Bukkit.getPlayer(uuid));
        }
        return ps;
    }

    public int getKills(Player player) {
        return killMap.getOrDefault(player.getUniqueId(), 0);
    }

    public void addKill(Player player) {
        killMap.put(player.getUniqueId(), killMap.getOrDefault(player.getUniqueId(), 0) + 1);
    }
}