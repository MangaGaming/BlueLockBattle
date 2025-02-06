package com.mguhc.listener;

import com.mguhc.Blb;
import com.mguhc.manager.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerDeathListener implements Listener {
    private final RoleManager roleManager;
    private final TeamManager teamManager;
    private final PlayerManager playerManager;
    private final BallManager ballManager;
    private final EffectManager effectManager;
    private final GameManager gameManager;

    public PlayerDeathListener() {
        Blb blb = Blb.getInstance();
        roleManager = blb.getRoleManager();
        teamManager = blb.getTeamManager();
        playerManager = blb.getPlayerManager();
        ballManager = blb.getBallManager();
        effectManager = blb.getEffectManager();
        gameManager = blb.getGameManager();
    }

    @EventHandler
    private void OnDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (player.getHealth() - event.getDamage() <= 0 &&
                player.getLocation().getY() < 10 &&
                gameManager.getState().equals(State.PLAYING)) {
                event.setCancelled(true);
                if (player.getInventory().contains(getSlimeBall())) {
                    ballManager.spawnBall(player.getLocation().add(0, 1, 0));
                }
                Location deathLocation = new Location(player.getWorld(), 282, 39, 1243);
                player.teleport(deathLocation);
                switch (roleManager.getRole(player)) {
                    case Isagi:
                        endDeathTime(player, 5);
                        break;
                    case Nagi:
                    case Bachira:
                    case Baro:
                    case Shidou:
                        endDeathTime(player, 10);
                        break;
                    case Chigiri:
                        endDeathTime(player, 15);
                        break;
                    case Rin:
                    case Karasu:
                        endDeathTime(player, 12);
                        break;
                    case Reo:
                    case Otoya:
                        endDeathTime(player, 8);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private void endDeathTime(Player player, int delay) {
        player.sendMessage(ChatColor.RED + "Vous êtes mort vous devez attendre " + delay + " secondes");
        new BukkitRunnable() {
            @Override
            public void run() {
                player.getInventory().clear();
                player.getInventory().setArmorContents(null);
                gameManager.giveMeetupGear(player);
                gameManager.teleportToRandomLocation(player, teamManager.getTeam(player));
            }
        }.runTaskLater(Blb.getInstance(), delay * 20L);
    }

    private ItemStack getSlimeBall() {
        // Donner un slime ball au joueur
        ItemStack slimeBall = new ItemStack(Material.SLIME_BALL);
        ItemMeta meta = slimeBall.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + "Ball");
            slimeBall.setItemMeta(meta);
        }
        return slimeBall;
    }
}
