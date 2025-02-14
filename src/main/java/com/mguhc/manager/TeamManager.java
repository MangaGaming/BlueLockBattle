package com.mguhc.manager;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class TeamManager {
    // Map pour stocker les joueurs et leurs équipes
    private final HashMap<Player, TeamEnum> playerTeamMap = new HashMap<>();
    // Map pour stocker les équipes et les joueurs dans chaque équipe
    private final HashMap<TeamEnum, Set<Player>> teamPlayerMap = new HashMap<>();

    // Méthode pour ajouter un joueur à une équipe
    public void addPlayer(Player player, TeamEnum team) {
        // Vérifier si l'équipe existe, sinon la créer
        teamPlayerMap.putIfAbsent(team, new HashSet<>());
        teamPlayerMap.get(team).add(player);
        playerTeamMap.put(player, team);
    }

    // Méthode pour retirer un joueur d'une équipe
    public void removePlayer(Player player) {
        TeamEnum team = playerTeamMap.get(player);
        if (team != null && teamPlayerMap.containsKey(team)) {
            teamPlayerMap.get(team).remove(player);
            playerTeamMap.remove(player);
        }
    }

    // Méthode pour obtenir tous les joueurs dans une équipe
    public Set<Player> getPlayersInTeam(TeamEnum team) {
        return teamPlayerMap.getOrDefault(team, new HashSet<>());
    }

    // Méthode pour obtenir l'équipe d'un joueur
    public TeamEnum getTeam(Player player) {
        return playerTeamMap.get(player);
    }
}