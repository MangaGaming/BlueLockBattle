package com.mguhc.manager;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PlayerManager {

    private List<Player> players = new ArrayList<>();

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
}