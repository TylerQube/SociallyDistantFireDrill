package me.cuube.firedrill.plugin;

import me.cuube.firedrill.engine.FireDrillEngine;
import me.cuube.firedrill.engine.Person;
import me.cuube.firedrill.states.DrillState;
import me.cuube.firedrill.states.RunningState;
import me.cuube.firedrill.utility.Geometry;
import me.cuube.firedrill.utility.Message;
import me.cuube.firedrill.utility.UtilityFunctions;
import me.cuube.firedrill.utility.Wall;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
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
    public double getPersonLeftCount() { return this.engine.getOriginalPeople().size() - this.engine.getPeople().size(); }
    public BoundingBox getBounds() {
        return this.engine.getBounds();
    }

    private final Map<Person, Entity> entities = new HashMap<>();

    private Color outlineColor = Color.fromRGB(255, 255, 255);
    public Color getColor() { return this.outlineColor; }
    public void setColor(Color newColor) { this.outlineColor = newColor; }

    public FireDrill(FireDrillManager manager, Player owner, FireDrillEngine engine, World world) {
        this.manager = manager;
        this.engine = engine;
        this.world = world;
        this.owner = owner;

        createEntities();
    }

    public void createEntities() {
        this.extinguishEntities.runTaskTimer(this.plugin, 0L, 1);

        for(Person p : this.engine.getPeople()) {
            EntityType type = p.getEntityType();
            Location loc = new Location(this.world, p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ());

            Entity e = this.world.spawnEntity(loc, type);
            System.out.println("Spawned entity at: " + loc.toString());
            e.setInvulnerable(true);
            if(e instanceof LivingEntity) {
                ((LivingEntity) e).setAI(false);
                ((LivingEntity) e).setCollidable(false);
                ((LivingEntity) e).setFireTicks(0);
            }

            this.entities.put(p, e);
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
    }

    public void stop() {
        FireDrillPlugin.getInstance().getRollbackManager().restorePlayer(this.owner);
        FireDrillPlugin.getInstance().getDrillManager().removeDrill(this);
        this.state.disable(this.plugin);

        for(Entity e : this.entities.values()) {
            e.remove();
        }
    }

    public boolean tick(double timeStep) {
        boolean tick = this.engine.tick(timeStep);
        updateEntities();
        return tick;
    }

    private BukkitRunnable extinguishEntities = new BukkitRunnable() {
        @Override
        public void run() {
            for(Person p : engine.getPeople()) {
                if(entities.get(p) != null)
                    entities.get(p).setFireTicks(0);
            }
        }
    };

    public void updateEntities() {
        for(Person p : this.engine.getPeople()) {
            if(p.isEscaped()) {
                entities.get(p).remove();
                owner.sendMessage(Message.prefix() + "Person #" + (this.engine.getOriginalPeople().indexOf(p) + 1) + " escaped!");
                continue;
            }
            boolean notMoving = false;
            if(this.entities.get(p).getLocation().equals(Geometry.locationFromVector(this.world, p.getLocation())))
                notMoving = true;
            Vector prevLocation = this.entities.get(p).getLocation().toVector().clone();

            this.entities.get(p).teleport(Geometry.locationFromVector(this.world, p.getLocation()));
            if(p.getLocation().clone().subtract(prevLocation.clone()).length() > 0) {
                Vector lookDir = p.getLocation().clone().subtract(prevLocation.clone()).normalize();
                Location orientedLoc = this.entities.get(p).getLocation().clone().setDirection(lookDir);
                this.entities.get(p).teleport(orientedLoc);
            }

            if(notMoving)
                drawExclusionParticles(p.getLocation().clone(), Particle.REDSTONE, Color.fromRGB(255, 0, 0));
            else
                drawExclusionParticles(p.getLocation().clone(), Particle.REDSTONE, Color.fromRGB(255, 255, 255));
        }
    }

    private boolean drawExclusionParticles = false;
    public void setDrawExclusion(boolean draw) {
        this.drawExclusionParticles = draw;
    }
    public boolean getDrawExclusion() { return this.drawExclusionParticles; }

    private void drawExclusionParticles(Vector loc, Particle part, Color color) {
        if(!this.drawExclusionParticles) return;

        Vector relativeParticleLoc = new Vector(0, 0, -(Person.getExclusionRadius() / 2 + Person.getPhysicalRadius()));
        for(int deg = 0; deg < 360; deg += 5) {
            Vector newParticleLoc = new Vector(0, 0, 0);
            newParticleLoc.setX(Math.cos(deg) * relativeParticleLoc.clone().getX() - Math.sin(deg) * relativeParticleLoc.clone().getZ());
            newParticleLoc.setZ(Math.sin(deg) * relativeParticleLoc.clone().getX() + Math.cos(deg) * relativeParticleLoc.clone().getZ());

            Vector absParticleLoc = loc.clone().add(newParticleLoc.clone());
            Particle.DustOptions dust = new Particle.DustOptions(
                    color, 1);
            this.world.spawnParticle(part, Geometry.locationFromVector(this.world, absParticleLoc.clone()), 1, 0, 0, 0, dust);
        }
    }

    public void drawBorderParticles() {
        drawParticleBoundingBox(this.world, this.getBounds(), Particle.REDSTONE);
    }

    private void drawParticleBoundingBox(World w, BoundingBox bounds, Particle part) {
        Vector min = bounds.getMin();
        Vector max = bounds.getMax();
        ArrayList<Wall> walls = this.engine.getWalls();

        Vector doorMin = new Vector(
                                this.engine.getDoorCenter().getX() - this.engine.getDoorWidth() / 2,
                                this.engine.getDoorCenter().getY(),
                                this.engine.getDoorCenter().getZ()
                            );
        Vector doorMax = new Vector(
                                this.engine.getDoorCenter().getX() + this.engine.getDoorWidth() / 2,
                                this.engine.getDoorCenter().getY() + Math.min(this.engine.getBounds().getHeight(), 3),
                                this.engine.getDoorCenter().getZ()
                            );

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
