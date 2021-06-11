package me.cuube.firedrill.plugin;

import me.cuube.firedrill.engine.FireDrillEngine;
import me.cuube.firedrill.engine.Person;
import me.cuube.firedrill.states.DrillState;
import me.cuube.firedrill.states.RunningState;
import me.cuube.firedrill.utility.Geometry;
import me.cuube.firedrill.utility.Wall;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FireDrill {
    private final FireDrillPlugin plugin = FireDrillPlugin.getInstance();
    private final FireDrillManager manager;

    private String name;
    private Player owner;
    public Player getOwner() {
        return this.owner;
    }
    private World world;
    private DrillState state = null;

    public double getTime() { return this.engine.getTime(); }

    private boolean running = false;
    public boolean isRunning() { return running; }
    public void setRunning(boolean bool) { this.running = bool; }

    private final FireDrillEngine engine;
    public BoundingBox getBounds() {
        return this.engine.getBounds();
    }

    private Map<Person, Entity> entities = new HashMap<>();

    private Color outlineColor = Color.fromRGB(255, 255, 255);
    public Color getColor() { return this.outlineColor; }
    public void setColor(Color newColor) { this.outlineColor = newColor; }

    public FireDrill(FireDrillManager manager, Player owner, FireDrillEngine engine, World world) {
        this.manager = manager;
        this.engine = engine;
        this.world = world;
        this.owner = owner;

        createEntities();
        updateRoom();
    }

    public void createEntities() {
        for(Person p : this.engine.getPeople()) {
            EntityType type = p.getEntityType();
            Location loc = new Location(this.world, p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ());

            Entity e = this.world.spawnEntity(loc, type);
            System.out.println("Spawned entity at: " + loc.toString());
            e.setInvulnerable(true);
            if(e instanceof LivingEntity) {
                ((LivingEntity) e).setAI(false);
                ((LivingEntity) e).setCollidable(false);
            }

            this.entities.put(p, e);
        }
    }

    public void updateEntities() {
        for(Person p : this.engine.getPeople()) {
            this.entities.get(p).teleport(Geometry.locationFromVector(this.world, p.getLocation()));
        }
    }

    public void setState(DrillState newState) {
        if(this.state != null && this.state.getClass() == newState.getClass()) return;

        if(this.state != null) {
            this.state.disable(this.plugin);
        }
        this.state = newState;
        System.out.println("Setting drillState: " + newState.getClass());
        if(newState instanceof RunningState) {
            System.out.println("Setting up RunningState");
            newState.setup();
        }
        this.state.enable(this.plugin);

        // do stuff based on newState
    }

    public void stop() {
        FireDrillPlugin.getInstance().getRollbackManager().restorePlayer(this.owner);
        FireDrillPlugin.getInstance().getDrillManager().removeDrill(this);
        this.state.disable(this.plugin);

        for(Entity e : this.entities.values()) {
            e.remove();
        }
    }

    public void updateRoom() {
        // particle effects
    }


    public boolean tick(double timeStep) {
        boolean tick = this.engine.tick(timeStep);
        updateEntities();
        updateRoom();
        return tick;
    }

    public void drawBorderParticles() {
        drawParticleBoundingBox(this.world, this.getBounds(), Particle.REDSTONE);
    }

    private void drawParticleBoundingBox(World w, BoundingBox bounds, Particle part) {
        Vector min = bounds.getMin();
        Vector max = bounds.getMax();
        ArrayList<Wall> walls = this.engine.getWalls();

        BoundingBox door = this.engine.getDoor();
        Vector doorMin = door.getMin();
        Vector doorMax = door.getMax();

        // bottom edges
        drawParticleInRange(w, new Vector(max.getX(), min.getY(), min.getZ()), new Vector(max.getX(), min.getY(), max.getZ()), part);
        drawParticleInRange(w, new Vector(min.getX(), min.getY(), max.getZ()), new Vector(max.getX(), min.getY(), max.getZ()), part);

        if(this.engine.getDoorCenter().getX() == bounds.getMinX() || this.engine.getDoorCenter().getX() == bounds.getMaxX())  {
            drawParticleInRange(w, min, new Vector(min.getX(), min.getY(), doorMin.getZ()), part);
            drawParticleInRange(w, new Vector(min.getX(), min.getY(), doorMax.getZ()), new Vector(min.getX(), min.getY(), max.getZ()), part);
        } else {
            drawParticleInRange(w, min, new Vector(min.getX(), min.getY(), max.getZ()), part);
        }

        if(this.engine.getDoorCenter().getZ() == bounds.getMinZ() || this.engine.getDoorCenter().getZ() == bounds.getMaxZ())  {
            drawParticleInRange(w, min, new Vector(doorMin.getX(), min.getY(), min.getZ()), part);
            drawParticleInRange(w, new Vector(doorMax.getX(), min.getY(), min.getZ()), new Vector(max.getX(), min.getY(), min.getZ()), part);
        } else {
            drawParticleInRange(w, min, new Vector(max.getX(), min.getY(), min.getZ()), part);
        }

        // vertical edges
        drawParticleInRange(w, min, new Vector(min.getX(), max.getY(), min.getZ()), part);
        drawParticleInRange(w, new Vector(max.getX(), min.getY(), min.getZ()), new Vector(max.getX(), max.getY(), min.getZ()), part);
        drawParticleInRange(w, new Vector(min.getX(), min.getY(), max.getZ()), new Vector(min.getX(), max.getY(), max.getZ()), part);
        drawParticleInRange(w, new Vector(max.getX(), min.getY(), max.getZ()), new Vector(max.getX(), max.getY(), max.getZ()), part);

        // top edges
        drawParticleInRange(w, new Vector(min.getX(), max.getY(), min.getZ()), new Vector(max.getX(), max.getY(), min.getZ()), part);
        drawParticleInRange(w, new Vector(min.getX(), max.getY(), min.getZ()), new Vector(min.getX(), max.getY(), max.getZ()), part);
        drawParticleInRange(w, new Vector(max.getX(), max.getY(), min.getZ()), new Vector(max.getX(), max.getY(), max.getZ()), part);
        drawParticleInRange(w, new Vector(min.getX(), max.getY(), max.getZ()), new Vector(max.getX(), max.getY(), max.getZ()), part);

        // door
//        System.out.println("door height: " + this.engine.getDoor().getHeight());


        if(this.engine.getDoorCenter().getX() == bounds.getMinX() || this.engine.getDoorCenter().getX() == bounds.getMaxX()) {
            drawParticleInRange(w, new Vector(doorMin.getX(), doorMax.getY(), doorMin.getZ()), new Vector(doorMin.getX(), doorMax.getY(), doorMax.getZ()), part);
            drawParticleInRange(w, new Vector(doorMin.getX(), doorMin.getY(), doorMax.getZ()), new Vector(doorMin.getX(), doorMax.getY(), doorMax.getZ()), part);
        }
        else {
            drawParticleInRange(w, new Vector(doorMin.getX(), doorMax.getY(), doorMin.getZ()), new Vector(doorMax.getX(), doorMax.getY(), doorMin.getZ()), part);
            drawParticleInRange(w, new Vector(doorMax.getX(), doorMin.getY(), doorMin.getZ()), new Vector(doorMax.getX(), doorMax.getY(), doorMin.getZ()), part);
        }
        drawParticleInRange(w, doorMin, new Vector(doorMin.getX(), doorMax.getY(), doorMin.getZ()), part);
    }

    private void drawParticleInRange(World w, Vector min, Vector max, Particle part) {
        double iterInterval = 0.1;
        for(double x = min.getX(); x <= max.getX(); x += iterInterval) {
            for(double y = min.getY(); y <= max.getY(); y += iterInterval) {
                for(double z = min.getZ(); z <= max.getZ(); z += iterInterval) {
                    Location particleLoc = new Location(w, x, y, z);
                    Particle.DustOptions dust = new Particle.DustOptions(
                            this.outlineColor, 1);
                    w.spawnParticle(part, particleLoc, 1, 0, 0, 0, dust);
                }
            }
        }
    }
}
