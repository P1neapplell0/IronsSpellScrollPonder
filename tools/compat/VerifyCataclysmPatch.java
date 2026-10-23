import com.p1nero.iss_ponder.compat.cataclysm.legacy.CataclysmLegacyTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.jar.JarFile;

/** Standalone ABI smoke check; needs ASM and the compiled transformer, not a Minecraft launch. */
public final class VerifyCataclysmPatch {
    public static void main(String[] args) throws Exception {
        int count = 0;
        try (JarFile input = new JarFile(args[0])) {
            for (var entry : input.stream().filter(e -> e.getName().endsWith(".class")).toList()) {
                byte[] original = input.getInputStream(entry).readAllBytes();
                ClassNode node = new ClassNode();
                new ClassReader(original).accept(node, 0);
                CataclysmLegacyTransformer.rewrite(node);
                byte[] transformed = bytes(node);
                if (Arrays.equals(original, transformed)) {
                    continue;
                }
                // A ClassNode roundtrip can change constant-pool ordering. Compare against an unpatched roundtrip.
                ClassNode baseline = new ClassNode();
                new ClassReader(original).accept(baseline, 0);
                if (Arrays.equals(bytes(baseline), transformed)) {
                    continue;
                }
                for (var method : node.methods) {
                    new Analyzer<>(new BasicVerifier()).analyze(node.name, method);
                }
                String pool = new String(transformed, StandardCharsets.ISO_8859_1);
                for (String obsolete : new String[]{"Ancient_Remnant_Rework_Renderer", "RingParticle$RingData",
                        "StormParticle$OrbData", "LightTrailParticle$OrbData", "TrackLightningParticle$OrbData"}) {
                    if (pool.contains(obsolete)) {
                        throw new AssertionError("Unpatched symbol " + obsolete + " in " + entry.getName());
                    }
                }
                CataclysmLegacyTransformer.rewrite(node);
                if (!Arrays.equals(transformed, bytes(node))) {
                    throw new AssertionError("Non-idempotent patch: " + entry.getName());
                }
                System.out.println("PASS " + entry.getName());
                count++;
            }
        }
        if (count != 12) {
            throw new AssertionError("Expected 12 changed classes (keep mixin targets synchronized), found " + count);
        }
        System.out.println("PASS: 12 classes; ASM BasicVerifier, obsolete-symbol scan, idempotence");
    }

    private static byte[] bytes(ClassNode node) {
        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }
}
