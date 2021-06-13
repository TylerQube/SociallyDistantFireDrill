package me.cuube.firedrill.engine;

import me.cuube.firedrill.plugin.FireDrill;
import me.cuube.firedrill.utility.Geometry;
import me.cuube.firedrill.utility.Wall;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class FireDrillEngine {

    private final BoundingBox bounds;
    public BoundingBox getBounds() {
        return this.bounds.clone();
    }
    private final ArrayList<Wall> walls = new ArrayList<>();
    public ArrayList<Wall> getWalls() { return this.walls; }

    public Vector getDoorCenter() { return new Vector(this.bounds.getCenterX(), this.bounds.getMinY(), this.bounds.getMinZ()); }
    private final double doorWidth;
    public double getDoorWidth() { return this.doorWidth; }

    private double time;
    public double getTime() { return this.time; }
    private final Random rand = new Random();

    private ArrayList<Person> people = new ArrayList<>();
    public ArrayList<Person> getPeople() {
        return this.people;
    }
    private ArrayList<Person> originalPeople;
    public ArrayList<Person> getOriginalPeople() {
        return this.originalPeople;
    }

    public FireDrillEngine(int numPeople, BoundingBox bounds, double doorWidth) {
        this.bounds = bounds;
        System.out.println("New engine: " + this.bounds.toString());
        this.doorWidth = doorWidth;
    }

    public void addWall(Wall newWall) {
        if(this.walls.contains(newWall)) return;
        this.walls.add(newWall);
    }

    public void removePerson(Person toRemove) {
        this.people.remove(toRemove);
    }

    public boolean tick(double timeStep) {
        ArrayList<Person> peopleTurns = new ArrayList<Person>(this.people);
        Collections.shuffle(peopleTurns);
        ArrayList<Person> peopleToRemove = new ArrayList<>();
        for(Person person : peopleTurns) {
            if(person.isEscaped()) {
                peopleToRemove.add(person);
                continue;
            }
            String personPrefix = "Person #" + (this.originalPeople.indexOf(person) + 1) + ": ";
            Vector proposedMove = person.getMove(timeStep);

            boolean invalidMove = false;
            if(breachesPersonalSpace(person, proposedMove.clone(), this.people) ||
                Geometry.hitsWall(proposedMove.clone(), Person.getPhysicalRadius(), this.walls) ||
                Geometry.intersectsWall(person.getLocation().clone(), proposedMove.clone(), this.walls))
                invalidMove = true;
            if(breachesPersonalSpace(person, proposedMove.clone(), this.people)) {
                System.out.println("Move breaches personal space.");
            }
            if(Geometry.hitsWall(proposedMove.clone(), Person.getPhysicalRadius(), this.walls)) {
                System.out.println("Move hits wall.");
            }
            if(Geometry.intersectsWall(person.getLocation().clone(), proposedMove.clone(), this.walls)) {
                System.out.println("Move intersects wall.");
            }

            if(!invalidMove) {
//                this.drill.updateEntityDirection(this.people.get(this.people.indexOf(person)), proposedMove.clone());
                person.setLocation(proposedMove.clone());
                System.out.println(personPrefix + " moved to " + person.getLocation().clone());
            } else {
                System.out.println("Invalid move.");
            }
        }

        for(Person p : peopleToRemove) {
            this.people.remove(p);
        }

        this.time += timeStep;
        return !roomIsClear();
    }

    public boolean roomIsClear() {
        for(Person person : this.people) {
            if(!person.isEscaped()) {
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
            } while(Geometry.hitsWall(newPosition, Person.getPhysicalRadius(), this.walls) || breachesPersonalSpace(person, person.getLocation(), newPeople));

            newPeople.add(person);
            System.out.println("Adding person at " + person.getLocation().toString());
        }
        this.people = newPeople;
        this.originalPeople = new ArrayList<>(newPeople);
    }

    public boolean breachesPersonalSpace(Person person, Vector location, ArrayList<Person> otherPeople) {
        for(Person otherPerson : otherPeople) {
            if(otherPerson.equals(person)) {
                continue;
            }
            double minDistance = ((Person.getPhysicalRadius() * 2) + Person.getExclusionRadius());
            if(location.distance(otherPerson.getLocation()) < minDistance)
                return true;
        }
        return false;
    }
}
