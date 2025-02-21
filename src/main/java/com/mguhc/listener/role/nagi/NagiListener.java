package com.mguhc.listener.role.nagi;

import com.mguhc.Blb;
import com.mguhc.events.RoleGiveEvent;
import com.mguhc.manager.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NagiListener implements Listener {

    private final RoleManager roleManager;
    private final TeamManager teamManager;
    private final PlayerManager playerManager;
    private final BallManager ballManager;
    private final EffectManager effectManager;
    private final AbilityManager abilityManager;
    private final CooldownManager cooldownManager;

    private DashAbility dashAbility;

    public NagiListener() {
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
        Player player = roleManager.getPlayerWithRole(Role.Nagi);
        if (player != null) {
            player.sendMessage("§f \n" +
                    "§8§l«§8§m---------------------------------------------------§8§l»\n" +
                    "§f\n" +
                    "§8│ §3§lINFORMATIONS\n" +
                    "§f §b▪ §fPersonnage §7: §9§lNagi\n" +
                    "§f §b▪ §fVie §7: §c10§4❤\n" +
                    "§f §b▪ §fEffets §7: §bVitesse I §fet §cForce I\n" +
                    "§f\n" +
                    "§8│ §3§lPARTICULARITES\n" +
                    "§f §b▪ §fVous ne perdez pas de §6nourriture§f.\n" +
                    "§f §b▪ §fVous mettez §e10 §fsecondes à réapparaitre.\n" +
                    "§f\n" +
                    "§8│ §3§lPOUVOIRS\n" +
                    "§f §b▪ §fDash §8(§b«§8)\n" +
                    "§f\n" +
                    "§8§l«§8§m---------------------------------------------------§8§l»");
            effectManager.setSpeed(player, 20);
            effectManager.setStrength(player, 20);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, Integer.MAX_VALUE, 255, false, false));
            player.setMaxHealth(20);
            player.getInventory().addItem(getDashItem());

            dashAbility = new DashAbility();
            abilityManager.registerAbility(Role.Nagi, Collections.singletonList(dashAbility));

            new BukkitRunnable() {
                @Override
                public void run() {
                    ItemStack item = player.getItemInHand();
                    if (item.equals(getDashItem())) {
                        Blb.sendActionBar(player, "§9» §f§lCooldown §b(§f" + cooldownManager.getRemainingCooldown(player, dashAbility) + "§b) §9«");
                    }
                }
            }.runTaskTimer(Blb.getInstance(), 0, 5);
        }
    }

    @EventHandler
    private void OnInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item != null &&
            item.equals(getDashItem())) {
            if (cooldownManager.getRemainingCooldown(player, dashAbility) == 0) {
                cooldownManager.startCooldown(player, dashAbility);
                launchPlayer(player, 1.5);
                player.sendMessage("§3│ §fVous venez d'utiliser §bDash§f.");
            }
            else {
                player.sendMessage("§6┃ §fVous avez un §6cooldown §fde §e" + (long) cooldownManager.getRemainingCooldown(player, dashAbility) / 1000 + " §fsur cette capacité.");
            }
        }
    }

    private void launchPlayer(Player player, double power) {
        // Obtenir la direction dans laquelle le joueur regarde
        Vector direction = player.getLocation().getDirection().normalize(); // Normaliser la direction

        // Appliquer la force au ballon
        player.setVelocity(direction.multiply(power)); // Multiplier la direction par la puissance
    }

    private ItemStack getDashItem() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.BLUE + "Dash");
            List<String> lore = new ArrayList<>();
            lore.add("§3≡ §b§lDash");
            lore.add("§f");
            lore.add("§8┃ §fPermet de se §7propulser §fd'une §bdizaine §fde blocs en avant.");
            lore.add("§f");
            lore.add("§6◆ §fCooldown §7: §e20 secondes");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}
