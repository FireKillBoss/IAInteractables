package me.FireKillGrib.iAInteractables.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
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
        ConfigurationSection particlesSec = section.getConfigurationSection("particles");
        ConfigurationSection soundsSec = section.getConfigurationSection("sounds");
        EffectConfig onStart = null;
        EffectConfig onCooking = null;
        EffectConfig onComplete = null;
        ParticleConfig startParticle = null;
        SoundConfig startSound = null;
        if (particlesSec != null && particlesSec.contains("on-start")) {
            startParticle = ParticleConfig.fromConfig(particlesSec.getConfigurationSection("on-start"));
        }
        if (soundsSec != null && soundsSec.contains("on-start")) {
            startSound = SoundConfig.fromConfig(soundsSec.getConfigurationSection("on-start"));
        }
        if (startParticle != null || startSound != null) {
            onStart = new EffectConfig(startParticle, startSound);
        }
        ParticleConfig cookingParticle = null;
        SoundConfig cookingSound = null;
        if (particlesSec != null && particlesSec.contains("on-cooking")) {
            cookingParticle = ParticleConfig.fromConfig(particlesSec.getConfigurationSection("on-cooking"));
        }
        if (soundsSec != null && soundsSec.contains("on-cooking")) {
            cookingSound = SoundConfig.fromConfig(soundsSec.getConfigurationSection("on-cooking"));
        }
        if (cookingParticle != null || cookingSound != null) {
            onCooking = new EffectConfig(cookingParticle, cookingSound);
        }
        ParticleConfig completeParticle = null;
        SoundConfig completeSound = null;
        if (particlesSec != null && particlesSec.contains("on-complete")) {
            completeParticle = ParticleConfig.fromConfig(particlesSec.getConfigurationSection("on-complete"));
        }
        if (soundsSec != null && soundsSec.contains("on-complete")) {
            completeSound = SoundConfig.fromConfig(soundsSec.getConfigurationSection("on-complete"));
        }
        if (completeParticle != null || completeSound != null) {
            onComplete = new EffectConfig(completeParticle, completeSound);
        }
        int interval = section.getInt("cooking-interval", 40);
        if (onStart == null && onCooking == null && onComplete == null) {
            return null;
        }
        return new FurnaceEffects(onStart, onCooking, onComplete, interval);
    }
}
