package com.mguhc.listener.role.isagi;

import com.mguhc.Blb;
import com.mguhc.events.RoleGiveEvent;
import com.mguhc.manager.*;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class IsagiListener implements Listener {
    private final RoleManager roleManager;
    private final TeamManager teamManager;
    private final PlayerManager playerManager;
    private final BallManager ballManager;
    private final EffectManager effectManager;
    private final AbilityManager abilityManager;
    private final CooldownManager cooldownManager;

    private RetourneAbility retourneAbility;

    // Map pour stocker les ArmorStands par joueur
    private final Map<Player, EntityArmorStand> armorStands = new HashMap<>();

    public IsagiListener() {
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
        Player player = roleManager.getPlayerWithRole(Role.Isagi);
        if (player != null) {
            effectManager.setSpeed(player, 20);
            player.setMaxHealth(20);
            player.getInventory().addItem(getRetournerItem());
            player.sendMessage("§f \n" +
                    "§8§l«§8§m---------------------------------------------------§8§l»\n" +
                    "§f\n" +
                    "§8│ §3§lINFORMATIONS\n" +
                    "§f §b▪ §fPersonnage §7: §9§lIsagi\n" +
                    "§f §b▪ §fVie §7: §c10§4❤\n" +
                    "§f §b▪ §fEffets §7: §bVitesse I\n" +
                    "§f\n" +
                    "§8│ §3§lPARTICULARITES\n" +
                    "§f §b▪ §fVous voyez la §cvie §fau dessus des joueurs.\n" +
                    "§f §b▪ §fVous mettez §e5 §fsecondes à réapparaitre.\n" +
                    "§f\n" +
                    "§8│ §3§lPOUVOIRS\n" +
                    "§f §b▪ §fRetourné Acrobatique §8(§b«§8)\n" +
                    "§f\n" +
                    "§8§l«§8§m---------------------------------------------------§8§l»");
            retourneAbility = new RetourneAbility();
            abilityManager.registerAbility(Role.Isagi, Collections.singletonList(retourneAbility));
            startArmorStandTask();

        }
    }

    private void startArmorStandTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : playerManager.getPlayers()) {
                    if (!player.equals(roleManager.getPlayerWithRole(Role.Isagi))) {
                        // Supprimer l'ArmorStand existant si présent
                        if (armorStands.containsKey(player)) {
                            EntityArmorStand existingStand = armorStands.get(player);
                            // Envoyer le paquet de destruction pour l'ArmorStand
                            PacketPlayOutEntityDestroy packetDestroy = new PacketPlayOutEntityDestroy(existingStand.getId());
                            ((CraftPlayer) roleManager.getPlayerWithRole(Role.Isagi)).getHandle().playerConnection.sendPacket(packetDestroy);
                            armorStands.remove(player); // Retirer l'ArmorStand de la map
                        }

                        // Créer un nouvel ArmorStand NMS
                        EntityArmorStand armorStand = getEntityArmorStand(player);

                        // Envoyer le paquet de spawn à tous les joueurs
                        PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving(armorStand);
                        ((CraftPlayer) roleManager.getPlayerWithRole(Role.Isagi)).getHandle().playerConnection.sendPacket(packet);

                        // Stocker le nouvel ArmorStand dans la map
                        armorStands.put(player, armorStand);
                    }
                }
            }
        }.runTaskTimer(Blb.getInstance(), 0, 1);
    }

    private static EntityArmorStand getEntityArmorStand(Player player) {
        WorldServer worldServer = ((CraftWorld) player.getWorld()).getHandle();
        EntityArmorStand armorStand = new EntityArmorStand(worldServer);

        // Définir la position et les propriétés de l'ArmorStand
        armorStand.setLocation(player.getLocation().getX(), player.getLocation().getY() + 1, player.getLocation().getZ(), 0, 0);
        armorStand.setCustomName(String.valueOf((long) player.getHealth()) + ChatColor.RED + "❤");
        armorStand.setCustomNameVisible(true);
        armorStand.setSmall(true);
        armorStand.setInvisible(true); // Rendre l'ArmorStand invisible
        return armorStand;
    }

    @EventHandler
    private void OnInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item != null && item.equals(getRetournerItem())) {
            if (cooldownManager.getRemainingCooldown(player, retourneAbility) == 0) {
                if (getTargetPlayer(player, 5) != null) {
                    cooldownManager.startCooldown(player, retourneAbility);
                    player.teleport(getTargetPlayer(player, 5));
                }
                else {
                    player.sendMessage(ChatColor.RED + "Vous ne visez personne");
                }
            }
            else {
                player.sendMessage(ChatColor.RED + "Vous êtes en cooldown pour " + (long) cooldownManager.getRemainingCooldown(player, retourneAbility) / 1000 + "s");
            }
        }
    }

    private Player getTargetPlayer(Player player, double maxDistance) {
        // Get the player's eye location and direction
        Location eyeLocation = player.getEyeLocation();
        Vector direction = eyeLocation.getDirection().normalize();

        // Get nearby entities within the specified distance
        List<Entity> nearbyEntities = player.getNearbyEntities(maxDistance, maxDistance, maxDistance);

        for (Entity entity : nearbyEntities) {
            if (entity instanceof Player && entity != player) {
                // Check if the entity is in the line of sight
                Vector toEntity = entity.getLocation().toVector().subtract(eyeLocation.toVector()).normalize();
                double dotProduct = direction.dot(toEntity);

                // Check if the entity is within the player's line of sight
                if (dotProduct > 0.9) {
                    return (Player) entity;
                }
            }
        }
        return null; // No target player found
    }

    private ItemStack getRetournerItem() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.BLUE + "Retourné Acrobatique");
            List<String> lore = new ArrayList<>();
            lore.add("§3≡ §b§lRetourné Acrobatique");
            lore.add("§f");
            lore.add("§fEn §6visant §fun joueur, permet de se §atéléporter §fdans son §7dos§f.");
            lore.add("§f");
            lore.add("§6◆ §fCooldown §7: §e10 secondes");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}