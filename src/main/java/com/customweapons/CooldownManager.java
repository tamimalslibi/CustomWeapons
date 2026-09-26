package com.customweapons;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple per-player, per-weapon-type cooldown tracker.
 * Applies to all four weapons so nobody can spam abilities.
 */
public class CooldownManager {

    private final Map<UUID, Map<WeaponType, Long>> cooldowns = new ConcurrentHashMap<>();

    /**
     * @return remaining seconds on cooldown, or 0 if it's ready to use.
     */
    public long getRemainingSeconds(UUID playerId, WeaponType type) {
        Map<WeaponType, Long> playerMap = cooldowns.get(playerId);
        if (playerMap == null) return 0;

        Long readyAt = playerMap.get(type);
        if (readyAt == null) return 0;

        long remainingMillis = readyAt - System.currentTimeMillis();
        if (remainingMillis <= 0) return 0;

        return (remainingMillis + 999) / 1000; // round up to whole seconds
    }

    public boolean isOnCooldown(UUID playerId, WeaponType type) {
        return getRemainingSeconds(playerId, type) > 0;
    }

    public void startCooldown(UUID playerId, WeaponType type) {
        long readyAt = System.currentTimeMillis() + (type.getCooldownSeconds() * 1000L);
        cooldowns.computeIfAbsent(playerId, id -> new EnumMap<>(WeaponType.class)).put(type, readyAt);
    }
}
