// UpdateChecker.java
package com.Lino.eatEverything.utils;

import com.yourname.eateverything.EatEverything;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateChecker {

    private final EatEverything plugin;
    private final int resourceId = 123456; // Replace with your SpigotMC resource ID

    public UpdateChecker(EatEverything plugin) {
        this.plugin = plugin;
    }

    public void checkForUpdates() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(
                        "https://api.spigotmc.org/legacy/update.php?resource=" + resourceId
                ).openConnection();

                connection.setRequestMethod("GET");
                String latestVersion = new BufferedReader(
                        new InputStreamReader(connection.getInputStream())
                ).readLine();

                String currentVersion = plugin.getDescription().getVersion();

                if (!currentVersion.equalsIgnoreCase(latestVersion)) {
                    plugin.getLogger().info("A new version of EatEverything is available!");
                    plugin.getLogger().info("Current version: " + currentVersion);
                    plugin.getLogger().info("Latest version: " + latestVersion);
                    plugin.getLogger().info("Download at: https://www.spigotmc.org/resources/" + resourceId);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to check for updates: " + e.getMessage());
            }
        });
    }

    public String getUpdateStatus() {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(
                    "https://api.spigotmc.org/legacy/update.php?resource=" + resourceId
            ).openConnection();

            connection.setRequestMethod("GET");
            String latestVersion = new BufferedReader(
                    new InputStreamReader(connection.getInputStream())
            ).readLine();

            String currentVersion = plugin.getDescription().getVersion();

            if (!currentVersion.equalsIgnoreCase(latestVersion)) {
                return "&aA new version is available! &e" + latestVersion + " &7(Current: " + currentVersion + ")";
            } else {
                return "&aYou are running the latest version!";
            }
        } catch (Exception e) {
            return "&cFailed to check for updates: " + e.getMessage();
        }
    }
}