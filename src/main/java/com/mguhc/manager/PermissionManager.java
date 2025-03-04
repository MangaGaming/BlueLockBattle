package com.mguhc.manager;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class PermissionManager {
    // Map pour stocker les permissions et les joueurs qui les possèdent
    private final Map<String, List<Player>> permissionMap = new HashMap<>();

    // Méthode pour ajouter un joueur à une permission
    public void addPermission(Player player, String permission) {
        permissionMap.putIfAbsent(permission, new ArrayList<>());
        if (!permissionMap.get(permission).contains(player)) {
            permissionMap.get(permission).add(player);
        }
    }

    // Méthode pour retirer un joueur d'une permission
    public void removePermission(Player player, String permission) {
        List<Player> playersWithPermission = permissionMap.get(permission);
        if (playersWithPermission != null) {
            playersWithPermission.remove(player);
            // Si la liste est vide, vous pouvez également supprimer la permission
            if (playersWithPermission.isEmpty()) {
                permissionMap.remove(permission);
            }
        }
    }

    // Méthode pour vérifier si un joueur a une permission
    public boolean hasPermission(Player player, String permission) {
        List<Player> playersWithPermission = permissionMap.get(permission);
        return playersWithPermission != null && playersWithPermission.contains(player);
    }
}