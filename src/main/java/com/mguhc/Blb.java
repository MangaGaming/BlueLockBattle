package com.mguhc;

import com.mguhc.listener.ConfigListener;
import com.mguhc.listener.PlayerDeathListener;
import com.mguhc.listener.RoleGuiListener;
import com.mguhc.listener.role.isagi.IsagiListener;
import com.mguhc.manager.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

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
    }

    private void registerListener() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(this, this);
        pluginManager.registerEvents(new ConfigListener(), this);
        pluginManager.registerEvents(new RoleGuiListener(), this);
        pluginManager.registerEvents(new PlayerDeathListener(), this);
        pluginManager.registerEvents(ballManager, this);
        pluginManager.registerEvents(effectManager, this);

        // RoleListener
        pluginManager.registerEvents(new IsagiListener(), this);
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

    public void clearAll(Player player) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.setMaxHealth(20);
        effectManager.removeEffects(player);
        roleManager.removeRole(player);
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

    public static Blb getInstance() {
        return instance;
    }
}