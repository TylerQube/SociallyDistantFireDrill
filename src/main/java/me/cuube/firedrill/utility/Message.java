package me.cuube.firedrill.utility;

import org.bukkit.ChatColor;

public class Message {
    private static final String prefix = ChatColor.RED + "[" + ChatColor.WHITE + "FIRE DRILL" + ChatColor.RED + "] " + ChatColor.RESET;

    public static String prefix() {
        return prefix;
    }
}
