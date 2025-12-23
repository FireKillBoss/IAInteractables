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
        if (section == null) {
            Plugin.getInstance().getLogger().warning("FurnaceEffects section == null");
            return null;
        }
        Plugin.getInstance().getLogger().info("Loading FurnaceEffects from config...");
        int interval = section.getInt("cooking-interval", 20);
        Plugin.getInstance().getLogger().info("  cooking-interval: " + interval);
        EffectConfig onStart = loadEffectConfig(section, "on-start");
        EffectConfig onCooking = loadEffectConfig(section, "on-cooking");
        EffectConfig onComplete = loadEffectConfig(section, "on-complete");
        if (onStart == null && onCooking == null && onComplete == null) {
            Plugin.getInstance().getLogger().warning("None effects are loaded! Returning null.");
            return null;
        }
        Plugin.getInstance().getLogger().info("✓ FurnaceEffects succesfully loaded:");
        Plugin.getInstance().getLogger().info("  on-start: " + (onStart != null));
        Plugin.getInstance().getLogger().info("  on-cooking: " + (onCooking != null));
        Plugin.getInstance().getLogger().info("  on-complete: " + (onComplete != null));
        return new FurnaceEffects(onStart, onCooking, onComplete, interval);
    }
    private static EffectConfig loadEffectConfig(ConfigurationSection parent, String key) {
        if (!parent.contains(key)) {
            Plugin.getInstance().getLogger().info("Section '" + key + "' is absent");
            return null;
        }
        ConfigurationSection effectSection = parent.getConfigurationSection(key);
        if (effectSection == null) {
            Plugin.getInstance().getLogger().warning("Wasn't able to get section '" + key + "'");
            return null;
        }
        Plugin.getInstance().getLogger().info("Loading effect '" + key + "'...");
        ParticleConfig particle = null;
        if (effectSection.contains("particle")) {
            Plugin.getInstance().getLogger().info("Loading particle...");
            particle = ParticleConfig.fromConfig(effectSection.getConfigurationSection("particle"));
            if (particle != null) {
                Plugin.getInstance().getLogger().info("Particle loaded: " + particle.getParticle());
            } else {
                Plugin.getInstance().getLogger().warning("Particle == null");
            }
        } else {
            Plugin.getInstance().getLogger().info("Particle is missing");
        }
        SoundConfig sound = null;
        if (effectSection.contains("sound")) {
            Plugin.getInstance().getLogger().info("Loading sound...");
            sound = SoundConfig.fromConfig(effectSection.getConfigurationSection("sound"));
            if (sound != null) {
                Plugin.getInstance().getLogger().info("Sound loaded: " + sound.getSound());
            } else {
                Plugin.getInstance().getLogger().warning("Sound == null");
            }
        } else {
            Plugin.getInstance().getLogger().info("No sound is present");
        }
        if (particle != null || sound != null) {
            Plugin.getInstance().getLogger().info("EffectConfig '" + key + "' created");
            return new EffectConfig(particle, sound);
        } else {
            Plugin.getInstance().getLogger().warning("EffectConfig '" + key + "' empty");
            return null;
        }
    }
}
