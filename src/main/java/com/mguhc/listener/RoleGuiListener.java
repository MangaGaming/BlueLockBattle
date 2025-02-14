package com.mguhc.listener;

import com.mguhc.Blb;
import com.mguhc.events.RoleGiveEvent; // Assurez-vous d'importer votre événement
import com.mguhc.events.StartGameEvent;
import com.mguhc.manager.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class RoleGuiListener implements Listener {

    private final RoleManager roleManager;
    private final PlayerManager playerManager;

    public RoleGuiListener() {
        this.roleManager = Blb.getInstance().getRoleManager();
        this.playerManager = Blb.getInstance().getPlayerManager();
    }

    @EventHandler
    private void onStart(StartGameEvent event) {
        // Ouvrir l'inventaire de sélection de rôle pour tous les joueurs
        for (Player player : playerManager.getPlayers()) {
            openRoleSelectionInventory(player);
        }

        // Créer un BukkitRunnable pour vérifier les rôles
        new BukkitRunnable() {
            @Override
            public void run() {
                boolean allPlayersHaveRoles = true;

                for (Player player : playerManager.getPlayers()) {
                    if (roleManager.getRole(player) == null) {
                        allPlayersHaveRoles = false;
                        break; // Un joueur n'a pas de rôle, sortir de la boucle
                    }
                }

                if (allPlayersHaveRoles) {
                    // Tous les joueurs ont un rôle, appeler l'événement RoleGiveEvent
                    Bukkit.getPluginManager().callEvent(new RoleGiveEvent());
                    cancel(); // Annuler le runnable
                }
            }
        }.runTaskTimer(Blb.getInstance(), 0, 20); // Vérifier toutes les secondes (20 ticks)
    }

    private void openRoleSelectionInventory(Player player) {
        Inventory roleInventory = Bukkit.createInventory(null, 27, ChatColor.GOLD + "Choisir un Rôle");

        // Ajouter tous les rôles de l'énumération Role à l'inventaire
        for (Role role : Role.values()) {
            ItemStack roleItem = new ItemStack(Material.PAPER); // Utiliser un item pour représenter le rôle
            ItemMeta meta = roleItem.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.BLUE + role.name());
                roleItem.setItemMeta(meta);
            }
            roleInventory.addItem(roleItem);
        }

        player.openInventory(roleInventory);
    }

    @EventHandler
    private void onRoleSelect(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(ChatColor.GOLD + "Choisir un Rôle")) {
            event.setCancelled(true); // Annuler l'événement pour éviter de déplacer les items

            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();

            if (clickedItem != null && clickedItem.getType() == Material.PAPER) {
                String roleName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
                Role selectedRole = Role.valueOf(roleName); // Convertir le nom en énumération Role

                // Vérifier si le rôle est déjà pris
                TeamManager teamManager = Blb.getInstance().getTeamManager();
                Player playerWithRole = roleManager.getPlayerWithRole(selectedRole);

                if (playerWithRole == null) {
                    // Si aucun joueur n'a ce rôle, attribuer le rôle au joueur
                    roleManager.setRole(player, selectedRole);
                    player.sendMessage(ChatColor.GREEN + "Vous avez choisi le rôle : " + roleName + " !");
                    player.closeInventory();
                } else {
                    // Vérifier si le joueur avec le rôle appartient à la même équipe
                    if (teamManager.getTeam(playerWithRole).equals(teamManager.getTeam(player))) {
                        player.sendMessage(ChatColor.RED + "Ce rôle est déjà pris par un autre joueur de ton équipe !");
                    } else {
                        // Si le joueur avec le rôle est dans une autre équipe, vous pouvez attribuer le rôle
                        roleManager.setRole(player, selectedRole);
                        player.sendMessage(ChatColor.GREEN + "Vous avez choisi le rôle : " + roleName + " !");
                        player.closeInventory();
                    }
                }
            }
        }
    }
}