# Screenshot review notes (2026-10-24)

Screenshots come from the real isolated clients described in
[DEVELOPMENT_TESTING.md](DEVELOPMENT_TESTING.md). This is a selective review,
not a visual pass for every registry entry. Per-case reports deliberately keep
`visualReview: PENDING`; automated completion is not a semantic oracle.

Observed in the local Forge T.O screenshots:

- Coral Barrage: a continuous orange projectile/particle stream reaches the
  targets. Psychic Bolt: cyan projectiles appear around the target group.
- Galena Shatter: red beam/ring geometry and its large weapon are visible.
- Nocturnal Swarm: large bat entities are projected, partly outside the frame.
- Echo of the Abyss / Jet Steam: the water-and-glass prerequisite volume is
  present. A separate Echo completion screenshot shows the Hullbreaker; its
  size exceeds the default viewport. Water alone does not prove Jet's movement
  or every particle was reproduced correctly.
- Other sampled frames (including Supernova and Extinction) show no persistent
  effect at the sampled instant. That is not enough to distinguish an expired
  effect from missing client visuals; manual live comparison remains needed.

Observed in NeoForge all-profile screenshots:

- GTBC Ensnare: a green enclosing field is visible. Psychic Bolt and
  Shatterpoint show cyan geometry/projectiles near the targets.
- Geomancy Chunker and Eroding Boulder project large block-textured entities.
- Sampled sustained/self-buff/follow-up scenes sometimes have no visible
  geometry in the final frame. Review intermediate frames and ordinary casting
  before treating those effects as correct.

Earlier individual screenshots also showed Forge Thoth's remnant and particles,
NeoForge Rewire's watcher target, and Hazen Dazzling Obliteration's blue effects.
The fresh NeoForge Switcheroo retest shows the mage familiar at the caster's
former platform position after casting; the owned-pet target is now present.
Large summons and airborne casters can require manual camera pan/zoom. A fixed
camera is not a guarantee of complete framing. Dedicated-server networking,
normal-cast parity, damage and all summon AI still require separate checks.
