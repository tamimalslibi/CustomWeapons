package com.customweapons;

public enum WeaponType {

    EARTH_MACE(10),
    VENOM_SPEAR(8),
    PULL_BOW(13),
    ASTRAL_MACE(8);

    private final int cooldownSeconds;

    WeaponType(int cooldownSeconds) {
        this.cooldownSeconds = cooldownSeconds;
    }

    public int getCooldownSeconds() {
        return cooldownSeconds;
    }
}
