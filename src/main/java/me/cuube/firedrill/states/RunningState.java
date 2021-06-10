package me.cuube.firedrill.states;

import me.cuube.firedrill.plugin.FireDrill;
import me.cuube.firedrill.plugin.FireDrillPlugin;
import me.cuube.firedrill.plugin.RollbackManager;
import me.cuube.firedrill.plugin.SetupFireDrill;
import me.cuube.firedrill.utility.ItemBuilder;
import me.cuube.firedrill.utility.Message;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

public class RunningState extends DrillState {
    private FireDrill drill;

    public RunningState(FireDrill drill) {
        this.drill = drill;
    }

    private final static String PLAY_ITEM_NAME = "Start/Resume Fire Drill";
    private final static String PAUSE_ITEM_NAME = "Pause Fire Drill";
    private final static String STOP_ITEM_NAME = "Stop Fire Drill";

    private final static ItemStack playItem = new ItemBuilder(Material.LIME_CONCRETE).setName(PLAY_ITEM_NAME);
    private final static ItemStack pauseItem = new ItemBuilder(Material.ORANGE_CONCRETE).setName(PAUSE_ITEM_NAME);
    private final static ItemStack stopItem = new ItemBuilder(Material.BARRIER).setName(STOP_ITEM_NAME);


    public void setup() {
        Player p = this.drill.getOwner();

        /* Backup player inventory */
        RollbackManager rollbackMngr = FireDrillPlugin.getInstance().getRollbackManager();
        rollbackMngr.savePlayer(p);

        /* Give fire drill toolbar */
        p.getInventory().clear();
        p.getInventory().setItem(0, playItem);
        p.getInventory().setItem(1, pauseItem);
        p.getInventory().setItem(2, stopItem);

        p.sendMessage(Message.prefix() + "Use the items to view the fire drill simulation!");
    }

    private final double timeStep = 0.1;
    BukkitRunnable fireDrillTick;

    @EventHandler
    public void itemUse(PlayerInteractEvent e) {
        if(!e.getPlayer().getUniqueId().equals(this.drill.getOwner().getUniqueId())) return;
        if(e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if(!e.hasItem()) return;
        if(!e.getItem().hasItemMeta() || !e.getItem().getItemMeta().hasDisplayName()) return;

        Player p = e.getPlayer();
        String itemName = e.getItem().getItemMeta().getDisplayName();

        if(itemName.equalsIgnoreCase(PLAY_ITEM_NAME)) {
            if(this.drill.isRunning()) {
                p.sendMessage(Message.prefix() + "Fire drill is already running.");
                return;
            } else {
                this.drill.setRunning(true);
                fireDrillTick = new BukkitRunnable() {
                    @Override
                    public void run() {
                        boolean tick = drill.tick(timeStep);
                        if(!tick) {
                            cancel();
                            drill.stop();
                            drill.getOwner().sendMessage(Message.prefix() + "Drill complete!");
                            return;
                        }
                    }
                };
                fireDrillTick.runTaskTimer(FireDrillPlugin.getInstance(), 0L, (long)(timeStep * 20));
                if(drill.getTime() == 0) {
                    p.sendMessage(Message.prefix() + ChatColor.GREEN + "Fire Drill started.");
                } else {
                    p.sendMessage(Message.prefix() + ChatColor.GREEN + "Fire Drill resumed.");
                }
                this.drill.setColor(Color.fromRGB(0, 255, 0));
            }
        }
        else if(itemName.equalsIgnoreCase(PAUSE_ITEM_NAME)) {
            if(!this.drill.isRunning()) {
                p.sendMessage(Message.prefix() + "Fire drill is already paused.");
            } else {
                this.drill.setRunning(false);
                fireDrillTick.cancel();
                p.sendMessage(Message.prefix() + ChatColor.YELLOW + "Fire Drill Paused at: " + String.format("%.2f", this.drill.getTime()) + "s");
                this.drill.setColor(Color.fromRGB(255, 165, 0));
            }
        }
        else if(itemName.equalsIgnoreCase(STOP_ITEM_NAME)) {
            if(this.drill.isRunning())
                fireDrillTick.cancel();
            drill.stop();
            p.sendMessage(Message.prefix() + ChatColor.RED + "Fire Drill Stopped.");
        }
    }

    @EventHandler
    public void preventItemThrow(PlayerDropItemEvent e) {
        if(e.getPlayer().getUniqueId().equals(this.drill.getOwner().getUniqueId())) e.setCancelled(true);
    }

    @EventHandler
    public void preventInvMove(InventoryMoveItemEvent e) {
        if(!(e.getInitiator().getHolder() instanceof Player)) return;

        Player p = (Player)e.getInitiator().getHolder();
        if(p.getUniqueId().equals(this.drill.getOwner().getUniqueId())) e.setCancelled(true);
    }

    @EventHandler
    public void preventBlockPlace(BlockPlaceEvent e) {
        if(e.getPlayer().getUniqueId().equals(drill.getOwner().getUniqueId()) || drill.getBounds().contains(e.getBlockPlaced().getLocation().toVector())) {
            e.setCancelled(true);
        }
    }

    public void preventBlockBreak(BlockBreakEvent e) {
        if(e.getPlayer().getUniqueId().equals(drill.getOwner().getUniqueId()) || drill.getBounds().contains(e.getBlock().getLocation().toVector())) {
            e.setCancelled(true);
        }
    }
}
