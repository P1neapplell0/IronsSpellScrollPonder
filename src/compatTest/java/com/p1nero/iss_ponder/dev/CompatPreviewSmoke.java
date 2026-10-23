package com.p1nero.iss_ponder.dev;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.p1nero.iss_ponder.ISSPonderMod;
import com.p1nero.iss_ponder.api.SpellPreviewAdapters;
import com.p1nero.iss_ponder.client.ClientPreviewController;
import com.p1nero.iss_ponder.client.PreviewProjection;
import com.p1nero.iss_ponder.client.SpellPreviewScreen;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Development-only real-client smoke runner; excluded from every distributable JAR. Not a visual oracle. */
@Mod.EventBusSubscriber(modid = ISSPonderMod.MOD_ID, value = Dist.CLIENT)
public final class CompatPreviewSmoke {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final List<Map<String, Object>> RESULTS = new ArrayList<>();
    private static List<Case> cases;
    private static int tick;
    private static int nextAt;
    private static int index;
    private static int startedAt;
    private static int peakEntities;
    private static boolean createdWorld;
    private static boolean finished;
    private static Case current;
    private static String fixtureIdentity;
    private static final String RUN_ID = Long.toString(System.currentTimeMillis());

    private CompatPreviewSmoke() {
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !Boolean.getBoolean("issponder.dev.smoke")) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        tick++;
        try {
            if (minecraft.level == null || minecraft.player == null) {
                if (!createdWorld && Boolean.getBoolean("issponder.dev.autoWorld")
                        && minecraft.screen instanceof TitleScreen && tick > 60) {
                    createdWorld = true;
                    createWorld(minecraft);
                }
                return;
            }
            // Never send automated previews to an unrelated remote server.
            if (!minecraft.hasSingleplayerServer()) {
                return;
            }
            if (cases == null) {
                cases = loadCases(minecraft);
                nextAt = tick + 60;
                log("BEGIN " + cases.size() + " cases; rendered smoke checks, NOT visual certification");
                writeReport(minecraft);
            }
            if (finished) {
                if (Boolean.getBoolean("issponder.dev.exit") && tick >= nextAt) {
                    minecraft.stop();
                }
                return;
            }
            if (current == null && tick >= nextAt) {
                if (index == cases.size()) {
                    finished = true;
                    nextAt = tick + 60;
                    log("FINISHED " + RESULTS.size() + " cases; screenshots need manual inspection");
                    writeReport(minecraft);
                    return;
                }
                current = cases.get(index++);
                peakEntities = 0;
                startedAt = tick;
                var spell = SpellRegistry.getSpell(current.spell());
                if (spell == SpellRegistry.none() || !spell.isEnabled()) {
                    complete(minecraft, "MISSING_OR_DISABLED_SPELL");
                    return;
                }
                ClientPreviewController.setStatus(Component.empty());
                ClientPreviewController.request(current.spell(), current.level());
                minecraft.getSingleplayerServer().execute(() -> log("ADAPTER " + current.spell() + " "
                        + SpellPreviewAdapters.get(current.spell()).getClass().getName()));
                log("START " + current.spell() + " level=" + current.level());
                writeReport(minecraft);
            }
            if (current == null) {
                return;
            }
            if (minecraft.screen instanceof SpellPreviewScreen screen) {
                field(SpellPreviewScreen.class, "playbackEnabled").setBoolean(screen, false);
                peakEntities = Math.max(peakEntities, ((Map<?, ?>) field(PreviewProjection.class, "ENTITIES").get(null)).size());
                int elapsed = tick - startedAt;
                if (elapsed == 10 || elapsed == 40 || elapsed == 80 || elapsed == 120) {
                    screenshot(minecraft, elapsed);
                }
                String status = statusKey();
                if (status.equals("gui.iss_ponder.cast_failed")) {
                    complete(minecraft, "CAST_FAILED");
                } else if (status.equals("gui.iss_ponder.restricted") && elapsed >= 10) {
                    complete(minecraft, "SKIPPED_BY_SAFETY_POLICY");
                } else if (status.equals("gui.iss_ponder.complete") && elapsed >= 45) {
                    complete(minecraft, peakEntities >= 3 ? "RENDERED_COMPLETE" : "NO_PROJECTED_TARGETS");
                }
            }
            if (current != null && tick - startedAt > 500) {
                complete(minecraft, "TIMEOUT_OR_SCREEN_NOT_OPENED");
            }
        } catch (Exception | LinkageError exception) {
            ISSPonderMod.LOGGER.error("[ISS-COMPAT-TEST] Runner failed", exception);
            if (current != null) {
                complete(minecraft, "RUNNER_ERROR: " + exception);
            } else {
                finished = true;
            }
        }
    }

    private static void createWorld(Minecraft minecraft) {
        // A fresh flat world, only in the isolated profile directory. No existing save or EULA is touched.
        String name = "iss-compat-" + System.currentTimeMillis();
        LevelSettings settings = new LevelSettings(name, GameType.CREATIVE, false, Difficulty.NORMAL,
                true, new GameRules(), WorldDataConfiguration.DEFAULT);
        minecraft.createWorldOpenFlows().createFreshLevel(name, settings, new WorldOptions(0L, false, false),
                registry -> registry.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT)
                        .value().createWorldDimensions());
    }

    private static List<Case> loadCases(Minecraft minecraft) throws Exception {
        Path file = minecraft.gameDirectory.toPath().resolve("compat-cases.json");
        List<String> ids = JSON.fromJson(Files.readString(file), new TypeToken<List<String>>() {}.getType());
        var fixture = JSON.fromJson(Files.readString(minecraft.gameDirectory.toPath().resolve("compat-fixtures.json")),
                com.google.gson.JsonObject.class);
        fixtureIdentity = JSON.toJson(fixture);
        if (fixture.has("scope") && fixture.get("scope").getAsString().equals("all")) {
            // Discover inherited/helper-registered spells from the real registry, not just the static audit.
            List<String> namespaces = new ArrayList<>();
            for (var mod : fixture.getAsJsonArray("fixtures")) {
                namespaces.add(mod.getAsJsonObject().get("modId").getAsString());
            }
            var discovered = new java.util.TreeSet<String>();
            List<String> disabled = new ArrayList<>();
            for (var spell : SpellRegistry.REGISTRY.get()) {
                if (spell != null && spell != SpellRegistry.none()
                        && namespaces.contains(spell.getSpellResource().getNamespace())) {
                    if (spell.isEnabled()) {
                        discovered.add(spell.getSpellResource().toString());
                    } else {
                        disabled.add(spell.getSpellResource().toString());
                    }
                }
            }
            List<String> unavailable = new ArrayList<>(ids);
            unavailable.removeAll(discovered);
            Files.writeString(minecraft.gameDirectory.toPath().resolve("compat-audit-unavailable.json"), JSON.toJson(unavailable));
            Files.writeString(minecraft.gameDirectory.toPath().resolve("compat-disabled-spells.json"), JSON.toJson(disabled));
            ids = new ArrayList<>(discovered);
        }
        String selected = System.getProperty("issponder.dev.spells", "");
        if (!selected.isBlank()) {
            ids = Arrays.asList(selected.split(","));
        }
        Files.writeString(minecraft.gameDirectory.toPath().resolve("compat-runtime-cases.json"), JSON.toJson(ids));
        List<Case> result = new ArrayList<>();
        for (String id : ids) {
            ResourceLocation resource = ResourceLocation.parse(id.trim());
            var spell = SpellRegistry.getSpell(resource);
            for (String level : System.getProperty("issponder.dev.levels", "max").split(",")) {
                int requested = level.trim().equals("min") ? spell.getMinLevel()
                        : level.trim().equals("max") ? spell.getMaxLevel() : Integer.parseInt(level.trim());
                result.add(new Case(resource, Math.max(spell.getMinLevel(), Math.min(spell.getMaxLevel(), requested))));
            }
        }
        Path reportPath = minecraft.gameDirectory.toPath().resolve("compat-test-results.json");
        if (Boolean.getBoolean("issponder.dev.resume") && Files.isRegularFile(reportPath)) {
            var previous = JSON.fromJson(Files.readString(reportPath), com.google.gson.JsonObject.class);
            if (previous.has("fixtureIdentity") && fixtureIdentity.equals(previous.get("fixtureIdentity").getAsString())) {
                List<Map<String, Object>> rows = JSON.fromJson(previous.get("results"),
                        new TypeToken<List<Map<String, Object>>>() {}.getType());
                for (var row : rows) {
                    if ("RENDERED_COMPLETE".equals(row.get("result"))) {
                        boolean matched = result.removeIf(test -> test.spell().toString().equals(row.get("spell"))
                                && test.level() == ((Number) row.get("level")).intValue());
                        if (matched) RESULTS.add(row);
                    }
                }
                log("RESUMED " + RESULTS.size() + " prior completed cases with identical fixtures; failures will rerun");
            }
        }
        return result;
    }

    private static void complete(Minecraft minecraft, String result) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("spell", current.spell().toString());
        row.put("level", current.level());
        row.put("result", result);
        row.put("ticks", tick - startedAt);
        row.put("peakProjectedEntities", peakEntities);
        row.put("status", statusKey());
        row.put("visualReview", "PENDING: inspect screenshots; completion alone does not prove correct effect");
        RESULTS.add(row);
        if (minecraft.screen instanceof SpellPreviewScreen) {
            screenshot(minecraft, tick - startedAt);
            minecraft.setScreen(null);
        }
        log("RESULT " + JSON.toJson(row));
        writeReport(minecraft);
        current = null;
        nextAt = tick + 40;
    }

    private static void screenshot(Minecraft minecraft, int elapsed) {
        String name = String.format("compat-" + RUN_ID + "-%02d-%s-l%d-t%d.png", index,
                current.spell().toString().replace(':', '_'), current.level(), elapsed);
        Screenshot.grab(minecraft.gameDirectory, name, minecraft.getMainRenderTarget(), message -> log(message.getString()));
    }

    private static String statusKey() {
        var content = ClientPreviewController.getStatus().getContents();
        return content instanceof TranslatableContents translated ? translated.getKey() : content.toString();
    }

    private static void writeReport(Minecraft minecraft) {
        try {
            Map<String, Object> report = new LinkedHashMap<>();
            report.put("updatedAt", Instant.now().toString());
            report.put("finished", finished);
            report.put("runId", RUN_ID);
            report.put("fixtureIdentity", fixtureIdentity);
            report.put("currentSpell", current == null ? null : current.spell().toString());
            report.put("scope", "Real integrated-server casting and real Ponder screen rendering; NOT dedicated-server or visual certification");
            report.put("results", RESULTS);
            Files.writeString(minecraft.gameDirectory.toPath().resolve("compat-test-results.json"), JSON.toJson(report));
        } catch (Exception exception) {
            ISSPonderMod.LOGGER.error("Could not write development smoke report", exception);
        }
    }

    private static Field field(Class<?> owner, String name) throws ReflectiveOperationException {
        Field field = owner.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    private static void log(String message) {
        ISSPonderMod.LOGGER.info("[ISS-COMPAT-TEST] {}", message);
    }

    private record Case(ResourceLocation spell, int level) {
    }
}
