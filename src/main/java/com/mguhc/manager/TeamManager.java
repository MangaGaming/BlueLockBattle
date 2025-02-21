package com.mguhc.manager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TeamManager {
    // Map pour stocker les joueurs et leurs équipes
    private final HashMap<UUID, TeamEnum> playerTeamMap = new HashMap<>();
    // Map pour stocker les équipes et les joueurs dans chaque équipe
    private final HashMap<TeamEnum, Set<UUID>> teamPlayerMap = new HashMap<>();

    // Méthode pour ajouter un joueur à une équipe
    public void addPlayer(Player player, TeamEnum team) {
        // Vérifier si l'équipe existe, sinon la créer
        teamPlayerMap.putIfAbsent(team, new HashSet<>());
        teamPlayerMap.get(team).add(player.getUniqueId());
        playerTeamMap.put(player.getUniqueId(), team);
    }

    // Méthode pour retirer un joueur d'une équipe
    public void removePlayer(Player player) {
        TeamEnum team = playerTeamMap.get(player.getUniqueId());
        if (team != null && teamPlayerMap.containsKey(team)) {
            teamPlayerMap.get(team).remove(player.getUniqueId());
            playerTeamMap.remove(player.getUniqueId());
        }
    }

    // Méthode pour obtenir tous les joueurs dans une équipe
    public Set<Player> getPlayersInTeam(TeamEnum team) {
        Set<Player> playerSet = new HashSet<>();
        Set<UUID> uuids = teamPlayerMap.get(team);
        if (uuids == null) {
            return null;
        }
        for (UUID uuid : uuids) {
            playerSet.add(Bukkit.getPlayer(uuid));
        }
        return playerSet;
    }

    // Méthode pour obtenir l'équipe d'un joueur
    public TeamEnum getTeam(Player player) {
        return playerTeamMap.get(player.getUniqueId());
    }
}