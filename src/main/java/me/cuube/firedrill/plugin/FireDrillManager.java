package me.cuube.firedrill.plugin;

import me.cuube.firedrill.engine.FireDrillEngine;
import me.cuube.firedrill.states.RunningState;
import me.cuube.firedrill.utility.Geometry;
import me.cuube.firedrill.utility.Message;
import me.cuube.firedrill.utility.Wall;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class FireDrillManager {
    private final FireDrillPlugin plugin;
    private final ArrayList<FireDrill> drills;

    public FireDrillManager(FireDrillPlugin plugin) {
        this.plugin = plugin;
        this.drills = new ArrayList<>();
        drillDraw.runTaskTimer(this.plugin, 0L, 5L);
    }

    public ArrayList<FireDrill> getDrills() {
        return this.drills;
    }

    public void createDrill(Player owner, SetupFireDrill setupFireDrill) {

        BoundingBox bounds = Geometry.boundsFromLocs(setupFireDrill.getPointOne(), setupFireDrill.getPointTwo());
        Vector doorCenter = calculateDoorCenter(bounds);
        FireDrillEngine engine = new FireDrillEngine(setupFireDrill.getNumPeople(), bounds, setupFireDrill.getDoorWidth());
        // ADD WALLS
        generateWalls(bounds, doorCenter, setupFireDrill.getDoorWidth()).forEach(engine::addWall);
        engine.generateRandomPeople(setupFireDrill.getNumPeople(), setupFireDrill.getEntityType());
        System.out.println("Created engine: " + engine.getPeople().size());
        FireDrill newDrill = new FireDrill(this, owner, engine, setupFireDrill.getWorld());
        drills.add(newDrill);
        newDrill.setState(new RunningState(newDrill));
    }

    public boolean removeDrill(FireDrill drill) {
        return this.drills.remove(drill);
    }

    private final BukkitRunnable drillDraw = new BukkitRunnable() {
        @Override
        public void run() {
            drawDrills();
        }
    };

    private void drawDrills() {
        for(FireDrill drill : this.drills) {
            drill.drawBorderParticles();
        }
    }

    public Vector calculateDoorCenter(BoundingBox bounds) {
        return new Vector(bounds.getCenterX(), bounds.getMinY(), bounds.getMinZ());
    }

    public ArrayList<Wall> generateWalls(BoundingBox bounds, Vector doorCenter, double doorWidth) {
        ArrayList<Wall> walls = new ArrayList<Wall>();
        walls.add(new Wall(
                    new Vector(bounds.getMinX(), bounds.getMinY(), bounds.getMaxZ()),
                    new Vector(bounds.getMaxX(), bounds.getMaxY(), bounds.getMaxZ())
        ));
        walls.add(new Wall(
                    new Vector(bounds.getMaxX(), bounds.getMinY(), bounds.getMaxZ()),
                    new Vector(bounds.getMaxX(), bounds.getMaxY(), bounds.getMinZ())
        ));
        walls.add(new Wall(
                new Vector(bounds.getMinX(), bounds.getMinY(), bounds.getMinZ()),
                new Vector(bounds.getMinX(), bounds.getMaxY(), bounds.getMaxZ())
        ));

        // placed on bottom wall
        walls.add(new Wall(
                new Vector(bounds.getMinX(), bounds.getMinY(), bounds.getMinZ()),
                new Vector(doorCenter.getX() - doorWidth / 2, bounds.getMaxY(), bounds.getMinZ())
        ));
        walls.add(new Wall(
                new Vector(doorCenter.getX() + doorWidth / 2, bounds.getMinY(), bounds.getMinZ()),
                new Vector(bounds.getMaxX(), bounds.getMaxY(), bounds.getMinZ())
        ));

        return walls;
    }
}
