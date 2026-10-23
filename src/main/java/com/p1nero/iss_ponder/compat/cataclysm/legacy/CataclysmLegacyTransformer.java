package com.p1nero.iss_ponder.compat.cataclysm.legacy;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.SimpleRemapper;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.ArrayDeque;
import java.util.Map;

public final class CataclysmLegacyTransformer {
    private static final String BASE = "com/github/L_Ender/cataclysm/";
    private static final String BRIDGE = "com/p1nero/iss_ponder/compat/cataclysm/legacy/CataclysmLegacyBridge";
    private static final String OLD_RING = BASE + "client/particle/RingParticle$RingData";
    // The guarded factory returns the interface (including its safe fallback). Remap local/stack-map
    // types to that interface too; retaining the concrete Ring type can fail JVM frame verification.
    private static final String RING = "net/minecraft/core/particles/ParticleOptions";
    private static final String OLD_MISSILE = "(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/LivingEntity;DDDDDDFLnet/minecraft/world/level/Level;)V";
    private static final String NEW_MISSILE = "(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/LivingEntity;DDDLnet/minecraft/world/phys/Vec3;FLnet/minecraft/world/level/Level;)V";
    private static final Map<String, String> ALIASES = Map.of(
            OLD_RING, RING,
            BASE + "client/render/entity/Ancient_Remnant_Rework_Renderer", BASE + "client/render/entity/Ancient_Remnant_Renderer",
            BASE + "client/particle/StormParticle$OrbData", BASE + "client/particle/Options/StormParticleOptions",
            BASE + "client/particle/LightTrailParticle$OrbData", BASE + "client/particle/Options/LightTrailParticleOptions",
            BASE + "client/particle/TrackLightningParticle$OrbData", BASE + "client/particle/Options/TrackLightningParticleOptions");

    private CataclysmLegacyTransformer() {
    }

    public static void rewrite(ClassNode original) {
        // ABI comparison: Cataclysm Spellbooks 1.2.9 vs Cataclysm 3.31; see docs/compatibility.
        // Work on a copy so an unexpected constructor shape cannot leave a partially patched class.
        ClassNode node = new ClassNode();
        original.accept(new ClassRemapper(node, new SimpleRemapper(ALIASES)));
        // The replacements are top-level classes/interfaces, not members of the obsolete outer class.
        node.innerClasses.removeIf(inner -> ALIASES.containsValue(inner.name));
        for (var method : node.methods) {
            ArrayDeque<AbstractInsnNode[]> allocations = new ArrayDeque<>();
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (instruction instanceof TypeInsnNode type && type.getOpcode() == Opcodes.NEW && type.desc.equals(RING)) {
                    AbstractInsnNode duplicate = instruction.getNext();
                    if (duplicate == null || duplicate.getOpcode() != Opcodes.DUP) {
                        throw new IllegalStateException("Unrecognized legacy RingData allocation");
                    }
                    allocations.push(new AbstractInsnNode[]{instruction, duplicate});
                }
                if (!(instruction instanceof MethodInsnNode call)) {
                    continue;
                }
                if (call.owner.equals(RING) && call.name.equals("<init>")) {
                    if (!call.desc.equals("(FFIFFFFFZLcom/github/L_Ender/cataclysm/client/particle/RingParticle$EnumRingBehavior;)V")
                            || allocations.isEmpty()) {
                        throw new IllegalStateException("Unrecognized legacy RingData constructor");
                    }
                    AbstractInsnNode[] allocation = allocations.pop();
                    method.instructions.remove(allocation[0]);
                    method.instructions.remove(allocation[1]);
                    method.instructions.set(call, new MethodInsnNode(Opcodes.INVOKESTATIC, BRIDGE, "ring",
                            "(FFIFFFFFZLjava/lang/Object;)Lnet/minecraft/core/particles/ParticleOptions;", false));
                } else if (call.owner.equals(BASE + "entity/projectile/Wither_Missile_Entity")
                        && call.name.equals("<init>") && call.desc.equals(OLD_MISSILE)) {
                    int levelSlot = method.maxLocals;
                    int damageSlot = levelSlot + 1;
                    method.maxLocals += 2;
                    InsnList replacement = new InsnList();
                    replacement.add(new VarInsnNode(Opcodes.ASTORE, levelSlot));
                    replacement.add(new VarInsnNode(Opcodes.FSTORE, damageSlot));
                    replacement.add(new MethodInsnNode(Opcodes.INVOKESTATIC, BRIDGE, "vector",
                            "(DDD)Lnet/minecraft/world/phys/Vec3;", false));
                    replacement.add(new VarInsnNode(Opcodes.FLOAD, damageSlot));
                    replacement.add(new VarInsnNode(Opcodes.ALOAD, levelSlot));
                    method.instructions.insertBefore(call, replacement);
                    call.desc = NEW_MISSILE;
                } else if (call.owner.equals(BASE + "util/CMDamageTypes") && call.name.equals("causeLaserDamage")
                        && call.desc.equals("(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/damagesource/DamageSource;")) {
                    call.desc = "(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/damagesource/DamageSource;";
                } else if (call.owner.equals("net/acetheeldritchking/cataclysm_spellbooks/entity/mobs/PhantomAncientRemnant")
                        && call.name.equals("setSleep") && call.desc.equals("(Z)V")) {
                    method.instructions.set(call, new MethodInsnNode(Opcodes.INVOKESTATIC, BRIDGE, "sleep",
                            "(Ljava/lang/Object;Z)V", false));
                }
            }
        }
        original.superName = node.superName;
        original.signature = node.signature;
        original.interfaces = node.interfaces;
        original.fields = node.fields;
        original.methods = node.methods;
        original.innerClasses = node.innerClasses;
        original.outerClass = node.outerClass;
        original.outerMethodDesc = node.outerMethodDesc;
    }
}
