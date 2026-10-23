# Real-client test matrix — 1.1.0

Local test date: **2026-10-24**. Forge and NeoForge clients were run serially,
never simultaneously. Both use Iron's Spellbooks **3.16.3**; NeoForge uses
**21.1.248**, Forge **47.4.4**. Counts below are enabled runtime registry spells,
not static language keys, and use each spell's maximum legal configured level.

| Add-on namespace | Forge 1.20.1 | NeoForge 1.21.1 |
| --- | --- | --- |
| `wind_spellbooks` | 7 completed | 7 completed |
| `legendary_spellbooks` | 30 completed | 30 completed |
| `cataclysm_spellbooks` | 67 completed | 59 completed |
| `alshanex_familiars` | 14 completed | 7 completed |
| `gtbcs_geomancy_plus` | 16 completed | 12 completed |
| `alexs_caves_spellbooks` | 3 completed | 3 completed |
| `darkermagic` | 4 completed | 4 completed |
| `ias_spellbooks` | 8 completed; 3 safely blocked | No selected release |
| `traveloptics` | 68 completed in isolated profile | Deprecated release excluded |
| `hazennstuff` | Reviewed file adds no spell implementations | 38 completed |
| `gametechbcs_spellbooks` | No selected release | 17 completed |
| **Totals** | **217 completed; 3 safely blocked** | **177 completed** |

“Completed” means actual integrated-server casting and Ponder-screen rendering
finished without a detected case failure. It is **not** a full visual/mechanical
pass. See [visual review](VISUAL_REVIEW.md), [runtime findings](RUNTIME_FINDINGS.md)
and the per-spell [JSON reports](test-results/).

The NeoForge full suite had 176 initial successes and a Switcheroo failure;
after fixing preparation timing, an identical-fixture resume reran that one
case successfully. Reports disclose reuse. Fresh min/max targeted tests cover
changed target setup and sustained book-spell follow-ups separately.
Both loaders also completed two Legendary generic cases with the experimental
master switch off; JVM class-loading logs recorded zero optional provider or
compatibility-support classes loaded. NeoForge's fresh changed-rule min/max
suite completed all ten cases.
Forge's corresponding four-spell min/max suite completed all eight cases after
the final ownership/fear-preparation and sustained-effect timing changes.

Twilight and the reviewed Ice and Fire Spellbooks release add books/items, not
spell implementations; they are not invented as extra spell-test passes.
BielGG's actual Forge startup attempt confirmed patch ownership yielding, but
its own premature config access blocked startup: **zero preview tests**. The
three Illage restrictions likewise are not working summon-effect passes.

T.O uses Cataclysm 3.16 / Lionfish 2.7 in isolation. Modern Forge Cataclysm
Spellbooks uses Cataclysm 3.31 with the opt-in legacy startup patch. These cannot
be installed as a single duplicate-mod profile. NeoForge's Hazen profile adds
Prometheus/Cobweb after a real projectile hit exposed its missing runtime API.

Release JARs exclude test runners and fixture dependencies. Dedicated-server,
multi-observer, complete visual parity and every upstream-version combination
are outside this matrix; unknown optional add-on versions fail closed.
