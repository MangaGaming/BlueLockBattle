package com.mguhc.manager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.ArrayList;

public class RoleManager {
    // Map pour stocker les rôles des joueurs
    private final Map<Role, List<UUID>> rolePlayerMap = new HashMap<>();

    // Méthode pour définir le rôle d'un joueur
    public void setRole(Player player, Role role) {
        // Assurez-vous que la liste pour ce rôle existe
        rolePlayerMap.putIfAbsent(role, new ArrayList<>());
        // Ajoutez le joueur à la liste de ce rôle
        rolePlayerMap.get(role).add(player.getUniqueId());
    }

    // Méthode pour retirer le rôle d'un joueur
    public void removeRole(Player player) {
        // Parcourez les rôles pour retirer le joueur
        for (Map.Entry<Role, List<UUID>> entry : rolePlayerMap.entrySet()) {
            entry.getValue().remove(player.getUniqueId());
            // Si la liste est vide après la suppression, vous pouvez également supprimer le rôle
            if (entry.getValue().isEmpty()) {
                rolePlayerMap.remove(entry.getKey());
            }
        }
    }

    // Méthode pour obtenir le rôle d'un joueur
    public Role getRole(Player player) {
        for (Map.Entry<Role, List<UUID>> entry : rolePlayerMap.entrySet()) {
            if (entry.getValue().contains(player.getUniqueId())) {
                return entry.getKey();
            }
        }
        return null; // Retourne null si le joueur n'a pas de rôle
    }

    // Méthode pour obtenir tous les joueurs avec un rôle spécifique
    public List<Player> getPlayersWithRole(Role role) {
        List<UUID> playerUUIDs = rolePlayerMap.get(role);
        if (playerUUIDs == null) {
            return null;
        }

        List<Player> players = new ArrayList<>();
        for (UUID uuid : playerUUIDs) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                players.add(player);
            }
        }

        return players;
    }
}