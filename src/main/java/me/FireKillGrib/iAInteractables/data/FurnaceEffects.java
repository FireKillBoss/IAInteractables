package me.FireKillGrib.iAInteractables.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.FireKillGrib.iAInteractables.Plugin;
import org.bukkit.configuration.ConfigurationSection;

@AllArgsConstructor
@Getter
public class FurnaceEffects {
    private final EffectConfig onStart;
    private final EffectConfig onCooking;
    private final EffectConfig onComplete;
    private final int cookingInterval;
    
    @AllArgsConstructor
    @Getter
    public static class EffectConfig {
        private final ParticleConfig particle;
        private final SoundConfig sound;
    }
    public static FurnaceEffects fromConfig(ConfigurationSection section) {
        if (section == null) return null;
        int interval = section.getInt("cooking-interval", 20);
        EffectConfig onStart = loadEffectConfig(section, "on-start");
        EffectConfig onCooking = loadEffectConfig(section, "on-cooking");
        EffectConfig onComplete = loadEffectConfig(section, "on-complete");
        if (onStart == null && onCooking == null && onComplete == null) {
            Plugin.getInstance().getLogger().warning(
                "No effects configured for furnace (all effect sections are empty)");
            return null;
        }
        return new FurnaceEffects(onStart, onCooking, onComplete, interval);
    }
    private static EffectConfig loadEffectConfig(ConfigurationSection parent, String key) {
        if (!parent.contains(key)) return null;
        ConfigurationSection effectSection = parent.getConfigurationSection(key);
        if (effectSection == null) return null;
        ParticleConfig particle = null;
        if (effectSection.contains("particle")) {
            particle = ParticleConfig.fromConfig(effectSection.getConfigurationSection("particle"));
        }
        SoundConfig sound = null;
        if (effectSection.contains("sound")) {
            sound = SoundConfig.fromConfig(effectSection.getConfigurationSection("sound"));
        }
        if (particle != null || sound != null) {
            return new EffectConfig(particle, sound);
        }
        return null;
    }
}
