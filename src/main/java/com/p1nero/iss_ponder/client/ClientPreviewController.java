package com.p1nero.iss_ponder.client;

import com.p1nero.iss_ponder.network.ModNetwork;
import com.p1nero.iss_ponder.ISSPonderMod;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public final class ClientPreviewController {
    private static Component status = Component.empty();
    private static boolean pending;
    private static long requestedAt;

    private ClientPreviewController() {
    }

    public static void request(ResourceLocation spellId, int spellLevel) {
        if (pending || Minecraft.getInstance().screen instanceof SpellPreviewScreen) {
            return;
        }
        pending = true;
        requestedAt = Util.getMillis();
        ModNetwork.sendToServer(new ModNetwork.StartPreview(spellId, spellLevel));
        Minecraft.getInstance().setScreen(null);
    }

    public static void open(ResourceLocation spellId, int spellLevel, boolean simulationAllowed) {
        open(spellId, spellLevel, simulationAllowed,
                Minecraft.getInstance().player == null ? 0 : Minecraft.getInstance().player.getX(),
                Minecraft.getInstance().player == null ? 0 : Minecraft.getInstance().player.getY(),
                Minecraft.getInstance().player == null ? 0 : Minecraft.getInstance().player.getZ());
    }

    public static void open(ResourceLocation spellId, int spellLevel, boolean simulationAllowed,
                            double projectionX, double projectionY, double projectionZ) {
        Minecraft minecraft = Minecraft.getInstance();
        pending = false;
        try {
            PreviewProjection.start(projectionX, projectionY, projectionZ);
        } catch (RuntimeException exception) {
            ISSPonderMod.LOGGER.error("Could not create the virtual spell preview", exception);
            PreviewProjection.clear();
        }
        if (!PreviewProjection.isActive()) {
            ModNetwork.sendToServer(new ModNetwork.EndPreview());
            if (minecraft.player != null) {
                minecraft.player.displayClientMessage(Component.translatable("gui.iss_ponder.preview_unavailable"), true);
            }
            if (minecraft.screen instanceof SpellPreviewScreen) {
                minecraft.setScreen(null);
            }
            return;
        }
        if (minecraft.screen instanceof SpellPreviewScreen screen) {
            screen.applySpell(spellId, spellLevel, simulationAllowed);
        } else {
            minecraft.setScreen(new SpellPreviewScreen(spellId, spellLevel, simulationAllowed));
        }
    }

    public static void setStatus(Component newStatus) {
        status = newStatus;
    }

    public static Component getStatus() {
        return status;
    }

    public static void previewComplete() {
        if (Minecraft.getInstance().screen instanceof SpellPreviewScreen screen) {
            screen.onPreviewComplete();
        }
    }

    public static void replayStarted() {
        if (Minecraft.getInstance().screen instanceof SpellPreviewScreen screen) {
            screen.onReplayStarted();
        }
    }

    public static boolean isPending() {
        if (pending && Util.getMillis() - requestedAt > 5_000) {
            pending = false;
        }
        return pending;
    }

    public static boolean isPreviewOpen() {
        return Minecraft.getInstance().screen instanceof SpellPreviewScreen;
    }

    public static void restoreCamera() {
        PreviewProjection.clear();
    }

    public static void moveCamera(float yawDelta, float pitchDelta) {
        PreviewProjection.updateCamera(yawDelta, pitchDelta);
    }

    public static void zoomCamera(double amount) {
        PreviewProjection.zoomCamera(amount);
    }

    public static void panCamera(double forward, double right, double vertical) {
        PreviewProjection.panCamera(forward, right, vertical);
    }

    public static void spawnProjectionEntity(int id, java.util.UUID uuid, String typeId, String name,
                                             double x, double y, double z, float yaw, float pitch, float health,
                                             int hurtTime, boolean swinging, CompoundTag data, byte[] spawnData,
                                             java.util.List<net.minecraft.network.syncher.SynchedEntityData.DataValue<?>> syncedData) {
        PreviewProjection.spawnOrUpdate(id, uuid, typeId, name, x, y, z, yaw, pitch, health,
                hurtTime, swinging, data, spawnData, syncedData);
    }

    public static void removeProjectionEntity(int id) {
        PreviewProjection.remove(id);
    }
}
