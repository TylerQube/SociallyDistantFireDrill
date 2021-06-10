package me.cuube.firedrill.utility;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class Geometry {

    public static boolean intersectsWall(Vector oldLoc, Vector newLoc, ArrayList<Wall> walls) {
        return walls.stream().anyMatch(w -> w.intersectsWall(oldLoc, newLoc));
    }

    public static boolean hitsWall(Vector loc, double personRadius, ArrayList<Wall> walls) {
        return walls.stream().anyMatch(w -> w.hitsWall(loc, personRadius));
    }

    public static double distancePointToSegment(Vector endpointOne, Vector endpointTwo, Vector point)  {
        Vector AB = endpointTwo.subtract(endpointOne);
        Vector BE = point.subtract(endpointTwo);
        Vector AE = point.subtract(endpointOne);

        if(AB.dot(BE) > 0)
            return BE.length();
        else if(AB.dot(BE) < 0)
            return AE.length();
        else
            return Math.abs(AB.getX() * AE.getY() - AB.getY() * AE.getX());
    }

    public static boolean isBetween(double test, double min, double max) {
        return (min < test && test < max) ||
                (max < test && test < min);
    }

    public static Location locationFromVector(World world, Vector vec) {
        return new Location(world, vec.getX(), vec.getY(), vec.getZ());
    }

    public static BoundingBox boundsFromLocs(Location loc1, Location loc2) {
        return new BoundingBox(loc1.getX(), loc1.getY(), loc1.getZ(), loc2.getX(), loc2.getY(), loc2.getZ());
    }

    public static double secondsToTicks(double seconds) {
        return seconds * 20;
    }
}
