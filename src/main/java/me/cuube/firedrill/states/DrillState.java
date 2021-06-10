package me.cuube.firedrill.states;

import me.cuube.firedrill.plugin.FireDrillPlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public abstract class DrillState implements Listener {


    public void enable(FireDrillPlugin plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void disable(FireDrillPlugin plugin) {
        HandlerList.unregisterAll(this);
    }

    public abstract void setup();
}
