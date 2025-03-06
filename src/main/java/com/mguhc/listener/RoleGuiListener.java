package com.mguhc.listener;

import com.mguhc.Blb;
import com.mguhc.events.RoleGiveEvent; // Assurez-vous d'importer votre événement
import com.mguhc.events.StartGameEvent;
import com.mguhc.manager.*;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.*;

public class RoleGuiListener implements Listener {

    private final RoleManager roleManager;
    private final PlayerManager playerManager;
    private boolean isChoosing;
    Map<Player, Role> chosenRole = new HashMap<>(); // Changer pour stocker un seul rôle par joueur

    public RoleGuiListener() {
        this.roleManager = Blb.getInstance().getRoleManager();
        this.playerManager = Blb.getInstance().getPlayerManager();
    }

    @EventHandler
    private void onStart(StartGameEvent event) throws NoSuchFieldException, IllegalAccessException {
        // Ouvrir l'inventaire de sélection de rôle pour tous les joueurs
        isChoosing = true;
        for (Player player : playerManager.getPlayers()) {
            openRoleSelectionInventory(player);
        }

        // Créer un BukkitRunnable pour vérifier les rôles
        new BukkitRunnable() {
            @Override
            public void run() {
                isChoosing = false;
                for (Map.Entry<Player, Role> entry : chosenRole.entrySet()) {
                    Player player = entry.getKey();
                    Role role = entry.getValue();
                    roleManager.setRole(player, role); // Attribuer le rôle au joueur
                }
                for (Player player : playerManager.getPlayers()) {
                    player.closeInventory();
                    if (roleManager.getRole(player) == null) {
                        Random random = new Random();
                        Role[] roles = Role.values();
                        int randomIndex = random.nextInt(roles.length);
                        Role role = roles[randomIndex];
                        roleManager.setRole(player, role);
                    }
                }
                Bukkit.getPluginManager().callEvent(new RoleGiveEvent());
            }
        }.runTaskLater(Blb.getInstance(), 30 * 20);
    }

    private void openRoleSelectionInventory(Player player) throws NoSuchFieldException, IllegalAccessException {
        Inventory roleInventory = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Choisir un Rôle");

        // Items de décoration
        ItemStack glassAquaItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 3);
        ItemMeta itemMeta = glassAquaItem.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName("§f");
            glassAquaItem.setItemMeta(itemMeta);
        }

            roleInventory.setItem(0, getGlassItem());
            roleInventory.setItem(1, getGlassItem());
            roleInventory.setItem(9, getGlassItem());
            roleInventory.setItem(36, getGlassItem());
            roleInventory.setItem(45, getGlassItem());
            roleInventory.setItem(46, getGlassItem());
            roleInventory.setItem(7, getGlassItem());
            roleInventory.setItem(8, getGlassItem());
            roleInventory.setItem(17, getGlassItem());
            roleInventory.setItem(52, getGlassItem());
            roleInventory.setItem(53, getGlassItem());
            roleInventory.setItem(44, getGlassItem());

            roleInventory.setItem(3, glassAquaItem);
            roleInventory.setItem(5, glassAquaItem);

        roleInventory.setItem( 4, getSoccerBall());

        roleInventory.setItem(49, getUnreadyItem());

        // Ajouter tous les rôles de l'énumération Role aux emplacements spécifiques
        int[] roleSlots = {19, 20, 21, 22, 23, 24, 25, 28, 29, 30};
        for (int i = 0; i < roleSlots.length; i++) {
            if (i < Role.values().length) { // Assurez-vous de ne pas dépasser le nombre de rôles
                Role role = Role.values()[i];
                ItemStack roleItem = new ItemStack(Material.INK_SACK, 1, (short) 10); // Utiliser un item pour représenter le rôle
                ItemMeta meta = roleItem.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName("§b§9" + role.name());
                    roleItem.setItemMeta(meta);
                }
                roleInventory.setItem(roleSlots[i], roleItem); // Ajouter l'item de rôle à l'emplacement spécifié
            }
        }

        player.openInventory(roleInventory);
    }

    @EventHandler
    private void onRoleSelect(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(ChatColor.GOLD + "Choisir un Rôle")) {
            event.setCancelled(true); // Annuler l'événement pour éviter de déplacer les items

            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();

            if (clickedItem != null && clickedItem.getType() == Material.INK_SACK) {
                String roleName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
                Role selectedRole = Role.valueOf(roleName); // Convertir le nom en énumération Role

                // Vérifier si le joueur a déjà un rôle
                if (!chosenRole.containsKey(player)) {
                    // Attribuer le rôle au joueur
                    chosenRole.put(player, selectedRole); // Mettre à jour la carte
                    player.sendMessage(ChatColor.GREEN + "Vous avez choisi le rôle : " + roleName + " !");
                } else {
                    player.sendMessage(ChatColor.RED + "Vous avez déjà un rôle !");
                }
            }
        }
    }

    @EventHandler
    private void OnReady(InventoryClickEvent event) throws NoSuchFieldException, IllegalAccessException {
        if (event.getView().getTitle().equals(ChatColor.GOLD + "Choisir un Rôle")) {
            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();
            Inventory inventory = event.getInventory();
            if (clickedItem.equals(getReadyItem())) {
                player.sendMessage("§cVous n'êtes maintenant plus prêt");
                inventory.remove(clickedItem);
                inventory.setItem(49, getUnreadyItem());
            } else if (clickedItem.equals(getUnreadyItem())) {
                player.sendMessage("§aVous êtes maintenant prêt");
                inventory.remove(clickedItem);
                inventory.setItem(49, getReadyItem());
            }
        }
    }

    @EventHandler
    private void onInventoryClose(InventoryCloseEvent event) throws NoSuchFieldException, IllegalAccessException {
        if (isChoosing) {
            Player player = (Player) event.getPlayer();
            openRoleSelectionInventory(player);
        }
    }

    private ItemStack getGlassItem() {
        ItemStack item = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 11);
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName("§f");
            item.setItemMeta(itemMeta);
        }
        return item;
    }

    private ItemStack getSoccerBall() throws NoSuchFieldException, IllegalAccessException {
        ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (byte) SkullType.PLAYER.ordinal());
        SkullMeta itemMeta = (SkullMeta) item.getItemMeta();
        itemMeta.setDisplayName("§9§lBlueLock Battle");

        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGI3NDc5ZjRkMjE4YmIzMjU2ZTI2Y2Q0ZGZhMDhjY2E1MGFmNTc4MmNjMmJiYmRmMDY3YzIxN2Q4MzQyZDN kNyJ9fX0="));
        Field field;
        field = itemMeta.getClass().getDeclaredField("profile");
        field.setAccessible(true);
        field.set(itemMeta, profile);
        item.setItemMeta(itemMeta);

        return item;
    }

    private ItemStack getReadyItem() throws NoSuchFieldException, IllegalAccessException {
        ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (byte) SkullType.PLAYER.ordinal());
        SkullMeta itemMeta = (SkullMeta) item.getItemMeta();
        itemMeta.setDisplayName("§a§lPrêt");

        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjFlOTc0YTI2MDhiZDZlZTU3ZjMzNDg1NjQ1ZGQ5MjJkMTZiNGEzOTc0NGViYWI0NzUzZjRkZWI0ZWY3ODIifX19"));
        Field field;
        field = itemMeta.getClass().getDeclaredField("profile");
        field.setAccessible(true);
        field.set(itemMeta, profile);
        item.setItemMeta(itemMeta);

        return item;
    }

    private ItemStack getUnreadyItem() throws NoSuchFieldException, IllegalAccessException {
        ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (byte) SkullType.PLAYER.ordinal());
        SkullMeta itemMeta = (SkullMeta) item.getItemMeta();
        itemMeta.setDisplayName("§c§lPas Prêt");

        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2U2MjMyMTFiZGE1ZGQ1YWI5ZTc2MDMwZjg2YjFjNDczMGI5ODg3MjZlZWY2YTNhYjI4YWExYzFmN2Q4NTAifX19"));
        Field field;
        field = itemMeta.getClass().getDeclaredField("profile");
        field.setAccessible(true);
        field.set(itemMeta, profile);
        item.setItemMeta(itemMeta);

        return item;
    }
}