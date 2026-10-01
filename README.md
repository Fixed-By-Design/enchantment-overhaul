<p align="center">
  <img src="docs/images/logo.png" alt="Enchantment Overhaul" width="600">
</p>

A Fabric mod for Minecraft 26.1.2 that rethinks the enchanting system.

Vanilla enchanting mixes stat boosts with magical effects in a single RNG system. This mod separates everything into three distinct systems where every choice is deliberate:

- **Smithing Table** handles stat boosts (damage, protection, durability, mining speed) through templates found in structures.
- **Innate Material Properties** give armor specialized defenses based on what it's made of.
- **Enchanting Table** is reserved for magical effects, unlocked by placing enchanted books in chiseled bookshelves, paid with a reagent + XP, and limited by a slot system tied to item material.

The mod adds 6 new enchantments, reworks the anvil into a maintenance station, and curates enchanted book loot across all structures.

## Documentation

See the [Wiki](https://github.com/Aqu1tain/enchantment-overhaul/wiki) for detailed system documentation.

## Credits

- **Aqu1tain** — design and development
- **[LolloNapo](https://modrinth.com/user/Napino)** — textures and art
- **[NotAida](https://namemc.com/profile/NotAida.1)** — logo design

## Requirements

- Minecraft 26.1.2
- Fabric Loader >= 0.18.6
- Fabric API

## Development

`./gradlew test` runs the unit tests. `./gradlew runClientGameTest` opens a game client, plays through the catalogue, anvil, grindstone, smithing table and equipment effects, and saves screenshots to `build/run/clientGameTest/screenshots`.

## Version support

Enchantment Overhaul supports only the latest Minecraft version targeted by
the `dev` branch, currently Minecraft 26.1.2. Older Minecraft versions do not
receive backports, bug fixes, or compatibility updates.
