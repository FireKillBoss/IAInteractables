package me.FireKillGrib.iAInteractables.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

@AllArgsConstructor
@Getter
public class SoundConfig {
    private final Sound sound;
    private final SoundCategory category;
    private final float volume;
    private final float pitch;
    public void play(Location location) {
        if (sound == null) return;
        location.getWorld().playSound(location, sound, category, volume, pitch);
    }
    public void play(Player player, Location location) {
        if (sound == null) return;
        player.playSound(location, sound, category, volume, pitch);
    }
    public static SoundConfig fromConfig(org.bukkit.configuration.ConfigurationSection section) {
    if (section == null) return null;
    String soundName = section.getString("sound");
    if (soundName == null || soundName.isEmpty()) return null;
    try {
        Sound sound = org.bukkit.Registry.SOUNDS.get(org.bukkit.NamespacedKey.minecraft(soundName.toLowerCase()));
        if (sound == null) {
            sound = Sound.valueOf(soundName.toUpperCase());
        }
        SoundCategory category = SoundCategory.valueOf(
            section.getString("category", "BLOCKS").toUpperCase()
        );
        float volume = (float) section.getDouble("volume", 1.0);
        float pitch = (float) section.getDouble("pitch", 1.0);
        return new SoundConfig(sound, category, volume, pitch);
        } catch (IllegalArgumentException e) {
        return null;
        }
    }
}
