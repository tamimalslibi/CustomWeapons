package com.customweapons;

import com.customweapons.commands.WeaponCommand;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public final class CustomWeaponsPlugin extends JavaPlugin {

    private NamespacedKey weaponTypeKey;
    private NamespacedKey pullBowOwnerKey;
    private CooldownManager cooldownManager;
    private ItemFactory itemFactory;

    @Override
    public void onEnable() {
        this.weaponTypeKey = new NamespacedKey(this, "weapon_type");
        this.pullBowOwnerKey = new NamespacedKey(this, "pullbow_owner");
        this.cooldownManager = new CooldownManager();
        this.itemFactory = new ItemFactory(weaponTypeKey);

        getServer().getPluginManager().registerEvents(
                new WeaponListener(this, weaponTypeKey, pullBowOwnerKey, cooldownManager), this);

        WeaponCommand command = new WeaponCommand(itemFactory);
        getCommand("earthquake").setExecutor(command);
        getCommand("venomspear").setExecutor(command);
        getCommand("pullbow").setExecutor(command);
        getCommand("astralmace").setExecutor(command);

        getLogger().info("CustomWeapons enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("CustomWeapons disabled.");
    }

    public NamespacedKey getWeaponTypeKey() {
        return weaponTypeKey;
    }

    public NamespacedKey getPullBowOwnerKey() {
        return pullBowOwnerKey;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public ItemFactory getItemFactory() {
        return itemFactory;
    }
}
