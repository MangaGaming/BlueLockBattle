package com.mguhc.manager;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffectType;

public class EffectManager implements Listener {
    private final Map<Player, Integer> speedEffects;
    private final Map<Player, Integer> strengthEffects;
    private final Map<Player, Integer> resistanceEffects;
    private final Map<Player, Integer> weaknessEffects;
    private final Map<Player, Boolean> noFallActive;

    public EffectManager() {
        this.speedEffects = new HashMap<>();
        this.strengthEffects = new HashMap<>();
        this.resistanceEffects = new HashMap<>();
        this.weaknessEffects = new HashMap<>();
        this.noFallActive = new HashMap<>();
    }

    // Method to apply a speed effect
    public void setSpeed(Player player, int percentage) {
        speedEffects.put(player, percentage);
    }

    // Method to apply a strength effect
    public void setStrength(Player player, int percentage) {
        strengthEffects.put(player, percentage);
    }

    // Method to apply a resistance effect
    public void setResistance(Player player, int percentage) {
        resistanceEffects.put(player, percentage);
    }

    public void setWeakness(Player player, int percentage) {
        weaknessEffects.put(player, percentage);
    }

    public void setNoFall(Player player, boolean b) {
        noFallActive.put(player, b);
    }

    // Method to remove an effect
    public void removeEffect(Player player, PotionEffectType effectType) {
        if (effectType == PotionEffectType.SPEED) {
            speedEffects.remove(player);
        } else if (effectType == PotionEffectType.INCREASE_DAMAGE) {
            strengthEffects.remove(player);
        } else if (effectType == PotionEffectType.DAMAGE_RESISTANCE) {
            resistanceEffects.remove(player);
        } else if (effectType == PotionEffectType.WEAKNESS) {
            weaknessEffects.remove(player);
        }
    }

    public void removeEffects(Player player) {
        speedEffects.remove(player);
        strengthEffects.remove(player);
        resistanceEffects.remove(player);
        weaknessEffects.remove(player);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (speedEffects.containsKey(player)) {
            player.setWalkSpeed((float) 0.2 * (1 + (float) speedEffects.get(player) / 100));
        }
        else {
            player.setWalkSpeed(0.2f);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            double originalDamage = event.getDamage();

            // Appliquer l'effet de force
            if (strengthEffects.containsKey(attacker)) {
                int percentage = strengthEffects.get(attacker);
                double damageMultiplier = 1 + (percentage / 100.0);
                originalDamage *= damageMultiplier;
            }

            // Appliquer l'effet de faiblesse
            if (weaknessEffects.containsKey(attacker)) {
                int percentage = weaknessEffects.get(attacker);
                originalDamage *= (100 - percentage) / 100.0;
            }

            event.setDamage(originalDamage);
        }

        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();
            double damage = event.getDamage();

            // Appliquer l'effet de résistance
            if (resistanceEffects.containsKey(victim)) {
                int percentage = resistanceEffects.get(victim);
                double damageReduction = damage * (percentage / 100.0);
                damage -= damageReduction;
            }

            // S'assurer que les dégâts ne tombent pas en dessous de zéro
            damage = Math.max(0, damage);
            event.setDamage(damage);
        }
    }

    @EventHandler
    private void OnDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            Player player = (Player) entity;
            if(noFallActive.containsKey(player) &&
                    noFallActive.get(player) &&
                    event.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage();

        if (command.equalsIgnoreCase("/effects")) {

            String message = "Vos effets :\n" + "Vitesse : " + speedEffects.getOrDefault(player, 0) + "%\n" +
                    "Force : " + strengthEffects.getOrDefault(player, 0) + "%\n" +
                    "Résistance : " + resistanceEffects.getOrDefault(player, 0) + "%\n" +
                    "Faiblesse : " + weaknessEffects.getOrDefault(player, 0) + "%\n";


            player.sendMessage(message);
            event.setCancelled(true); // Cancel the command to prevent default display
        }
    }

    public int getEffect(Player player, PotionEffectType effect) {
        if (effect.equals(PotionEffectType.SPEED) && speedEffects.containsKey(player)) {
            return speedEffects.get(player);
        } else if (effect.equals(PotionEffectType.INCREASE_DAMAGE) && strengthEffects.containsKey(player)) {
            return strengthEffects.get(player);
        } else if (effect.equals(PotionEffectType.DAMAGE_RESISTANCE) && resistanceEffects.containsKey(player)) {
            return resistanceEffects.get(player);
        } else if (effect.equals(PotionEffectType.WEAKNESS) && weaknessEffects.containsKey(player)) {
            return weaknessEffects.get(player);
        } else {
            return 0; // Return 0 if no effect is found
        }
    }

    public Map<PotionEffectType, Integer> getEffectsMap(Player player) {
        Map<PotionEffectType, Integer> effectsMap = new HashMap<>();

        effectsMap.put(PotionEffectType.SPEED, speedEffects.getOrDefault(player, 0));
        effectsMap.put(PotionEffectType.INCREASE_DAMAGE, strengthEffects.getOrDefault(player, 0));
        effectsMap.put(PotionEffectType.DAMAGE_RESISTANCE, resistanceEffects.getOrDefault(player, 0));
        effectsMap.put(PotionEffectType.WEAKNESS, weaknessEffects.getOrDefault(player, 0));

        return effectsMap;
    }

    public void setEffects(Player player, Map<PotionEffectType, Integer> effects) {
        for (Map.Entry<PotionEffectType, Integer> entry : effects.entrySet()) {
            PotionEffectType effectType = entry.getKey();
            int percentage = entry.getValue();

            // Appliquer l'effet en fonction de son type
            if (effectType.equals(PotionEffectType.SPEED)) {
                setSpeed(player, percentage);
            } if (effectType.equals(PotionEffectType.INCREASE_DAMAGE)) {
                setStrength(player, percentage);
            } if (effectType.equals(PotionEffectType.DAMAGE_RESISTANCE)) {
                setResistance(player, percentage);
            } if (effectType.equals(PotionEffectType.WEAKNESS)) {
                setWeakness(player, percentage);
            }
        }
    }
}