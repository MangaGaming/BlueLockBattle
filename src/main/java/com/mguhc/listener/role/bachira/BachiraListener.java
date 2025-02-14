package com.mguhc.listener.role.bachira;

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

import java.util.Collections;
import java.util.Random;

public class BachiraListener implements Listener {

    private final RoleManager roleManager;
    private final TeamManager teamManager;
    private final PlayerManager playerManager;
    private final BallManager ballManager;
    private final EffectManager effectManager;
    private final AbilityManager abilityManager;
    private final CooldownManager cooldownManager;

    private DribleAbility dribleAbility;

    public BachiraListener() {
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
        Player player = roleManager.getPlayerWithRole(Role.Bachira);
        if (player != null) {
            player.setMaxHealth(20);
            player.getInventory().addItem(getDribleItem());

            dribleAbility = new DribleAbility();
            abilityManager.registerAbility(Role.Bachira, Collections.singletonList(dribleAbility));
        }
    }

    @EventHandler
    private void OnDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player damager = (Player) event.getDamager();
            Player victim = (Player) event.getEntity();
            if (roleManager.getRole(damager).equals(Role.Bachira) && teamManager.getTeam(damager) != teamManager.getTeam(victim)) {
                Random random = new Random();
                int randomNumber = random.nextInt(100);
                if (randomNumber <= 5) {
                    returnPlayer(victim);
                }
            }
        }
    }

    private void returnPlayer(Player player) {
        // Obtenir la position actuelle du joueur
        Location currentLocation = player.getLocation();

        // Calculer le nouveau yaw en ajoutant 180 degrés
        float newYaw = currentLocation.getYaw() + 180.0f;

        // Définir le nouveau yaw tout en gardant le pitch inchangé
        currentLocation.setYaw(newYaw);

        // Mettre à jour la position du joueur avec le nouveau yaw
        player.teleport(currentLocation);
    }

    @EventHandler
    private void OnInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item != null &&
            item.equals(getDribleItem())) {
            if (cooldownManager.getRemainingCooldown(player, dribleAbility) == 0) {
                cooldownManager.startCooldown(player, dribleAbility);

                for (Entity e : player.getNearbyEntities(20, 20, 20)) {
                    if (e instanceof Player) {
                        Player p = (Player) e;
                        if (teamManager.getTeam(player) != teamManager.getTeam(p)) {
                            returnPlayer(p);
                        }
                    }
                }
            }
            else {
                player.sendMessage(ChatColor.RED + "Vous êtes en cooldown pour " + (long) cooldownManager.getRemainingCooldown(player, dribleAbility) / 1000 + "s");
            }
        }
    }

    private ItemStack getDribleItem() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.BLUE + "Drible");
            item.setItemMeta(meta);
        }
        return item;
    }
}
