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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
            player.sendMessage("§f \n" +
                    "§8§l«§8§m---------------------------------------------------§8§l»\n" +
                    "§f \n" +
                    "§8│ §3§lINFORMATIONS\n" +
                    "§f §b▪ §fPersonnage §7: §9§lChigiri\n" +
                    "§f §b▪ §fVie §7: §c8§4❤\n" +
                    "§f §b▪ §fEffets §7: §bVitesse II §fet §7Weakness I\n" +
                    "§f \n" +
                    "§8│ §3§lPARTICULARITES\n" +
                    "§f §b▪ §fVous mettez §e15 §fsecondes à réapparaitre.\n" +
                    "§f \n" +
                    "§8│ §3§lPOUVOIRS\n" +
                    "§f §b▪ §fSprint §8(§b«§8)\n" +
                    "§f \n" +
                    "§8§l«§8§m---------------------------------------------------§8§l»");
            effectManager.setSpeed(player, 40);
            effectManager.setWeakness(player, 20);
            player.setMaxHealth(16);
            player.getInventory().addItem(getSprintItem());

            sprintAbility = new SprintAbility();
            abilityManager.registerAbility(Role.Chigiri, Collections.singletonList(sprintAbility));

            new BukkitRunnable() {
                @Override
                public void run() {
                    ItemStack item = player.getItemInHand();
                    if (item.equals(getSprintItem())) {
                        Blb.sendActionBar(player, "§9» §f§lCooldown §b(§f" + cooldownManager.getRemainingCooldown(player, sprintAbility) + "§b) §9«");
                    }
                }
            }.runTaskTimer(Blb.getInstance(), 0, 5);
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
                        effectManager.setSpeed(player, 40);;
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 15*20, 1));
                    }
                }.runTaskLater(Blb.getInstance(), 5*20);
                player.sendMessage("§3│ §fVous venez d'utiliser §bSprint§f.");
            }
            else {
                player.sendMessage("§6┃ §fVous avez un §6cooldown §fde §e" + (long) cooldownManager.getRemainingCooldown(player, sprintAbility) / 1000 + " §fsur cette capacité.");
            }
        }
    }

    private ItemStack getSprintItem() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.BLUE + "Sprint");
            List<String> lore = new ArrayList<>();
            lore.add("§3≡ §b§lSprint");
            lore.add("§f");
            lore.add("§8┃ §fPermet d'obtenir l'effet §bVitesse II §fpendant §e5 §fsecondes. Une fois\n" +
                    "l'effet §aterminé§f, vous écopez de l'effet §5Slowness II §fdurant §e15 §fsecondes.");
            lore.add("§f");
            lore.add("§6◆ §fCooldown §7: §e20 secondes");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}
