package com.mguhc.listener.role.chigiri;

import com.mguhc.manager.Ability;
import org.bukkit.entity.Player;

public class SprintAbility implements Ability {
    private double cooldownDuration = 20*1000;

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
