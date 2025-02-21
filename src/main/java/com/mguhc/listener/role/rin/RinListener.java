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
                    "§f\n" +
                    "§8│ §3§lINFORMATIONS\n" +
                    "§f §b▪ §fPersonnage §7: §9§lRin\n" +
                    "§f §b▪ §fVie §7: §c10§4❤\n" +
                    "§f §b▪ §fEffets §7: §cForce I\n" +
                    "§f\n" +
                    "§8│ §3§lPARTICULARITES\n" +
                    "§f §b▪ §fVous envoyez §b1,5x §fplus loin le §9Ballon§f.\n" +
                    "§f §b▪ §fVous mettez §e12 §fsecondes à réapparaitre.\n" +
                    "§f\n" +
                    "§8│ §3§lPOUVOIRS\n" +
                    "§f §b▪ §fCoup Franc §8(§b«§8)\n" +
                    "§f\n" +
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
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 3*20, 255));
                player.sendMessage("§3│ §fVous venez d'utiliser §bCoup Franc§f.");
            }
            else {
                player.sendMessage("§6┃ §fVous avez un §6cooldown §fde §e" + (long) cooldownManager.getRemainingCooldown(player, coupFrancAbility) / 1000 + " §fsur cette capacité.");            }
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
