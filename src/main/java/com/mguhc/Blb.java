package com.mguhc;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.mguhc.events.StartGameEvent;
import com.mguhc.listener.ConfigListener;
import com.mguhc.listener.PlayerDeathListener;
import com.mguhc.listener.RoleGuiListener;
import com.mguhc.listener.ScenariosListener;
import com.mguhc.listener.role.bachira.BachiraListener;
import com.mguhc.listener.role.baro.BaroListener;
import com.mguhc.listener.role.chigiri.ChigiriListener;
import com.mguhc.listener.role.isagi.IsagiListener;
import com.mguhc.listener.role.karasu.KarasuListener;
import com.mguhc.listener.role.nagi.NagiListener;
import com.mguhc.listener.role.otaya.OtayaListener;
import com.mguhc.listener.role.reo.ReoListener;
import com.mguhc.listener.role.rin.RinListener;
import com.mguhc.listener.role.shidou.ShidouListener;
import com.mguhc.manager.*;
import com.mguhc.scoreboard.BlbScoreboard;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class Blb extends JavaPlugin implements Listener {

    private GameManager gameManager;
    private RoleManager roleManager;
    private TeamManager teamManager;
    private PlayerManager playerManager;
    private BallManager ballManager;
    private EffectManager effectManager;
    private AbilityManager abilityManager;
    private CooldownManager cooldownManager;
    private static Blb instance;
    private int timer;
    private BlbScoreboard blbScoreboard;
    private Team rouge;
    private Team bleu;
    private final List<Team> teams = new ArrayList<>();
    private ProtocolManager protocolManager;
    private PermissionManager permissionManager;

    public void onEnable() {
        instance = this;
        roleManager = new RoleManager();
        teamManager = new TeamManager();
        playerManager = new PlayerManager();
        effectManager = new EffectManager();
        permissionManager = new PermissionManager();
        ballManager = new BallManager();
        abilityManager = new AbilityManager();
        cooldownManager = new CooldownManager();
        gameManager = new GameManager();
        protocolManager = ProtocolLibrary.getProtocolManager();
        registerListener();

        blbScoreboard = new BlbScoreboard();
    }

    private void registerListener() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(this, this);
        pluginManager.registerEvents(new ConfigListener(), this);
        pluginManager.registerEvents(new RoleGuiListener(), this);
        pluginManager.registerEvents(new PlayerDeathListener(), this);
        pluginManager.registerEvents(new ScenariosListener(), this);
        pluginManager.registerEvents(ballManager, this);
        pluginManager.registerEvents(effectManager, this);

        // RoleListener
        pluginManager.registerEvents(new IsagiListener(), this);
        pluginManager.registerEvents(new NagiListener(), this);
        pluginManager.registerEvents(new BachiraListener(), this);
        pluginManager.registerEvents(new ChigiriListener(), this);
        pluginManager.registerEvents(new BaroListener(), this);
        pluginManager.registerEvents(new RinListener(), this);
        pluginManager.registerEvents(new ShidouListener(), this);
        pluginManager.registerEvents(new ReoListener(), this);
        pluginManager.registerEvents(new KarasuListener(), this);
        pluginManager.registerEvents(new OtayaListener(), this);
    }

    @EventHandler
    private void OnDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (player.getLocation().getY() > 10) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    private void OnJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        blbScoreboard.createScoreboard(player);
        new BukkitRunnable() {
            @Override
            public void run() {
                setTabHeaderFooter(player);
            }
        }.runTaskTimer(Blb.getInstance(), 0, 20);
    }

    private void setTabHeaderFooter(Player player) {
        String header = "§3§l» §f§lVerse Studio §3§l«\n§7BlueLock Battle §8● §fV2.0\n";
        int strength = effectManager.getEffect(player, PotionEffectType.INCREASE_DAMAGE);
        int resistance = effectManager.getEffect(player, PotionEffectType.DAMAGE_RESISTANCE);
        int speed = effectManager.getEffect(player, PotionEffectType.SPEED);
        String footer = "\n§c⚔ " + strength + "% §8| §7❂ " + resistance + "% §8| §b✪ " + speed + "%\n §f \n §3§lDiscord §8● §fdiscord.gg/versestudio\n§3§lBoutique §8● §fversestudio.fr";

        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER);
        packet.getChatComponents().write(0, WrappedChatComponent.fromText(fixColors(header)))
                .write(1, WrappedChatComponent.fromText(fixColors(footer)));

        try {
            protocolManager.sendServerPacket(player, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String fixColors(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    @EventHandler
    private void OnStart(StartGameEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                timer ++;
            }
        }.runTaskTimer(Blb.getInstance(), 0, 20);
    }

    public static void clearAll(Player player) {
        EffectManager effectManager = Blb.getInstance().getEffectManager();
        RoleManager roleManager = Blb.getInstance().getRoleManager();
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.setMaxHealth(20);
        effectManager.removeEffects(player);
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        roleManager.removeRole(player);
    }

    public static void sendActionBar(Player player, String message) {
        if (player == null || message == null) {
            return;
        }

        IChatBaseComponent chatBaseComponent = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + message + "\"}");
        PacketPlayOutChat packet = new PacketPlayOutChat(chatBaseComponent, (byte) 2); // Type 2 = Action Bar

        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }


    public int getTimer() {
        return timer;
    }

    public TeamManager getTeamManager() {
        return teamManager;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public RoleManager getRoleManager() {
        return roleManager;
    }

    public BallManager getBallManager() {
        return ballManager;
    }

    public EffectManager getEffectManager() {
        return effectManager;
    }

    public AbilityManager getAbilityManager() {
        return abilityManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public Team getRouge() {
        return rouge;
    }

    public Team getBleu() {
        return bleu;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public static Blb getInstance() {
        return instance;
    }

    public PermissionManager getPermissionManager() {
        return permissionManager;
    }
}