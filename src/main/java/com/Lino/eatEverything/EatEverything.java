package com.Lino.eatEverything;

import org.bukkit.plugin.java.JavaPlugin;
import com.Lino.eatEverything.commands.EatEverythingCommand;
import com.Lino.eatEverything.listeners.PlayerInteractListener;
import com.Lino.eatEverything.managers.FoodComponentManager;
import com.Lino.eatEverything.managers.ConfigManager;
import com.Lino.eatEverything.utils.UpdateChecker;

public class EatEverything extends JavaPlugin {

    private static EatEverything instance;
    private FoodComponentManager foodManager;
    private ConfigManager configManager;
    private UpdateChecker updateChecker;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize configuration
        saveDefaultConfig();
        configManager = new ConfigManager(this);

        // Initialize managers
        foodManager = new FoodComponentManager(this);
        updateChecker = new UpdateChecker(this);

        // Register commands
        getCommand("eateverything").setExecutor(new EatEverythingCommand(this));
        getCommand("eateverything").setTabCompleter(new EatEverythingCommand(this));

        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);

        // Check for updates
        if (getConfig().getBoolean("check-updates", true)) {
            updateChecker.checkForUpdates();
        }

        getLogger().info("EatEverything v" + getDescription().getVersion() + " has been enabled!");
    }

    @Override
    public void onDisable() {
        if (foodManager != null) {
            foodManager.saveData();
        }
        getLogger().info("EatEverything has been disabled!");
    }

    public static EatEverything getInstance() {
        return instance;
    }

    public FoodComponentManager getFoodManager() {
        return foodManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public UpdateChecker getUpdateChecker() {
        return updateChecker;
    }

    public void reload() {
        reloadConfig();
        configManager.reload();
        foodManager.reload();
    }
}