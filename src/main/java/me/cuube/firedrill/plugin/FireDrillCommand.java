package me.cuube.firedrill.plugin;

import me.cuube.firedrill.utility.Message;
import me.cuube.firedrill.utility.UtilityFunctions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Locale;

public class FireDrillCommand implements CommandExecutor {
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
            switch (args[2].toLowerCase()) {
                default:
                case "villager":
                    entityType = EntityType.VILLAGER;
                    break;
                case "zombie":
                    entityType = EntityType.ZOMBIE;
                    break;
                case "piglin":
                    entityType = EntityType.PIGLIN;
                    break;
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
