// FoodEffect.java
package com.Lino.eatEverything.models;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class FoodEffect {

    private final PotionEffectType type;
    private final int duration;
    private final int amplifier;
    private final float probability;

    public FoodEffect(PotionEffectType type, int duration, int amplifier, float probability) {
        this.type = type;
        this.duration = duration;
        this.amplifier = amplifier;
        this.probability = Math.max(0f, Math.min(1f, probability));
    }

    public FoodEffect(PotionEffectType type, int duration, int amplifier) {
        this(type, duration, amplifier, 1.0f);
    }

    public PotionEffectType getType() {
        return type;
    }

    public int getDuration() {
        return duration;
    }

    public int getAmplifier() {
        return amplifier;
    }

    public float getProbability() {
        return probability;
    }

    public PotionEffect toPotionEffect() {
        return new PotionEffect(type, duration, amplifier);
    }

    public boolean shouldApply() {
        return Math.random() < probability;
    }
}