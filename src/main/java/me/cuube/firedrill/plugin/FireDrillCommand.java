package me.cuube.firedrill.plugin;

import me.cuube.firedrill.engine.Person;
import me.cuube.firedrill.utility.Message;
import me.cuube.firedrill.utility.UtilityFunctions;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Random;

public class FireDrillCommand implements CommandExecutor {
    private final Random rand = new Random();

    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if(!(sender instanceof Player)) return true;

        Player p = (Player)sender;
        if(args.length < 1) {
            p.sendMessage(Message.prefix() + "/firedrill [setup]");
            return true;
        }

        if(args[0].equalsIgnoreCase("setup")) {
            if(args.length < 4 || !UtilityFunctions.isInt(args[1]) || !UtilityFunctions.isDouble(args[3])) {
                p.sendMessage(Message.prefix() + "/firedrill setup [num_people] [entity_type] [door_width]");
                return true;
            }
            int numPeople = Integer.parseInt(args[1]);
            double doorWidth = Double.parseDouble(args[3]);
            EntityType entityType;
            try {
                entityType = EntityType.valueOf(args[2].toUpperCase());
            } catch (IllegalArgumentException e) {
                switch (args[2].toLowerCase()) {
                    case "villager":
                        entityType = EntityType.VILLAGER;
                        break;
                    case "zombie":
                        entityType = EntityType.ZOMBIE;
                        break;
                    case "piglin":
                        entityType = EntityType.PIGLIN;
                        break;
                    case "random":
                    default:
                        Entity entity;
                        do {
                            entityType = EntityType.values()[rand.nextInt(EntityType.values().length)];
                        } while (!entityType.isAlive() || !entityType.isSpawnable());
                        break;
                }
            }

            FireDrillPlugin.getInstance().getCreationManager().addToSetup(p, numPeople, entityType, doorWidth);
            p.sendMessage(Message.prefix() + "Creating fire drill simulation: " + numPeople + " entities.");
        } else {
            p.sendMessage(Message.prefix() + "/firedrill [setup]");
            return true;
        }
        return true;
    }
}
