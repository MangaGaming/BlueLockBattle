package com.mguhc.manager;

import com.connorlinfoot.titleapi.TitleAPI;
import com.mguhc.Blb;
import com.mguhc.events.StartGameEvent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameManager {

    private final RoleManager roleManager;
    private final TeamManager teamManager;
    private final PlayerManager playerManager;
    private BukkitRunnable countdownTask;
    private State state = State.WAITING;

    public GameManager() {
        Blb blb = Blb.getInstance();
        roleManager = blb.getRoleManager();
        teamManager = blb.getTeamManager();
        playerManager = blb.getPlayerManager(); // Initialisation de PlayerManager
    }

    public void startGame() {
        state = State.PLAYING;

        // Démarrer le compte à rebours
        countdownTask = new BukkitRunnable() {
            int countdown = 5; // Démarrer à 5 secondes

            @Override
            public void run() {
                // Afficher le titre avec le temps restant
                for (Player p : playerManager.getPlayers()) {
                    if (countdown == 5) {
                        TitleAPI.sendTitle(p, 5, 30, 5, "§95", "");
                    } else if (countdown == 4) {
                        TitleAPI.sendTitle(p, 5, 30, 5, "§c4", "");
                    } else if (countdown == 3) {
                        TitleAPI.sendTitle(p, 5, 30, 5, "§63", "");
                    } else if (countdown == 2) {
                        TitleAPI.sendTitle(p, 5, 30, 5, "§e2", "");
                    } else if (countdown == 1) {
                        TitleAPI.sendTitle(p, 5, 30, 5, "§a1", "");
                    }
                }

                // Si le compte à rebours atteint 0, démarrer le jeu
                if (countdown <= 0) {
                    // Jouer le son de l'Ender Dragon
                    for (Player p : playerManager.getPlayers()) {
                        p.playSound(p.getLocation(), Sound.ENDERDRAGON_GROWL, 1.0F, 1.0F);
                    }

                    // Terminer le compte à rebours
                    cancel();

                    // Réinitialiser l'inventaire et téléporter les joueurs
                    for (Player p : playerManager.getPlayers()) {
                        p.getInventory().clear();
                        sendClickableMessage(p);
                        teleportToRandomLocation(p, teamManager.getTeam(p));
                        giveMeetupGear(p);
                    }

                    // Appeler l'événement de début de jeu
                    Blb.getInstance().getBallManager().spawnBall(new Location(Bukkit.getWorld("world"), 282, 7, 1243));
                    Bukkit.getPluginManager().callEvent(new StartGameEvent());
                }

                countdown--; // Décrémenter le compte à rebours
            }
        };
        countdownTask.runTaskTimer(Blb.getInstance(), 0, 20);
    }

    public void cancelStart() {
        if (countdownTask != null) {
            countdownTask.cancel(); // Annuler le compte à rebours
            countdownTask = null; // Réinitialiser la référence
        }
        state = State.WAITING; // Remettre l'état à WAITING
        for (Player player : playerManager.getPlayers()) {
            player.getInventory().clear();
            if (player.hasPermission("blb.host")) {
                ItemStack config = new ItemStack(Material.NETHER_STAR);
                ItemMeta configMeta = config.getItemMeta();
                if (configMeta != null) {
                    configMeta.setDisplayName(ChatColor.RED + "Config");
                    config.setItemMeta(configMeta);
                }
                player.getInventory().setItem(4, config);
            }
            ItemStack team = new ItemStack(Material.BANNER);
            ItemMeta teamMeta = team.getItemMeta();
            if (teamMeta != null) {
                teamMeta.setDisplayName(ChatColor.RED + "Choisir son équipe");
                team.setItemMeta(teamMeta);
            }
            player.getInventory().addItem(team);
        }
        Bukkit.broadcastMessage(ChatColor.RED + "Le démarrage du jeu a été annulé.");
    }

    public void teleportToRandomLocation(Player p, TeamEnum team) {
        World world = p.getWorld();
        List<Location> bleuLocations = new ArrayList<>();
        List<Location> rougeLocations = new ArrayList<>();

        // Ajouter les emplacements pour l'équipe bleue
        bleuLocations.add(new Location(world, 282, 7, 1283));
        bleuLocations.add(new Location(world, 259, 7, 1272));
        bleuLocations.add(new Location(world, 273, 7, 1256));
        bleuLocations.add(new Location(world, 289, 7, 1255));
        bleuLocations.add(new Location(world, 307, 7, 1273));

        // Ajouter les emplacements pour l'équipe rouge
        rougeLocations.add(new Location(world, 273, 7, 1232));
        rougeLocations.add(new Location(world, 289, 7, 1232));
        rougeLocations.add(new Location(world, 282, 7, 1203));
        rougeLocations.add(new Location(world, 249, 7, 1214));
        rougeLocations.add(new Location(world, 307, 7, 1214));

        Random random = new Random();

        if (team.equals(TeamEnum.BLEU)) {
            // Téléporter le joueur à un emplacement aléatoire de l'équipe bleue
            Location randomBleuLocation = bleuLocations.get(random.nextInt(bleuLocations.size()));
            p.teleport(randomBleuLocation);
        } else if (team.equals(TeamEnum.ROUGE)) {
            // Téléporter le joueur à un emplacement aléatoire de l'équipe rouge
            Location randomRougeLocation = rougeLocations.get(random.nextInt(rougeLocations.size()));
            p.teleport(randomRougeLocation);
        }
    }

    private static void sendClickableMessage(Player p) {
        // Créer le message principal
        TextComponent message = new TextComponent("§f \n§f §3§l» §f§lVerseUHC §8● §fBlueLock Battle\n§f\n");

        // Ajouter le texte pour les explications
        TextComponent explanation = new TextComponent("§3◼ §fPour plus d'explications sur le mode. §b(§fcliquez ici§b)");
        explanation.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://verse-studio.gitbook.io/bluelock-battle"));
        message.addExtra(explanation); // Ajouter le texte d'explication au message

        // Ajouter un saut de ligne
        message.addExtra("\n");

        // Ajouter le texte pour le règlement
        TextComponent rules = new TextComponent("§3◼ §fPour accéder à notre règlement. §b(§fcliquez ici§b)\n§f ");
        rules.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://verse-studio.gitbook.io/versestudio/verse-studio/reglement"));
        message.addExtra(rules); // Ajouter le texte de règlement au message

        // Envoyer le message au joueur
        p.spigot().sendMessage(message);
    }

    public void giveMeetupGear(Player player) {
        // Casque en fer avec Protection III
        ItemStack ironHelmet = new ItemStack(Material.IRON_HELMET);
        ItemMeta ironHelmetMeta = ironHelmet.getItemMeta();
        if (ironHelmetMeta != null) {
            ironHelmetMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 3, true);
            ironHelmet.setItemMeta(ironHelmetMeta);
        }
        player.getInventory().setHelmet(ironHelmet); // Mettre le casque

        // Plastron en diamant avec Protection II
        ItemStack diamondChestplate = new ItemStack(Material.DIAMOND_CHESTPLATE);
        ItemMeta diamondChestplateMeta = diamondChestplate.getItemMeta();
        if (diamondChestplateMeta != null) {
            diamondChestplateMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2, true);
            diamondChestplate.setItemMeta(diamondChestplateMeta);
        }
        player.getInventory().setChestplate(diamondChestplate); // Mettre le plastron

        // Pantalon en fer avec Protection III
        ItemStack ironLeggings = new ItemStack(Material.IRON_LEGGINGS);
        ItemMeta ironLeggingsMeta = ironLeggings.getItemMeta();
        if (ironLeggingsMeta != null) {
            ironLeggingsMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 3, true);
            ironLeggings.setItemMeta(ironLeggingsMeta);
        }
        player.getInventory().setLeggings(ironLeggings); // Mettre le pantalon

        // Bottes en diamant avec Protection II
        ItemStack diamondBoots = new ItemStack(Material.DIAMOND_BOOTS);
        ItemMeta diamondBootsMeta = diamondBoots.getItemMeta();
        if (diamondBootsMeta != null) {
            diamondBootsMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2, true);
            diamondBoots.setItemMeta(diamondBootsMeta);
        }
        player.getInventory().setBoots(diamondBoots); // Mettre les bottes

        // Épée en diamant avec Tranchant III
        ItemStack diamondSword = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta diamondSwordMeta = diamondSword.getItemMeta();
        if (diamondSwordMeta != null) {
            diamondSwordMeta.addEnchant(Enchantment.DAMAGE_ALL, 3, true);
            diamondSword.setItemMeta(diamondSwordMeta);
        }
        player.getInventory().setItem(0, diamondSword); // Mettre l'épée en slot 1

        // Arc avec Power III
        ItemStack bow = new ItemStack(Material.BOW);
        ItemMeta bowMeta = bow.getItemMeta();
        if (bowMeta != null) {
            bowMeta.addEnchant(Enchantment.ARROW_DAMAGE, 3, true);
            bow.setItemMeta(bowMeta);
        }
        player.getInventory().setItem(2, bow); // Mettre l'arc en slot 3

        // Flèches
        ItemStack arrows = new ItemStack(Material.ARROW, 16);
        player.getInventory().setItem(3, arrows); // Mettre les flèches en slot 4

        // Golden Apples
        ItemStack goldenApples = new ItemStack(Material.GOLDEN_APPLE, 5);
        player.getInventory().setItem(1, goldenApples); // Mettre les Golden Apples en slot 2

        // Golden Carrots
        ItemStack goldenCarrots = new ItemStack(Material.GOLDEN_CARROT, 64);
        player.getInventory().setItem(5, goldenCarrots);

        // Seau d'eau
        ItemStack waterBucket = new ItemStack(Material.WATER_BUCKET);
        player.getInventory().setItem(6, waterBucket); // Mettre le seau d'eau en slot 7

        // Seau de lave
        ItemStack lavaBucket = new ItemStack(Material.LAVA_BUCKET);
        player.getInventory().setItem(7, lavaBucket); // Mettre le seau de lave en slot 8

        // Cobblestone
        ItemStack cobblestone = new ItemStack(Material.COBBLESTONE, 64);
        player.getInventory().setItem(8, cobblestone); // Mettre la cobblestone en slot 9

        // Mettre à jour l'inventaire du joueur
        player.updateInventory();
    }

    public void finishGame(TeamEnum winner) {
        Bukkit.broadcastMessage(ChatColor.GREEN + "Le vainqueur est l'équipe " + winner.name());
        Bukkit.broadcastMessage(ChatColor.RED + "Le jeu est terminé le serveur va reload il laguera pendant quelques secondes");
        for (Player player : playerManager.getPlayers()) {
            player.teleport(new Location(Bukkit.getWorld("world"), 282, 7, 1243));
            Blb.clearAll(player);
            if (player.hasPermission("blb.host")) {
                player.getInventory().addItem(getConfigItem());
            }
        }
        Bukkit.reload();
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

    public State getState() {
        return state;
    }
}