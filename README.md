# Iron's Spell Scroll Ponder

Iron's Spell Scroll Ponder adds an interactive spell preview to scrolls from
[Iron's Spells 'n Spellbooks](https://www.curseforge.com/minecraft/mc-mods/irons-spells-n-spellbooks).
Hover a valid spell scroll and hold Ponder's configurable key (`W` by default)
to open a live preview.

Instead of maintaining a hand-authored scene for every spell, the mod casts the
selected spell on a server-side simulated player in an isolated preview
dimension. Entities, blocks, particles, sounds, cast state, and selected Iron's
Spellbooks client packets are projected into a Ponder virtual level. This gives
built-in spells and many add-on spells a useful preview without per-spell scene
code.

## Add-on Compatibility

Most Iron's Spellbooks add-ons work automatically because this mod discovers
their registered spells through `SpellRegistry` and executes the real spell on
a server-side simulated player. It then projects the resulting entities,
particles, sounds, block changes, cast state, and reviewed visual packets into
the Ponder scene. An ordinary add-on spell that uses Iron's standard casting and
world APIs does not need a dedicated integration.

### Experimental complex-spell adapters (1.1.0)

Optional integrations add follow-up actions for selected spells from Legendary,
Cataclysm Spellbooks, T.O Magic (Forge), Alshanex's Familiars, Hazen 'n
Stuff (NeoForge), GTBC's Spellbooks (NeoForge), Geomancy Plus and Alex's Caves
Spellbooks. Ordinary spells keep using the generic preview. No add-on becomes
a required dependency.

This feature is **disabled by default**. Set `experimental.enabled = true` in
`config/iss_ponder-common.toml` and restart to opt in. Each add-on also has its
own `experimental.addons.<modid>` switch. Disabled providers are not loaded;
unknown add-on versions are skipped, and matching rules are guarded against
runtime/linkage failures. Explicit adapters registered by add-on authors win.
These guards cannot repair arbitrary exceptions in the add-on's own spell code.

Forge also offers a separate, default-off `experimental.cataclysmLegacyPatch`
for Cataclysm Spellbooks 1.2.9 with Cataclysm 3.31. It requires the master switch,
is skipped when BielGG's Spells is installed, and does not exist on NeoForge.
See the [compatibility guide](docs/compatibility/README.md) for the exact audited
versions, configuration, startup recovery instructions and limitations;
[SPELL_AUDIT.md](docs/compatibility/SPELL_AUDIT.md) records individual spell
reviews. Real-client registry smoke runs and remaining upstream restrictions
are recorded in the development guide; complete visual/mechanical and
dedicated-server verification is still required. T.O's deprecated 1.21.1 release is
not included in the supported experimental profiles.

For opt-in development dependencies, isolated add-on profiles and a real-client
preview runner, see [Development testing](docs/compatibility/DEVELOPMENT_TESTING.md).
These fixtures are not release dependencies. The guide separates actual
rendered-complete cases, blocked cases and outstanding visual review;
compilation alone does not certify demonstrations.

### Integration API for add-on authors

Complex spells may require an adapter when their important behavior happens
after the initial cast, such as a recast, a later physical attack, an effect
removal callback, or another gameplay interaction. Add-on authors can register a
server-side `SpellPreviewAdapter` during common initialization:

```java
SpellPreviewAdapters.register(
        new ResourceLocation("your_mod", "complex_spell"),
        new SpellPreviewAdapter() {
            @Override
            public void onCastCompleted(SpellPreviewContext context) {
                // Prepare any state required by the follow-up demonstration.
            }

            @Override
            public Action tickAfterCast(SpellPreviewContext context) {
                if (context.ticksSinceLastCast() == 12) {
                    context.performPhysicalAttack(1.0F);
                }
                return context.ticksSinceLastCast() >= 20 ? Action.COMPLETE : Action.WAIT;
            }
        });
```

The adapter lifecycle also provides `primaryTargetType` for spells that require
a specific target, `initialBlocks` for origin-relative scene structures,
`onSceneReady`, `beforeCast`, `beforeRecast`, and `onSessionClosed`. Override
`castSource` only when normal validation requires a source other than COMMAND
(for example, a book-exclusive spell with its required Curios book equipped).
`SpellPreviewContext`
exposes bounded helpers for common interactions. Return `false` from
`allowsSimulation` for spells that cannot be isolated safely, such as persistent
portals or cross-dimension travel. Custom client-only packets are not
automatically trusted: prefer standard entity, particle, sound, and block APIs,
or contribute a narrowly reviewed projection handler for that packet. See
[`SpellPreviewAdapter`](src/main/java/com/p1nero/iss_ponder/api/SpellPreviewAdapter.java)
and [`SpellPreviewContext`](src/main/java/com/p1nero/iss_ponder/api/SpellPreviewContext.java)
for the complete API.

## Features

- Hold the normal Ponder key while viewing a scroll tooltip to open the preview.
- Display spell name, description, school, rarity, level, cast type, mana cost,
  cooldown, spell power, unique statistics, and observed target damage.
- Render a compact `7 x 11` arena with two extra rows behind both the simulated
  caster and the three targets.
- Loop completed previews by default, with a play/pause control that does not
  freeze the simulated scene.
- Browse enabled spells by school, search them, and switch the active preview.
- Rotate, pan, and zoom the Ponder-style scene.
- Show server-synchronized cast progress for charged and continuous spells.
- Run spell logic on the server while keeping the real player in place.
- Bundle Ponder and Flywheel inside the distributable JAR.

## Requirements

This branch targets:

- Minecraft `1.20.1`
- Forge `47.4.4` or newer in the Forge 47 line
- Iron's Spells 'n Spellbooks `1.20.1-3.16.3 (development baseline; dependency metadata remains permissive)`
- Java `17`

Ponder `1.0.92` and Flywheel `1.0.0-215` are packaged with Jar-in-Jar and do not
need to be installed separately. Iron's Spellbooks and its normal runtime
dependencies are still required. Install this mod on both the client and server.

## Usage

1. Open an inventory screen and hover a valid Iron's Spellbooks scroll.
2. Hold the Ponder key shown in the tooltip.
3. Drag to rotate the scene, use the mouse wheel to zoom, and use the movement
   keys shown by the interface to pan the camera.
4. Use **Pause** to stop looping after the current preview, or **Play** to resume.
5. Click the scroll icon to search for and preview another enabled spell.
6. Press Escape or close the screen to end the server preview session.

The Ponder key can be changed in Minecraft's key bindings.
The generated `config/iss_ponder-client.toml` file provides
`tooltip.showScrollSpellDescription`; set it to `false` to stop adding the
spell's guide description to scroll tooltips without disabling the preview
prompt.

## Safety and Compatibility

Spell code is executable mod code, so automatic compatibility can never be
absolute. The preview limits a cast to 15 seconds and isolates each player in a
distant cell of `iss_ponder:spell_preview`. The built-in `pocket_dimension`,
`recall`, and `portal` spells are information-only because they can change
dimensions or persistent world state.

Add-on spells that depend on a biome, structure, block interaction, real player
inventory, or a second gameplay action may need a dedicated adapter. Custom
client packets also require explicit review before they are replayed in the
virtual level.

The project includes dedicated follow-up adapters for multi-stage, recast,
reactive defense, and subsequent-projectile mechanics. See
[SPECIAL_SPELLS.md](SPECIAL_SPELLS.md) for the test matrix and
[TECHNICAL_NOTES.md](TECHNICAL_NOTES.md) for the verified architecture, source
references, and upgrade checklist.

## License

The mod's current metadata declares **All Rights Reserved**. Bundled dependencies
retain their own licenses; Ponder is distributed under the MIT License.
