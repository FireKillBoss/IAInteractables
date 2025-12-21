package me.FireKillGrib.iAInteractables.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

@AllArgsConstructor
@Getter
public class ParticleConfig {
    private final Particle particle;
    private final int count;
    private final double offsetX;
    private final double offsetY;
    private final double offsetZ;
    private final double speed;
    public void spawn(Location location) {
        if (particle == null) return;
        location.getWorld().spawnParticle(
            particle, 
            location, 
            count, 
            offsetX, offsetY, offsetZ, 
            speed
        );
    }
    public void spawn(Player player, Location location) {
        if (particle == null) return;
        player.spawnParticle(
            particle, 
            location, 
            count, 
            offsetX, offsetY, offsetZ, 
            speed
        );
    }
    public static ParticleConfig fromConfig(org.bukkit.configuration.ConfigurationSection section) {
        if (section == null) return null;
        String particleName = section.getString("particle");
        if (particleName == null || particleName.isEmpty()) return null;
        try {
            Particle particle = Particle.valueOf(particleName.toUpperCase());
            int count = section.getInt("count", 10);
            double offsetX = section.getDouble("offset-x", 0.5);
            double offsetY = section.getDouble("offset-y", 0.5);
            double offsetZ = section.getDouble("offset-z", 0.5);
            double speed = section.getDouble("speed", 0.1);
            return new ParticleConfig(particle, count, offsetX, offsetY, offsetZ, speed);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
