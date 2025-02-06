package com.mguhc.manager;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class TeamManager {
    // Map pour stocker les joueurs et leurs équipes
    private final HashMap<Player, Team> playerTeamMap = new HashMap<>();
    // Map pour stocker les équipes et les joueurs dans chaque équipe
    private final HashMap<Team, Set<Player>> teamPlayerMap = new HashMap<>();

    // Méthode pour ajouter un joueur à une équipe
    public void addPlayer(Player player, Team team) {
        // Vérifier si l'équipe existe, sinon la créer
        teamPlayerMap.putIfAbsent(team, new HashSet<>());
        // Ajouter le joueur à l'équipe
        teamPlayerMap.get(team).add(player);
        // Mettre à jour la map des joueurs
        playerTeamMap.put(player, team);

        if (team.equals(Team.ROUGE)) {
            player.setPlayerListName(ChatColor.RED + player.getName());
            player.setCustomName(ChatColor.RED + player.getName());// Passer la couleur rouge
        }
        if (team.equals(Team.BLEU)) {
            player.setPlayerListName(ChatColor.BLUE + player.getName());
            player.setCustomName(ChatColor.BLUE + player.getName());// Passer la couleur rouge
        }
    }


    // Méthode pour retirer un joueur d'une équipe
    public void removePlayer(Player player, Team team) {
        // Vérifier si le joueur est dans l'équipe
        if (teamPlayerMap.containsKey(team) && teamPlayerMap.get(team).contains(player)) {
            teamPlayerMap.get(team).remove(player);
            playerTeamMap.remove(player);
        }
    }

    // Méthode pour obtenir tous les joueurs dans une équipe
    public Set<Player> getPlayersInTeam(Team team) {
        return teamPlayerMap.getOrDefault(team, new HashSet<>());
    }

    // Méthode pour obtenir l'équipe d'un joueur
    public Team getTeam(Player player) {
        return playerTeamMap.get(player);
    }
}