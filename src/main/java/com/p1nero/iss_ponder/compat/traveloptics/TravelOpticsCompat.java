package com.p1nero.iss_ponder.compat.traveloptics;

import com.p1nero.iss_ponder.compat.CompatModule;
import com.p1nero.iss_ponder.compat.support.PreviewActions;

/**
 * Upstream: Traveloptics spell onCast/recast state, ReversalEffect, EchoOfTheAbyssSpell#checkPreCastConditions.
 * Audited release JARs and exact implementation paths: docs/compatibility/releases.json and SPELL_AUDIT.md.
 */
public final class TravelOpticsCompat extends CompatModule {
    public TravelOpticsCompat() {
        // T.O's 1.21.1 release is deprecated upstream, not an audited latest-Iron's profile.
        super("6.3.0-1.20.1", "unavailable");
        // Exact checkPreCastConditions weapon predicates; never replace the addon's validation.
        rule("apex_harvest", "com.gametechbc.traveloptics.spells.eldritch.ApexHarvestSpell",
                PreviewActions.equipment("traveloptics:cursed_wraithblade", net.minecraft.world.entity.EquipmentSlot.MAINHAND));
        rule("skypiercer", "com.gametechbc.traveloptics.spells.aqua.SkypiercerSpell",
                PreviewActions.equipment("traveloptics:trident_of_the_eternal_maelstrom", net.minecraft.world.entity.EquipmentSlot.MAINHAND));
        rule("shear_of_the_stars", "com.gametechbc.traveloptics.spells.ender.ShearOfTheStarsSpell",
                PreviewActions.equipment("traveloptics:stellothorn", net.minecraft.world.entity.EquipmentSlot.MAINHAND));
        rule("supernova", "com.gametechbc.traveloptics.spells.ender.SupernovaSpell",
                PreviewActions.equipment("traveloptics:wand_of_final_light", net.minecraft.world.entity.EquipmentSlot.MAINHAND));
        rule("extinction", "com.gametechbc.traveloptics.spells.fire.ExtinctionSpell",
                PreviewActions.equipment("traveloptics:gauntlet_of_extinction", net.minecraft.world.entity.EquipmentSlot.MAINHAND));
        rule("tectonic_rift", "com.gametechbc.traveloptics.spells.fire.TectonicRiftSpell",
                PreviewActions.equipment("traveloptics:gauntlet_of_extinction", net.minecraft.world.entity.EquipmentSlot.MAINHAND));
        rule("gyro_slash", "com.gametechbc.traveloptics.spells.fire.GyroSlashSpell",
                PreviewActions.equipment("traveloptics:infernal_devastator", net.minecraft.world.entity.EquipmentSlot.MAINHAND));
        rule("galena_shatter", "com.gametechbc.traveloptics.spells.lightning.GalenaShatterSpell",
                PreviewActions.equipment("traveloptics:galenic_polarizer", net.minecraft.world.entity.EquipmentSlot.MAINHAND));
        rule("call_forth_the_dead_king", "com.gametechbc.traveloptics.spells.blood.CallForthTheDeadKingSpell",
                PreviewActions.castSource(io.redspace.ironsspellbooks.api.spells.CastSource.SCROLL));
        rule("jet_steam", "com.gametechbc.traveloptics.spells.aqua.JetSteamSpell", PreviewActions.submerged(true));
        rule("blood_howl", "com.gametechbc.traveloptics.spells.blood.BloodHowlSpell", PreviewActions.recasts(6, 12));
        rule("nocturnal_swarm", "com.gametechbc.traveloptics.spells.blood.NocturnalSwarmSpell", PreviewActions.recasts(6, 12));
        rule("psychic_bolt", "com.gametechbc.traveloptics.spells.eldritch.PsychicBoltSpell", PreviewActions.recasts(6, 12));
        rule("coral_barrage", "com.gametechbc.traveloptics.spells.aqua.CoralBarrageSpell", PreviewActions.recasts(6, 12));
        rule("void_eruption", "com.gametechbc.traveloptics.spells.ender.VoidEruptionSpell", PreviewActions.recasts(6, 12));
        rule("annihilation", "com.gametechbc.traveloptics.spells.fire.AnnihilationSpell", PreviewActions.recasts(6, 12));
        rule("nullflare", "com.gametechbc.traveloptics.spells.holy.NullflareSpell", PreviewActions.recasts(6, 12));
        rule("despair", "com.gametechbc.traveloptics.spells.ice.DespairSpell", PreviewActions.recasts(6, 12));
        rule("reversal", "com.gametechbc.traveloptics.spells.eldritch.ReversalSpell", PreviewActions.reversal());
        rule("echo_of_the_abyss", "com.gametechbc.traveloptics.spells.aqua.EchoOfTheAbyssSpell", PreviewActions.submerged());
    }
}
