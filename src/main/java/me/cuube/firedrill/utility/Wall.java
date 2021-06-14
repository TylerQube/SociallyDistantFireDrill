package me.cuube.firedrill.utility;

import org.bukkit.util.Vector;

import java.util.ArrayList;

public class Wall {
    private final Vector pointOne;
    private final Vector pointTwo;

    public Wall(Vector p1, Vector p2) {
        this.pointOne = p1;
        this.pointTwo = p2;
    }

    public Vector getPointOne() {
        return this.pointOne;
    }
    public Vector getPointTwo() {
        return this.pointTwo;
    }

    public boolean hitsWall(Vector loc, double personRadius) {
        return Geometry.distancePointToSegment(this.getPointOne(), this.getPointTwo(), loc) < personRadius;
    }

    public boolean intersectsWall(Vector oldLoc, Vector newLoc) {
        double oldX = oldLoc.getX();
        double oldZ = oldLoc.getZ();
        double newX = newLoc.getX();
        double newZ = newLoc.getZ();

            if(this.getPointOne().getX() == this.getPointTwo().getX()) {
                if ((Geometry.isBetween(this.getPointOne().getX(), oldX, newX))) {
                    if(Geometry.isBetween(oldZ, this.getPointOne().getZ(), this.getPointTwo().getZ()) &&
                    Geometry.isBetween(newZ, this.getPointOne().getZ(), this.getPointTwo().getZ())) {
                        return true;
                    }
                }
            }

            if(this.getPointOne().getZ() == this.getPointTwo().getZ()) {
                if ((Geometry.isBetween(this.getPointOne().getZ(), oldZ, newZ))) {
                    if(Geometry.isBetween(oldX, this.getPointOne().getX(), this.getPointTwo().getX()) &&
                    Geometry.isBetween(newX, this.getPointOne().getX(), this.getPointTwo().getX())) {
                        return true;
                    }

                }
            }
        return false;
    }
}
