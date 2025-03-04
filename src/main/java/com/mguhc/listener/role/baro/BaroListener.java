package com.mguhc.listener.role.baro;

import com.mguhc.Blb;
import com.mguhc.events.RoleGiveEvent;
import com.mguhc.manager.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
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
        List<Player> players = roleManager.getPlayersWithRole(Role.Baro);
        if (players != null) {
            for (Player player : players) {
                player.sendMessage("§f \n" +
                        "§8§l«§8§m---------------------------------------------------§8§l»\n" +
                        "§f \n" +
                        "§8│ §3§lINFORMATIONS\n" +
                        "§f §b▪ §fPersonnage §7: §9§lBaro\n" +
                        "§f §b▪ §fVie §7: §c13§4❤\n" +
                        "§f §b▪ §fEffets §7: §7Résistance I\n" +
                        "§f \n" +
                        "§8│ §3§lPARTICULARITES\n" +
                        "§f §b▪ §fVous possédez §b5% §fde chance de d'§bimmobiliser §fpendant §e1 §fseconde les joueurs que vous §cfrappez§f.\n" +
                        "§f §b▪ §fVous mettez §e10 §fsecondes à réapparaitre.\n" +
                        "§f \n" +
                        "§8│ §3§lPOUVOIRS\n" +
                        "§f §b▪ §fConfrontation §8(§b«§8)\n" +
                        "§f \n" +
                        "§8§l«§8§m---------------------------------------------------§8§l»");
                player.setMaxHealth(26);
                player.setHealth(player.getMaxHealth());
                effectManager.setResistance(player, 20);
                player.getInventory().addItem(getConfrontationItem());

                confrontationAbility = new ConfrontationAbility();
                abilityManager.registerAbility(Role.Baro, Collections.singletonList(confrontationAbility));

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        ItemStack item = player.getItemInHand();
                        if (item.equals(getConfrontationItem())) {
                            Blb.sendActionBar(player, "§9» §f§lCooldown §b(§f" + (long) cooldownManager.getRemainingCooldown(player, confrontationAbility) / 1000 + "§b) §9« " + "§3| " + "§9» §f§lPhysique Supérieur §b(§f5%§b) §9«");
                        }
                        else {
                            Blb.sendActionBar(player, "§9» §f§lPhysique Supérieur §b(§f5%§b) §9«");
                        }
                    }
                }.runTaskTimer(Blb.getInstance(), 0, 5);
            }
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
                    damager.playSound(damager.getLocation(), Sound.LEVEL_UP, 1, 1);

                    // Créer un ArmorStand invisible
                    ArmorStand armorStand = (ArmorStand) damager.getWorld().spawnEntity(damager.getLocation(), EntityType.ARMOR_STAND);
                    armorStand.setVisible(false); // Rendre l'ArmorStand invisible
                    armorStand.setGravity(false); // Empêcher la gravité
                    armorStand.setPassenger(damager); // Mettre le joueur sur l'ArmorStand

                    // Créer un BukkitRunnable pour gérer la durée de l'immobilisation
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            damager.leaveVehicle(); // Faire descendre le joueur de l'ArmorStand
                            armorStand.remove(); // Retirer l'ArmorStand
                            cancel(); // Annuler le Runnable
                        }
                    }.runTaskLater(Blb.getInstance(), 20); // Exécuter après 20 ticks (1 seconde)
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
                    player.sendMessage("§3│ §fVous venez d'utiliser §bConfrontation§f.");
                }
            }
            else {
                player.sendMessage("§6┃ §fVous avez un §6cooldown §fde §e" + (long) cooldownManager.getRemainingCooldown(player, confrontationAbility) / 1000 + "s §fsur cette capacité.");
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
            List<String> lore = new ArrayList<>();
            lore.add("§3≡ §b§lConfrontation");
            lore.add("§f");
            lore.add("§8┃ §fPermet d'§aéjecter §fle joueur ciblé.");
            lore.add("§f");
            lore.add("§6◆ §fCooldown §7: §e15 secondes");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}