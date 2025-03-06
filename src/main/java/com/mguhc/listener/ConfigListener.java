package com.mguhc.listener;

import com.mguhc.Blb;
import com.mguhc.manager.*;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ConfigListener implements Listener {

    private final RoleManager roleManager;
    private final TeamManager teamManager;
    private final GameManager gameManager;
    private final PlayerManager playerManager;
    private final PermissionManager permissionManager;

    public ConfigListener() {
        Blb blb = Blb.getInstance();
        roleManager = blb.getRoleManager();
        teamManager = blb.getTeamManager();
        gameManager = blb.getGameManager();
        playerManager = blb.getPlayerManager();
        permissionManager = blb.getPermissionManager();
    }

    @EventHandler
    private void OnJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (gameManager.getState().equals(State.WAITING)) {
            playerManager.addPlayer(player);
            event.setJoinMessage("§f \n§a│ §fLe joueur §a§l" + player.getName() + " §fvient de rejoindre. §3(§f" + playerManager.getPlayers().size() + "§b/§f40§3)");
            sendClickableMessage(player);
            player.setHealth(player.getMaxHealth());
            player.setSaturation(20f);
            player.teleport(new Location(Bukkit.getWorld("world"), 282, 7, 1243));
            Blb.clearAll(player);
            if (permissionManager.hasPermission(player, "blb.host") || player.isOp()) {
                player.getInventory().setItem(4, getConfigItem());
            }
            player.getInventory().addItem(getTeamItem());
        }
        else {
            player.sendMessage("§cLa partie est déja en cours");
            player.setGameMode(GameMode.SPECTATOR);
        }
    }

    @EventHandler
    private void OnQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (player != null && playerManager.getPlayers().contains(player)) {
            playerManager.removePlayer(player);
            event.setQuitMessage("§c│ §fLe joueur §c§l" + player.getName() + " §fvient de quiter. §3(§f" + playerManager.getPlayers().size() + "§b/§f40§3)");
        }
    }

    public static void sendClickableMessage(Player player) {
        // Créer le message
        TextComponent message = new TextComponent("§f\n§f\n§3§l» §f§lVerse Studio §3● §fBlueLock Battle\n§f\n");

        // Ajouter le texte pour le serveur professionnel
        TextComponent professional = new TextComponent("§3│ §fNotre serveur de §eprofessionnel§f. §b(§fcliquez§b)");
        professional.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/versestudio"));
        message.addExtra(professional);

        // Ajouter un saut de ligne
        message.addExtra("\n");

        // Ajouter le texte pour le serveur communautaire
        TextComponent community = new TextComponent("§3│ §fNotre serveur de §ccommunautaire§f. §b(§fcliquez§b)");
        community.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/versegame"));
        message.addExtra(community);

        // Envoyer le message au joueur
        player.spigot().sendMessage(message);
    }

    @EventHandler
    private void OnInteract(PlayerInteractEvent event) throws NoSuchFieldException, IllegalAccessException {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }
        if (item.equals(getConfigItem())) {
            openConfigInventory(player);
        } else if (item.equals(getTeamItem())) {
            event.setCancelled(true);
            openTeamSelectionInventory(player);
        } else if (item.equals(getStopItem())) {
            gameManager.cancelStart();
        }
    }

    private void openTeamSelectionInventory(Player player) {
        Inventory teamInventory = Bukkit.createInventory(null, 27, ChatColor.BLUE + "Choisir une Équipe");

        // Ajouter l'équipe Rouge
        ItemStack redTeamItem = new ItemStack(Material.BANNER, 1, (short) 1);
        ItemMeta redMeta = redTeamItem.getItemMeta();
        if (redMeta != null) {
            redMeta.setDisplayName(ChatColor.RED + "§lEquipe Rouge");
            Set<Player> playersInTeam = teamManager.getPlayersInTeam(TeamEnum.ROUGE);
            if (playersInTeam != null) {
                List<String> lore = new ArrayList<>();
                for (Player p : playersInTeam) {
                    lore.add("§8-§f" + p.getName());
                }
                redMeta.setLore(lore);
            }
            redTeamItem.setItemMeta(redMeta);
        }
        teamInventory.setItem(12, redTeamItem);

        // Ajouter l'équipe Bleue
        ItemStack blueTeamItem = new ItemStack(Material.BANNER, 1, (short) 4);
        ItemMeta blueMeta = blueTeamItem.getItemMeta();
        if (blueMeta != null) {
            blueMeta.setDisplayName(ChatColor.BLUE + "§lEquipe Bleue");
            Set<Player> playersInTeam = teamManager.getPlayersInTeam(TeamEnum.BLEU);
            if (playersInTeam != null) {
                List<String> lore = new ArrayList<>();
                for (Player p : playersInTeam) {
                    lore.add("§8-§f" + p.getName());
                }
                blueMeta.setLore(lore);
            }
            blueTeamItem.setItemMeta(blueMeta);
        }
        teamInventory.setItem(14, blueTeamItem);

        teamInventory.setItem(0, getGlassItem());
        teamInventory.setItem(9, getGlassItem());
        teamInventory.setItem(18, getGlassItem());
        teamInventory.setItem(8, getGlassItem());
        teamInventory.setItem(17, getGlassItem());
        teamInventory.setItem(26, getGlassItem());

        player.openInventory(teamInventory);
    }

    @EventHandler
    private void OnInventoryClick(InventoryClickEvent event) throws NoSuchFieldException, IllegalAccessException {
        if (event.getView().getTitle().equals(ChatColor.GOLD + "Configuration")) {
            event.setCancelled(true); // Annuler l'événement pour éviter de déplacer les items

            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();

            if (clickedItem != null) {
                if (clickedItem.equals(getModItem())) {
                    // Ouvrir l'inventaire des Mods
                    openModInventory(player);
                } else if (clickedItem.equals(getStartItem())) {
                    player.sendMessage(ChatColor.GREEN + "Partie lancée");
                    player.closeInventory();
                    player.getInventory().clear();
                    player.getInventory().setItem(4, getStopItem());
                    gameManager.startGame();
                } else if (clickedItem.equals(getHostItem())) {
                    // Ouvrir l'inventaire des joueurs
                    openHostInventory(player);
                }
            }
        } else if (event.getView().getTitle().equals(ChatColor.BLUE + "Choisir une Équipe")) {
            event.setCancelled(true); // Annuler l'événement pour éviter de déplacer les items

            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();

            if (clickedItem != null) {
                if (clickedItem.getType() == Material.BANNER && clickedItem.getDurability() == 1) {
                    teamManager.removePlayer(player);
                    teamManager.addPlayer(player, TeamEnum.ROUGE);
                    player.sendMessage(ChatColor.RED + "Vous avez rejoint l'équipe Rouge !");
                    player.closeInventory(); // Fermer l'inventaire après la sélection
                } else if (clickedItem.getType() == Material.BANNER && clickedItem.getDurability() == 4) {
                    teamManager.removePlayer(player);
                    teamManager.addPlayer(player, TeamEnum.BLEU);
                    player.sendMessage(ChatColor.BLUE + "Vous avez rejoint l'équipe Bleue !");
                    player.closeInventory(); // Fermer l'inventaire après la sélection
                }
            }
        }
    }

    private void openConfigInventory(Player player) throws NoSuchFieldException, IllegalAccessException {
        Inventory configInventory = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Configuration");

        configInventory.setItem(0, getGlassItem());
        configInventory.setItem(1, getGlassItem());
        configInventory.setItem(9, getGlassItem());
        configInventory.setItem(36, getGlassItem());
        configInventory.setItem(45, getGlassItem());
        configInventory.setItem(46, getGlassItem());
        configInventory.setItem(7, getGlassItem());
        configInventory.setItem(8, getGlassItem());
        configInventory.setItem(17, getGlassItem());
        configInventory.setItem(52, getGlassItem());
        configInventory.setItem(53, getGlassItem());
        configInventory.setItem(44, getGlassItem());

        configInventory.setItem(20, getModItem());

        configInventory.setItem(24, getHostItem());

        configInventory.setItem(40, getStartItem());

        player.openInventory(configInventory);
    }

    private void openHostInventory(Player player) {
        // Créer un inventaire de 54 slots
        Inventory hostInventory = Bukkit.createInventory(null, 54, ChatColor.GREEN + "Sélectionner un Host");

        // Récupérer tous les joueurs en ligne
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            // Créer un item pour chaque joueur
            ItemStack playerItem = new ItemStack(Material.SKULL_ITEM, 1, (short) 3); // Utiliser une tête de joueur
            ItemMeta meta = playerItem.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(onlinePlayer.getName()); // Nom du joueur
                List<String> lore = new ArrayList<>();
                // Vérifier si le joueur a déjà la permission
                if (permissionManager.hasPermission(onlinePlayer, "blb.host")) {
                    lore.add(ChatColor.RED + "Déjà Host");
                } else {
                    lore.add(ChatColor.GREEN + "Cliquez pour donner le statut de Host");
                }
                meta.setLore(lore);
                playerItem.setItemMeta(meta);
            }
            // Ajouter l'item à l'inventaire
            hostInventory.addItem(playerItem);
        }

        // Ouvrir l'inventaire pour le joueur
        player.openInventory(hostInventory);
    }

    private void openModInventory(Player player) {
        // Créer un inventaire de 54 slots
        Inventory modInventory = Bukkit.createInventory(null, 54, ChatColor.GREEN + "Sélectionner un Mod");

        // Récupérer tous les joueurs en ligne
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            // Créer un item pour chaque joueur
            ItemStack playerItem = new ItemStack(Material.SKULL_ITEM, 1, (short) 3); // Utiliser une tête de joueur
            ItemMeta meta = playerItem.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(onlinePlayer.getName()); // Nom du joueur
                List<String> lore = new ArrayList<>();
                // Vérifier si le joueur a déjà la permission
                if (permissionManager.hasPermission(onlinePlayer, "blb.mod")) {
                    lore.add(ChatColor.RED + "Déjà Mod");
                } else {
                    lore.add(ChatColor.GREEN + "Cliquez pour donner le statut de Mod");
                }
                meta.setLore(lore);
                playerItem.setItemMeta(meta);
            }
            // Ajouter l'item à l'inventaire
            modInventory.addItem(playerItem);
        }

        // Ouvrir l'inventaire pour le joueur
        player.openInventory(modInventory);
    }

    @EventHandler
    private void onHostInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(ChatColor.GREEN + "Sélectionner un Host")) {
            event.setCancelled(true); // Annuler l'événement pour éviter de déplacer les items

            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();

            // Vérifier si l'item cliqué est un item de joueur
            if (clickedItem != null && clickedItem.getType() == Material.SKULL_ITEM) {
                String playerName = clickedItem.getItemMeta().getDisplayName();
                Player selectedPlayer = Bukkit.getPlayer(playerName);

                if (selectedPlayer != null) {
                    if (permissionManager.hasPermission(selectedPlayer, "blb.host")) {
                        permissionManager.removePermission(selectedPlayer, "blb.host");
                        player.sendMessage(ChatColor.RED + selectedPlayer.getName() + " n'est plus Host.");
                        selectedPlayer.sendMessage(ChatColor.RED + "Vous avez été retiré du statut de Host.");

                    } else {
                        permissionManager.addPermission(selectedPlayer, "blb.host");
                        player.sendMessage(ChatColor.GREEN + selectedPlayer.getName() + " a maintenant le statut de Host.");
                        selectedPlayer.sendMessage(ChatColor.GREEN + "Vous avez été promu au statut de Host.");
                        selectedPlayer.getInventory().addItem(getConfigItem()); // Donner l'étoile du Nether au joueur
                        selectedPlayer.updateInventory();
                    }
                }
            }
        }
    }

    @EventHandler
    private void OnModInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(ChatColor.GREEN + "Sélectionner un Mod")) {
            event.setCancelled(true); // Annuler l'événement pour éviter de déplacer les items

            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();

            // Vérifier si l'item cliqué est un item de joueur
            if (clickedItem != null && clickedItem.getType() == Material.SKULL_ITEM) {
                String playerName = clickedItem.getItemMeta().getDisplayName();
                Player selectedPlayer = Bukkit.getPlayer(playerName);

                if (selectedPlayer != null) {
                    if (permissionManager.hasPermission(selectedPlayer, "blb.mod")) {
                        // Retirer la permission blb.mod
                        permissionManager.removePermission(selectedPlayer, "blb.mod");

                        player.sendMessage(ChatColor.RED + selectedPlayer.getName() + " n'est plus Mod.");
                        selectedPlayer.sendMessage(ChatColor.RED + "Vous avez été retiré du statut de Mod.");
                        playerManager.addPlayer(selectedPlayer);
                    } else {
                        // Ajouter la permission blb.mod
                        permissionManager.addPermission(player, "blb.mod");

                        player.sendMessage(ChatColor.GREEN + selectedPlayer.getName() + " a maintenant le statut de Mod.");
                        selectedPlayer.sendMessage(ChatColor.GREEN + "Vous avez été promu au statut de Mod.");
                        playerManager.removePlayer(selectedPlayer);
                    }
                }
            }
        }
    }

    @EventHandler
    private void OnDrop(PlayerDropItemEvent event) {
        Item item = event.getItemDrop();
        if (item.getItemStack().equals(getConfigItem()) || item.getItemStack().equals(getTeamItem())) {
            event.setCancelled(true);
        }
    }

    private ItemStack getStopItem() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName("§cArrêter le lancement");
        item.setItemMeta(itemMeta);
        return item;
    }

    private ItemStack getConfigItem() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.RED + "Config");
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack getTeamItem() {
        ItemStack item = new ItemStack(Material.BANNER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.RED + "Choisir son équipe");
            item.setItemMeta(meta);
        }
        return item;
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

    private ItemStack getModItem() throws NoSuchFieldException, IllegalAccessException {
        ItemStack item = new ItemStack(Material.SKULL_ITEM,1,(byte) SkullType.PLAYER.ordinal());
        SkullMeta itemMeta = (SkullMeta) item.getItemMeta();
        itemMeta.setDisplayName("§l§9Modérateurs");

        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmQzMGEzZGZlN2UyOWY0YmFkNzM4MTMxYWZjM2RkZmQ2OWIxNDQ5ZDVmZTU2YjI1YzY0YmI0ODkxMTNjNTQ4ZCJ9fX0="));
        Field field;
        field = itemMeta.getClass().getDeclaredField("profile");
        field.setAccessible(true);
        field.set(itemMeta, profile);
        item.setItemMeta(itemMeta);

        return item;
    }

    private ItemStack getHostItem() throws NoSuchFieldException, IllegalAccessException {
        ItemStack item = new ItemStack(Material.SKULL_ITEM,1,(byte) SkullType.PLAYER.ordinal());
        SkullMeta itemMeta = (SkullMeta) item.getItemMeta();
        itemMeta.setDisplayName("§d§lHost");

        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2UxZjVjMDM1MDEwMGQ1NWY5NWQxNzNhZTliODQ4ODJhNTAyNmMwOTVkODhjY2E1ZjliOGU4OTM1NjJhMDZjZiJ9fX0="));
        Field field;
        field = itemMeta.getClass().getDeclaredField("profile");
        field.setAccessible(true);
        field.set(itemMeta, profile);
        item.setItemMeta(itemMeta);

        return item;
    }

    private ItemStack getStartItem() throws NoSuchFieldException, IllegalAccessException {
        ItemStack item = new ItemStack(Material.SKULL_ITEM,1,(byte) SkullType.PLAYER.ordinal());
        SkullMeta itemMeta = (SkullMeta) item.getItemMeta();
        itemMeta.setDisplayName("§a§lLancer");

        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjFlOTc0YTI2MDhiZDZlZTU3ZjMzNDg1NjQ1ZGQ5MjJkMTZiNGEzOTc0NGViYWI0NzUzZjRkZWI0ZWY3ODIifX19"));
        Field field;
        field = itemMeta.getClass().getDeclaredField("profile");
        field.setAccessible(true);
        field.set(itemMeta, profile);
        item.setItemMeta(itemMeta);

        return item;
    }
}