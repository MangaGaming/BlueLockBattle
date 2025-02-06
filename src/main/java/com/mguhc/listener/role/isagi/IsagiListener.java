package com.mguhc.listener.role.isagi;

import com.mguhc.Blb;
import com.mguhc.events.RoleGiveEvent;
import com.mguhc.manager.*;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
            retourneAbility = new RetourneAbility();
            abilityManager.registerAbility(Role.Isagi, Collections.singletonList(retourneAbility));

        }
    }

    @EventHandler
    private void OnMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Player isagiPlayer = roleManager.getPlayerWithRole(Role.Isagi);

        if (isagiPlayer != null && !roleManager.getRole(player).equals(Role.Isagi) &&
                player.getLocation().distance(isagiPlayer.getLocation()) <= 10) {

            // Supprimer l'ArmorStand existant si présent
            if (armorStands.containsKey(player)) {
                EntityArmorStand existingStand = armorStands.get(player);
                // Envoyer le paquet de destruction pour l'ArmorStand
                PacketPlayOutEntityDestroy packetDestroy = new PacketPlayOutEntityDestroy(existingStand.getId());
                for (Player onlinePlayer : player.getServer().getOnlinePlayers()) {
                    ((CraftPlayer) onlinePlayer).getHandle().playerConnection.sendPacket(packetDestroy);
                }
                armorStands.remove(player); // Retirer l'ArmorStand de la map
            }

            // Créer un nouvel ArmorStand NMS
            EntityArmorStand armorStand = getEntityArmorStand(player);

            // Envoyer le paquet de spawn à tous les joueurs
            PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving(armorStand);
            for (Player onlinePlayer : player.getServer().getOnlinePlayers()) {
                ((CraftPlayer) onlinePlayer).getHandle().playerConnection.sendPacket(packet);
            }

            // Stocker le nouvel ArmorStand dans la map
            armorStands.put(player, armorStand);
        }
    }

    private static EntityArmorStand getEntityArmorStand(Player player) {
        WorldServer worldServer = ((CraftWorld) player.getWorld()).getHandle();
        EntityArmorStand armorStand = new EntityArmorStand(worldServer);

        // Définir la position et les propriétés de l'ArmorStand
        armorStand.setLocation(player.getLocation().getX(), player.getLocation().getY() + 1, player.getLocation().getZ(), 0, 0);
        armorStand.setCustomName(ChatColor.RED + String.valueOf(player.getHealth()) + "❤");
        armorStand.setCustomNameVisible(true);
        armorStand.setGravity(false); // Rendre l'ArmorStand sans graviter
        armorStand.setSmall(true); // Optionnel : rendre l'ArmorStand plus petit
        armorStand.setInvisible(true); // Rendre l'ArmorStand invisible
        return armorStand;
    }

    @EventHandler
    private void OnInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item != null && item.equals(getRetournerItem())) {

        }
    }

    private ItemStack getRetournerItem() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.BLUE + "Retourné Acrobatique");
            item.setItemMeta(meta);
        }
        return item;
    }
}