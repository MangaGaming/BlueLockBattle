package com.mguhc;

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
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
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

    public void onEnable() {
        instance = this;
        roleManager = new RoleManager();
        teamManager = new TeamManager();
        playerManager = new PlayerManager();
        effectManager = new EffectManager();
        ballManager = new BallManager();
        abilityManager = new AbilityManager();
        cooldownManager = new CooldownManager();
        gameManager = new GameManager();
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

    public void clearAll(Player player) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.setMaxHealth(20);
        effectManager.removeEffects(player);
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        roleManager.removeRole(player);
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

}