package com.mguhc.manager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RoleManager {
    // Map pour stocker les rôles des joueurs
    private final Map<UUID, Role> playerRoleMap = new HashMap<>();

    // Méthode pour définir le rôle d'un joueur
    public void setRole(Player player, Role role) {
        playerRoleMap.put(player.getUniqueId(), role);
    }

    // Méthode pour retirer le rôle d'un joueur
    public void removeRole(Player player) {
        playerRoleMap.remove(player.getUniqueId());
    }

    // Méthode pour obtenir le rôle d'un joueur
    public Role getRole(Player player) {
        return playerRoleMap.get(player.getUniqueId());
    }

    // Méthode pour obtenir un joueur avec un rôle spécifique
    public Player getPlayerWithRole(Role role) {
        for (Map.Entry<UUID, Role> entry : playerRoleMap.entrySet()) {
            if (entry.getValue() == role) {
                return Bukkit.getPlayer(entry.getKey());
            }
        }
        return null; // Retourne null si aucun joueur n'a ce rôle
    }
}