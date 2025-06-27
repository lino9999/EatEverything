// FoodComponent.java
package com.Lino.eatEverything.models;

import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import java.util.ArrayList;
import java.util.List;

public class FoodComponent {

    private int nutrition = 1;
    private float saturation = 0.5f;
    private float eatSeconds = 1.6f;
    private boolean canAlwaysEat = false;
    private List<FoodEffect> effects = new ArrayList<>();
    private Material convertTo = null;
    private String sound = "entity.generic.eat";
    private int cooldown = 0;
    private List<String> commands = new ArrayList<>();

    public FoodComponent() {
    }

    public FoodComponent(int nutrition, float saturation, float eatSeconds, boolean canAlwaysEat) {
        this.nutrition = nutrition;
        this.saturation = saturation;
        this.eatSeconds = eatSeconds;
        this.canAlwaysEat = canAlwaysEat;
    }

    // Getters and Setters
    public int getNutrition() {
        return nutrition;
    }

    public void setNutrition(int nutrition) {
        this.nutrition = Math.max(0, nutrition);
    }

    public float getSaturation() {
        return saturation;
    }

    public void setSaturation(float saturation) {
        this.saturation = Math.max(0, saturation);
    }

    public float getEatSeconds() {
        return eatSeconds;
    }

    public void setEatSeconds(float eatSeconds) {
        this.eatSeconds = Math.max(0.1f, eatSeconds);
    }

    public boolean canAlwaysEat() {
        return canAlwaysEat;
    }

    public void setCanAlwaysEat(boolean canAlwaysEat) {
        this.canAlwaysEat = canAlwaysEat;
    }

    public List<FoodEffect> getEffects() {
        return effects;
    }

    public void setEffects(List<FoodEffect> effects) {
        this.effects = effects;
    }

    public void addEffect(FoodEffect effect) {
        this.effects.add(effect);
    }

    public void clearEffects() {
        this.effects.clear();
    }

    public Material getConvertTo() {
        return convertTo;
    }

    public void setConvertTo(Material convertTo) {
        this.convertTo = convertTo;
    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }

    public int getCooldown() {
        return cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = Math.max(0, cooldown);
    }

    public List<String> getCommands() {
        return commands;
    }

    public void setCommands(List<String> commands) {
        this.commands = commands;
    }

    public void addCommand(String command) {
        this.commands.add(command);
    }
}