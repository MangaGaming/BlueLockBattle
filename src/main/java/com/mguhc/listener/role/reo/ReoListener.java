package com.mguhc.listener.role.reo;

import com.mguhc.Blb;
import com.mguhc.events.RoleGiveEvent;
import com.mguhc.manager.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;

import java.util.Collections;

public class ReoListener implements Listener {

    private final RoleManager roleManager;
    private final TeamManager teamManager;
    private final PlayerManager playerManager;
    private final BallManager ballManager;
    private final EffectManager effectManager;
    private final AbilityManager abilityManager;
    private final CooldownManager cooldownManager;

    private TeleportationAbility teleportationAbility;
    private Player nagi;

    public ReoListener() {
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
        Player player = roleManager.getPlayerWithRole(Role.Reo);
        if (player != null) {
            player.setMaxHealth(20);
            player.getInventory().addItem(getTeleportationItem());

            teleportationAbility = new TeleportationAbility();
            abilityManager.registerAbility(Role.Reo, Collections.singletonList(teleportationAbility));
            
            Player p = roleManager.getPlayerWithRole(Role.Nagi);
            if (p != null && teamManager.getTeam(p) == teamManager.getTeam(player)) {
                nagi = p;
            }
        }
    }

    @EventHandler
    private void OnMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Role role = roleManager.getRole(player);
        if (role != null && role.equals(Role.Reo)) {
            boolean hasNagiAround = false;
            for (Entity e : player.getNearbyEntities(7, 7, 7)) {
                if (e instanceof Player) {
                    Player p = (Player) e;
                    if (roleManager.getRole(p).equals(Role.Nagi) && teamManager.getTeam(p) == teamManager.getTeam(player)) {
                        hasNagiAround = true;
                        break;
                    }
                }
            }
            if (hasNagiAround) {
                effectManager.removeEffect(player, PotionEffectType.WEAKNESS);
                effectManager.setResistance(player, 20);
            }
            else {
                effectManager.removeEffect(player, PotionEffectType.DAMAGE_RESISTANCE);
                effectManager.setWeakness(player, 20);
            }
        }
    }

    @EventHandler
    private void OnInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item != null && item.equals(getTeleportationItem())) {
            if (cooldownManager.getRemainingCooldown(player, teleportationAbility) == 0) {
                if (nagi != null &&
                    nagi.getLocation().getY() < 20) {
                    cooldownManager.startCooldown(player, teleportationAbility);
                    player.teleport(nagi);
                }
                else {
                    player.sendMessage(ChatColor.RED + "Nagi non trouvé ou en cooldown de mort");
                }
            }
            else {
                player.sendMessage(ChatColor.RED + "Vous êtes en cooldown pour " + (long) cooldownManager.getRemainingCooldown(player, teleportationAbility) / 1000 + "s");
            }
        }
    }

    private ItemStack getTeleportationItem() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.BLUE + "Téléportation");
            item.setItemMeta(meta);
        }
        return item;
    }
}
