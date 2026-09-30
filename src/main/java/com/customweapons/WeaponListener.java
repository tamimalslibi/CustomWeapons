package com.customweapons;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class WeaponListener implements Listener {

    private final CustomWeaponsPlugin plugin;
    private final NamespacedKey weaponTypeKey;
    private final CooldownManager cooldowns;

    public WeaponListener(CustomWeaponsPlugin plugin, NamespacedKey weaponTypeKey, CooldownManager cooldowns) {
        this.plugin = plugin;
        this.weaponTypeKey = weaponTypeKey;
        this.cooldowns = cooldowns;
    }

    // ---------- Shift + right-click abilities (Earthmace, Venom Spear, Astral Mace) ----------

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        if (!player.isSneaking()) return;

        ItemStack item = event.getItem();
        WeaponType type = getWeaponType(item);
        if (type == null) return;

        event.setCancelled(true);

        if (cooldowns.isOnCooldown(player.getUniqueId(), type)) {
            long remaining = cooldowns.getRemainingSeconds(player.getUniqueId(), type);
            player.sendMessage(Component.text("On cooldown for " + remaining + "s.", NamedTextColor.RED));
            return;
        }

        cooldowns.startCooldown(player.getUniqueId(), type);

        switch (type) {
            case EARTH_MACE -> earthMace(player);
            case VENOM_SPEAR -> venomSpear(player);
            case ASTRAL_MACE -> astralMace(player);
        }
    }

    private void earthMace(Player player) {
        // Leap up - noticeably higher than a normal jump, with a slight forward lean.
        Vector leap = player.getLocation().getDirection().setY(0).normalize().multiply(0.35);
        leap.setY(2.0);
        player.setVelocity(leap);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1f, 0.6f);

        new BukkitRunnable() {
            int ticks = 0;
            boolean hasLeftGround = false;

            @Override
            public void run() {
                ticks++;

                if (!hasLeftGround) {
                    // Wait until the player has actually left the ground before
                    // we start watching for a landing, otherwise isOnGround()
                    // can still read true for the first tick or two.
                    if (!player.isOnGround()) {
                        hasLeftGround = true;
                    } else if (ticks > 10) {
                        // Fail-safe: something stopped them from leaving the ground
                        // (e.g. a low ceiling). Trigger anyway so the ability doesn't silently do nothing.
                        slamGround(player);
                        cancel();
                    }
                    return;
                }

                // Now watch for the landing.
                if (player.isOnGround() || ticks > 100) {
                    slamGround(player);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    private void slamGround(Player player) {
        Location center = player.getLocation();
        double radius = 8.0;

        center.getWorld().spawnParticle(Particle.EXPLOSION, center, 1);
        center.getWorld().spawnParticle(Particle.BLOCK, center, 60,
                radius / 2, 0.3, radius / 2, center.getBlock().getBlockData());
        center.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.2f, 0.8f);

        for (Entity nearby : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
            if (nearby.equals(player)) continue;
            if (!(nearby instanceof LivingEntity living)) continue;
            if (nearby.getLocation().distance(center) > radius) continue;

            living.damage(20.0, player); // ~10 hearts
            living.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 5 * 20, 1));
        }
    }

    private void venomSpear(Player player) {
        Vector direction = player.getLocation().getDirection().normalize();
        Location mistOrigin = player.getLocation();

        Vector dash = direction.clone().multiply(2.4);
        dash.setY(Math.max(dash.getY(), 0.2));
        player.setVelocity(dash);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PHANTOM_SWOOP, 1f, 1.2f);

        // Poisonous mist left behind at the dash's starting point.
        org.bukkit.entity.AreaEffectCloud cloud = mistOrigin.getWorld().spawn(
                mistOrigin, org.bukkit.entity.AreaEffectCloud.class);
        cloud.setRadius(3.0f);
        cloud.setDuration(4 * 20);
        cloud.setColor(org.bukkit.Color.fromRGB(0x2ECC71));
        cloud.setParticle(Particle.ENTITY_EFFECT);
        cloud.addCustomEffect(new PotionEffect(PotionEffectType.POISON, 5 * 20 + 10, 0), true);
        cloud.setSource(player);
    }

    private void astralMace(Player player) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1f, 0.8f);
        player.setVelocity(new Vector(0, 2.6, 0));

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) return;
                player.setVelocity(new Vector(0, -3.2, 0));
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1f, 1.4f);
                player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 40, 0.3, 0.1, 0.3, 0.05);
            }
        }.runTaskLater(plugin, 16L); // brief float at the top before slamming down
    }

    // ---------- helpers ----------

    private WeaponType getWeaponType(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        String raw = item.getItemMeta().getPersistentDataContainer().get(weaponTypeKey, PersistentDataType.STRING);
        if (raw == null) return null;
        try {
            return WeaponType.valueOf(raw);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    // Prevent Astral Mace's/Earthmace's self-launch from also triggering Minecraft's
    // native mace fall-damage smash attack mid-dash, which would double up damage.
    @EventHandler(ignoreCancelled = true)
    public void onFallDamageFromLaunch(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (cooldowns.isOnCooldown(player.getUniqueId(), WeaponType.EARTH_MACE)
                || cooldowns.isOnCooldown(player.getUniqueId(), WeaponType.ASTRAL_MACE)) {
            // Only suppress fall damage in the second right after using these abilities.
            event.setCancelled(true);
        }
    }
}
