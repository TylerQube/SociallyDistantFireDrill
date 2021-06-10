package me.cuube.firedrill.engine;

import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

public class Person {

    public Person(FireDrillEngine engine, Vector position, EntityType eType) {
        this.engine = engine;
        this.location = position;
        this.entityType = eType;
    }

    private final FireDrillEngine engine;
    public FireDrillEngine getEngine() {
        return this.engine;
    }

    private Intelligence intelligence;
    public Intelligence getIntelligence() {
        return this.intelligence;
    }
    public void setIntelligence(Intelligence intelligence) {
        this.intelligence = intelligence;
    }

    public Vector getMove(double timeStep) {
//        return this.intelligence.getMove(timeStep);
        return this.location;
    }

    private Vector location;
    public Vector getLocation() {
        return this.location;
    }
    public void setLocation(Vector newLoc) {
        this.location = newLoc;
    }

    private EntityType entityType;
    public EntityType getEntityType() {
        return this.entityType;
    }

    private final double radius = 0.5;
    public double getPhysicalRadius() {
        return this.radius;
    }

    private final double exclusionRadius = 1.8288;
    public double getExclusionRadius() {
        return this.exclusionRadius;
    }
}
