package me.cuube.firedrill.engine;

import org.bukkit.util.Vector;

import java.util.List;
import java.util.stream.Collectors;

public abstract class Intelligence {
    private Person owner;

    protected double maxSpeed = 1.8;
    public double getMaxSpeed() {
        return this.maxSpeed;
    }

    public Vector getLocation() {
        return this.owner.getLocation();
    }

    protected List<Vector> otherPeopleLocations() {
        return this.owner.getEngine().getPeople().stream().map(Person::getLocation).collect(Collectors.toList());
    }

    protected double getPhysicalRadius() {
        return this.owner.getPhysicalRadius();
    }

    protected double getExclusionRadius() {
        return this.owner.getExclusionRadius();
    }

    public Vector getMove(double timeStep) {
        Vector proposedPosition = chooseNewPosition(timeStep);
        return isLegalDistance(proposedPosition, timeStep) ? proposedPosition : this.getLocation();
    }

    private boolean isLegalDistance(Vector proposedPosition, double timeStep) {
        Vector displacement = proposedPosition.subtract(this.getLocation());
        double distanceSquared = displacement.lengthSquared();

        double maxDistance = this.maxSpeed * timeStep;
        final double tolerance = 1e5;

        double difference = Math.pow(maxDistance, 2) - distanceSquared;
        return difference > -tolerance;
    }

    protected Vector moveToward(Vector location, double timeStep) {
        Vector difference = location.subtract(this.getLocation());
        double currentDistanceSquared = difference.lengthSquared();

        double maxDistance = this.maxSpeed * timeStep;
        return currentDistanceSquared < Math.pow(maxDistance, 2) ? location : this.getLocation().add(difference.normalize().multiply(maxDistance));
    }

    public abstract Vector chooseNewPosition(double timeStep);
}
