package com.mguhc.listener.role.otaya;

import com.mguhc.Blb;
import com.mguhc.events.RoleGiveEvent;
import com.mguhc.manager.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.Random;

public class OtayaListener implements Listener {

    private final RoleManager roleManager;
    private final TeamManager teamManager;
    private final PlayerManager playerManager;
    private final BallManager ballManager;
    private final EffectManager effectManager;
    private final AbilityManager abilityManager;
    private final CooldownManager cooldownManager;

    private FantomeAbility fantomeAbility;

    public OtayaListener() {
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
        Player player = roleManager.getPlayerWithRole(Role.Otoya);
        if (player != null) {
            player.setMaxHealth(20);
            player.getInventory().addItem(getFantomeItem());

            fantomeAbility = new FantomeAbility();
            abilityManager.registerAbility(Role.Otoya, Collections.singletonList(fantomeAbility));
        }
    }

    @EventHandler
    private void OnDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player damager = (Player) event.getDamager();
            Player victim = (Player) event.getEntity();
            Role role = roleManager.getRole(damager);
            if (role != null && role.equals(Role.Otoya) && teamManager.getTeam(damager) != teamManager.getTeam(victim)) {
                Random random = new Random();
                int randomNumber = random.nextInt(100);
                if (randomNumber <= 5) {
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 5*20, 0));
                }
            }
        }
    }

    @EventHandler
    private void OnInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item != null && item.equals(getFantomeItem())) {
            if (cooldownManager.getRemainingCooldown(player, fantomeAbility) == 0) {
                cooldownManager.startCooldown(player, fantomeAbility);
                effectManager.setSpeed(player, 40);
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20*10, 0));
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        effectManager.removeEffect(player, PotionEffectType.SPEED);
                    }
                }.runTaskLater(Blb.getInstance(), 10*20);
            }
            else {
                player.sendMessage(ChatColor.RED + "Vous êtes en cooldown pour " + (long) cooldownManager.getRemainingCooldown(player, fantomeAbility) / 1000 + "s");
            }
        }
    }


    private ItemStack getFantomeItem() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.BLUE + "Fantôme");
            item.setItemMeta(meta);
        }
        return item;
    }
}
