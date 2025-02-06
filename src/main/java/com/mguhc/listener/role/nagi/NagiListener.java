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
import org.bukkit.util.Vector;

import java.util.Collections;

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
            effectManager.setSpeed(player, 20);
            effectManager.setStrength(player, 20);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, Integer.MAX_VALUE, 255, false, false));
            player.setMaxHealth(20);
            player.getInventory().addItem(getDashItem());

            dashAbility = new DashAbility();
            abilityManager.registerAbility(Role.Nagi, Collections.singletonList(dashAbility));
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
            }
            else {
                player.sendMessage(ChatColor.RED + "Vous êtes en cooldown pour " + (long) cooldownManager.getRemainingCooldown(player, dashAbility) + "s");
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
            item.setItemMeta(meta);
        }
        return item;
    }
}
