package me.cuube.firedrill.plugin;

import org.bukkit.Bukkit;
import org.bukkit.block.data.type.Fire;
import org.bukkit.plugin.java.JavaPlugin;

public class FireDrillPlugin extends JavaPlugin {
    private static FireDrillPlugin instance;
    private FireDrillManager drillManager;
    private RollbackManager rollbackManager;
    private FireDrillCreationManager creationManager;

    @Override
    public void onEnable() {
        System.out.println("Fire Drill Enabled!");

        instance = this;
        drillManager = new FireDrillManager(this);
        rollbackManager = new RollbackManager(this);
        creationManager = new FireDrillCreationManager(this);

        Bukkit.getServer().getPluginManager().registerEvents(creationManager, this);

        this.getCommand("firedrill").setExecutor(new FireDrillCommand());
    }

    @Override
    public void onDisable() {
        System.out.println("Fire Drill Disabled.");
    }

    public static FireDrillPlugin getInstance() {
        return instance;
    }

    public FireDrillManager getDrillManager() {
        return this.drillManager;
    }

    public RollbackManager getRollbackManager() {
        return this.rollbackManager;
    }

    public FireDrillCreationManager getCreationManager() {
        return this.creationManager;
    }
}
