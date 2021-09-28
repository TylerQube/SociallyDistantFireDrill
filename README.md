# Socially Distant Fire Drill 
## Recreation of IMSA Computational Science Project 2 in Minecraft 1.16.3

The original version of this project was written in C# as part of a one semester course surveying various fields within Computational Science. 

The scenario (pictured below) was posed in which a variable amount of people, represented by cylinders, must escape from a rectangular room through a doorway of variable width.
As an added constraint, all escapees must remain socially distanced for the entirety of the drill.

The assignment itself was to write a single algorithm used by each escapee to select their next intended location to move to.

![Original C# Project Simulation](https://github.com/TylerQube/FireDrillPlugin/blob/master/resources/fire_drill.png)

This plugin is an implementation of the project into Minecraft 1.16.3 using the Spigot API. Players use a command to define a new fire drill consisting of custom:
* Number of entities (representing escapees)
* Type of Minecraft entity
* Door width

```/firedrill setup [num_entities] [entity_type] [door_width]```

The player then uses a custom hotbar of items to define the location and bounds of the room to be escaped from. 
An additional hotbar controls the starting, stopping, and pausing of the drill. 

Once started, the room is visualized using ingame particle effects, and the entities will move autonomously each tick based on the return value of the implemented function.

![Minecraft Fire Drill](https://github.com/TylerQube/FireDrillPlugin/blob/master/resources/dolphin_drill.png)

The plugin currently uses a java implementation of my original C# algorithm, but developers may substitute their own as well!
