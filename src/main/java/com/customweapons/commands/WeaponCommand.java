package com.customweapons.commands;

import com.customweapons.ItemFactory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class WeaponCommand implements CommandExecutor {

    private final ItemFactory itemFactory;

    public WeaponCommand(ItemFactory itemFactory) {
        this.itemFactory = itemFactory;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // plugin.yml already restricts these commands to the customweapons.admin
        // permission (default: op), but we double-check isOp() per the spec.
        if (!sender.isOp()) {
            sender.sendMessage(Component.text("Only operators can use this command.", NamedTextColor.RED));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only a player can hold this item - run this in-game.", NamedTextColor.RED));
            return true;
        }

        ItemStack item;
        String niceName;

        switch (command.getName().toLowerCase()) {
            case "earthquake" -> {
                item = itemFactory.createEarthMace();
                niceName = "Earthmace";
            }
            case "venomspear" -> {
                item = itemFactory.createVenomSpear();
                niceName = "Venom Spear";
            }
            case "pullbow" -> {
                item = itemFactory.createPullBow();
                niceName = "Pull Bow";
            }
            case "astralmace" -> {
                item = itemFactory.createAstralMace();
                niceName = "Astral Mace";
            }
            default -> {
                return false;
            }
        }

        player.getInventory().addItem(item);
        player.sendMessage(Component.text("You received the " + niceName + ".", NamedTextColor.YELLOW));
        return true;
    }
}
