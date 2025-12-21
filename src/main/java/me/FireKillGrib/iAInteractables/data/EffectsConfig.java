package me.FireKillGrib.iAInteractables.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@AllArgsConstructor
@Getter
public class EffectsConfig {
    private final SoundConfig sound;
    private final ParticleConfig particle;
    public void play(Location location) {
        if (sound != null) sound.play(location);
        if (particle != null) particle.spawn(location);
    }
    public void play(Player player, Location location) {
        if (sound != null) sound.play(player, location);
        if (particle != null) particle.spawn(player, location);
    }
    public static EffectsConfig fromConfig(org.bukkit.configuration.ConfigurationSection section) {
        if (section == null) return null;
        SoundConfig sound = SoundConfig.fromConfig(section.getConfigurationSection("sound"));
        ParticleConfig particle = ParticleConfig.fromConfig(section.getConfigurationSection("particle"));
        if (sound == null && particle == null) return null;
        return new EffectsConfig(sound, particle);
    }
}
