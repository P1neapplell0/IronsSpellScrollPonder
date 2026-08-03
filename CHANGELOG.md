# Changelog

All notable changes to Iron's Spell Scroll Ponder are documented in this file.

## 1.0.2 - 2026-08-03

This release targets Minecraft 1.20.1 and Forge 47.

### Changed

- Documented the Forge FakePlayer networking lifecycle and its upstream code
  references in `TECHNICAL_NOTES.md`.

### Fixed

- Initialized Forge's shared FakePlayer connection with a persistent local
  Netty channel so optional network checks from add-on mods no longer crash
  the preview server tick with a null channel.
- Kept virtual packet handling local; unsupported remote channels are reported
  as absent and FakePlayer packets continue to be discarded.
- Fixed the Thunderstorm preview exhausting the render buffer by translating
  absolute endpoints embedded in Iron's lightning particle options.
- Added a client-side distance and finite-value guard for destination-bearing
  particles so malformed add-on effects cannot allocate unbounded geometry.

## 1.0.1 - 2026-07-31

This release targets Minecraft 1.20.1 and Forge 47.

### Added

- Added play/pause controls for the preview loop in every supported language.
- Added adapter support for spell-specific primary target entity types and
  origin-relative initial scene blocks.
- Added prepared targets for `wololo`, `sacrifice`, `spectral_hammer`, and
  `touch_dig`.

### Changed

- Expanded the arena to a symmetrical `7 x 11` platform, with two extra rows
  behind both the caster and the targets.
- Preserved the original `7 x 7` camera basis so the larger platform does not
  alter the default framing height.
- Replaced the Replay button with a playback toggle. Previews loop by default;
  pausing prevents the next replay without freezing entities, particles, or
  simulation time.
- Updated the add-on compatibility and special-spell documentation for the new
  adapter hooks.

### Fixed

- Fixed `wololo` failing because the preview supplied a zombie instead of a
  sheep.
- Fixed `sacrifice` failing because its target was not an owned magic summon.
- Fixed `spectral_hammer` and `touch_dig` failing because no valid block was
  present on the caster's horizontal ray.
- Ensured prepared scene blocks are projected on first load and every replay,
  then removed when the preview session closes.
