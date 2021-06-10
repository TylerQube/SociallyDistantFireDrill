package me.cuube.firedrill.plugin;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RollbackManager {
    private FireDrillPlugin plugin;

    private final Map<UUID, ItemStack[]> rollbackInventories = new HashMap<>();
    private final Map<UUID, ItemStack[]> rollbackArmor = new HashMap<>();

    private final Map<UUID, GameMode> rollbackGamemodes = new HashMap<>();
    private final Map<UUID, Integer> rollbackExp = new HashMap<>();

    public RollbackManager(FireDrillPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isStored(Player p) {
        return rollbackInventories.containsKey(p.getUniqueId()) ||
                rollbackArmor.containsKey(p.getUniqueId()) ||
                rollbackGamemodes.containsKey(p.getUniqueId()) ||
                rollbackExp.containsKey(p.getUniqueId());
    }

    public void savePlayer(Player p) {
        if(isStored(p)) {
            throw new IllegalArgumentException("Rollback Manager may only store each player once.");
        }
        rollbackInventories.put(p.getUniqueId(), p.getInventory().getContents());
        rollbackArmor.put(p.getUniqueId(), p.getInventory().getArmorContents());
        rollbackGamemodes.put(p.getUniqueId(), p.getGameMode());
        rollbackExp.put(p.getUniqueId(), p.getTotalExperience());
    }

    public void restorePlayer(Player p) {
        p.getInventory().clear();
        if(rollbackInventories.containsKey(p.getUniqueId())) {
            ItemStack[] inv = rollbackInventories.get(p.getUniqueId());
            p.getInventory().setContents(inv);
            rollbackInventories.remove(p.getUniqueId());
        }

        if(rollbackArmor.containsKey(p.getUniqueId())) {
            ItemStack[] armor = rollbackArmor.get(p.getUniqueId());
            p.getInventory().setArmorContents(armor);
            rollbackArmor.remove(p.getUniqueId());
        }

        if(rollbackGamemodes.containsKey(p.getUniqueId())) {
            p.setGameMode(rollbackGamemodes.get(p.getUniqueId()));
            rollbackGamemodes.remove(p.getUniqueId());
        }

        if(rollbackExp.containsKey(p.getUniqueId())) {
            p.setTotalExperience(rollbackExp.get(p.getUniqueId()));
            rollbackExp.remove(p.getUniqueId());
        }
    }
}
