package com.mguhc.manager;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class RoleManager {
    // Map pour stocker les rôles des joueurs
    private final Map<Player, Role> playerRoleMap = new HashMap<>();

    // Méthode pour définir le rôle d'un joueur
    public void setRole(Player player, Role role) {
        playerRoleMap.put(player, role);
    }

    // Méthode pour retirer le rôle d'un joueur
    public void removeRole(Player player) {
        playerRoleMap.remove(player);
    }

    // Méthode pour obtenir le rôle d'un joueur
    public Role getRole(Player player) {
        return playerRoleMap.get(player);
    }

    // Méthode pour obtenir un joueur avec un rôle spécifique
    public Player getPlayerWithRole(Role role) {
        for (Map.Entry<Player, Role> entry : playerRoleMap.entrySet()) {
            if (entry.getValue() == role) {
                return entry.getKey();
            }
        }
        return null; // Retourne null si aucun joueur n'a ce rôle
    }
}