package com.mguhc.listener.role.rin;

import com.connorlinfoot.titleapi.TitleAPI;
import com.mguhc.Blb;
import com.mguhc.events.RoleGiveEvent;
import com.mguhc.manager.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RinListener implements Listener {

    private final RoleManager roleManager;
    private final TeamManager teamManager;
    private final PlayerManager playerManager;
    private final BallManager ballManager;
    private final EffectManager effectManager;
    private final AbilityManager abilityManager;
    private final CooldownManager cooldownManager;

    private CoupFrancAbility coupFrancAbility;

    public RinListener() {
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
        Player player = roleManager.getPlayerWithRole(Role.Rin);
        if (player != null) {
            player.sendMessage("§f \n" +
                    "§8§l«§8§m---------------------------------------------------§8§l»\n" +
                    "§f \n" +
                    "§8│ §3§lINFORMATIONS\n" +
                    "§f §b▪ §fPersonnage §7: §9§lRin\n" +
                    "§f §b▪ §fVie §7: §c10§4❤\n" +
                    "§f §b▪ §fEffets §7: §cForce I\n" +
                    "§f \n" +
                    "§8│ §3§lPARTICULARITES\n" +
                    "§f §b▪ §fVous envoyez §b1,5x §fplus loin le §9Ballon§f.\n" +
                    "§f §b▪ §fVous mettez §e12 §fsecondes à réapparaitre.\n" +
                    "§f \n" +
                    "§8│ §3§lPOUVOIRS\n" +
                    "§f §b▪ §fCoup Franc §8(§b«§8)\n" +
                    "§f \n" +
                    "§8§l«§8§m---------------------------------------------------§8§l»");
            effectManager.setStrength(player, 20);
            player.setMaxHealth(20);
            player.getInventory().addItem(getCoupFrancItem());

            coupFrancAbility = new CoupFrancAbility();
            abilityManager.registerAbility(Role.Rin, Collections.singletonList(coupFrancAbility));

            new BukkitRunnable() {
                @Override
                public void run() {
                    ItemStack item = player.getItemInHand();
                    if (item.equals(getCoupFrancItem())) {
                        Blb.sendActionBar(player, "§9» §f§lCooldown §b(§f" + cooldownManager.getRemainingCooldown(player, coupFrancAbility) + "§b) §9«");
                    }
                }
            }.runTaskTimer(Blb.getInstance(), 0, 5);
        }
    }

    @EventHandler
    private void OnInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item != null && item.equals(getCoupFrancItem())) {
            if (cooldownManager.getRemainingCooldown(player, coupFrancAbility) == 0) {
                cooldownManager.startCooldown(player, coupFrancAbility);
                TitleAPI.sendTitle(player, 5, 50, 5, "§f§lImmobilisé", "");

                // Créer un ArmorStand invisible
                ArmorStand armorStand = (ArmorStand) player.getWorld().spawnEntity(player.getLocation(), EntityType.ARMOR_STAND);
                armorStand.setVisible(false); // Rendre l'ArmorStand invisible
                armorStand.setMaxHealth(2000);
                armorStand.setHealth(2000);
                armorStand.setGravity(false); // Empêcher la gravité
                armorStand.setPassenger(player); // Mettre le joueur sur l'ArmorStand

                // Empêcher le joueur de descendre
                player.setAllowFlight(true); // Permettre le vol pour éviter de descendre
                player.setFlying(true); // Mettre le joueur en mode vol

                player.sendMessage("§3│ §fVous venez d'utiliser §bCoup Franc§f.");

                // Créer un BukkitRunnable pour gérer la durée de l'immobilisation
                new BukkitRunnable() {
                    int duration = 3 * 20; // Durée de l'immobilisation en ticks (3 secondes)

                    @Override
                    public void run() {
                        if (duration <= 0) {
                            // Fin de l'immobilisation
                            player.leaveVehicle(); // Faire descendre le joueur de l'ArmorStand
                            armorStand.remove(); // Retirer l'ArmorStand
                            player.setAllowFlight(false); // Désactiver le vol
                            player.setFlying(false); // Désactiver le mode vol
                            cancel(); // Annuler le Runnable
                        }
                        duration--; // Décrémenter la durée
                    }
                }.runTaskTimer(Blb.getInstance(), 0, 1); // Exécuter chaque tick
            } else {
                player.sendMessage("§6┃ §fVous avez un §6cooldown §fde §e" + (long) cooldownManager.getRemainingCooldown(player, coupFrancAbility) / 1000 + " §fsur cette capacité.");
            }
        }
    }

    private ItemStack getCoupFrancItem() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.BLUE + "Coup Franc");
            List<String> lore = new ArrayList<>();
            lore.add("§3≡ §b§lCoup Franc");
            lore.add("§f");
            lore.add("§8┃ §fPermet de s'§bimmobiliser §fet pendant cette §edurée§f, d'envoyer §b2x §fplus loin\n" +
                    "le §9Ballon§f.");
            lore.add("§f");
            lore.add("§6◆ §fCooldown §7: §e15 secondes");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}
