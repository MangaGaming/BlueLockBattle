package com.mguhc.listener.role.chigiri;

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

import java.util.Collections;

public class ChigiriListener implements Listener {

    private final RoleManager roleManager;
    private final TeamManager teamManager;
    private final PlayerManager playerManager;
    private final BallManager ballManager;
    private final EffectManager effectManager;
    private final AbilityManager abilityManager;
    private final CooldownManager cooldownManager;

    private SprintAbility sprintAbility;

    public ChigiriListener() {
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
        Player player = roleManager.getPlayerWithRole(Role.Chigiri);
        if (player != null) {
            effectManager.setSpeed(player, 40);
            effectManager.setWeakness(player, 20);
            player.setMaxHealth(16);
            player.getInventory().addItem(getSprintItem());

            sprintAbility = new SprintAbility();
            abilityManager.registerAbility(Role.Chigiri, Collections.singletonList(sprintAbility));
        }
    }

    @EventHandler
    private void OnInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item != null && item.equals(getSprintItem())) {
            if (cooldownManager.getRemainingCooldown(player, sprintAbility) == 0) {
                cooldownManager.startCooldown(player, sprintAbility);
                effectManager.setSpeed(player, 60);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        effectManager.removeEffect(player, PotionEffectType.SPEED);
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 15*20, 1));
                    }
                }.runTaskLater(Blb.getInstance(), 5*20);
            }
            else {
                player.sendMessage(ChatColor.RED + "Vous êtes en cooldown pour " + (long) cooldownManager.getRemainingCooldown(player, sprintAbility) / 1000 + "s");
            }
        }
    }

    private ItemStack getSprintItem() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.BLUE + "Sprint");
            item.setItemMeta(meta);
        }
        return item;
    }
}
