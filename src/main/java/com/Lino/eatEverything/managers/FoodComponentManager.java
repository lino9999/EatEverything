package com.Lino.eatEverything.managers;

import com.Lino.eatEverything.EatEverything;
import com.Lino.eatEverything.models.FoodComponent;
import com.Lino.eatEverything.models.FoodEffect;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import com.Lino.eatEverything.utils.MessageUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class FoodComponentManager {

    private final EatEverything plugin;
    private final Map<String, FoodComponent> defaultComponents = new HashMap<>();
    private final Map<String, ItemStack> customItems = new HashMap<>();

    private final NamespacedKey nutritionKey;
    private final NamespacedKey saturationKey;
    private final NamespacedKey eatSecondsKey;
    private final NamespacedKey canAlwaysEatKey;
    private final NamespacedKey effectsKey;
    private final NamespacedKey convertToKey;
    private final NamespacedKey soundKey;
    private final NamespacedKey cooldownKey;
    private final NamespacedKey commandsKey;

    public FoodComponentManager(EatEverything plugin) {
        this.plugin = plugin;

        this.nutritionKey = new NamespacedKey(plugin, "nutrition");
        this.saturationKey = new NamespacedKey(plugin, "saturation");
        this.eatSecondsKey = new NamespacedKey(plugin, "eat_seconds");
        this.canAlwaysEatKey = new NamespacedKey(plugin, "can_always_eat");
        this.effectsKey = new NamespacedKey(plugin, "effects");
        this.convertToKey = new NamespacedKey(plugin, "convert_to");
        this.soundKey = new NamespacedKey(plugin, "sound");
        this.cooldownKey = new NamespacedKey(plugin, "cooldown");
        this.commandsKey = new NamespacedKey(plugin, "commands");

        loadDefaultComponents();
        loadCustomItems();
    }

    private void loadDefaultComponents() {
        defaultComponents.clear();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("default-components");

        if (section == null) return;

        for (String materialName : section.getKeys(false)) {
            try {
                Material material = Material.valueOf(materialName);
                ConfigurationSection compSection = section.getConfigurationSection(materialName);

                if (compSection == null) continue;

                FoodComponent component = new FoodComponent();
                component.setNutrition(compSection.getInt("nutrition", 1));
                component.setSaturation((float) compSection.getDouble("saturation", 0.5));
                component.setEatSeconds((float) compSection.getDouble("eat-seconds", 1.6));
                component.setCanAlwaysEat(compSection.getBoolean("can-always-eat", false));

                if (compSection.contains("effects")) {
                    List<Map<?, ?>> effectList = compSection.getMapList("effects");
                    for (Map<?, ?> effectMap : effectList) {
                        String typeName = (String) effectMap.get("type");
                        PotionEffectType type = Registry.EFFECT.get(NamespacedKey.minecraft(typeName.toLowerCase()));

                        if (type != null) {
                            int duration = ((Number) effectMap.get("duration")).intValue();
                            int amplifier = ((Number) effectMap.get("amplifier")).intValue();
                            float probability = effectMap.containsKey("probability") ?
                                    ((Number) effectMap.get("probability")).floatValue() : 1.0f;

                            component.addEffect(new FoodEffect(type, duration, amplifier, probability));
                        }
                    }
                }

                if (compSection.contains("convert-to")) {
                    try {
                        Material convertTo = Material.valueOf(compSection.getString("convert-to"));
                        component.setConvertTo(convertTo);
                    } catch (IllegalArgumentException ignored) {}
                }

                component.setSound(compSection.getString("sound", "entity.generic.eat"));
                component.setCooldown(compSection.getInt("cooldown", 0));

                if (compSection.contains("commands")) {
                    component.setCommands(compSection.getStringList("commands"));
                }

                defaultComponents.put(materialName, component);

            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid material in default-components: " + materialName);
            }
        }
    }

    private void loadCustomItems() {
        customItems.clear();

        File customItemsFile = new File(plugin.getDataFolder(), "custom_items.yml");
        if (!customItemsFile.exists()) {
            plugin.saveResource("custom_items.yml", false);
        }

        FileConfiguration itemsConfig = YamlConfiguration.loadConfiguration(customItemsFile);
        ConfigurationSection itemsSection = itemsConfig.getConfigurationSection("items");

        if (itemsSection == null) return;

        for (String itemId : itemsSection.getKeys(false)) {
            ConfigurationSection itemSection = itemsSection.getConfigurationSection(itemId);
            if (itemSection == null) continue;

            try {
                Material material = Material.valueOf(itemSection.getString("material", "APPLE"));
                ItemStack item = new ItemStack(material);
                ItemMeta meta = item.getItemMeta();

                if (meta == null) continue;

                if (itemSection.contains("name")) {
                    meta.setDisplayName(MessageUtils.colorize(itemSection.getString("name")));
                }

                if (itemSection.contains("lore")) {
                    List<String> lore = new ArrayList<>();
                    for (String line : itemSection.getStringList("lore")) {
                        lore.add(MessageUtils.colorize(line));
                    }
                    meta.setLore(lore);
                }

                PersistentDataContainer container = meta.getPersistentDataContainer();

                container.set(nutritionKey, PersistentDataType.INTEGER, itemSection.getInt("nutrition", 1));
                container.set(saturationKey, PersistentDataType.FLOAT, (float) itemSection.getDouble("saturation", 0.5));
                container.set(eatSecondsKey, PersistentDataType.FLOAT, (float) itemSection.getDouble("eat-seconds", 1.6));
                container.set(canAlwaysEatKey, PersistentDataType.BYTE, (byte) (itemSection.getBoolean("can-always-eat", false) ? 1 : 0));

                if (itemSection.contains("effects")) {
                    StringBuilder effectsData = new StringBuilder();
                    List<Map<?, ?>> effectList = itemSection.getMapList("effects");

                    for (Map<?, ?> effectMap : effectList) {
                        String typeName = (String) effectMap.get("type");
                        int duration = ((Number) effectMap.get("duration")).intValue();
                        int amplifier = ((Number) effectMap.get("amplifier")).intValue();
                        float probability = effectMap.containsKey("probability") ?
                                ((Number) effectMap.get("probability")).floatValue() : 1.0f;

                        effectsData.append(typeName).append(":").append(duration).append(":")
                                .append(amplifier).append(":").append(probability).append(";");
                    }

                    if (effectsData.length() > 0) {
                        container.set(effectsKey, PersistentDataType.STRING, effectsData.toString());
                    }
                }

                if (itemSection.contains("convert-to")) {
                    container.set(convertToKey, PersistentDataType.STRING, itemSection.getString("convert-to"));
                }

                container.set(soundKey, PersistentDataType.STRING, itemSection.getString("sound", "entity.generic.eat"));
                container.set(cooldownKey, PersistentDataType.INTEGER, itemSection.getInt("cooldown", 0));

                if (itemSection.contains("commands")) {
                    container.set(commandsKey, PersistentDataType.STRING, String.join("|", itemSection.getStringList("commands")));
                }

                item.setItemMeta(meta);
                customItems.put(itemId, item);

            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load custom item: " + itemId);
                e.printStackTrace();
            }
        }
    }

    public FoodComponent getFoodComponent(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return new FoodComponent();
        }

        FoodComponent component = new FoodComponent();

        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();

            if (container.has(nutritionKey, PersistentDataType.INTEGER)) {
                component.setNutrition(container.get(nutritionKey, PersistentDataType.INTEGER));
                component.setSaturation(container.getOrDefault(saturationKey, PersistentDataType.FLOAT, 0.5f));
                component.setEatSeconds(container.getOrDefault(eatSecondsKey, PersistentDataType.FLOAT, 1.6f));
                component.setCanAlwaysEat(container.getOrDefault(canAlwaysEatKey, PersistentDataType.BYTE, (byte) 0) == 1);

                String effectsData = container.get(effectsKey, PersistentDataType.STRING);
                if (effectsData != null && !effectsData.isEmpty()) {
                    String[] effects = effectsData.split(";");
                    for (String effectStr : effects) {
                        String[] parts = effectStr.split(":");
                        if (parts.length >= 4) {
                            PotionEffectType type = Registry.EFFECT.get(NamespacedKey.minecraft(parts[0].toLowerCase()));
                            if (type != null) {
                                int duration = Integer.parseInt(parts[1]);
                                int amplifier = Integer.parseInt(parts[2]);
                                float probability = Float.parseFloat(parts[3]);
                                component.addEffect(new FoodEffect(type, duration, amplifier, probability));
                            }
                        }
                    }
                }

                String convertTo = container.get(convertToKey, PersistentDataType.STRING);
                if (convertTo != null) {
                    try {
                        component.setConvertTo(Material.valueOf(convertTo));
                    } catch (IllegalArgumentException ignored) {}
                }

                component.setSound(container.getOrDefault(soundKey, PersistentDataType.STRING, "entity.generic.eat"));
                component.setCooldown(container.getOrDefault(cooldownKey, PersistentDataType.INTEGER, 0));

                String commandsData = container.get(commandsKey, PersistentDataType.STRING);
                if (commandsData != null && !commandsData.isEmpty()) {
                    component.setCommands(Arrays.asList(commandsData.split("\\|")));
                }

                return component;
            }
        }

        FoodComponent defaultComp = defaultComponents.get(item.getType().toString());
        if (defaultComp != null) {
            return defaultComp;
        }

        return component;
    }

    public void setNutrition(ItemStack item, int nutrition) {
        if (item == null || item.getType().isAir()) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        meta.getPersistentDataContainer().set(nutritionKey, PersistentDataType.INTEGER, nutrition);
        item.setItemMeta(meta);
    }

    public void setSaturation(ItemStack item, float saturation) {
        if (item == null || item.getType().isAir()) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        meta.getPersistentDataContainer().set(saturationKey, PersistentDataType.FLOAT, saturation);
        item.setItemMeta(meta);
    }

    public void setEatSeconds(ItemStack item, float eatSeconds) {
        if (item == null || item.getType().isAir()) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        meta.getPersistentDataContainer().set(eatSecondsKey, PersistentDataType.FLOAT, eatSeconds);
        item.setItemMeta(meta);
    }

    public void setCanAlwaysEat(ItemStack item, boolean canAlwaysEat) {
        if (item == null || item.getType().isAir()) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        meta.getPersistentDataContainer().set(canAlwaysEatKey, PersistentDataType.BYTE, (byte) (canAlwaysEat ? 1 : 0));
        item.setItemMeta(meta);
    }

    public void addEffect(ItemStack item, PotionEffectType type, int duration, int amplifier, float probability) {
        if (item == null || item.getType().isAir()) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer container = meta.getPersistentDataContainer();
        String existingEffects = container.getOrDefault(effectsKey, PersistentDataType.STRING, "");

        String newEffect = type.getKey().getKey() + ":" + duration + ":" + amplifier + ":" + probability;

        if (existingEffects.isEmpty()) {
            container.set(effectsKey, PersistentDataType.STRING, newEffect);
        } else {
            container.set(effectsKey, PersistentDataType.STRING, existingEffects + ";" + newEffect);
        }

        item.setItemMeta(meta);
    }

    public void clearEffects(ItemStack item) {
        if (item == null || item.getType().isAir()) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        meta.getPersistentDataContainer().remove(effectsKey);
        item.setItemMeta(meta);
    }

    public String generateGiveCommand(ItemStack item) {
        StringBuilder command = new StringBuilder("/give @p " + item.getType().toString().toLowerCase());

        FoodComponent food = getFoodComponent(item);
        command.append("{food_properties:{");
        command.append("nutrition:").append(food.getNutrition()).append(",");
        command.append("saturation:").append(food.getSaturation()).append(",");
        command.append("can_always_eat:").append(food.canAlwaysEat());

        if (!food.getEffects().isEmpty()) {
            command.append(",effects:[");
            for (FoodEffect effect : food.getEffects()) {
                command.append("{effect:{id:\"minecraft:").append(effect.getType().getKey().getKey())
                        .append("\",amplifier:").append(effect.getAmplifier())
                        .append(",duration:").append(effect.getDuration())
                        .append("},probability:").append(effect.getProbability()).append("},");
            }
            command.deleteCharAt(command.length() - 1);
            command.append("]");
        }

        command.append("}}");

        return command.toString();
    }

    public ItemStack getCustomItem(String id) {
        return customItems.get(id);
    }

    public Set<String> getCustomItemIds() {
        return customItems.keySet();
    }

    public void saveData() {
    }

    public void reload() {
        loadDefaultComponents();
        loadCustomItems();
    }
}