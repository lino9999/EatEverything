package com.Lino.eatEverything.commands;

import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import com.Lino.eatEverything.EatEverything;
import com.Lino.eatEverything.models.FoodComponent;
import com.Lino.eatEverything.utils.MessageUtils;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;

import java.util.*;

public class EatEverythingCommand implements CommandExecutor, TabCompleter {

    private final EatEverything plugin;

    public EatEverythingCommand(EatEverything plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!sender.hasPermission("eateverything.main")) {
                sender.sendMessage(MessageUtils.colorize("&cYou don't have permission to use this command!"));
                return true;
            }
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "saturation":
                return handleSaturation(sender, args);
            case "nutrition":
                return handleNutrition(sender, args);
            case "eatseconds":
                return handleEatSeconds(sender, args);
            case "edible":
                return handleEdible(sender, args);
            case "info":
                return handleInfo(sender);
            case "effect":
                return handleEffect(sender, args);
            case "help":
                return handleHelp(sender, args);
            case "item":
                return handleItem(sender, args);
            case "check":
                return handleCheck(sender);
            case "reload":
                return handleReload(sender);
            case "version":
                return handleVersion(sender);
            default:
                sender.sendMessage(MessageUtils.colorize("&cUnknown command! Use /ee help for help."));
                return true;
        }
    }

    private boolean handleSaturation(CommandSender sender, String[] args) {
        if (!sender.hasPermission("eateverything.saturation")) {
            sender.sendMessage(MessageUtils.colorize("&cYou don't have permission!"));
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtils.colorize("&cThis command can only be used by players!"));
            return true;
        }

        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType().isAir()) {
            player.sendMessage(MessageUtils.colorize("&cYou must hold an item!"));
            return true;
        }

        if (args.length == 1) {
            // Show current saturation
            FoodComponent food = plugin.getFoodManager().getFoodComponent(item);
            player.sendMessage(MessageUtils.colorize("&aCurrent saturation: &e" + food.getSaturation()));
        } else {
            try {
                float saturation = Float.parseFloat(args[1]);
                if (saturation < 0) {
                    player.sendMessage(MessageUtils.colorize("&cSaturation must be non-negative!"));
                    return true;
                }

                plugin.getFoodManager().setSaturation(item, saturation);
                player.sendMessage(MessageUtils.colorize("&aSaturation set to &e" + saturation));
            } catch (NumberFormatException e) {
                player.sendMessage(MessageUtils.colorize("&cInvalid number!"));
            }
        }
        return true;
    }

    private boolean handleNutrition(CommandSender sender, String[] args) {
        if (!sender.hasPermission("eateverything.nutrition")) {
            sender.sendMessage(MessageUtils.colorize("&cYou don't have permission!"));
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtils.colorize("&cThis command can only be used by players!"));
            return true;
        }

        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType().isAir()) {
            player.sendMessage(MessageUtils.colorize("&cYou must hold an item!"));
            return true;
        }

        if (args.length == 1) {
            FoodComponent food = plugin.getFoodManager().getFoodComponent(item);
            player.sendMessage(MessageUtils.colorize("&aCurrent nutrition: &e" + food.getNutrition()));
        } else {
            try {
                int nutrition = Integer.parseInt(args[1]);
                if (nutrition < 0) {
                    player.sendMessage(MessageUtils.colorize("&cNutrition must be non-negative!"));
                    return true;
                }

                plugin.getFoodManager().setNutrition(item, nutrition);
                player.sendMessage(MessageUtils.colorize("&aNutrition set to &e" + nutrition));
            } catch (NumberFormatException e) {
                player.sendMessage(MessageUtils.colorize("&cInvalid number!"));
            }
        }
        return true;
    }

    private boolean handleEatSeconds(CommandSender sender, String[] args) {
        if (!sender.hasPermission("eateverything.eatseconds")) {
            sender.sendMessage(MessageUtils.colorize("&cYou don't have permission!"));
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtils.colorize("&cThis command can only be used by players!"));
            return true;
        }

        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType().isAir()) {
            player.sendMessage(MessageUtils.colorize("&cYou must hold an item!"));
            return true;
        }

        if (args.length == 1) {
            FoodComponent food = plugin.getFoodManager().getFoodComponent(item);
            player.sendMessage(MessageUtils.colorize("&aCurrent eat seconds: &e" + food.getEatSeconds()));
        } else {
            try {
                float eatSeconds = Float.parseFloat(args[1]);
                if (eatSeconds <= 0) {
                    player.sendMessage(MessageUtils.colorize("&cEat seconds must be greater than 0!"));
                    return true;
                }

                plugin.getFoodManager().setEatSeconds(item, eatSeconds);
                player.sendMessage(MessageUtils.colorize("&aEat seconds set to &e" + eatSeconds));
            } catch (NumberFormatException e) {
                player.sendMessage(MessageUtils.colorize("&cInvalid number!"));
            }
        }
        return true;
    }

    private boolean handleEdible(CommandSender sender, String[] args) {
        if (!sender.hasPermission("eateverything.edible")) {
            sender.sendMessage(MessageUtils.colorize("&cYou don't have permission!"));
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtils.colorize("&cThis command can only be used by players!"));
            return true;
        }

        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType().isAir()) {
            player.sendMessage(MessageUtils.colorize("&cYou must hold an item!"));
            return true;
        }

        if (args.length == 1) {
            FoodComponent food = plugin.getFoodManager().getFoodComponent(item);
            player.sendMessage(MessageUtils.colorize("&aCan always eat: &e" + food.canAlwaysEat()));
        } else {
            boolean canAlwaysEat = Boolean.parseBoolean(args[1]);
            plugin.getFoodManager().setCanAlwaysEat(item, canAlwaysEat);
            player.sendMessage(MessageUtils.colorize("&aCan always eat set to &e" + canAlwaysEat));
        }
        return true;
    }

    private boolean handleInfo(CommandSender sender) {
        if (!sender.hasPermission("eateverything.info")) {
            sender.sendMessage(MessageUtils.colorize("&cYou don't have permission!"));
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtils.colorize("&cThis command can only be used by players!"));
            return true;
        }

        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType().isAir()) {
            player.sendMessage(MessageUtils.colorize("&cYou must hold an item!"));
            return true;
        }

        FoodComponent food = plugin.getFoodManager().getFoodComponent(item);

        player.sendMessage(MessageUtils.colorize("&7&m-------------------"));
        player.sendMessage(MessageUtils.colorize("&6Food Component Info:"));
        player.sendMessage(MessageUtils.colorize("&aNutrition: &e" + food.getNutrition()));
        player.sendMessage(MessageUtils.colorize("&aSaturation: &e" + food.getSaturation()));
        player.sendMessage(MessageUtils.colorize("&aEat Seconds: &e" + food.getEatSeconds()));
        player.sendMessage(MessageUtils.colorize("&aCan Always Eat: &e" + food.canAlwaysEat()));

        if (!food.getEffects().isEmpty()) {
            player.sendMessage(MessageUtils.colorize("&aEffects:"));
            food.getEffects().forEach(effect -> {
                player.sendMessage(MessageUtils.colorize("  &7- &e" + effect.getType().getName() +
                        " &7(Level " + (effect.getAmplifier() + 1) + ", " +
                        (effect.getDuration() / 20) + "s, " +
                        (effect.getProbability() * 100) + "% chance)"));
            });
        }

        // Create clickable give command
        TextComponent giveCommand = new TextComponent(MessageUtils.colorize("&a[Click to copy give command]"));
        giveCommand.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD,
                plugin.getFoodManager().generateGiveCommand(item)));
        giveCommand.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new Text("Click to copy the give command")));

        player.spigot().sendMessage(giveCommand);
        player.sendMessage(MessageUtils.colorize("&7&m-------------------"));

        return true;
    }

    private boolean handleEffect(CommandSender sender, String[] args) {
        if (!sender.hasPermission("eateverything.effect")) {
            sender.sendMessage(MessageUtils.colorize("&cYou don't have permission!"));
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtils.colorize("&cThis command can only be used by players!"));
            return true;
        }

        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType().isAir()) {
            player.sendMessage(MessageUtils.colorize("&cYou must hold an item!"));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(MessageUtils.colorize("&cUsage: /ee effect <clear|add> [args...]"));
            return true;
        }

        if (args[1].equalsIgnoreCase("clear")) {
            plugin.getFoodManager().clearEffects(item);
            player.sendMessage(MessageUtils.colorize("&aAll effects cleared!"));
        } else if (args[1].equalsIgnoreCase("add")) {
            if (args.length < 5) {
                player.sendMessage(MessageUtils.colorize("&cUsage: /ee effect add <effect> <duration> <amplifier> [probability]"));
                return true;
            }

            PotionEffectType effectType = PotionEffectType.getByName(args[2].toUpperCase());
            if (effectType == null) {
                player.sendMessage(MessageUtils.colorize("&cInvalid effect type!"));
                return true;
            }

            try {
                int duration = Integer.parseInt(args[3]);
                int amplifier = Integer.parseInt(args[4]);
                float probability = args.length > 5 ? Float.parseFloat(args[5]) : 1.0f;

                plugin.getFoodManager().addEffect(item, effectType, duration, amplifier, probability);
                player.sendMessage(MessageUtils.colorize("&aEffect added successfully!"));
            } catch (NumberFormatException e) {
                player.sendMessage(MessageUtils.colorize("&cInvalid numbers!"));
            }
        }

        return true;
    }

    private boolean handleHelp(CommandSender sender, String[] args) {
        if (!sender.hasPermission("eateverything.help")) {
            sender.sendMessage(MessageUtils.colorize("&cYou don't have permission!"));
            return true;
        }

        if (args.length > 1 && args[1].equals("--general")) {
            sendGeneralHelp(sender);
        } else {
            sendHelp(sender);
        }
        return true;
    }

    private boolean handleItem(CommandSender sender, String[] args) {
        if (!sender.hasPermission("eateverything.item")) {
            sender.sendMessage(MessageUtils.colorize("&cYou don't have permission!"));
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtils.colorize("&cThis command can only be used by players!"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(MessageUtils.colorize("&cUsage: /ee item <id>"));
            return true;
        }

        Player player = (Player) sender;
        ItemStack item = plugin.getFoodManager().getCustomItem(args[1]);

        if (item == null) {
            player.sendMessage(MessageUtils.colorize("&cItem with ID '" + args[1] + "' not found!"));
            return true;
        }

        player.getInventory().addItem(item);
        player.sendMessage(MessageUtils.colorize("&aGave you custom item: " + args[1]));
        return true;
    }

    private boolean handleCheck(CommandSender sender) {
        if (!sender.hasPermission("eateverything.check")) {
            sender.sendMessage(MessageUtils.colorize("&cYou don't have permission!"));
            return true;
        }

        sender.sendMessage(MessageUtils.colorize("&aChecking for updates..."));
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            String updateMessage = plugin.getUpdateChecker().getUpdateStatus();
            sender.sendMessage(MessageUtils.colorize(updateMessage));
        });

        return true;
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("eateverything.reload")) {
            sender.sendMessage(MessageUtils.colorize("&cYou don't have permission!"));
            return true;
        }

        plugin.reload();
        sender.sendMessage(MessageUtils.colorize("&aEatEverything has been reloaded!"));
        return true;
    }

    private boolean handleVersion(CommandSender sender) {
        if (!sender.hasPermission("eateverything.version")) {
            sender.sendMessage(MessageUtils.colorize("&cYou don't have permission!"));
            return true;
        }

        sender.sendMessage(MessageUtils.colorize("&7&m-------------------"));
        sender.sendMessage(MessageUtils.colorize("&6EatEverything &ev" + plugin.getDescription().getVersion()));
        sender.sendMessage(MessageUtils.colorize("&aAuthor: &e" + String.join(", ", plugin.getDescription().getAuthors())));
        sender.sendMessage(MessageUtils.colorize("&aServer: &e" + plugin.getServer().getName() + " " + plugin.getServer().getVersion()));
        sender.sendMessage(MessageUtils.colorize("&aJava: &e" + System.getProperty("java.version")));
        sender.sendMessage(MessageUtils.colorize("&7&m-------------------"));
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(MessageUtils.colorize("&7&m-------------------"));
        sender.sendMessage(MessageUtils.colorize("&6EatEverything Commands:"));
        sender.sendMessage(MessageUtils.colorize("&a/ee saturation [value] &7- View/set saturation"));
        sender.sendMessage(MessageUtils.colorize("&a/ee nutrition [value] &7- View/set nutrition"));
        sender.sendMessage(MessageUtils.colorize("&a/ee eatseconds [value] &7- View/set eat time"));
        sender.sendMessage(MessageUtils.colorize("&a/ee edible [true/false] &7- View/set can always eat"));
        sender.sendMessage(MessageUtils.colorize("&a/ee info &7- Show item food info"));
        sender.sendMessage(MessageUtils.colorize("&a/ee effect <clear|add> &7- Manage effects"));
        sender.sendMessage(MessageUtils.colorize("&a/ee item <id> &7- Get custom item"));
        sender.sendMessage(MessageUtils.colorize("&a/ee reload &7- Reload plugin"));
        sender.sendMessage(MessageUtils.colorize("&a/ee version &7- Show version info"));
        sender.sendMessage(MessageUtils.colorize("&a/ee help [--general] &7- Show help"));
        sender.sendMessage(MessageUtils.colorize("&7&m-------------------"));
    }

    private void sendGeneralHelp(CommandSender sender) {
        sender.sendMessage(MessageUtils.colorize("&7&m-------------------"));
        sender.sendMessage(MessageUtils.colorize("&6Food Components Info:"));
        sender.sendMessage(MessageUtils.colorize("&aNutrition: &7Food points restored (hunger bars)"));
        sender.sendMessage(MessageUtils.colorize("&aSaturation: &7Additional hidden food points"));
        sender.sendMessage(MessageUtils.colorize("&aEat Seconds: &7Time to consume the item"));
        sender.sendMessage(MessageUtils.colorize("&aCan Always Eat: &7Eat even when full"));
        sender.sendMessage(MessageUtils.colorize("&aEffects: &7Potion effects applied when eaten"));
        sender.sendMessage(MessageUtils.colorize("&7&m-------------------"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("eateverything.tabcompleter")) {
            return Collections.emptyList();
        }

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String[] subCommands = {"saturation", "nutrition", "eatseconds", "edible",
                    "info", "effect", "help", "item", "check", "reload", "version"};
            for (String sub : subCommands) {
                if (sub.startsWith(args[0].toLowerCase())) {
                    completions.add(sub);
                }
            }
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "edible":
                    completions.add("true");
                    completions.add("false");
                    break;
                case "effect":
                    completions.add("clear");
                    completions.add("add");
                    break;
                case "help":
                    completions.add("--general");
                    break;
                case "item":
                    completions.addAll(plugin.getFoodManager().getCustomItemIds());
                    break;
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("effect") && args[1].equalsIgnoreCase("add")) {
            for (PotionEffectType type : PotionEffectType.values()) {
                if (type != null && type.getName().toLowerCase().startsWith(args[2].toLowerCase())) {
                    completions.add(type.getName());
                }
            }
        }

        return completions;
    }
}