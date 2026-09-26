# CustomWeapons

A Paper/Spigot plugin adding four op-only custom weapons with unique abilities.

> **Requires 1.21.11+ (or a build that includes the Spear).** Venom Spear is a real `NETHERITE_SPEAR` and uses the real `Lunge` enchantment, both added to vanilla Minecraft in the "Mounts of Mayhem" update. If your server core doesn't include the Spear yet, that one item won't work — the other three weapons don't depend on it.

## Weapons

| Weapon | Color | Command | Base item | Enchants | Ability | Cooldown |
|---|---|---|---|---|---|---|
| **Earthmace** | Orange | `/earthquake` | Mace | Density V, Wind Burst II, Unbreaking III | Shift + Right-Click: leap up high, then slam the ground the moment you land, cracking it open. Deals ~2 hearts (4 damage) and applies Slowness for 5s to everyone within an 8-block radius. | 10s |
| **Venom Spear** | Green | `/venomspear` | Netherite Spear | Sharpness V, Mending, Unbreaking III, Lunge III | Shift + Right-Click: dash in the direction you're aiming and leave behind a poisonous mist. Anyone caught in it is poisoned for 5-6 seconds. | 8s |
| **Pull Bow** | Red | `/pullbow` | Bow | Power V, Unbreaking III, Mending | Fire an arrow — on hit, it yanks that player toward you. Cooldown starts the instant you fire, whether you hit or miss, so it can't be spammed. | 13s |
| **Astral Mace** | Blue | `/astralmace` | Mace | Density V, Wind Burst II, Unbreaking III | Shift + Right-Click: rocket straight up, then slam back down at high speed. | 8s |

All four commands are restricted to server operators (`customweapons.admin`, default `op`) and give the item to the command sender. Each item's lore/description is colored to match its name.

Every ability has its own cooldown, tracked per-player, so none of the weapons can be spammed.

## Project structure

```
CustomWeapons/
├── pom.xml
├── README.md
└── src/main/
    ├── java/com/customweapons/
    │   ├── CustomWeaponsPlugin.java   # plugin entrypoint, registers commands/listener
    │   ├── WeaponType.java            # enum of the 4 weapons + their cooldowns
    │   ├── ItemFactory.java           # builds the colored/named items with lore
    │   ├── CooldownManager.java       # per-player, per-weapon cooldown tracking
    │   ├── WeaponListener.java        # all ability logic (interact/shoot/hit events)
    │   └── commands/WeaponCommand.java
    └── resources/plugin.yml
```

## Building the .jar

You'll need [Maven](https://maven.apache.org/) and JDK 17+ installed locally (this container has no internet access, so the build has to happen on your machine or in GitHub Actions, where Maven can download the Paper API).

```bash
git clone <your-repo-url>
cd CustomWeapons
mvn clean package
```

The compiled jar will be at `target/CustomWeapons.jar`. Drop it into your server's `plugins/` folder and restart.

### Don't have Maven/JDK locally? Let GitHub build it for you

This repo includes `.github/workflows/build.yml`, a GitHub Actions workflow that runs `mvn clean package` on every push and uploads the resulting jar as a build artifact. Once you push to GitHub:

1. Go to the **Actions** tab on your repo.
2. Open the latest **Build** run.
3. Download the `CustomWeapons-jar` artifact (it's a zip containing `CustomWeapons.jar`) from the run summary page.

## Pushing to GitHub

```bash
cd CustomWeapons
git init
git add .
git commit -m "Initial commit: CustomWeapons plugin"
git branch -M main
git remote add origin <your-repo-url>
git push -u origin main
```

Consider adding a `.gitignore` with:
```
target/
.idea/
*.iml
```

## Notes / things you may want to tweak

- **Item base materials**: Earthmace and Astral Mace both use vanilla `MACE`, Venom Spear uses the new vanilla `NETHERITE_SPEAR`, Pull Bow uses `BOW`. Swap these in `ItemFactory.java` if you'd rather use a resource pack with custom model data.
- **Enchant levels**: all set via `enchantsFor()` in `ItemFactory.java` with `ignoreLevelRestriction = true`, so they'll apply even if a level is technically above the enchant's vanilla max — adjust freely.
- **Numbers**: leap heights, mist radius, pull strength, etc. are tuned to feel reasonable — adjust the constants in `WeaponListener.java` to taste.
- **Identification**: items are tagged via a hidden `PersistentDataContainer` key (`customweapons:weapon_type`), so renaming the item in an anvil won't break its ability.
