package com.mguhc.manager;

import com.connorlinfoot.titleapi.TitleAPI;
import com.mguhc.Blb;
import com.mguhc.events.StartGameEvent;
import net.minecraft.server.v1_8_R3.EntitySlime;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftSlime;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

public class BallManager implements Listener {

    private Slime ball; // Référence au ballon
    private final Map<TeamEnum, Integer> goalMap = new HashMap<>();

    public void spawnBall(Location location) {
        // Créer le slime pour représenter le ballon
        ball = (Slime) location.getWorld().spawnEntity(location, EntityType.SLIME);
        ball.setSize(3); // Taille du slime, ajustez pour le rendre plus grand
        ball.setCustomName("Ballon"); // Nommer le slime
        ball.setCustomNameVisible(true); // Rendre le nom visible
    }

    public void disableSlimeAI(Slime slime, Boolean b) {
        EntitySlime nmsSlime = ((CraftSlime) slime).getHandle();
        NBTTagCompound tag = new NBTTagCompound();
        nmsSlime.e(tag); // Récupérer les données de l'entité
        tag.setBoolean("NoAI", b); // Définir le tag NoAI à true
        nmsSlime.f(tag); // Appliquer les données modifiées à l'entité
    }

    public void launchBall(Player player, double power) {
        if (ball != null) {
            disableSlimeAI(ball, false);
            // Obtenir la direction dans laquelle le joueur regarde
            Vector direction = player.getLocation().getDirection().normalize(); // Normaliser la direction

            // Appliquer la force au ballon
            ball.setVelocity(direction.multiply(power)); // Multiplier la direction par la puissance
        }
    }

    @EventHandler
    public void onPlayerInteractWithBall(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Slime) {
            Slime clickedBall = (Slime) event.getRightClicked();
            Player player = event.getPlayer();

            // Vérifier si le slime est le ballon
            if (clickedBall.getCustomName() != null && clickedBall.getCustomName().equals("Ballon")) {
                // Détruire le ballon
                clickedBall.remove();
                ball = null;

                player.getInventory().addItem(getSlimeBall());
            }
        }
    }

    @EventHandler
    private void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item != null && item.equals(getSlimeBall())) {
            player.getInventory().remove(getSlimeBall());
            spawnBall(player.getLocation().add(0, 1, 0)); // Spawn le slime juste au-dessus du joueur
            if (Blb.getInstance().getRoleManager().getRole(player).equals(Role.Rin) &&
                player.getAllowFlight() &&
                player.isFlying()) {
                launchBall(player, 2*3.5);
            }
            else if (Blb.getInstance().getRoleManager().getRole(player).equals(Role.Rin)) {
                launchBall(player, 2*1.5);
            }
            else {
                launchBall(player, 2);
            }
        }
    }

    @EventHandler
    private void OnSlimeDamageToPlayer(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Slime) {
            Player player = (Player) event.getEntity();
            Slime slime = (Slime) event.getDamager();
            if (slime.equals(ball)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    private void OnSlimeDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Slime) {
            Slime slime = (Slime) event.getEntity();
            if (slime.equals(ball)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    private void OnStart(StartGameEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (ball != null) {
                    Location location = ball.getLocation();
                    if (location.getY() == 7) {
                        disableSlimeAI(ball, true);
                    }
                    if (location.getZ() < 1178) {
                        ball.remove();
                        addGoal(TeamEnum.BLEU);
                    }
                    else if (location.getZ() > 1308) {
                        ball.remove();
                        addGoal(TeamEnum.ROUGE);
                    }
                }
            }
        }.runTaskTimer(Blb.getInstance(), 0, 5);
    }

    private void addGoal(TeamEnum team) {
        goalMap.put(team, goalMap.getOrDefault(team, 0) + 1);
        for (Player player : Blb.getInstance().getPlayerManager().getPlayers()) {
            TitleAPI.sendTitle(player,10,3*20,10,"§6§l» §e§lBut §7: §l§9 " + getGoal(TeamEnum.BLEU) + " §7-§l§c " + getGoal(TeamEnum.ROUGE) + " §6§l«","§fPoint pour l'équipe §l " + team.name());
        }
        if (goalMap.getOrDefault(TeamEnum.BLEU, 0) >= 5) {
            Blb.getInstance().getGameManager().finishGame(TeamEnum.BLEU);
        }
        else if (goalMap.getOrDefault(TeamEnum.ROUGE, 0) >= 5) {
            Blb.getInstance().getGameManager().finishGame(TeamEnum.ROUGE);
        }
        spawnBall(new Location(Bukkit.getWorld("world"), 282, 7, 1243));
    }

    public int getGoal(TeamEnum team) {
        return goalMap.getOrDefault(team, 0);
    }

    public ItemStack getSlimeBall() {
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



