# Changelog

All notable changes to Iron's Spell Scroll Ponder are documented in this file.

## 1.0.1 - 2026-07-31

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
