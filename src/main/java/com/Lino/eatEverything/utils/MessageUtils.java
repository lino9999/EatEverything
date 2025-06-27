package com.Lino.eatEverything.utils;

import org.bukkit.ChatColor;

public class MessageUtils {

    public static String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String strip(String message) {
        return ChatColor.stripColor(message);
    }
}