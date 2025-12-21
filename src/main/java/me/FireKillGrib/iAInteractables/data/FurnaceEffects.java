package me.FireKillGrib.iAInteractables.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class FurnaceEffects {
    private final EffectsConfig onStart;
    private final EffectsConfig onCooking;
    private final EffectsConfig onComplete;
    private final int cookingInterval;
    public static FurnaceEffects fromConfig(org.bukkit.configuration.ConfigurationSection section) {
        if (section == null) return null;
        EffectsConfig onStart = EffectsConfig.fromConfig(section.getConfigurationSection("on-start"));
        EffectsConfig onCooking = EffectsConfig.fromConfig(section.getConfigurationSection("on-cooking"));
        EffectsConfig onComplete = EffectsConfig.fromConfig(section.getConfigurationSection("on-complete"));
        int cookingInterval = section.getInt("cooking-interval", 20);
        return new FurnaceEffects(onStart, onCooking, onComplete, cookingInterval);
    }
}
