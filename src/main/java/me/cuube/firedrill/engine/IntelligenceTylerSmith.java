package me.cuube.firedrill.engine;

import me.cuube.firedrill.utility.Wall;
import org.bukkit.util.Vector;

import java.util.Iterator;
import java.util.List;

public class IntelligenceTylerSmith extends Intelligence {
    protected Vector chooseNewPosition(double timeStep)
    {
        boolean escaped = this.isEscaped();

        Vector doorCenter = this.getDoorCenter().clone();

        Vector doorMin = doorCenter.clone().setX(doorCenter.getX() - this.getDoorWidth() / 2);
        Vector doorMax = doorCenter.clone().setX(doorCenter.getX() + this.getDoorWidth() / 2);

        if (!escaped)
        {
            // Aim for closest side of door, saves small amount of time per person
            Vector intendedPos = doorCenter;

            double targetX;
            if (this.getLocation().getX() > doorCenter.clone().getX())
                targetX = (Math.min(doorMax.getX() - this.getPhysicalRadius() * 2, this.getLocation().clone().getX()));
            else
                targetX = (Math.max(doorMin.getX() + this.getPhysicalRadius() * 2, this.getLocation().getX()));
            intendedPos = new Vector(targetX, doorCenter.clone().getY(), doorCenter.clone().getZ());

            // if too close to wall, adjust target closer to center of room
            if (Math.abs(this.getLocation().clone().getZ() - doorCenter.clone().getZ()) <= this.getPhysicalRadius() * 2 &&
                    (this.getLocation().getX() > doorMax.clone().getX() || this.getLocation().clone().getX() < doorMin.clone().getX()))
                intendedPos = new Vector(intendedPos.getX(), intendedPos.getY(), intendedPos.clone().getZ() + this.getPhysicalRadius() * 1.5);

            // count all other people closer to door
            List<Vector> closerLocations = this.otherPeopleLocations();
            Iterator<Vector> iter = closerLocations.iterator();
            while(iter.hasNext()) {
                Vector loc = iter.next();
                if(loc.getZ() >= doorCenter.getZ() && loc.distance(doorCenter) < this.getLocation().clone().distance(doorCenter)) {
                    continue;
                }
                iter.remove();
            }
            // reduce distance moved for every person closer to door
            timeStep /= (closerLocations.size() / 5 + 1);

            // don't move if there are closer people within your vicinity
            // this prevents people from piling up at a crowded doorway
            // person closest to door always prioritized
            for(Vector loc : closerLocations) {
                if (loc.clone().distance(getLocation().clone()) <= (Person.getPhysicalRadius() + Person.getExclusionRadius()) * 2) {
                    timeStep /= 10;
                    break;
                }
            }
            return getLocation().clone().add(intendedPos.clone().subtract(getLocation().clone()).normalize().multiply(maxSpeed * timeStep));
        }
        else
        {
            Vector moveForward = this.getLocation().clone().setZ(this.getLocation().clone().getZ() - 1);
            System.out.println("Already escaped, moving to: " + moveForward);
            return moveToward(this.getLocation().clone(), moveForward, timeStep);
        }
    }
}
