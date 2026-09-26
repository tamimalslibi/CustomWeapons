# CustomWeapons

A Paper/Spigot plugin (Minecraft 1.21+) adding four op-only custom weapons with unique abilities.

## Weapons

| Weapon | Color | Command | Ability | Cooldown |
|---|---|---|---|---|
| **Earthmace** | Orange | `/earthquake` | Shift + Right-Click: leap up, then slam down cracking the ground. Deals ~2 hearts (4 damage) and applies Slowness for 5s to everyone within an 8-block radius. | 10s |
| **Venom Spear** | Green | `/venomspear` | Shift + Right-Click: dash in the direction you're aiming and leave behind a poisonous mist. Anyone caught in it is poisoned for 5-6 seconds. | 8s |
| **Pull Bow** | Red | `/pullbow` | Fire an arrow — on hit, it yanks that player toward you. Cooldown starts the instant you fire, whether you hit or miss, so it can't be spammed. | 13s |
| **Astral Mace** | Blue | `/astralmace` | Shift + Right-Click: rocket straight up, then slam back down at high speed. | 8s |

All four commands are restricted to server operators (`customweapons.admin`, default `op`) and give the item to the command sender.

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

- **Item base materials**: Earthmace and Astral Mace both use vanilla `MACE` (1.21+), Venom Spear uses `TRIDENT` (re-themed, since there's no vanilla spear), Pull Bow uses `BOW`. Swap these in `ItemFactory.java` if you'd rather use a resource pack with custom model data.
- **Numbers**: leap heights, mist radius, pull strength, etc. are tuned to feel reasonable — adjust the constants in `WeaponListener.java` to taste.
- **Identification**: items are tagged via a hidden `PersistentDataContainer` key (`customweapons:weapon_type`), so renaming the item in an anvil won't break its ability.
