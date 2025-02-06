package com.mguhc.listener.role.isagi;

import com.mguhc.manager.Ability;
import org.bukkit.entity.Player;

public class RetourneAbility implements Ability {
    private double cooldownDuration = 10*1000;

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
