package com.mguhc.listener.role.rin;

import com.mguhc.manager.Ability;
import org.bukkit.entity.Player;

public class CoupFrancAbility implements Ability {
    private double cooldownDuration = 15*1000;

    @Override
    public void activate(Player player) {

    }

    @Override
    public void deactivate(Player player) {

    }

    @Override
    public double getCooldownDuration() {
        return cooldownDuration;
    }

    @Override
    public void setCooldownDuration(double n) {
        cooldownDuration = n;
    }
}
