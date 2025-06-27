// ConfigManager.java
package com.Lino.eatEverything.managers;

import org.bukkit.configuration.file.FileConfiguration;
import com.Lino.eatEverything.EatEverything;

public class ConfigManager {

    private final EatEverything plugin;
    private FileConfiguration config;

    // Config values
    private boolean checkUpdates;
    private boolean requireSneak;
    private boolean sendConsumeMessage;
    private String consumeMessage;

    public ConfigManager(EatEverything plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();

        // Load values
        checkUpdates = config.getBoolean("check-updates", true);
        requireSneak = config.getBoolean("require-sneak-to-eat", false);
        sendConsumeMessage = config.getBoolean("send-consume-message", true);
        consumeMessage = config.getString("messages.item-consumed", "&aYou consumed %item%!");
    }

    public boolean isCheckUpdates() {
        return checkUpdates;
    }

    public boolean isRequireSneak() {
        return requireSneak;
    }

    public boolean isSendConsumeMessage() {
        return sendConsumeMessage;
    }

    public String getConsumeMessage() {
        return consumeMessage;
    }
}
