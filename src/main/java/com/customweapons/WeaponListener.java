package com.customweapons;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.UUID;

public class WeaponListener implements Listener {

    private final CustomWeaponsPlugin plugin;
    private final NamespacedKey weaponTypeKey;
    private final NamespacedKey pullBowOwnerKey;
    private final CooldownManager cooldowns;

    public WeaponListener(CustomWeaponsPlugin plugin, NamespacedKey weaponTypeKey,
                           NamespacedKey pullBowOwnerKey, CooldownManager cooldowns) {
        this.plugin = plugin;
        this.weaponTypeKey = weaponTypeKey;
        this.pullBowOwnerKey = pullBowOwnerKey;
        this.cooldowns = cooldowns;
    }

    // ---------- Shift + right-click abilities (Earthmace, Venom Spear, Astral Mace) ----------

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR &&
                event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();

        if (!player.isSneaking()) return;

        ItemStack item = event.getItem();
        WeaponType type = getWeaponType(item);

        if (type == null) return;

        // Pull Bow's ability triggers on shooting, not on shift-right-click.
        if (type == WeaponType.PULL_BOW) return;

        event.setCancelled(true);

        if (cooldowns.isOnCooldown(player.getUniqueId(), type)) {
            long remaining = cooldowns.getRemainingSeconds(player.getUniqueId(), type);
            player.sendMessage(Component.text(
                    "On cooldown for " + remaining + "s.",
                    NamedTextColor.RED
            ));
            return;
        }

        cooldowns.startCooldown(player.getUniqueId(), type);

        switch (type) {
            case EARTH_MACE -> earthMace(player);
            case VENOM_SPEAR -> venomSpear(player);
            case ASTRAL_MACE -> astralMace(player);
            default -> {}
        }
    }

    private void earthMace(Player player) {

        // Leap up - noticeably higher than a normal jump, with a slight forward lean.
        Vector leap = player.getLocation()
                .getDirection()
                .setY(0)
                .normalize()
                .multiply(0.35);

        leap.setY(2.0);

        player.setVelocity(leap);

        player.getWorld().playSound(
                player.getLocation(),
                Sound.ENTITY_ENDER_DRAGON_FLAP,
                1f,
                0.6f
        );

        new BukkitRunnable() {

            int ticks = 0;
            boolean hasLeftGround = false;

            @Override
            public void run() {

                ticks++;

                if (!hasLeftGround) {

                    // Wait until the player has actually left the ground.
                    if (!player.isOnGround()) {
                        hasLeftGround = true;

                    } else if (ticks > 10) {

                        // Fail-safe if something stopped the player from leaving the ground.
                        slamGround(player);
                        cancel();
                    }

                    return;
                }

                // Watch for the landing.
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

        center.getWorld().spawnParticle(
                Particle.EXPLOSION,
                center,
                1
        );

        center.getWorld().spawnParticle(
                Particle.BLOCK,
                center,
                60,
                radius / 2,
                0.3,
                radius / 2,
                center.getBlock().getBlockData()
        );

        center.getWorld().playSound(
                center,
                Sound.ENTITY_GENERIC_EXPLODE,
                1.2f,
                0.8f
        );

        for (Entity nearby : center.getWorld().getNearbyEntities(
                center,
                radius,
                radius,
                radius
        )) {

            if (nearby.equals(player)) continue;

            if (!(nearby instanceof LivingEntity living)) continue;

            if (nearby.getLocation().distance(center) > radius) continue;

            // 4 HP = 2 hearts.
            // Direct health reduction prevents armor/damage immunity
            // from making the earthquake deal tiny damage.
            double newHealth = Math.max(0.0, living.getHealth() - 30.0);
            living.setHealth(newHealth);

            // Force Slowness II for 5 seconds.
            living.addPotionEffect(
                    new PotionEffect(
                            PotionEffectType.SLOWNESS,
                            5 * 20,
                            1,
                            false,
                            true,
                            true
                    )
            );
        }
    }

    private void venomSpear(Player player) {

        Vector direction = player.getLocation()
                .getDirection()
                .normalize();

        Location mistOrigin = player.getLocation();

        Vector dash = direction.clone().multiply(2.4);

        dash.setY(Math.max(dash.getY(), 0.2));

        player.setVelocity(dash);

        player.getWorld().playSound(
                player.getLocation(),
                Sound.ENTITY_PHANTOM_SWOOP,
                1f,
                1.2f
        );

        // Poisonous mist left behind at the dash's starting point.
        org.bukkit.entity.AreaEffectCloud cloud =
                mistOrigin.getWorld().spawn(
                        mistOrigin,
                        org.bukkit.entity.AreaEffectCloud.class
                );

        cloud.setRadius(3.0f);
        cloud.setDuration(4 * 20);
        cloud.setColor(org.bukkit.Color.fromRGB(0x2ECC71));
        cloud.setParticle(Particle.ENTITY_EFFECT);

        cloud.addCustomEffect(
                new PotionEffect(
                        PotionEffectType.POISON,
                        5 * 20 + 10,
                        0
                ),
                true
        );

        cloud.setSource(player);
    }

    private void astralMace(Player player) {

        player.getWorld().playSound(
                player.getLocation(),
                Sound.ENTITY_FIREWORK_ROCKET_LAUNCH,
                1f,
                0.8f
        );

        player.setVelocity(
                new Vector(0, 2.6, 0)
        );

        new BukkitRunnable() {

            @Override
            public void run() {

                if (!player.isOnline()) return;

                player.setVelocity(
                        new Vector(0, -3.2, 0)
                );

                player.getWorld().playSound(
                        player.getLocation(),
                        Sound.ENTITY_GENERIC_EXPLODE,
                        1f,
                        1.4f
                );

                player.getWorld().spawnParticle(
                        Particle.CLOUD,
                        player.getLocation(),
                        40,
                        0.3,
                        0.1,
                        0.3,
                        0.05
                );
            }

        }.runTaskLater(plugin, 16L);
    }

    // ---------- Pull Bow ----------

    @EventHandler
    public void onShootBow(EntityShootBowEvent event) {

        if (!(event.getEntity() instanceof Player player)) return;

        ItemStack bow = event.getBow();

        WeaponType type = getWeaponType(bow);

        if (type != WeaponType.PULL_BOW) return;

        if (cooldowns.isOnCooldown(
                player.getUniqueId(),
                type
        )) {

            long remaining =
                    cooldowns.getRemainingSeconds(
                            player.getUniqueId(),
                            type
                    );

            player.sendMessage(
                    Component.text(
                            "Pull Bow on cooldown for " + remaining + "s.",
                            NamedTextColor.RED
                    )
            );

            event.setCancelled(true);
            return;
        }

        // Cooldown starts the instant it's fired.
        cooldowns.startCooldown(
                player.getUniqueId(),
                type
        );

        if (event.getProjectile() instanceof Arrow arrow) {

            arrow.getPersistentDataContainer().set(
                    pullBowOwnerKey,
                    PersistentDataType.STRING,
                    player.getUniqueId().toString()
            );
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {

        Projectile projectile = event.getEntity();

        if (!(projectile instanceof Arrow arrow)) return;

        String ownerIdRaw =
                arrow.getPersistentDataContainer().get(
                        pullBowOwnerKey,
                        PersistentDataType.STRING
                );

        if (ownerIdRaw == null) return;

        if (!(event.getHitEntity() instanceof LivingEntity target)) return;

        UUID ownerId;

        try {
            ownerId = UUID.fromString(ownerIdRaw);
        } catch (IllegalArgumentException ex) {
            return;
        }

        Player owner =
                plugin.getServer().getPlayer(ownerId);

        if (owner == null) return;

        if (target.equals(owner)) return;

        /*
         * PULL BOW
         *
         * Instead of giving the target one small velocity boost,
         * keep pulling them toward the shooter for several ticks.
         * This makes the target actually reach the shooter.
         */

        new BukkitRunnable() {

            int ticks = 0;

            @Override
            public void run() {

                if (ticks++ >= 15) {
                    cancel();
                    return;
                }

                if (!owner.isOnline() || !target.isValid()) {
                    cancel();
                    return;
                }

                Vector pull = owner.getLocation()
                        .toVector()
                        .subtract(target.getLocation().toVector());

                double distance = pull.length();

                // Stop once they are basically at the shooter.
                if (distance <= 1.5) {
                    target.setVelocity(new Vector(0, 0, 0));
                    cancel();
                    return;
                }

                pull.normalize();

                /*
                 * Strong pull.
                 * The farther away they are, the stronger the pull.
                 * Maximum velocity is 4.5.
                 */
                double strength = Math.min(
                        Math.max(distance * 0.8, 2.0),
                        4.5
                );

                pull.multiply(strength);

                // Slight upward pull so they don't get stuck scraping the ground.
                pull.setY(Math.max(pull.getY(), 0.35));

                target.setVelocity(pull);
            }

        }.runTaskTimer(plugin, 0L, 1L);

        target.getWorld().playSound(
                target.getLocation(),
                Sound.ENTITY_ENDERMAN_TELEPORT,
                1f,
                0.7f
        );

        target.getWorld().spawnParticle(
                Particle.CRIT,
                target.getLocation(),
                20,
                0.3,
                0.5,
                0.3,
                0.05
        );
    }

    // ---------- helpers ----------

    private WeaponType getWeaponType(ItemStack item) {

        if (item == null || !item.hasItemMeta()) return null;

        String raw =
                item.getItemMeta()
                        .getPersistentDataContainer()
                        .get(
                                weaponTypeKey,
                                PersistentDataType.STRING
                        );

        if (raw == null) return null;

        try {

            return WeaponType.valueOf(raw);

        } catch (IllegalArgumentException ex) {

            return null;
        }
    }

    // Prevent Astral Mace's/Earthmace's self-launch from also triggering
    // Minecraft's native mace fall-damage smash attack mid-dash.

    @EventHandler(ignoreCancelled = true)
    public void onFallDamageFromLaunch(EntityDamageEvent event) {

        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;

        if (!(event.getEntity() instanceof Player player)) return;

        if (cooldowns.isOnCooldown(
                player.getUniqueId(),
                WeaponType.EARTH_MACE
        ) || cooldowns.isOnCooldown(
                player.getUniqueId(),
                WeaponType.ASTRAL_MACE
        )) {

            // Suppress fall damage while these abilities are active.
            event.setCancelled(true);
        }
    }
}
