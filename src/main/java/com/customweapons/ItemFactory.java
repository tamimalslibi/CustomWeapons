package com.customweapons;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Map;

/**
 * Creates the four custom weapons. Each item is tagged in its
 * PersistentDataContainer with a WeaponType so the listener knows
 * which ability to run, no matter what the item is renamed to in an anvil.
 */
public class ItemFactory {

    // Colors requested: Earthmace = orange, Venom Spear = green, Pull Bow = red, Astral Mace = blue
    private static final TextColor ORANGE = TextColor.color(0xFF8C00);
    private static final TextColor GREEN = TextColor.color(0x00C853);
    private static final TextColor RED = TextColor.color(0xE53935);
    private static final TextColor BLUE = TextColor.color(0x2979FF);

    private final NamespacedKey weaponTypeKey;

    public ItemFactory(NamespacedKey weaponTypeKey) {
        this.weaponTypeKey = weaponTypeKey;
    }

    public ItemStack createEarthMace() {
        ItemStack item = new ItemStack(Material.MACE);
        applyBase(item, "Earthmace", ORANGE, WeaponType.EARTH_MACE, List.of(
                "Shift + Right-Click to leap high and",
                "slam the ground when you land,",
                "cracking it open. Deals damage to",
                "everyone within 8 blocks and slows",
                "them for 5s.",
                cooldownLine(WeaponType.EARTH_MACE)
        ));
        return item;
    }

    public ItemStack createVenomSpear() {
        ItemStack item = new ItemStack(Material.NETHERITE_SPEAR);
        applyBase(item, "Venom Spear", GREEN, WeaponType.VENOM_SPEAR, List.of(
                "Shift + Right-Click to dash toward",
                "where you're aiming, leaving behind",
                "a venomous mist. Anyone caught in it",
                "is poisoned for 5-6 seconds.",
                cooldownLine(WeaponType.VENOM_SPEAR)
        ));
        return item;
    }

    public ItemStack createPullBow() {
        ItemStack item = new ItemStack(Material.BOW);
        applyBase(item, "Pull Bow", RED, WeaponType.PULL_BOW, List.of(
                "Fire an arrow to yank the player",
                "it hits toward you.",
                "Cooldown starts the moment you",
                "fire, hit or miss, so it can't be spammed.",
                cooldownLine(WeaponType.PULL_BOW)
        ));
        return item;
    }

    public ItemStack createAstralMace() {
        ItemStack item = new ItemStack(Material.MACE);
        applyBase(item, "Astral Mace", BLUE, WeaponType.ASTRAL_MACE, List.of(
                "Shift + Right-Click to rocket straight",
                "up, then slam back down at high speed.",
                cooldownLine(WeaponType.ASTRAL_MACE)
        ));
        return item;
    }

    private String cooldownLine(WeaponType type) {
        return "Cooldown: " + type.getCooldownSeconds() + "s";
    }

    private void applyBase(ItemStack item, String name, TextColor color, WeaponType type, List<String> loreLines) {
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text(name, color).decoration(TextDecoration.ITALIC, false));

        List<Component> lore = loreLines.stream()
                .<Component>map(line -> Component.text(line, color).decoration(TextDecoration.ITALIC, false))
                .toList();
        meta.lore(lore);

        meta.getPersistentDataContainer().set(weaponTypeKey, PersistentDataType.STRING, type.name());

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
        meta.setUnbreakable(true);

        enchantsFor(type).forEach((enchant, level) -> meta.addEnchant(enchant, level, true));

        item.setItemMeta(meta);
    }

    private Map<Enchantment, Integer> enchantsFor(WeaponType type) {
        return switch (type) {
            case VENOM_SPEAR -> Map.of(
                    Enchantment.SHARPNESS, 5,
                    Enchantment.MENDING, 1,
                    Enchantment.UNBREAKING, 3,
                    Enchantment.LUNGE, 3
            );
            case PULL_BOW -> Map.of(
                    Enchantment.POWER, 5,
                    Enchantment.UNBREAKING, 3,
                    Enchantment.MENDING, 1
            );
            // Both maces (Earthmace and Astral Mace) get the same enchant set.
            case EARTH_MACE, ASTRAL_MACE -> Map.of(
                    Enchantment.DENSITY, 5,
                    Enchantment.WIND_BURST, 2,
                    Enchantment.UNBREAKING, 3
            );
        };
    }
}
