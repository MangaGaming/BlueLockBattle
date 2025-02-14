package com.mguhc.listener.role.rin;

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

import java.util.Collections;

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
            effectManager.setStrength(player, 20);
            player.setMaxHealth(20);
            player.getInventory().addItem(getCoupFrancItem());

            coupFrancAbility = new CoupFrancAbility();
            abilityManager.registerAbility(Role.Rin, Collections.singletonList(coupFrancAbility));
        }
    }

    @EventHandler
    private void OnInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item != null && item.equals(getCoupFrancItem())) {
            if (cooldownManager.getRemainingCooldown(player, coupFrancAbility) == 0) {
                cooldownManager.startCooldown(player, coupFrancAbility);
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 3*20, 255));
            }
            else {
                player.sendMessage(ChatColor.RED + "Vous êtes en cooldown pour " + (long) cooldownManager.getRemainingCooldown(player, coupFrancAbility) / 1000 + "s");
            }
        }
    }

    private ItemStack getCoupFrancItem() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.BLUE + "Coup Franc");
            item.setItemMeta(meta);
        }
        return item;
    }
}
