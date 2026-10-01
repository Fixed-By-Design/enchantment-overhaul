# Innate Material Properties

Specialized protections are intrinsic properties of armor materials, not enchantments. They replace the removed Protection variants (Fire, Blast, Projectile Protection).

## Resistances

| Material | Resistance | Damage Types |
|---|---|---|
| Netherite | Fire | Fire, lava, magma blocks |
| Copper | Poison | Poison, harming potions |
| Iron | Projectiles | Arrows, tridents, shulker bullets |
| Chainmail | Projectiles | Arrows, tridents, shulker bullets |
| Diamond | Explosions | Creepers, TNT, beds, respawn anchors |
| Gold | Magic | Poison, harming potions, Evoker fangs, Wither, dragon's breath |
| Leather | Falls | Fall damage |

Rose gold armor from Additional Additions resists explosions like diamond.

The armor tooltip shows its resistance, for example "Fire Resistance (5% per piece)".

## Netherite

Netherite is the only material that resists fire. On top of the innate 5% per piece damage reduction, it keeps its vanilla properties: items do not burn in lava, and the knockback resistance bonus remains. A full set gives 20% fire damage reduction from the innate system plus the best base armor value in the game.

## Stacking Formula

Each matching piece reduces incoming damage by 5%. The formula:

damage x (1.0 - 0.05 x matching_pieces)

| Matching Pieces | Damage Reduction |
|---|---|
| 1 | 5% (x0.95) |
| 2 | 10% (x0.90) |
| 3 | 15% (x0.85) |
| 4 (full set) | 20% (x0.80) |

Mixed sets stack additively. 2 Iron + 2 Diamond gives 10% vs projectiles and 10% vs explosions independently.
