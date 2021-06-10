package me.cuube.firedrill.plugin;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public class SetupFireDrill {
    private Location pointOne;
    public Location getPointOne() {
        return this.pointOne;
    }
    public void setPointOne(Location loc) {
        this.pointOne = loc;
    }

    private Location pointTwo;
    public Location getPointTwo() {
        return this.pointTwo;
    }
    public void setPointTwo(Location loc) {
        this.pointTwo = loc;
    }

    private int numPeople;
    public int getNumPeople() {
        return numPeople;
    }

    private EntityType entityType;
    public EntityType getEntityType() {
        return entityType;
    }

    private World world;
    public World getWorld() {
        return this.world;
    }

    private double doorWidth;
    public double getDoorWidth() {
        return this.doorWidth;
    }

    public SetupFireDrill(int numPeople, EntityType eType, double doorWidth, World world) {
        this.numPeople = numPeople;
        this.entityType = eType;
        this.doorWidth = doorWidth;
        this.world = world;
    }

    public BoundingBox getBounds() {
        if(this.pointOne == null || this.pointTwo == null) {
            return null;
        }
        BoundingBox bounds = new BoundingBox(pointOne.getX(), pointOne.getY(), pointOne.getZ(), pointTwo.getX(), pointTwo.getY(), pointTwo.getZ());
        return bounds;
    }

    public double getArea() {
        if(this.pointOne == null || this.pointTwo == null) {
            return -1;
        }
        return this.getBounds().getWidthX() * this.getBounds().getWidthZ();
    }

    public void drawBorderParticles() {
        BoundingBox bounds = new BoundingBox(pointOne.getX(), pointOne.getY(), pointOne.getZ(), pointTwo.getX(), pointTwo.getY(), pointTwo.getZ());
        drawParticleBoundingBox(pointOne.getWorld(), bounds, Particle.REDSTONE);
    }

    private void drawParticleBoundingBox(World w, BoundingBox bounds, Particle part) {
        Vector min = bounds.getMin();
        Vector max = bounds.getMax();

        // bottom edges
        drawParticleInRange(w, min, new Vector(max.getX(), min.getY(), min.getZ()), part);
        drawParticleInRange(w, min, new Vector(min.getX(), max.getY(), min.getZ()), part);
        drawParticleInRange(w, new Vector(max.getX(), min.getY(), min.getZ()), new Vector(max.getX(), max.getY(), min.getZ()), part);
        drawParticleInRange(w, new Vector(min.getX(), max.getY(), min.getZ()), new Vector(max.getX(), max.getY(), min.getZ()), part);

        // vertical edges
        drawParticleInRange(w, min, new Vector(min.getX(), min.getY(), max.getZ()), part);
        drawParticleInRange(w, new Vector(max.getX(), min.getY(), min.getZ()), new Vector(max.getX(), min.getY(), max.getZ()), part);
        drawParticleInRange(w, new Vector(min.getX(), max.getY(), min.getZ()), new Vector(min.getX(), max.getY(), max.getZ()), part);
        drawParticleInRange(w, new Vector(max.getX(), max.getY(), min.getZ()), new Vector(max.getX(), max.getY(), max.getZ()), part);

        // top edges
        drawParticleInRange(w, new Vector(min.getX(), min.getY(), max.getZ()), new Vector(max.getX(), min.getY(), max.getZ()), part);
        drawParticleInRange(w, new Vector(min.getX(), min.getY(), max.getZ()), new Vector(min.getX(), max.getY(), max.getZ()), part);
        drawParticleInRange(w, new Vector(max.getX(), min.getY(), max.getZ()), new Vector(max.getX(), max.getY(), max.getZ()), part);
        drawParticleInRange(w, new Vector(min.getX(), max.getY(), max.getZ()), new Vector(max.getX(), max.getY(), max.getZ()), part);
    }

    private void drawParticleInRange(World w, Vector min, Vector max, Particle part) {
        double iterInterval = 0.1;
        for(double x = min.getX(); x <= max.getX(); x += iterInterval) {
            for(double y = min.getY(); y <= max.getY(); y += iterInterval) {
                for(double z = min.getZ(); z <= max.getZ(); z += iterInterval) {
                    Location particleLoc = new Location(w, x, y, z);
                    Particle.DustOptions dust = new Particle.DustOptions(
                            Color.fromRGB(255, 150, 50), 1);
                    w.spawnParticle(part, particleLoc, 1, 0, 0, 0, dust);
                }
            }
        }
    }
}
