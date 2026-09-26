package com.customweapons;

public enum WeaponType {

    EARTH_MACE(15),
    VENOM_SPEAR(13),
    ASTRAL_MACE(13);

    private final int cooldownSeconds;

    WeaponType(int cooldownSeconds) {
        this.cooldownSeconds = cooldownSeconds;
    }

    public int getCooldownSeconds() {
        return cooldownSeconds;
    }
}
