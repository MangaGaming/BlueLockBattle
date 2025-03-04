package com.mguhc.listener.role.shidou;

import com.mguhc.Blb;
import com.mguhc.events.RoleGiveEvent;
import com.mguhc.manager.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShidouListener implements Listener {

    private final RoleManager roleManager;
    private final TeamManager teamManager;
    private final PlayerManager playerManager;
    private final BallManager ballManager;
    private final EffectManager effectManager;
    private final AbilityManager abilityManager;
    private final CooldownManager cooldownManager;

    private DominationAbility dominationAbility;
    private PassTroughAbility passTroughAbility;

    public ShidouListener() {
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
        List<Player> players = roleManager.getPlayersWithRole(Role.Shidou);
        if (players != null) {
            for (Player player : players) {
                if (player != null) {
                    player.sendMessage("§f \n" +
                            "§8§l«§8§m---------------------------------------------------§8§l»\n" +
                            "§f \n" +
                            "§8│ §3§lINFORMATIONS\n" +
                            "§f §b▪ §fPersonnage §7: §9§lShidou\n" +
                            "§f §b▪ §fVie §7: §c10§4❤\n" +
                            "§f §b▪ §fEffets §7: §cForce I\n" +
                            "§f \n" +
                            "§8│ §3§lPARTICULARITES\n" +
                            "§f §b▪ §fVous ...\n" +
                            "§f §b▪ §fVous mettez §e10 §fsecondes à réapparaitre.\n" +
                            "§f \n" +
                            "§8│ §3§lPOUVOIRS\n" +
                            "§f §b▪ §fDomination §8(§b«§8)\n" +
                            "§f \n" +
                            "§8§l«§8§m---------------------------------------------------§8§l»");
                    effectManager.setStrength(player, 20);
                    player.setMaxHealth(20);
                    player.getInventory().addItem(getDominationItem());

                    dominationAbility = new DominationAbility();
                    passTroughAbility = new PassTroughAbility();
                    abilityManager.registerAbility(Role.Shidou, Arrays.asList(dominationAbility, passTroughAbility));

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            ItemStack item = player.getItemInHand();
                            if (item.equals(getDominationItem())) {
                                Blb.sendActionBar(player, "§9» §f§lCooldown §b(§f" + (long) cooldownManager.getRemainingCooldown(player, dominationAbility) / 1000 + "§b) §9« " + "§3| " + "§9» §f§lDribble Agressif §b(§f5%§b) §9«");
                            }
                            else {
                                Blb.sendActionBar(player, "§9» §f§lDribble Agressif §b(§f5%§b) §9«");
                            }
                        }
                    }.runTaskTimer(Blb.getInstance(), 0, 5);
                }
            }
        }
    }

    @EventHandler
    private void OnDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player damager = (Player) event.getDamager();
            Player victim = (Player) event.getEntity();
            if (roleManager.getRole(victim).equals(Role.Shidou) && teamManager.getTeam(victim) != teamManager.getTeam(damager)) {
                if (cooldownManager.getRemainingCooldown(victim, passTroughAbility) == 0) {
                    cooldownManager.startCooldown(victim, passTroughAbility);
                    event.setCancelled(true);
                    victim.playSound(damager.getLocation(),  Sound.LEVEL_UP, 1, 1);
                }
            }
        }
    }

    @EventHandler
    private void OnMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Role role = roleManager.getRole(player);
        if (role != null && role.equals(Role.Shidou)) {
            boolean hasTeamMemberAround = false;
            for (Entity e : player.getNearbyEntities(10, 10, 10)) {
                if (e instanceof Player) {
                    Player p = (Player) e;
                    if (teamManager.getTeam(player) == teamManager.getTeam(p)) {
                        hasTeamMemberAround = true;
                        break;
                    }
                }
            }
            if (hasTeamMemberAround) {
                effectManager.removeEffect(player, PotionEffectType.SPEED);
                effectManager.removeEffect(player, PotionEffectType.DAMAGE_RESISTANCE);
                effectManager.setWeakness(player, 20);
            }
            else {
                effectManager.removeEffect(player, PotionEffectType.WEAKNESS);
                effectManager.setSpeed(player, 20);
                effectManager.setResistance(player, 20);
            }
        }
    }

    @EventHandler
    private void OnInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item != null && item.equals(getDominationItem())) {
            if (cooldownManager.getRemainingCooldown(player, dominationAbility) == 0) {
                cooldownManager.startCooldown(player, dominationAbility);
                for (Entity e : player.getNearbyEntities(5, 5, 5)) {
                    if (e instanceof Player) {
                        Player p = (Player) e;
                        launchPlayer(p, -1.5);
                    }
                }
                player.sendMessage("§3│ §fVous venez d'utiliser §bDomination§f.");
            }
            else {
                player.sendMessage("§6┃ §fVous avez un §6cooldown §fde §e" + (long) cooldownManager.getRemainingCooldown(player, dominationAbility) / 1000 + "s §fsur cette capacité.");
            }
        }

    }

    private void launchPlayer(Player player, double power) {
        // Obtenir la direction dans laquelle le joueur regarde
        Vector direction = player.getLocation().getDirection().normalize(); // Normaliser la direction

        // Appliquer la force au ballon
        player.setVelocity(direction.multiply(power)); // Multiplier la direction par la puissance
    }

    private ItemStack getDominationItem() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.BLUE + "Domination");
            List<String> lore = new ArrayList<>();
            lore.add("§3≡ §b§lDomination");
            lore.add("§f");
            lore.add("§8┃ §fPermet de ...");
            lore.add("§f");
            lore.add("§6◆ §fCooldown §7: §e15 secondes");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}
