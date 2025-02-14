package com.mguhc.listener.role.baro;

import com.mguhc.Blb;
import com.mguhc.events.RoleGiveEvent;
import com.mguhc.manager.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class BaroListener implements Listener {

    private final RoleManager roleManager;
    private final TeamManager teamManager;
    private final PlayerManager playerManager;
    private final BallManager ballManager;
    private final EffectManager effectManager;
    private final AbilityManager abilityManager;
    private final CooldownManager cooldownManager;

    private ConfrontationAbility confrontationAbility;

    public BaroListener() {
        Blb blb = Blb.getInstance();
        roleManager = blb.getRoleManager();
        teamManager = blb.getTeamManager();
        playerManager = blb.getPlayerManager();
        ballManager = blb.getBallManager();
        effectManager = blb.getEffectManager();
        abilityManager = blb.getAbilityManager();
        cooldownManager = blb.getCooldownManager();
    }

    @EventHandler
    private void OnRoleGive(RoleGiveEvent event) {
        Player player = roleManager.getPlayerWithRole(Role.Baro);
        if (player != null) {
            player.setMaxHealth(26);
            player.setHealth(player.getMaxHealth());
            effectManager.setResistance(player, 20);
            player.getInventory().addItem(getConfrontationItem());

            confrontationAbility = new ConfrontationAbility();
            abilityManager.registerAbility(Role.Baro, Collections.singletonList(confrontationAbility));
        }
    }

    @EventHandler
    private void OnDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player damager = (Player) event.getDamager();
            Player victim = (Player) event.getEntity();
            if (roleManager.getRole(damager).equals(Role.Baro) && teamManager.getTeam(damager) != teamManager.getTeam(victim)) {
                Random random = new Random();
                int randomNumber = random.nextInt(100);
                if (randomNumber <= 5) {
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20, 255));
                }
            }
        }
    }

    @EventHandler
    private void OnInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item != null &&
            item.equals(getConfrontationItem())) {
            if (cooldownManager.getRemainingCooldown(player, confrontationAbility) == 0) {
                Player target = getTargetPlayer(player, 5);
                if (target != null) {
                    cooldownManager.startCooldown(player, confrontationAbility);
                    launchPlayer(target, -1.5);
                }
                else {
                    player.sendMessage(ChatColor.RED + "Vous ne visez personne");
                }
            }
            else {
                player.sendMessage(ChatColor.RED + "Vous êtes en cooldown pour " + (long) cooldownManager.getRemainingCooldown(player, confrontationAbility) / 1000 + "s");
            }
        }
    }

    private void launchPlayer(Player player, double power) {
        // Obtenir la direction dans laquelle le joueur regarde
        Vector direction = player.getLocation().getDirection().normalize(); // Normaliser la direction

        // Appliquer la force au ballon
        player.setVelocity(direction.multiply(power)); // Multiplier la direction par la puissance
    }

    private Player getTargetPlayer(Player player, double maxDistance) {
        // Get the player's eye location and direction
        Location eyeLocation = player.getEyeLocation();
        Vector direction = eyeLocation.getDirection().normalize();

        // Get nearby entities within the specified distance
        List<Entity> nearbyEntities = player.getNearbyEntities(maxDistance, maxDistance, maxDistance);

        for (Entity entity : nearbyEntities) {
            if (entity instanceof Player && entity != player) {
                // Check if the entity is in the line of sight
                Vector toEntity = entity.getLocation().toVector().subtract(eyeLocation.toVector()).normalize();
                double dotProduct = direction.dot(toEntity);

                // Check if the entity is within the player's line of sight
                if (dotProduct > 0.9) {
                    return (Player) entity;
                }
            }
        }
        return null; // No target player found
    }


    private ItemStack getConfrontationItem() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.RED + "Confrontation");
            item.setItemMeta(meta);
        }
        return item;
    }
}