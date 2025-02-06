package com.mguhc.manager;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class CooldownManager {
    // Map pour stocker les cooldowns des joueurs pour chaque capacité
    public Map<Player, Map<Ability, Double>> playerCooldowns;

    public CooldownManager() {
        this.playerCooldowns = new HashMap<>();
    }

    // Méthode pour démarrer un cooldown pour un joueur et une capacité spécifique
    public void startCooldown(Player player, Ability ability) {
        double duration = ability.getCooldownDuration(); // Obtenir la durée de cooldown de l'ability
        playerCooldowns.putIfAbsent(player, new HashMap<>());
        playerCooldowns.get(player).put(ability, System.currentTimeMillis() + duration);
    }

    // Méthode pour vérifier si un joueur est en cooldown pour une capacité spécifique
    public boolean isInCooldown(Player player, Ability ability) {
        if (!playerCooldowns.containsKey(player) || !playerCooldowns.get(player).containsKey(ability)) {
            return false; // Pas de cooldown actif pour cette capacité
        }
        Double endTime = playerCooldowns.get(player).get(ability);
        if (System.currentTimeMillis() > endTime) {
            playerCooldowns.get(player).remove(ability); // Retirer le cooldown si le temps est écoulé
            return false;
        }
        return true; // Le joueur est toujours en cooldown pour cette capacité
    }

    // Méthode pour obtenir le temps restant sur le cooldown d'un joueur pour une capacité spécifique
    public double getRemainingCooldown(Player player, Ability ability) {
        if (!isInCooldown(player, ability)) {
            return 0; // Pas de cooldown actif pour cette capacité
        }
        double endTime = playerCooldowns.get(player).get(ability);
        return endTime - System.currentTimeMillis(); // Temps restant en millisecondes
    }

    // Méthode pour retirer le cooldown d'un joueur pour une capacité spécifique manuellement
    public void removeCooldown(Player player, Ability ability) {
        if (playerCooldowns.containsKey(player)) {
            playerCooldowns.get(player).remove(ability);
        }
    }

    // Méthode pour retirer tous les cooldowns d'un joueur
    public void removeAllCooldowns(Player player) {
        playerCooldowns.remove(player);
    }
}