package com.Lino.eatEverything.listeners;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import com.Lino.eatEverything.EatEverything;
import com.Lino.eatEverything.models.FoodComponent;
import com.Lino.eatEverything.models.FoodEffect;
import com.Lino.eatEverything.utils.MessageUtils;

import java.util.*;

public class PlayerInteractListener implements Listener {

    private final EatEverything plugin;
    private final Map<UUID, Long> eatingPlayers = new HashMap<>();
    private final Map<UUID, Integer> eatingTasks = new HashMap<>();
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public PlayerInteractListener(EatEverything plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Only handle right-click actions
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        // Only handle main hand
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Check if item is air
        if (item.getType().isAir()) {
            return;
        }

        // Skip if item is already edible in vanilla
        if (item.getType().isEdible() && !hasCustomFoodData(item)) {
            return;
        }

        // Get food component
        FoodComponent food = plugin.getFoodManager().getFoodComponent(item);

        // Check if item has food data or default components
        if (!hasCustomFoodData(item) && !hasDefaultComponent(item.getType())) {
            return;
        }

        // Check if player can eat
        if (!food.canAlwaysEat() && player.getFoodLevel() >= 20) {
            return;
        }

        // Check cooldown
        UUID playerId = player.getUniqueId();
        if (food.getCooldown() > 0 && cooldowns.containsKey(playerId)) {
            long timeLeft = (cooldowns.get(playerId) + (food.getCooldown() * 1000)) - System.currentTimeMillis();
            if (timeLeft > 0) {
                player.setCooldown(item.getType(), (int) (timeLeft / 50)); // Convert to ticks
                return;
            }
        }

        // Check if already eating
        if (eatingPlayers.containsKey(playerId)) {
            return;
        }

        // Start eating
        event.setCancelled(true);
        startEating(player, item, food);
    }

    @EventHandler
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // Check if this is a custom food item
        if (!hasCustomFoodData(item)) {
            return;
        }

        // Cancel vanilla consumption and handle it ourselves
        event.setCancelled(true);

        FoodComponent food = plugin.getFoodManager().getFoodComponent(item);
        consumeItem(player, item, food);
    }

    private void startEating(Player player, ItemStack item, FoodComponent food) {
        UUID playerId = player.getUniqueId();

        // Mark player as eating
        eatingPlayers.put(playerId, System.currentTimeMillis());

        // Calculate eating time in ticks
        int eatTicks = (int) (food.getEatSeconds() * 20);

        // Create eating animation task
        BukkitRunnable eatTask = new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                // Check if player is still holding the item
                ItemStack currentItem = player.getInventory().getItemInMainHand();
                if (!currentItem.isSimilar(item)) {
                    stopEating(playerId);
                    return;
                }

                // Check if player is still sneaking (if required)
                if (plugin.getConfig().getBoolean("require-sneak-to-eat", false) && !player.isSneaking()) {
                    stopEating(playerId);
                    return;
                }

                // Play eating particles/sound periodically
                if (ticks % 4 == 0) {
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 0.5f, 1.0f);
                }

                // Check if finished eating
                if (ticks >= eatTicks) {
                    stopEating(playerId);
                    consumeItem(player, item, food);
                    return;
                }

                ticks++;
            }
        };

        eatTask.runTaskTimer(plugin, 0L, 1L);
        eatingTasks.put(playerId, eatTask.getTaskId());
    }

    private void stopEating(UUID playerId) {
        eatingPlayers.remove(playerId);

        if (eatingTasks.containsKey(playerId)) {
            Bukkit.getScheduler().cancelTask(eatingTasks.get(playerId));
            eatingTasks.remove(playerId);
        }
    }

    private void consumeItem(Player player, ItemStack item, FoodComponent food) {
        UUID playerId = player.getUniqueId();

        // Apply food effects
        player.setFoodLevel(Math.min(20, player.getFoodLevel() + food.getNutrition()));
        player.setSaturation(Math.min(player.getFoodLevel(), player.getSaturation() + food.getSaturation()));

        // Apply potion effects
        for (FoodEffect effect : food.getEffects()) {
            if (effect.shouldApply()) {
                player.addPotionEffect(effect.toPotionEffect());
            }
        }

        // Play consume sound
        try {
            Sound sound = Sound.valueOf(food.getSound().toUpperCase());
            player.getWorld().playSound(player.getLocation(), sound, 1.0f, 1.0f);
        } catch (IllegalArgumentException e) {
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 1.0f, 1.0f);
        }

        // Execute commands
        for (String command : food.getCommands()) {
            String processedCommand = command.replace("%player%", player.getName());
            if (command.startsWith("console:")) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand.substring(8));
            } else if (command.startsWith("player:")) {
                Bukkit.dispatchCommand(player, processedCommand.substring(7));
            } else {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
            }
        }

        // Handle item consumption
        if (player.getGameMode() != GameMode.CREATIVE) {
            item.setAmount(item.getAmount() - 1);

            // Convert to another item if specified
            if (food.getConvertTo() != null) {
                ItemStack convertedItem = new ItemStack(food.getConvertTo());

                // Add to inventory or drop if full
                HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(convertedItem);
                for (ItemStack drop : leftover.values()) {
                    player.getWorld().dropItem(player.getLocation(), drop);
                }
            }
        }

        // Set cooldown
        if (food.getCooldown() > 0) {
            cooldowns.put(playerId, System.currentTimeMillis());
            player.setCooldown(item.getType(), food.getCooldown() * 20); // Convert seconds to ticks
        }

        // Send consume message if configured
        if (plugin.getConfig().getBoolean("send-consume-message", true)) {
            String message = plugin.getConfig().getString("messages.item-consumed", "&aYou consumed %item%!")
                    .replace("%item%", item.getType().toString());
            player.sendMessage(MessageUtils.colorize(message));
        }
    }

    private boolean hasCustomFoodData(ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) {
            return false;
        }

        return item.getItemMeta().getPersistentDataContainer().has(
                new org.bukkit.NamespacedKey(plugin, "nutrition"),
                org.bukkit.persistence.PersistentDataType.INTEGER
        );
    }

    private boolean hasDefaultComponent(Material material) {
        return plugin.getConfig().contains("default-components." + material.toString());
    }
}