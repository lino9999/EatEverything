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
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;

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
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType().isAir()) {
            return;
        }

        if (item.getType().isEdible() && !hasCustomFoodData(item)) {
            return;
        }

        FoodComponent food = plugin.getFoodManager().getFoodComponent(item);

        if (!hasCustomFoodData(item) && !hasDefaultComponent(item.getType())) {
            return;
        }

        if (!food.canAlwaysEat() && player.getFoodLevel() >= 20) {
            return;
        }

        UUID playerId = player.getUniqueId();
        if (food.getCooldown() > 0 && cooldowns.containsKey(playerId)) {
            long timeLeft = (cooldowns.get(playerId) + (food.getCooldown() * 1000)) - System.currentTimeMillis();
            if (timeLeft > 0) {
                player.setCooldown(item.getType(), (int) (timeLeft / 50));
                return;
            }
        }

        if (eatingPlayers.containsKey(playerId)) {
            return;
        }

        event.setCancelled(true);
        startEating(player, item, food);
    }

    @EventHandler
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (!hasCustomFoodData(item)) {
            return;
        }

        event.setCancelled(true);

        FoodComponent food = plugin.getFoodManager().getFoodComponent(item);
        consumeItem(player, item, food);
    }

    private void startEating(Player player, ItemStack item, FoodComponent food) {
        UUID playerId = player.getUniqueId();

        eatingPlayers.put(playerId, System.currentTimeMillis());

        int eatTicks = (int) (food.getEatSeconds() * 20);

        BukkitRunnable eatTask = new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                ItemStack currentItem = player.getInventory().getItemInMainHand();
                if (!currentItem.isSimilar(item)) {
                    stopEating(playerId);
                    return;
                }

                if (plugin.getConfig().getBoolean("require-sneak-to-eat", false) && !player.isSneaking()) {
                    stopEating(playerId);
                    return;
                }

                if (ticks % 4 == 0) {
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 0.5f, 1.0f);
                }

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

        player.setFoodLevel(Math.min(20, player.getFoodLevel() + food.getNutrition()));
        player.setSaturation(Math.min(player.getFoodLevel(), player.getSaturation() + food.getSaturation()));

        for (FoodEffect effect : food.getEffects()) {
            if (effect.shouldApply()) {
                player.addPotionEffect(effect.toPotionEffect());
            }
        }

        try {
            Sound sound = Registry.SOUNDS.get(NamespacedKey.minecraft(food.getSound().toLowerCase().replace(".", "_")));
            if (sound != null) {
                player.getWorld().playSound(player.getLocation(), sound, 1.0f, 1.0f);
            } else {
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 1.0f, 1.0f);
            }
        } catch (Exception e) {
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 1.0f, 1.0f);
        }

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

        if (player.getGameMode() != GameMode.CREATIVE) {
            item.setAmount(item.getAmount() - 1);

            if (food.getConvertTo() != null) {
                ItemStack convertedItem = new ItemStack(food.getConvertTo());

                HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(convertedItem);
                for (ItemStack drop : leftover.values()) {
                    player.getWorld().dropItem(player.getLocation(), drop);
                }
            }
        }

        if (food.getCooldown() > 0) {
            cooldowns.put(playerId, System.currentTimeMillis());
            player.setCooldown(item.getType(), food.getCooldown() * 20);
        }

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