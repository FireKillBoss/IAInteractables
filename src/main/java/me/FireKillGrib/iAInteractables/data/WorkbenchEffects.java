package me.FireKillGrib.iAInteractables.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class WorkbenchEffects {
    private final EffectsConfig onCraft;
    public static WorkbenchEffects fromConfig(org.bukkit.configuration.ConfigurationSection section) {
        if (section == null) return null;
        EffectsConfig onCraft = EffectsConfig.fromConfig(section.getConfigurationSection("on-craft"));
        return new WorkbenchEffects(onCraft);
    }
}
