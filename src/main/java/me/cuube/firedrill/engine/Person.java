package me.cuube.firedrill.engine;

import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

public class Person {

    public Person(FireDrillEngine engine, Vector position, EntityType eType) {
        this.engine = engine;
        this.location = position;
        this.entityType = eType;

        this.intelligence = new IntelligenceTylerSmith();
        this.intelligence.setPerson(this);
    }

    private FireDrillEngine engine;
    public FireDrillEngine getEngine() {
        return this.engine;
    }

    private final Intelligence intelligence;
    public Intelligence getIntelligence() {
        return this.intelligence;
    }

    public boolean isEscaped() {
        return this.getLocation().getZ() <= this.engine.getDoorCenter().getZ();
    }

    public Vector getMove(double timeStep) {
//        return this.getLocation()/*.clone()*/.add(this.engine.getDoorCenter().subtract(this.getLocation()/*.clone()*/).normalize().multiply(Intelligence.maxSpeed));
        return this.intelligence.getMove(timeStep);
    }


    private Vector location;
    public Vector getLocation() {
        return this.location.clone();
    }
    public void setLocation(Vector newLoc) {
        this.location = newLoc;
    }

    private final EntityType entityType;
    public EntityType getEntityType() {
        return this.entityType;
    }

    private static final double radius = 0.5;
    public static double getPhysicalRadius() {
        return radius;
    }

    private static final double exclusionRadius = 2.5;
    public static double getExclusionRadius() {
        return exclusionRadius;
    }

    public static double getTotalArea() { return Math.pow(2 * (radius + exclusionRadius), 2); }
}
