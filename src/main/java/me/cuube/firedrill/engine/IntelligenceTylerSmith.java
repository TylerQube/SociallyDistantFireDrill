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
        Vector doorMax = doorCenter.clone().setX(doorCenter.getX() - this.getDoorWidth() / 2);

//        System.out.println("Door center at: " + doorCenter.toString());
//        System.out.println("Currently located at: " + getLocation().toString());

        if (!escaped)
        {
            // calculate location of door based on walls
            /*var doorWallPoints = new List<double>();
            foreach (var wall in Walls)
            {
                var slope = wall.Position.Slope;
                var endpoints = wall.Endpoints;
                if (endpoints.Item1.Y == 0 && endpoints.Item2.Y == 0)
                {
                    doorWallPoints.Add(endpoints.Item1.X);
                    doorWallPoints.Add(endpoints.Item2.X);
                }
            }

            doorWallPoints.Sort();
            var doorEndpoints = new Tuple<Point, Point>(new Point(doorWallPoints[1], 0), new Point(doorWallPoints[2], 0));
            double doorCenterX = (doorEndpoints.Item1.X + doorEndpoints.Item2.X) / 2;
            var doorCenter = new Point(doorCenterX, 0);*/


            // Aim for closest side of door, saves small amount of time per person

            Vector intendedPos = doorCenter;
//            System.out.println("Intelligence target: " + intendedPos);
            return getLocation().clone().add(intendedPos.clone().subtract(getLocation().clone()).normalize().multiply(maxSpeed * timeStep));
//            return moveToward(this.getLocation().clone(), intendedPos, timeStep);
            /*if(doorOnX) {
                if (this.getLocation().getZ() > 0)
                    targetLat = (Math.min(doorMax.getZ() - this.getPhysicalRadius() * 2, this.getLocation().getZ()));
                else
                    targetLat = (Math.max(doorMin.getZ() + this.getPhysicalRadius() * 2, this.getLocation().getZ()));
                intendedPos = new Vector(doorCenter.getX(), doorCenter.getY(), targetLat);
            } else {
                if (this.getLocation().getX() > 0)
                    targetLat = (Math.min(doorMax.getX() - this.getPhysicalRadius() * 2, this.getLocation().getX()));
                else
                    targetLat = (Math.max(doorMin.getX() + this.getPhysicalRadius() * 2, this.getLocation().getX()));
                intendedPos = new Vector(doorCenter.getX(), doorCenter.getY(), targetLat);
            }*/


            // if too close to wall, adjust target closer to center of room
//            if(doorOnX) {
//                if (Math.abs(this.getLocation().getX() - doorCenter.getX()) <= this.getPhysicalRadius() * 2 &&
//                    (this.getLocation().getZ() > doorMax.getZ() || this.getLocation().getZ() < doorMin.getZ()))
//                        intendedPos = new Vector(intendedPos.getX() + this.getPhysicalRadius() * 1.5, intendedPos.getY(), intendedPos.getZ());
//            } else {
//                if (Math.abs(this.getLocation().getZ() - doorCenter.getZ()) <= this.getPhysicalRadius() * 2 &&
//                        (this.getLocation().getX() > doorMax.getX() || this.getLocation().getX() < doorMin.getX()))
//                    intendedPos = new Vector(intendedPos.getX(), intendedPos.getY(), intendedPos.getZ() + this.getPhysicalRadius() * 1.5);
//            }
            // count all other people closer to door
//            List<Vector> closerLocations = this.otherPeopleLocations();
//            Iterator<Vector> iter = closerLocations.iterator();
//            while(iter.hasNext()) {
//                Vector loc = iter.next();
//                if(doorOnX) {
//                    if(loc.getX() >= doorCenter.getX() && loc.distance(doorCenter) < loc.distance(this.getLocation()))
//                        continue;
//                } else {
//                    if(loc.getZ() >= doorCenter.getZ() && loc.distance(doorCenter) < loc.distance(this.getLocation()))
//                        continue;
//                }
//                iter.remove();
//            }
            /*for(Vector loc : closerLocations) {
                if(doorOnX) {
                    if(loc.getX() >= doorCenter.getX() && loc.distance(doorCenter) < loc.distance(this.getLocation()))
                        continue;
                } else {
                    if(loc.getZ() >= doorCenter.getZ() && loc.distance(doorCenter) < loc.distance(this.getLocation()))
                        continue;
                }
                closerLocations.remove(loc);
            }*/
            // reduce distance moved for every person closer to door
//            timeStep /= (closerLocations.size() / 5 + 1);

            // don't move if there are closer people within your vicinity
            // this prevents people from piling up at a crowded doorway
            // person closest to door always prioritized
            /*if (this.LocationsOfOtherPeople.ToList().Where(loc => Point.Distance(loc, this.Position) <= (this.RadiusOfPerson + this.RadiusOfPersonalZone) * 2 && closerLocations.Contains(loc)).ToList().Count != 0)
            timeStep /= 10;*/

//            System.out.println("Intelligence target: " + intendedPos);
//            return moveToward(intendedPos, timeStep);
        }
        else
        {
            Vector moveForward = this.getLocation().clone().setZ(this.getLocation().clone().getZ() - 1);
            System.out.println("Already escaped, moving to: " + moveForward);
            return moveToward(this.getLocation().clone(), moveForward, timeStep);
        }
    }
}
