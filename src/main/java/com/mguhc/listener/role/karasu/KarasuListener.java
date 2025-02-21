package com.mguhc.listener.role.karasu;

import com.mguhc.Blb;
import com.mguhc.events.RoleGiveEvent;
import com.mguhc.manager.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
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

public class KarasuListener implements Listener {

    private final RoleManager roleManager;
    private final TeamManager teamManager;
    private final PlayerManager playerManager;
    private final BallManager ballManager;
    private final EffectManager effectManager;
    private final AbilityManager abilityManager;
    private final CooldownManager cooldownManager;

    private EsquiveAbility esquiveAbility;

    public KarasuListener() {
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
        Player player = roleManager.getPlayerWithRole(Role.Karasu);
        if (player != null) {
            player.sendMessage("§f \n" +
                    "§8§l«§8§m---------------------------------------------------§8§l»\n" +
                    "§f\n" +
                    "§8│ §3§lINFORMATIONS\n" +
                    "§f §b▪ §fPersonnage §7: §9§lKarasu\n" +
                    "§f §b▪ §fVie §7: §c12§4❤\n" +
                    "§f §b▪ §fEffets §7: §7Résistance I\n" +
                    "§f\n" +
                    "§8│ §3§lPARTICULARITES\n" +
                    "§f §b▪ §f...\n" +
                    "§f §b▪ §fVous mettez §e12 §fsecondes à réapparaitre.\n" +
                    "§f\n" +
                    "§8│ §3§lPOUVOIRS\n" +
                    "§f §b▪ §fEsquive §8(§b«§8)\n" +
                    "§f\n" +
                    "§8§l«§8§m---------------------------------------------------§8§l»");
            player.setMaxHealth(24);
            player.getInventory().addItem(getEsquiveItem());
            effectManager.setResistance(player, 20);

            esquiveAbility = new EsquiveAbility();
            abilityManager.registerAbility(Role.Karasu, Collections.singletonList(esquiveAbility));

            new BukkitRunnable() {
                @Override
                public void run() {
                    ItemStack item = player.getItemInHand();
                    if (item.equals(getEsquiveItem())) {
                        Blb.sendActionBar(player, "§9» §f§lCooldown §b(§f" + cooldownManager.getRemainingCooldown(player, esquiveAbility) + "§b) §9« " + "§3| " + "§9» §f§lMilieu de Terrain §b(§f5%§b) §9«");
                    }
                    else {
                        Blb.sendActionBar(player, "§9» §f§lMilieu de Terrain §b(§f5%§b) §9«");
                    }
                }
            }.runTaskTimer(Blb.getInstance(), 0, 5);
        }
    }

    @EventHandler
    private void OnDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player damager = (Player) event.getDamager();
            Player victim = (Player) event.getEntity();
            Role role = roleManager.getRole(damager);
            if (role != null && role.equals(Role.Karasu) && teamManager.getTeam(damager) != teamManager.getTeam(victim)) {
                Random random = new Random();
                int randomNumber = random.nextInt(100);
                if (randomNumber <= 5) {
                    if (victim.getInventory().contains(ballManager.getSlimeBall())) {
                        damager.playSound(damager.getLocation(),  Sound.LEVEL_UP, 1, 1);
                        victim.getInventory().remove(ballManager.getSlimeBall());
                        ballManager.spawnBall(victim.getLocation());
                    }
                }
            }
        }
    }

    @EventHandler
    private void OnInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item != null && item.equals(getEsquiveItem())) {
            if (cooldownManager.getRemainingCooldown(player, esquiveAbility) == 0) {
                cooldownManager.startCooldown(player, esquiveAbility);
                Player p = getTargetPlayer(player, 10);
                if (p != null) {
                    Location location = p.getLocation().add(3, 0, 5);
                    player.teleport(location);
                }
                player.sendMessage("§3│ §fVous venez d'utiliser §bEsquive§f.");
            }
            else {
                player.sendMessage("§6┃ §fVous avez un §6cooldown §fde §e" + (long) cooldownManager.getRemainingCooldown(player, esquiveAbility) / 1000 + " §fsur cette capacité.");
            }
        }
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


    private ItemStack getEsquiveItem() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.BLUE + "Esquive");
            List<String> lore = new ArrayList<>();
            lore.add("§3≡ §b§lEsquive");
            lore.add("§f");
            lore.add("§8┃ §fPermet de ...");
            lore.add("§f");
            lore.add("§6◆ §fCooldown §7: §e15 secondes");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}
