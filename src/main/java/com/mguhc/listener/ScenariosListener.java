package com.mguhc.listener;

import com.mguhc.Blb;
import com.mguhc.manager.TeamEnum;
import com.mguhc.manager.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class ScenariosListener implements Listener {

    private final TeamManager teamManager = Blb.getInstance().getTeamManager();

    @EventHandler
    private void OnPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        new BukkitRunnable() {
            @Override
            public void run() {
                block.setType(Material.AIR);
            }
        }.runTaskLater(Blb.getInstance(), 5*20);
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        int id = event.getBlock().getTypeId();
        if (id == 8 || id == 9 || id == 10 || id == 11) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void OnDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            for (ItemStack item : player.getInventory().getArmorContents()) {
                if (item != null && item.getType() != Material.AIR) {
                    // Rendre l'armure incassable
                    item.setDurability((short) 0); // Réinitialiser la durabilité
                    item.addUnsafeEnchantment(Enchantment.DURABILITY, 100); // Ajouter l'enchantement d'incassabilité
                }
            }
        }
    }

    @EventHandler
    private void OnChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        TeamEnum team = teamManager.getTeam(player);

        // Si le message contient "!", envoyer le message à tous les joueurs
        if (message.contains("!")) {
            message = message.replace("!", ""); // Enlever le "!" du message
            event.setCancelled(true); // Annuler l'événement pour éviter l'envoi normal du message
            if (team != null && team.equals(TeamEnum.BLEU)) {
                Bukkit.broadcastMessage("§9" + player.getName() + " §8» §f" + message);
            } else {
                Bukkit.broadcastMessage("§c" + player.getName() + " §8» §f" + message);
            }
        } else {
            // Sinon, envoyer le message uniquement aux membres de l'équipe
            if (team != null) {
                for (Player p : teamManager.getPlayersInTeam(team)) {
                    if (team.equals(TeamEnum.BLEU)) {
                        p.sendMessage("§9§l[Bleu] §9" + player.getName() + " §8» §f" + message);
                    } else {
                        p.sendMessage("§c§l[Rouge] §c" + player.getName() + " §8» §f" + message);
                    }
                }
                event.setCancelled(true); // Annuler l'événement pour éviter l'envoi normal du message
            }
        }
    }
}
