package me.cuube.firedrill.plugin;

import me.cuube.firedrill.engine.Person;
import me.cuube.firedrill.utility.Geometry;
import me.cuube.firedrill.utility.ItemBuilder;
import me.cuube.firedrill.utility.Message;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FireDrillCreationManager implements Listener {
    private final FireDrillPlugin plugin;

    Map<UUID, SetupFireDrill> setupList = new HashMap<>();
    public Map<UUID, SetupFireDrill> getSetupList() {
        return this.setupList;
    }

    private final static String POINT_ONE_SELECTOR_NAME = "Set Location One";
    private final static String POINT_TWO_SELECTOR_NAME = "Set Location Two";
    private final static String CONFIRM_ITEM_NAME = "Confirm Selection";
    private final static String EXIT_SETUP_ITEM_NAME = "Exit Setup Mode";

    private final static ItemStack pointOneSelect = new ItemBuilder(Material.ORANGE_DYE).setName(POINT_ONE_SELECTOR_NAME);
    private final static ItemStack pointTwoSelect = new ItemBuilder(Material.CYAN_DYE).setName(POINT_TWO_SELECTOR_NAME);
    private final static ItemStack confirmItem = new ItemBuilder(Material.LIME_BANNER).setName(CONFIRM_ITEM_NAME);
    private final static ItemStack exitItem = new ItemBuilder(Material.BARRIER).setName(EXIT_SETUP_ITEM_NAME);

    public FireDrillCreationManager(FireDrillPlugin plugin) {
        this.plugin = plugin;
        this.drillDraw.runTaskTimer(this.plugin, 0L, 5L);
    }

    public void addToSetup(Player p, int numPeople, EntityType eType, double doorWidth) {
        if(setupList.containsKey(p.getUniqueId())) return;

        SetupFireDrill newSetup = new SetupFireDrill(numPeople, eType, doorWidth, p.getWorld());
        this.setupList.put(p.getUniqueId(), newSetup);
        plugin.getRollbackManager().savePlayer(p);

        p.getInventory().clear();
        p.getInventory().setItem(0, pointOneSelect);
        p.getInventory().setItem(1, pointTwoSelect);
        p.getInventory().setItem(8, exitItem);
    }

    public void removeFromSetup(Player p) {
        if(!setupList.containsKey(p.getUniqueId())) return;

        if(plugin.getRollbackManager().isStored(p))
            plugin.getRollbackManager().restorePlayer(p);
        setupList.remove(p.getUniqueId());
    }

    public boolean isInSetupMode(Player p) {
        return setupList.containsKey(p.getUniqueId());
    }

    private final BukkitRunnable drillDraw = new BukkitRunnable() {
        @Override
        public void run() {
            drawDrills();
        }
    };

    private void drawDrills() {
        for(SetupFireDrill drill : this.setupList.values()) {
            if(drill.getPointOne() != null && drill.getPointTwo() != null) {
                drill.drawBorderParticles();
            }
        }
    }

    @EventHandler
    public void itemUse(PlayerInteractEvent e) {
        System.out.println("Player interact event");
        if(!isInSetupMode(e.getPlayer())) return;
        if(e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if(!e.hasItem()) return;
        if(!e.getItem().hasItemMeta() || !e.getItem().getItemMeta().hasDisplayName()) return;

        Player p = e.getPlayer();
        String itemName = e.getItem().getItemMeta().getDisplayName();

        SetupFireDrill setupDrill = setupList.get(p.getUniqueId());
        if(itemName.equalsIgnoreCase(POINT_ONE_SELECTOR_NAME)) {
            setupDrill.setPointOne(getBlockLoc(p.getLocation()));
            p.playNote(p.getLocation(), Instrument.PIANO, Note.natural(1, Note.Tone.F));
            p.sendMessage(Message.prefix() + "Point one set!");
        }
        else if(itemName.equalsIgnoreCase(POINT_TWO_SELECTOR_NAME)) {
            setupDrill.setPointTwo(getBlockLoc(p.getLocation()));
            p.playNote(p.getLocation(), Instrument.PIANO, Note.natural(1, Note.Tone.F));
            p.sendMessage(Message.prefix() + "Point two set!");
        }
        else if(itemName.equalsIgnoreCase(EXIT_SETUP_ITEM_NAME)) {
            // remove player from setup mode
            removeFromSetup(p);
            p.sendMessage(Message.prefix() + "Exited setup mode.");
        }
        else if(itemName.equalsIgnoreCase(CONFIRM_ITEM_NAME)) {
            if(setupDrill.getPointOne() == null || setupDrill.getPointTwo() == null) {
                p.sendMessage(Message.prefix() + "Fire drill points are not set!");
                return;
            }

            if(setupDrill.getArea() < Person.getTotalArea() * setupDrill.getNumPeople()) {
                p.sendMessage(Message.prefix() + "Room too small for chosen number of people! Try defining a larger room.");
                return;
            }
            // remove player from setup
            removeFromSetup(p);
            // create Fire Drill
            plugin.getDrillManager().createDrill(p, setupDrill);
            return;
        }

        if(isInSetupMode(p) && setupDrill.getPointOne() != null && setupDrill.getPointTwo() != null && !p.getInventory().contains(confirmItem)) {
            p.getInventory().setItem(2, confirmItem);
        }
    }

    @EventHandler
    public void preventItemThrow(PlayerDropItemEvent e) {
        if(isInSetupMode(e.getPlayer())) e.setCancelled(true);
    }

    @EventHandler
    public void preventInvMove(InventoryMoveItemEvent e) {
        if(!(e.getInitiator().getHolder() instanceof Player)) return;

        Player p = (Player)e.getInitiator().getHolder();
        if(isInSetupMode(p)) e.setCancelled(true);
    }

    @EventHandler
    public void preventBlockPlace(BlockPlaceEvent e) {
        if(isInSetupMode(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    public void preventBlockBreak(BlockBreakEvent e) {
        if(isInSetupMode(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    public static Location getBlockLoc(Location loc) {
        return new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }
}
