package com.p1nero.iss_ponder.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.p1nero.iss_ponder.network.ModNetwork;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.util.TooltipsUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SpellPreviewScreen extends Screen {
    private static final long OPEN_DURATION_MS = 320;
    private static final long CLOSE_DURATION_MS = 180;
    private static final long REPLAY_FEEDBACK_MS = 520;

    private static final int BACKGROUND_TOP = 0xB8050708;
    private static final int BACKGROUND_BOTTOM = 0xD0090D0F;
    private static final int PANEL = 0xF20E1214;
    private static final int PANEL_INNER = 0xE8191D1E;
    private static final int GOLD = 0xFFD6B46A;
    private static final int GOLD_DARK = 0xFF77623B;
    private static final int PARCHMENT = 0xFFE9E2D2;
    private static final int MUTED = 0xFFAEB4B1;
    private static final int ARCANE = 0xFF62A99B;
    private static final int CRIMSON = 0xFF7C3E49;

    private ResourceLocation spellId;
    private int spellLevel;
    private boolean simulationAllowed;
    private AbstractSpell spell;
    private final long openedAt = Util.getMillis();
    private final SpellSelectionMenu spellMenu;

    private long closingAt = -1;
    private long replayAt = -1;
    private double panelScroll;
    private int panelContentHeight;
    private int panelViewportHeight;
    private boolean ending;

    public SpellPreviewScreen(ResourceLocation spellId, int spellLevel, boolean simulationAllowed) {
        super(Component.translatable("gui.iss_ponder.title"));
        this.spellId = spellId;
        this.spellLevel = spellLevel;
        this.simulationAllowed = simulationAllowed;
        this.spell = SpellRegistry.getSpell(spellId);
        this.spellMenu = new SpellSelectionMenu(this::requestSpellSwitch);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        float progress = transitionProgress();
        int panelWidth = panelWidth();
        int panelX = width - panelWidth;
        int animatedPanelX = panelX + Math.round((1.0F - progress) * 38.0F);
        int sceneLeft = 12;
        int sceneTop = 46;
        int sceneRight = Math.max(sceneLeft + 1, panelX - 12);
        int sceneBottom = Math.max(sceneTop + 1, height - 16);

        graphics.fillGradient(0, 0, width, height, BACKGROUND_TOP, BACKGROUND_BOTTOM);
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        graphics.enableScissor(sceneLeft, sceneTop, sceneRight, sceneBottom);
        PreviewProjection.render(graphics, Math.max(1, panelX), height, partialTick);
        graphics.disableScissor();
        if (progress < 1.0F) {
            graphics.fill(sceneLeft, sceneTop, sceneRight, sceneBottom,
                    withOpacity(0xFF000000, 1.0F - progress));
        }

        // Ponder entities use a real depth buffer. UI is drawn in a separate layer so
        // spell projectiles can never cover the title, panel or controls.
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        RenderSystem.disableDepthTest();
        try {
            int surfaceMouseX = spellMenu.isVisible() ? -10_000 : mouseX;
            int surfaceMouseY = spellMenu.isVisible() ? -10_000 : mouseY;
            renderSceneFrame(graphics, sceneLeft, sceneTop, sceneRight, sceneBottom, progress);
            renderCameraHint(graphics, sceneLeft, sceneRight, sceneBottom, progress);
            renderHeader(graphics, panelX, surfaceMouseX, surfaceMouseY, progress);
            renderPanel(graphics, animatedPanelX, panelWidth, surfaceMouseX, surfaceMouseY, progress);
            spellMenu.render(graphics, font, width, height, mouseX, mouseY);
        } finally {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.enableDepthTest();
        }
    }

    private void renderSceneFrame(GuiGraphics graphics, int left, int top, int right, int bottom, float opacity) {
        int border = withOpacity(GOLD_DARK, opacity);
        int accent = withOpacity(GOLD, opacity * 0.78F);
        graphics.fill(left - 1, top - 1, right + 1, top, border);
        graphics.fill(left - 1, bottom, right + 1, bottom + 1, border);
        graphics.fill(left - 1, top, left, bottom, border);
        graphics.fill(right, top, right + 1, bottom, border);

        drawCorner(graphics, left - 3, top - 3, 1, 1, accent);
        drawCorner(graphics, right + 2, top - 3, -1, 1, accent);
        drawCorner(graphics, left - 3, bottom + 2, 1, -1, accent);
        drawCorner(graphics, right + 2, bottom + 2, -1, -1, accent);
    }

    private void drawCorner(GuiGraphics graphics, int x, int y, int horizontal, int vertical, int color) {
        int x2 = x + horizontal * 9;
        int y2 = y + vertical * 9;
        graphics.fill(Math.min(x, x2), y, Math.max(x, x2) + 1, y + 1, color);
        graphics.fill(x, Math.min(y, y2), x + 1, Math.max(y, y2) + 1, color);
    }

    private void renderCameraHint(GuiGraphics graphics, int sceneLeft, int sceneRight,
                                  int sceneBottom, float opacity) {
        Component hint = Component.translatable("gui.iss_ponder.rotate_hint");
        int maxTextWidth = Math.max(36, sceneRight - sceneLeft - 24);
        List<FormattedCharSequence> lines = font.split(hint, maxTextWidth);
        if (lines.isEmpty()) {
            return;
        }
        int lineCount = Math.min(2, lines.size());
        int textWidth = 0;
        for (int i = 0; i < lineCount; i++) {
            textWidth = Math.max(textWidth, font.width(lines.get(i)));
        }
        int hintWidth = textWidth + 14;
        int hintHeight = 7 + lineCount * 9;
        int x = Math.max(sceneLeft + 4, (sceneLeft + sceneRight - hintWidth) / 2);
        int y = sceneBottom - hintHeight - 4;
        graphics.fill(x, y, Math.min(sceneRight - 4, x + hintWidth), y + hintHeight,
                withOpacity(0xB80A0E10, opacity));
        graphics.fill(x, y, x + 2, y + hintHeight, withOpacity(ARCANE, opacity));
        for (int i = 0; i < lineCount; i++) {
            graphics.drawString(font, lines.get(i), x + 7, y + 3 + i * 9,
                    withOpacity(MUTED, opacity), false);
        }
    }

    private void renderHeader(GuiGraphics graphics, int panelX, int mouseX, int mouseY, float opacity) {
        int offsetY = -Math.round((1.0F - opacity) * 14.0F);
        graphics.fill(0, offsetY, panelX, offsetY + 36, withOpacity(0xE90B0E10, opacity));
        graphics.fill(0, offsetY + 35, panelX, offsetY + 36, withOpacity(GOLD_DARK, opacity));
        if (spell == null) {
            return;
        }

        int iconX = 14;
        int iconY = 5 + offsetY;
        boolean iconHovered = !spellMenu.isVisible()
                && contains(mouseX, mouseY, iconX - 4, iconY - 4, 32, 32);
        graphics.fill(iconX - 3, iconY - 3, iconX + 29, iconY + 29,
                withOpacity(iconHovered ? ARCANE : GOLD_DARK, opacity));
        graphics.fill(iconX - 1, iconY - 1, iconX + 27, iconY + 27, withOpacity(0xFF101617, opacity));
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, opacity);
        graphics.pose().pushPose();
        float iconScale = iconHovered ? 1.1F : 1.0F;
        graphics.pose().translate(iconX + 12, iconY + 12, 0);
        graphics.pose().scale(iconScale, iconScale, 1.0F);
        graphics.blit(spell.getSpellIconResource(), -12, -12, 0, 0, 24, 24, 16, 16);
        graphics.pose().popPose();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        if (iconHovered) {
            graphics.drawString(font, ">", iconX + 23, iconY + 18, withOpacity(ARCANE, opacity), false);
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        int textX = 50;
        int maxTitleWidth = Math.max(40, panelX - textX - 12);
        List<FormattedCharSequence> titleLines = font.split(
                spell.getDisplayName(player).copy().withStyle(ChatFormatting.BOLD), maxTitleWidth);
        if (!titleLines.isEmpty()) {
            graphics.drawString(font, titleLines.get(0), textX, 7 + offsetY,
                    withOpacity(PARCHMENT, opacity), false);
        }
        graphics.drawString(font, spell.getSchoolType().getDisplayName(), textX, 21 + offsetY,
                withOpacity(ARCANE, opacity), false);
    }

    private void renderPanel(GuiGraphics graphics, int panelX, int panelWidth,
                             int mouseX, int mouseY, float opacity) {
        graphics.fill(panelX, 0, width, height, withOpacity(PANEL, opacity));
        graphics.fill(panelX, 0, panelX + 1, height, withOpacity(GOLD_DARK, opacity));
        graphics.fill(panelX + 5, 7, width - 6, 8, withOpacity(GOLD_DARK, opacity * 0.75F));
        graphics.fill(panelX + 5, height - 8, width - 6, height - 7, withOpacity(GOLD_DARK, opacity * 0.75F));

        graphics.drawString(font, title.copy().withStyle(ChatFormatting.BOLD), panelX + 15, 15,
                withOpacity(GOLD, opacity), false);
        graphics.fill(panelX + 15, 31, width - 15, 32, withOpacity(GOLD_DARK, opacity));
        List<FormattedCharSequence> statusLines = font.split(ClientPreviewController.getStatus(), panelWidth - 44);
        int statusLineCount = Math.max(1, Math.min(2, statusLines.size()));
        graphics.fill(panelX + 15, 42, panelX + 19, 42 + statusLineCount * 10,
                withOpacity(ARCANE, opacity));
        for (int i = 0; i < statusLineCount && i < statusLines.size(); i++) {
            graphics.drawString(font, statusLines.get(i), panelX + 25, 40 + i * 10,
                    withOpacity(PARCHMENT, opacity), false);
        }

        float castProgress = PreviewProjection.castProgress();
        int contentTop = 60 + (statusLineCount - 1) * 10;
        if (castProgress >= 0.0F) {
            int progressY = contentTop - 7;
            String percentage = Math.round(castProgress * 100.0F) + "%";
            int percentageWidth = font.width(percentage);
            int barX = panelX + 25;
            int barRight = Math.max(barX + 10, width - 21 - percentageWidth);
            graphics.fill(barX, progressY, barRight, progressY + 5, withOpacity(GOLD_DARK, opacity));
            graphics.fill(barX + 1, progressY + 1,
                    barX + 1 + Math.round((barRight - barX - 2) * castProgress), progressY + 4,
                    withOpacity(ARCANE, opacity));
            graphics.drawString(font, percentage, width - 16 - percentageWidth, progressY - 2,
                    withOpacity(PARCHMENT, opacity), false);
            contentTop += 9;
        }
        int contentBottom = Math.max(contentTop + 1, height - 52);
        panelViewportHeight = Math.max(1, contentBottom - contentTop);
        graphics.fill(panelX + 9, contentTop - 5, width - 9, contentBottom + 3,
                withOpacity(PANEL_INNER, opacity));
        graphics.enableScissor(panelX + 10, contentTop - 4, width - 10, contentBottom + 2);
        try {
            renderPanelContent(graphics, panelX, panelWidth, contentTop, opacity);
        } finally {
            graphics.disableScissor();
        }
        renderScrollIndicator(graphics, panelX, panelWidth, contentTop, contentBottom, opacity);
        if (!spellMenu.isVisible()) {
            renderFooter(graphics, panelX, panelWidth, mouseX, mouseY, opacity);
        }
    }

    private void renderPanelContent(GuiGraphics graphics, int panelX, int panelWidth,
                                    int contentTop, float opacity) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (spell == null || player == null) {
            panelContentHeight = 0;
            return;
        }
        int x = panelX + 17;
        int maxWidth = panelWidth - 34;
        int logicalY = 0;
        int y = contentTop - (int) panelScroll;

        Component description = Component.translatable(spell.getComponentId() + ".guide")
                .withStyle(ChatFormatting.GRAY);
        for (FormattedCharSequence line : font.split(description, maxWidth)) {
            graphics.drawString(font, line, x, y + logicalY, withOpacity(MUTED, opacity), false);
            logicalY += 11;
        }
        logicalY += 10;
        graphics.fill(x, y + logicalY, x + maxWidth, y + logicalY + 1, withOpacity(GOLD_DARK, opacity));
        logicalY += 9;
        graphics.drawString(font, Component.translatable("gui.iss_ponder.details").withStyle(ChatFormatting.BOLD),
                x, y + logicalY, withOpacity(GOLD, opacity), false);
        logicalY += 16;

        for (StatEntry stat : buildStats(player)) {
            int rowTop = y + logicalY;
            graphics.drawString(font, stat.label(), x, rowTop, withOpacity(MUTED, opacity), false);
            int labelWidth = font.width(stat.label());
            int valueWidth = font.width(stat.value());
            if (labelWidth + valueWidth + 14 <= maxWidth) {
                graphics.drawString(font, stat.value(), x + maxWidth - valueWidth, rowTop,
                        withOpacity(stat.color(), opacity), false);
                logicalY += 14;
            } else {
                logicalY += 11;
                for (FormattedCharSequence line : font.split(stat.value(), maxWidth - 8)) {
                    graphics.drawString(font, line, x + 8, y + logicalY,
                            withOpacity(stat.color(), opacity), false);
                    logicalY += 10;
                }
                logicalY += 4;
            }
            graphics.fill(x, y + logicalY - 3, x + maxWidth, y + logicalY - 2,
                    withOpacity(0xFF303737, opacity * 0.65F));
        }
        panelContentHeight = logicalY + 5;
        clampPanelScroll();
    }

    private List<StatEntry> buildStats(LocalPlayer player) {
        List<StatEntry> stats = new ArrayList<>();
        stats.add(stat("gui.iss_ponder.level", Component.literal(Integer.toString(spellLevel))));
        stats.add(stat("gui.iss_ponder.rarity", spell.getRarity(spellLevel).getDisplayName()));
        stats.add(stat("gui.iss_ponder.school", spell.getSchoolType().getDisplayName()));

        Component castType = spell.getCastType() == CastType.INSTANT
                ? Component.translatable("ui.irons_spellbooks.cast_instant")
                : TooltipsUtils.getCastTimeComponent(spell.getCastType(),
                Utils.timeFromTicks(spell.getEffectiveCastTime(spellLevel, player), 2));
        stats.add(stat("gui.iss_ponder.cast_type", castType));
        stats.add(stat("gui.iss_ponder.mana", Component.literal(Integer.toString(spell.getManaCost(spellLevel)))));
        stats.add(stat("gui.iss_ponder.cooldown", Component.literal(
                Utils.timeFromTicks(MagicManager.getEffectiveSpellCooldown(spell, player,
                        io.redspace.ironsspellbooks.api.spells.CastSource.SCROLL), 2) + "s")));
        stats.add(stat("gui.iss_ponder.spell_power", Component.literal(format(spell.getSpellPower(spellLevel, player)))));
        stats.add(stat("gui.iss_ponder.power_multiplier", Component.literal(
                format(spell.getEntityPowerMultiplier(player)) + "x")));
        stats.add(stat("gui.iss_ponder.observed_damage", Component.literal(format(PreviewProjection.observedDamage()))));

        try {
            for (MutableComponent uniqueLine : spell.getUniqueInfo(spellLevel, player)) {
                stats.add(new StatEntry(Component.literal("+"), uniqueLine, ARCANE));
            }
        } catch (RuntimeException ignored) {
            // Some add-on spells assume a server entity while producing unique tooltip data.
        }
        return stats;
    }

    private StatEntry stat(String key, Component value) {
        return new StatEntry(Component.translatable(key), value, PARCHMENT);
    }

    private void renderScrollIndicator(GuiGraphics graphics, int panelX, int panelWidth,
                                       int top, int bottom, float opacity) {
        int viewport = Math.max(1, bottom - top);
        if (panelContentHeight <= viewport) {
            return;
        }
        int trackX = panelX + panelWidth - 13;
        int thumbHeight = Math.max(12, viewport * viewport / panelContentHeight);
        int travel = viewport - thumbHeight;
        int thumbY = top + (int) Math.round(travel * panelScroll / maxPanelScroll());
        graphics.fill(trackX, top, trackX + 1, bottom, withOpacity(0xFF394140, opacity));
        graphics.fill(trackX - 1, thumbY, trackX + 2, thumbY + thumbHeight, withOpacity(ARCANE, opacity));
    }

    private void renderFooter(GuiGraphics graphics, int panelX, int panelWidth,
                              int mouseX, int mouseY, float opacity) {
        int footerY = height - 43 + Math.round((1.0F - opacity) * 15.0F);
        graphics.fill(panelX + 8, footerY - 5, width - 8, height - 9,
                withOpacity(0xE8101415, opacity));

        int replayX = panelX + 15;
        int closeX = width - 37;
        int replayWidth = Math.max(60, closeX - replayX - 8);
        boolean replayHovered = contains(mouseX, mouseY, replayX, footerY, replayWidth, 22);
        boolean closeHovered = contains(mouseX, mouseY, closeX, footerY, 22, 22);
        float replayPulse = replayPulse();

        int replayFill = simulationAllowed
                ? blend(0xFF182120, 0xFF263B36, Math.max(replayHovered ? 1.0F : 0.0F, replayPulse))
                : 0xFF171919;
        drawButton(graphics, replayX, footerY, replayWidth, 22,
                replayFill, replayHovered ? ARCANE : GOLD_DARK, opacity);
        graphics.drawCenteredString(font, Component.translatable("gui.iss_ponder.replay"),
                replayX + replayWidth / 2, footerY + 7,
                withOpacity(simulationAllowed ? PARCHMENT : 0xFF6F7472, opacity));

        drawButton(graphics, closeX, footerY, 22, 22,
                closeHovered ? 0xFF3A2025 : 0xFF1C1719,
                closeHovered ? CRIMSON : GOLD_DARK, opacity);
        graphics.drawCenteredString(font, "X", closeX + 11, footerY + 7,
                withOpacity(PARCHMENT, opacity));
    }

    private void drawButton(GuiGraphics graphics, int x, int y, int width, int height,
                            int fill, int border, float opacity) {
        graphics.fill(x, y, x + width, y + height, withOpacity(border, opacity));
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, withOpacity(fill, opacity));
        graphics.fill(x + 2, y + 2, x + width - 2, y + 3, withOpacity(0xFFB9C9C1, opacity * 0.18F));
    }

    private String format(float value) {
        return Mth.equal(value, Math.round(value))
                ? Integer.toString(Math.round(value))
                : String.format(Locale.ROOT, "%.2f", value);
    }

    private int panelWidth() {
        return Math.min(318, Math.max(190, Math.round(width * 0.34F)));
    }

    private float transitionProgress() {
        float opening = Mth.clamp((Util.getMillis() - openedAt) / (float) OPEN_DURATION_MS, 0.0F, 1.0F);
        float progress = smoothStep(opening);
        if (closingAt >= 0) {
            float closing = Mth.clamp((Util.getMillis() - closingAt) / (float) CLOSE_DURATION_MS, 0.0F, 1.0F);
            progress *= 1.0F - smoothStep(closing);
        }
        return progress;
    }

    private float replayPulse() {
        if (replayAt < 0) {
            return 0.0F;
        }
        float elapsed = Mth.clamp((Util.getMillis() - replayAt) / (float) REPLAY_FEEDBACK_MS, 0.0F, 1.0F);
        return (float) Math.sin(elapsed * Math.PI) * (1.0F - elapsed);
    }

    private static float smoothStep(float value) {
        return value * value * (3.0F - 2.0F * value);
    }

    private int maxPanelScroll() {
        return Math.max(0, panelContentHeight - Math.max(1, panelViewportHeight));
    }

    private void clampPanelScroll() {
        panelScroll = Mth.clamp(panelScroll, 0.0, maxPanelScroll());
    }

    private static boolean contains(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private static int withOpacity(int color, float opacity) {
        int alpha = Math.round(((color >>> 24) & 0xFF) * Mth.clamp(opacity, 0.0F, 1.0F));
        return (color & 0x00FFFFFF) | (alpha << 24);
    }

    private static int blend(int first, int second, float amount) {
        amount = Mth.clamp(amount, 0.0F, 1.0F);
        int r = Mth.lerpInt(amount, (first >> 16) & 0xFF, (second >> 16) & 0xFF);
        int g = Mth.lerpInt(amount, (first >> 8) & 0xFF, (second >> 8) & 0xFF);
        int b = Mth.lerpInt(amount, first & 0xFF, second & 0xFF);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        PreviewProjection.tick();
        spellMenu.tick();
        if (closingAt >= 0 && Util.getMillis() - closingAt >= CLOSE_DURATION_MS) {
            finishClose();
            return;
        }
        if (closingAt >= 0 || spellMenu.isVisible()) {
            return;
        }

        long window = Minecraft.getInstance().getWindow().getWindow();
        if (isDown(window, GLFW.GLFW_KEY_W)) {
            ClientPreviewController.panCamera(0.18, 0.0, 0.0);
        }
        if (isDown(window, GLFW.GLFW_KEY_S)) {
            ClientPreviewController.panCamera(-0.18, 0.0, 0.0);
        }
        if (isDown(window, GLFW.GLFW_KEY_A)) {
            ClientPreviewController.panCamera(0.0, -0.18, 0.0);
        }
        if (isDown(window, GLFW.GLFW_KEY_D)) {
            ClientPreviewController.panCamera(0.0, 0.18, 0.0);
        }
        if (isDown(window, GLFW.GLFW_KEY_SPACE)) {
            ClientPreviewController.panCamera(0.0, 0.0, 0.14);
        }
        if (isDown(window, GLFW.GLFW_KEY_LEFT_SHIFT) || isDown(window, GLFW.GLFW_KEY_RIGHT_SHIFT)) {
            ClientPreviewController.panCamera(0.0, 0.0, -0.14);
        }
    }

    private static boolean isDown(long window, int key) {
        return GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (spellMenu.isVisible()) {
            return spellMenu.mouseClicked(mouseX, mouseY, button, width, height);
        }
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT || closingAt >= 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        if (contains(mouseX, mouseY, 10, 1, 36, 36)) {
            spellMenu.open(spellId);
            return true;
        }
        float progress = transitionProgress();
        int panelWidth = panelWidth();
        int panelX = width - panelWidth + Math.round((1.0F - progress) * 38.0F);
        int footerY = height - 43 + Math.round((1.0F - progress) * 15.0F);
        int replayX = panelX + 15;
        int closeX = width - 37;
        int replayWidth = Math.max(60, closeX - replayX - 8);
        if (contains(mouseX, mouseY, closeX, footerY, 22, 22)) {
            onClose();
            return true;
        }
        if (simulationAllowed && contains(mouseX, mouseY, replayX, footerY, replayWidth, 22)) {
            replayAt = Util.getMillis();
            ModNetwork.sendToServer(new ModNetwork.ReplayPreview());
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (spellMenu.isVisible()) {
            return true;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && closingAt < 0 && mouseX < width - panelWidth()) {
            ClientPreviewController.moveCamera((float) dragX * 0.35F, (float) dragY * 0.25F);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (spellMenu.isVisible()) {
            return spellMenu.mouseScrolled(scrollY);
        }
        if (mouseX >= width - panelWidth()) {
            panelScroll -= scrollY * 18.0;
            clampPanelScroll();
            return true;
        }
        ClientPreviewController.zoomCamera(scrollY);
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (spellMenu.isVisible()) {
            return spellMenu.keyPressed(keyCode, modifiers);
        }
        switch (keyCode) {
            case GLFW.GLFW_KEY_ESCAPE -> {
                onClose();
                return true;
            }
            case GLFW.GLFW_KEY_W, GLFW.GLFW_KEY_S, GLFW.GLFW_KEY_A, GLFW.GLFW_KEY_D,
                    GLFW.GLFW_KEY_SPACE, GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT -> {
                return true;
            }
            default -> {
                return super.keyPressed(keyCode, scanCode, modifiers);
            }
        }
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (spellMenu.isVisible()) {
            return spellMenu.charTyped(codePoint);
        }
        return super.charTyped(codePoint, modifiers);
    }

    public void applySpell(ResourceLocation newSpellId, int newSpellLevel, boolean newSimulationAllowed) {
        spellId = newSpellId;
        spellLevel = newSpellLevel;
        simulationAllowed = newSimulationAllowed;
        spell = SpellRegistry.getSpell(newSpellId);
        panelScroll = 0;
        replayAt = -1;
        spellMenu.closeImmediately();
    }

    private void requestSpellSwitch(ResourceLocation newSpellId) {
        ModNetwork.sendToServer(new ModNetwork.SwitchPreview(newSpellId, spellLevel));
    }

    @Override
    public void onClose() {
        if (closingAt < 0) {
            closingAt = Util.getMillis();
        }
    }

    private void finishClose() {
        if (!ending) {
            ending = true;
            ModNetwork.sendToServer(new ModNetwork.EndPreview());
        }
        super.onClose();
    }

    @Override
    public void removed() {
        if (!ending) {
            ending = true;
            ModNetwork.sendToServer(new ModNetwork.EndPreview());
        }
        ClientPreviewController.restoreCamera();
        super.removed();
    }

    private record StatEntry(Component label, Component value, int color) {
    }
}
