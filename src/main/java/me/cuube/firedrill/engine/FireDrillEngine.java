package me.cuube.firedrill.engine;

import me.cuube.firedrill.utility.Geometry;
import me.cuube.firedrill.utility.Wall;
import org.bukkit.entity.EntityType;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class FireDrillEngine {

    private BoundingBox bounds;
    public BoundingBox getBounds() {
        return this.bounds;
    }
    private ArrayList<Wall> walls = new ArrayList<>();
    public ArrayList<Wall> getWalls() { return this.walls; }

    private Vector doorCenter;
    public Vector getDoorCenter() { return this.doorCenter; }
    private double doorWidth;
    public BoundingBox getDoor() {
        BoundingBox doorBounds;
        // parallel to x axis
        if(doorCenter.getX() == bounds.getMinX() || doorCenter.getX() == bounds.getMaxX()) {
            doorBounds = new BoundingBox(
                doorCenter.getX(), bounds.getMinY(), doorCenter.getZ() - doorWidth / 2,
                doorCenter.getX(), Math.min(bounds.getMinY() + 3, bounds.getMaxY()), doorCenter.getZ() + doorWidth / 2
            );
        }
        // parallel to z axis
        else {
            doorBounds = new BoundingBox(
                doorCenter.getX() - doorWidth / 2, bounds.getMinY(), doorCenter.getZ(),
                doorCenter.getX() + doorWidth / 2, Math.min(bounds.getMinY() + 3, bounds.getMaxY()), doorCenter.getZ()
            );
        }
        return doorBounds;
    }

    private double time;
    public double getTime() { return this.time; }
    private Random rand = new Random();

    private ArrayList<Person> people = new ArrayList<>();
    public ArrayList<Person> getPeople() {
        return this.people;
    }

    public FireDrillEngine(int numPeople, BoundingBox bounds, Vector doorCenter, double doorWidth) {
        this.bounds = bounds;
        System.out.println("New engine: " + this.bounds.toString());
        this.doorCenter = doorCenter;
        this.doorWidth = doorWidth;
    }

    public void addWall(Wall newWall) {
        if(this.walls.contains(newWall)) return;
        this.walls.add(newWall);
    }

    public boolean tick(double timeStep) {
        ArrayList<Person> peopleTurns = (ArrayList<Person>) this.people.clone();
        Collections.shuffle(peopleTurns);

        for(Person person : peopleTurns) {
           /* Vector proposedMove = person.getMove(timeStep);

            // cancel movement if runs into wall
            if(Geometry.hitsWall(proposedMove, person.getPhysicalRadius(), this.walls)
                || Geometry.intersectsWall(person.getLocation(), proposedMove, this.walls))*/
                Vector proposedMove = person.getLocation();

            person.setLocation(proposedMove);
        }

        this.time += timeStep;
        return !roomIsClear();
    }

    public boolean roomIsClear() {
        for(Person person : this.people) {
            Vector loc = person.getLocation();
            // Person escapes if outside/on edge of bounds
            System.out.println(loc.toString());
            System.out.println("Entity #" + this.people.indexOf(person) + " contained: " + this.bounds.contains(loc));
            System.out.println("Entity #" + this.people.indexOf(person) + " not on edge: " + !onEdgeOfBounds(loc, this.bounds));

            if(this.bounds.contains(loc) || !onEdgeOfBounds(loc, this.bounds)) {
                return false;
            }
        }
        return true;
    }

    public boolean onEdgeOfBounds(Vector loc, BoundingBox bounds) {
        return (loc.getX() == bounds.getMinX()) ||
                (loc.getX() == bounds.getMaxX()) ||
                (loc.getZ() == bounds.getMinZ()) ||
                (loc.getZ() == bounds.getMaxZ());
    }

    public void generateRandomPeople(int numPeople, EntityType entityType) {
        ArrayList<Person> newPeople = new ArrayList<>();
        System.out.println("generating " + numPeople + " people.");
        for(int i = 0; i < numPeople; i++) {
            Person person = new Person(this, new Vector(0, 0, 0), entityType);

            Vector newPosition;
            do {
                double x = rand.nextDouble() * bounds.getWidthX() + bounds.getMinX();
                double z = rand.nextDouble() * bounds.getWidthZ() + bounds.getMinZ();

                newPosition = new Vector(x, bounds.getMinY(), z);
                person.setLocation(newPosition.clone());
            } while(Geometry.hitsWall(newPosition, person.getPhysicalRadius(), this.walls) || breachesPersonalSpace(person, newPeople));

            newPeople.add(person);
            System.out.println("Adding person at " + person.getLocation().toString());
        }
        this.people = newPeople;
    }

    public boolean breachesPersonalSpace(Person person, ArrayList<Person> otherPeople) {
        System.out.println("Checking for breach of personal space.");
        for(Person otherPerson : otherPeople) {
            if(otherPerson.equals(person)) {
                System.out.println("Person #" + this.people.indexOf(otherPerson) + " == " + "Person #" + this.people.indexOf(person));
                continue;
            }

            double minDistance = person.getPhysicalRadius()
                    + person.getExclusionRadius()
                    + otherPerson.getPhysicalRadius()
                    + otherPerson.getExclusionRadius();

            System.out.println("Distance between ppl: " + person.getLocation().distance(otherPerson.getLocation()));
            if(person.getLocation().distance(otherPerson.getLocation()) < minDistance)
                return true;
        }
        return false;
    }
}
