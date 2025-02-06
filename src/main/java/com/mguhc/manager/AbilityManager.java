package com.mguhc.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AbilityManager {
    // Map pour stocker les capacités de chaque rôle
    private final Map<Role, List<Ability>> abilities;

    public AbilityManager() {
        this.abilities = new HashMap<>();
    }

    // Méthode pour enregistrer une capacité pour un rôle
    public void registerAbility(Role role, List<Ability> ability) {
        abilities.put(role, ability);
    }

    // Méthode pour obtenir la capacité d'un rôle
    public List<Ability> getAbilitys(Role role) {
        return abilities.get(role);
    }
}