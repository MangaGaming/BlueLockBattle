package com.mguhc.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class EffectManager implements Listener {
    private final Map<UUID, Integer> speedEffects;
    private final Map<UUID, Integer> strengthEffects;
    private final Map<UUID, Integer> resistanceEffects;
    private final Map<UUID, Integer> weaknessEffects;
    private final Map<UUID, Boolean> noFallActive;

    public EffectManager() {
        this.speedEffects = new HashMap<>();
        this.strengthEffects = new HashMap<>();
        this.resistanceEffects = new HashMap<>();
        this.weaknessEffects = new HashMap<>();
        this.noFallActive = new HashMap<>();
    }

    // Method to apply a speed effect
    public void setSpeed(Player player, int percentage) {
        speedEffects.put(player.getUniqueId(), percentage);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, true, false)); // Appliquer effet de vitesse
    }

    // Method to apply a strength effect
    public void setStrength(Player player, int percentage) {
        strengthEffects.put(player.getUniqueId(), percentage);
        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 0, true, false)); // Appliquer effet de force
    }

    // Method to apply a resistance effect
    public void setResistance(Player player, int percentage) {
        resistanceEffects.put(player.getUniqueId(), percentage);
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 0, true, false)); // Appliquer effet de résistance
    }

    public void setWeakness(Player player, int percentage) {
        weaknessEffects.put(player.getUniqueId(), percentage);
        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, Integer.MAX_VALUE, 0, true, false)); // Appliquer effet de faiblesse
    }

    public void setNoFall(Player player, boolean b) {
        noFallActive.put(player.getUniqueId(), b);
    }

    // Method to remove an effect
    public void removeEffect(Player player, PotionEffectType effectType) {
        if (effectType == PotionEffectType.SPEED) {
            speedEffects.remove(player.getUniqueId());
        } else if (effectType == PotionEffectType.INCREASE_DAMAGE) {
            strengthEffects.remove(player.getUniqueId());
        } else if (effectType == PotionEffectType.DAMAGE_RESISTANCE) {
            resistanceEffects.remove(player.getUniqueId());
        } else if (effectType == PotionEffectType.WEAKNESS) {
            weaknessEffects.remove(player.getUniqueId());
        }
        player.removePotionEffect(effectType);
    }

    public void removeEffects(Player player) {
        speedEffects.remove(player.getUniqueId());
        strengthEffects.remove(player.getUniqueId());
        resistanceEffects.remove(player.getUniqueId());
        weaknessEffects.remove(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (speedEffects.containsKey(player.getUniqueId())) {
            player.setWalkSpeed((float) 0.16 * (1 + (float) speedEffects.get(player.getUniqueId()) / 100));
        } else {
            player.setWalkSpeed(0.2f);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            Player victim = (Player) event.getEntity();
            double originalDamage;
            originalDamage = getDamage(attacker.getItemInHand());

            // Appliquer l'effet de force
            if (strengthEffects.containsKey(attacker.getUniqueId())) {
                int percentage = strengthEffects.get(attacker.getUniqueId());
                double damageMultiplier = 1 + (percentage / 100.0);
                originalDamage *= damageMultiplier;
            }

            // Appliquer l'effet de faiblesse
            if (weaknessEffects.containsKey(attacker.getUniqueId())) {
                int percentage = weaknessEffects.get(attacker.getUniqueId());
                originalDamage *= (100 - percentage) / 100.0;
            }

            // Appliquer l'effet de résistance
            if (resistanceEffects.containsKey(victim.getUniqueId())) {
                int percentage = resistanceEffects.get(victim.getUniqueId());
                double damageReduction = originalDamage * (percentage / 100.0);
                originalDamage -= damageReduction;
                // S'assurer que les dégâts ne tombent pas en dessous de zéro
                originalDamage = Math.max(0, originalDamage);
            }

            event.setDamage(originalDamage);
        }
    }

    @EventHandler
    private void OnDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            Player player = (Player) entity;
            if (noFallActive.containsKey(player.getUniqueId()) &&
                    noFallActive.get(player.getUniqueId()) &&
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
            String message = "Vos effets :\n" + "Vitesse : " + speedEffects.getOrDefault(player.getUniqueId(), 0) + "%\n" +
                    "Force : " + strengthEffects.getOrDefault(player.getUniqueId(), 0) + "%\n" +
                    "Résistance : " + resistanceEffects.getOrDefault(player.getUniqueId(), 0) + "%\n" +
                    "Faiblesse : " + weaknessEffects.getOrDefault(player.getUniqueId(), 0) + "%\n";

            player.sendMessage(message);
            event.setCancelled(true); // Cancel the command to prevent default display
        }
    }

    public int getEffect(Player player, PotionEffectType effect) {
        if (effect.equals(PotionEffectType.SPEED) && speedEffects.containsKey(player.getUniqueId())) {
            return speedEffects.get(player.getUniqueId());
        } else if (effect.equals(PotionEffectType.INCREASE_DAMAGE) && strengthEffects.containsKey(player.getUniqueId())) {
            return strengthEffects.get(player.getUniqueId());
        } else if (effect.equals(PotionEffectType.DAMAGE_RESISTANCE) && resistanceEffects.containsKey(player.getUniqueId())) {
            return resistanceEffects.get(player.getUniqueId());
        } else if (effect.equals(PotionEffectType.WEAKNESS) && weaknessEffects.containsKey(player.getUniqueId())) {
            return weaknessEffects.get(player.getUniqueId());
        } else {
            return 0; // Return 0 if no effect is found
        }
    }

    public Map<PotionEffectType, Integer> getEffectsMap(Player player) {
        Map<PotionEffectType, Integer> effectsMap = new HashMap<>();

        effectsMap.put(PotionEffectType.SPEED, speedEffects.getOrDefault(player.getUniqueId(), 0));
        effectsMap.put(PotionEffectType.INCREASE_DAMAGE, strengthEffects.getOrDefault(player.getUniqueId(), 0));
        effectsMap.put(PotionEffectType.DAMAGE_RESISTANCE, resistanceEffects.getOrDefault(player.getUniqueId(), 0));
        effectsMap.put(PotionEffectType.WEAKNESS, weaknessEffects.getOrDefault(player.getUniqueId(), 0));

        return effectsMap;
    }

    public void setEffects(Player player, Map<PotionEffectType, Integer> effects) {
        for (Map.Entry<PotionEffectType, Integer> entry : effects.entrySet()) {
            PotionEffectType effectType = entry.getKey();
            int percentage = entry.getValue();

            // Appliquer l'effet en fonction de son type
            if (effectType.equals(PotionEffectType.SPEED)) {
                setSpeed(player, percentage);
            } else if (effectType.equals(PotionEffectType.INCREASE_DAMAGE)) {
                setStrength(player, percentage);
            } else if (effectType.equals(PotionEffectType.DAMAGE_RESISTANCE)) {
                setResistance(player, percentage);
            } else if (effectType.equals(PotionEffectType.WEAKNESS)) {
                setWeakness(player, percentage);
            }
        }
    }

    public double getDamage(ItemStack item) {
        if (item != null &&
                item.getType().equals(Material.DIAMOND_SWORD)) {
            if (item.getItemMeta().hasEnchant(Enchantment.DAMAGE_ALL)) {
                int amplifier = 0;
                for (Map.Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
                    if (entry.getKey().equals(Enchantment.DAMAGE_ALL)) {
                        amplifier = entry.getValue();
                        break;
                    }
                }
                switch (amplifier) {
                    case 1:
                        return 8.25;
                    case 2:
                        return 9.5;
                    case 3:
                        return 10.75;
                    default:
                        return 7;
                }
            }
            else {
                return 7;
            }
        }
        else {
            if (item != null && item.getType().equals(Material.IRON_SWORD)) {
                if (item.getItemMeta().hasEnchant(Enchantment.DAMAGE_ALL)) {
                    int amplifier = 0;
                    for (Map.Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
                        if (entry.getKey().equals(Enchantment.DAMAGE_ALL)) {
                            amplifier = entry.getValue();
                            break;
                        }
                    }
                    switch (amplifier) {
                        case 1:
                            return 7;
                        case 2:
                            return 7.5;
                        case 3:
                            return 8;
                        default:
                            return 6;
                    }
                }
                else {
                    return 6;
                }
            }
            else {
                return 1;
            }
        }
    }
}