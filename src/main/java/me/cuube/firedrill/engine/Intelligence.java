package me.cuube.firedrill.engine;

import me.cuube.firedrill.plugin.FireDrill;
import me.cuube.firedrill.utility.Wall;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Intelligence {

    private Person person;
    private FireDrillEngine engine;
    public void setPerson(Person person) {
        this.person = person;
        this.engine = person.getEngine();
    }

    protected static double maxSpeed = 1;
    protected double getMaxSpeed() {
        return Intelligence.maxSpeed;
    }

    protected Vector getLocation() {
        return this.person.getLocation().clone();
    }
    protected boolean isEscaped() { return this.person.isEscaped(); }

    protected List<Vector> otherPeopleLocations() {
        return this.person.getEngine().getPeople().stream().map(Person::getLocation).collect(Collectors.toList());
    }

    protected ArrayList<Wall> walls;
    protected BoundingBox getBounds() { return this.engine.getBounds().clone(); }
    protected Vector getDoorCenter() { return this.engine.getDoorCenter().clone(); }
    protected double getDoorWidth() { return this.engine.getDoorWidth(); }

    protected double getPhysicalRadius() {
        return Person.getPhysicalRadius();
    }

    protected double getExclusionRadius() {
        return Person.getExclusionRadius();
    }

    public Vector getMove(double timeStep) {
        Vector proposedPosition = chooseNewPosition(timeStep);
//        proposedPosition = new Vector(proposedPosition.clone().getX(), this.engine.getBounds().clone().getMinY(), proposedPosition.clone().getZ());
//        System.out.println("Revised target: " + proposedPosition);
//        System.out.println("Current location in getMove(): " + this.getLocation());
        System.out.println("Trying to move to: " + proposedPosition.clone().toString());
        return isLegalDistance(proposedPosition.clone(), timeStep) ? proposedPosition.clone() : this.getLocation().clone();
    }

    private boolean isLegalDistance(Vector proposedPosition, double timeStep) {
        Vector displacement = proposedPosition.clone().subtract(this.getLocation().clone());
        double dist = displacement.length();

        double maxDistance = maxSpeed * timeStep;
        final double tolerance = 1e-3;

        double difference = Math.abs(maxDistance - dist);
        return dist < maxDistance || difference < tolerance;
    }

    protected static Vector moveToward(Vector curLoc, Vector target, double timeStep) {
//        System.out.println("Current location: " + this.getLocation());
//        System.out.println("Moving toward: " + location);

        Vector difference = target.clone().subtract(curLoc);
        double currentDistance = difference.length();
        double maxDistance = maxSpeed * timeStep;

//        System.out.println("Moved to: " + (currentDistance < maxDistance ? location : this.getLocation().add(difference.normalize().multiply(maxDistance))).toString());
//        return currentDistance < maxDistance ? difference : curLoc.clone().add(difference.normalize().multiply(maxDistance));
        return currentDistance < maxDistance ? difference : curLoc.clone().add(difference.normalize().multiply(maxDistance));

    }

    protected abstract Vector chooseNewPosition(double timeStep);
}
