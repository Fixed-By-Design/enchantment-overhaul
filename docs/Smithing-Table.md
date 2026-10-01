# Smithing Table (Stat Boosts)

Templates found in structures determine the upgrade type. The material in the second slot determines the level. One upgrade per category per item.

![Honing II upgrade on a Diamond Sword](images/smithing-upgrade.png)

## Templates

| Template | Replaces | Effect | Sources |
|---|---|---|---|
| Honing | Sharpness, Power, Density | Weapon damage | Weaponsmith, Pillager Outpost, Stronghold Library, Dungeon |
| Warding | Protection | Damage reduction | Armorer, Bastion Treasure, Buried Treasure, Jungle Temple |
| Tempering | Unbreaking | Durability | Toolsmith, Mineshaft, Desert Pyramid, Shipwreck |
| Grinding | Efficiency | Mining speed | Mineshaft, Dungeon, Desert Pyramid, Ruined Portal, Trial Chambers |

You duplicate templates via shaped crafting. Each uses the same shape (8 copper ingots surrounding the template) but a unique signature material in the top center slot:

| Template | Signature Material |
|---|---|
| Honing | Flint |
| Warding | Iron Ingot |
| Tempering | Obsidian |
| Grinding | Stone |

![Honing template duplication](images/duplication-honing.png)
![Warding template duplication](images/duplication-warding.png)
![Tempering template duplication](images/duplication-tempering.png)
![Grinding template duplication](images/duplication-grinding.png)

## Levels by Material

| Material | Level |
|---|---|
| Copper | I |
| Iron | II |
| Gold | III |
| Diamond | IV |
| Netherite | V |

No XP cost. The only cost is the template and the material. Any material higher than the current level works. An item at level III goes directly to V with a Netherite Ingot. Unupgraded items (level 0) start at any tier.

### What each upgrade modifies

| Template | Attribute Changed |
|---|---|
| Honing | Base attack damage +1 / +1.5 / +2 / +2.5 / +3, like Sharpness. Bows and crossbows add it to each arrow, like Power |
| Warding | 3.2% less damage per level, summed over every armor piece and capped at 64% (a full Warding V set) |
| Tempering | Durability loss like Unbreaking I / I / II / II / III |
| Grinding | Mining efficiency +2 / +5 / +10 / +17 / +26, like Efficiency |

## Visual Changes

Warding and Tempering upgrades on armor are visible as overlays on the 3D model, tinted to match the armor material. Both overlays stack together and are compatible with vanilla cosmetic trims.

![Iron armor with Warding and Tempering overlays](images/armor-overlays.png)
