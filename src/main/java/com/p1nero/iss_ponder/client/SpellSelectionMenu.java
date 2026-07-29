package com.p1nero.iss_ponder.client;

import com.mojang.blaze3d.systems.RenderSystem;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

/** Custom, dependency-free spell browser layered over the preview screen. */
final class SpellSelectionMenu {
    private static final long OPEN_DURATION_MS = 180;
    private static final long CLOSE_DURATION_MS = 130;
    private static final int GOLD = 0xFFD6B46A;
    private static final int GOLD_DARK = 0xFF77623B;
    private static final int PARCHMENT = 0xFFE9E2D2;
    private static final int MUTED = 0xFFAEB4B1;
    private static final int ARCANE = 0xFF62A99B;

    private final Consumer<ResourceLocation> switchHandler;
    private final List<MenuRow> rows = new ArrayList<>();
    private ResourceLocation selectedSpell;
    private String query = "";
    private double scroll;
    private int contentHeight;
    private int viewportHeight;
    private long openedAt;
    private long closingAt = -1;
    private boolean visible;
    private boolean dirty;

    SpellSelectionMenu(Consumer<ResourceLocation> switchHandler) {
        this.switchHandler = switchHandler;
    }

    void open(ResourceLocation currentSpell) {
        selectedSpell = currentSpell;
        query = "";
        scroll = 0;
        closingAt = -1;
        openedAt = Util.getMillis();
        visible = true;
        dirty = true;
    }

    void close() {
        if (visible && closingAt < 0) {
            closingAt = Util.getMillis();
        }
    }

    void closeImmediately() {
        visible = false;
        closingAt = -1;
    }

    boolean isVisible() {
        return visible;
    }

    void tick() {
        if (visible && closingAt >= 0 && Util.getMillis() - closingAt >= CLOSE_DURATION_MS) {
            closeImmediately();
        }
    }

    void render(GuiGraphics graphics, Font font, int screenWidth, int screenHeight, int mouseX, int mouseY) {
        if (!visible) {
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (dirty && player != null) {
            rebuildRows(player);
        }

        float progress = transitionProgress();
        int modalWidth = Math.max(230, Math.min(390, screenWidth - 24));
        int modalHeight = Math.max(150, Math.min(236, screenHeight - 20));
        int modalX = (screenWidth - modalWidth) / 2;
        int modalY = (screenHeight - modalHeight) / 2 + Math.round((1.0F - progress) * 12.0F);
        int alpha = Math.round(progress * 255.0F);

        graphics.fill(0, 0, screenWidth, screenHeight, withAlpha(0xC9000000, progress));
        graphics.fill(modalX, modalY, modalX + modalWidth, modalY + modalHeight, withAlpha(GOLD_DARK, progress));
        graphics.fill(modalX + 1, modalY + 1, modalX + modalWidth - 1, modalY + modalHeight - 1,
                withAlpha(0xFA0B1012, progress));
        graphics.fill(modalX + 7, modalY + 7, modalX + modalWidth - 7, modalY + 8,
                withAlpha(GOLD_DARK, progress));

        graphics.drawString(font, Component.translatable("gui.iss_ponder.spell_browser").withStyle(ChatFormatting.BOLD),
                modalX + 14, modalY + 14, withAlpha(GOLD, progress), false);
        int countWidth = font.width(Integer.toString(spellCount()));
        graphics.drawString(font, Integer.toString(spellCount()), modalX + modalWidth - 14 - countWidth,
                modalY + 14, withAlpha(MUTED, progress), false);

        int searchX = modalX + 14;
        int searchY = modalY + 31;
        int searchWidth = modalWidth - 28;
        graphics.fill(searchX, searchY, searchX + searchWidth, searchY + 20, withAlpha(GOLD_DARK, progress));
        graphics.fill(searchX + 1, searchY + 1, searchX + searchWidth - 1, searchY + 19,
                withAlpha(0xFF151C1D, progress));
        graphics.drawString(font, "?", searchX + 7, searchY + 6, withAlpha(ARCANE, progress), false);
        String visibleQuery = visibleSearchQuery(font, query, searchWidth - 30, true);
        Component searchText = query.isEmpty()
                ? Component.literal(visibleSearchQuery(font,
                        Component.translatable("gui.iss_ponder.search").getString(), searchWidth - 30, false))
                .withStyle(ChatFormatting.DARK_GRAY)
                : Component.literal(visibleQuery);
        graphics.drawString(font, searchText, searchX + 20, searchY + 6,
                withAlpha(query.isEmpty() ? MUTED : PARCHMENT, progress), false);
        if (closingAt < 0 && Util.getMillis() / 500L % 2L == 0L) {
            int cursorX = searchX + 20 + font.width(visibleQuery);
            graphics.fill(Math.min(cursorX, searchX + searchWidth - 6), searchY + 5,
                    Math.min(cursorX + 1, searchX + searchWidth - 5), searchY + 15,
                    withAlpha(ARCANE, progress));
        }

        int listTop = searchY + 27;
        int footerY = modalY + modalHeight - 31;
        int listBottom = footerY - 6;
        viewportHeight = Math.max(1, listBottom - listTop);
        graphics.fill(modalX + 10, listTop - 3, modalX + modalWidth - 10, listBottom + 3,
                withAlpha(0xD9101517, progress));
        graphics.enableScissor(modalX + 11, listTop, modalX + modalWidth - 11, listBottom);
        try {
            renderRows(graphics, font, modalX + 14, modalWidth - 28, listTop, mouseX, mouseY, progress);
        } finally {
            graphics.disableScissor();
        }
        renderScrollBar(graphics, modalX + modalWidth - 13, listTop, listBottom, progress);

        int cancelWidth = 72;
        int gap = 7;
        int switchWidth = 88;
        int cancelX = modalX + modalWidth - 14 - cancelWidth;
        int switchX = cancelX - gap - switchWidth;
        drawButton(graphics, font, switchX, footerY, switchWidth, 21,
                Component.translatable("gui.iss_ponder.switch"), selectedSpell != null,
                contains(mouseX, mouseY, switchX, footerY, switchWidth, 21), progress);
        drawButton(graphics, font, cancelX, footerY, cancelWidth, 21,
                Component.translatable("gui.iss_ponder.cancel"), true,
                contains(mouseX, mouseY, cancelX, footerY, cancelWidth, 21), progress);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha / 255.0F);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void renderRows(GuiGraphics graphics, Font font, int x, int width, int top,
                            int mouseX, int mouseY, float opacity) {
        int logicalY = 0;
        int renderedTop = top - (int) scroll;
        for (MenuRow row : rows) {
            int rowY = renderedTop + logicalY;
            if (row.spell == null) {
                graphics.fill(x, rowY + 13, x + width, rowY + 14, withAlpha(GOLD_DARK, opacity * 0.75F));
                graphics.drawString(font, row.label.copy().withStyle(ChatFormatting.BOLD), x + 2, rowY + 2,
                        withAlpha(row.accent, opacity), false);
            } else {
                boolean hovered = contains(mouseX, mouseY, x, rowY, width, row.height);
                boolean selected = row.spell.getSpellResource().equals(selectedSpell);
                if (hovered || selected) {
                    graphics.fill(x, rowY, x + width, rowY + row.height,
                            withAlpha(selected ? 0xFF223631 : 0xFF1D2827, opacity));
                    graphics.fill(x, rowY, x + 2, rowY + row.height,
                            withAlpha(selected ? ARCANE : row.accent, opacity));
                }
                RenderSystem.enableBlend();
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, opacity);
                graphics.blit(row.spell.getSpellIconResource(), x + 6, rowY + 2, 0, 0, 16, 16, 16, 16);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                graphics.drawString(font, row.label, x + 28, rowY + 6,
                        withAlpha(selected ? PARCHMENT : MUTED, opacity), false);
            }
            logicalY += row.height;
        }
        contentHeight = logicalY;
        clampScroll();
    }

    private void renderScrollBar(GuiGraphics graphics, int x, int top, int bottom, float opacity) {
        if (contentHeight <= viewportHeight) {
            return;
        }
        int thumbHeight = Math.max(12, viewportHeight * viewportHeight / contentHeight);
        int travel = viewportHeight - thumbHeight;
        int thumbY = top + (int) Math.round(travel * scroll / maxScroll());
        graphics.fill(x, top, x + 1, bottom, withAlpha(0xFF394140, opacity));
        graphics.fill(x - 1, thumbY, x + 2, thumbY + thumbHeight, withAlpha(ARCANE, opacity));
    }

    private void drawButton(GuiGraphics graphics, Font font, int x, int y, int width, int height,
                            Component label, boolean enabled, boolean hovered, float opacity) {
        int border = enabled && hovered ? ARCANE : GOLD_DARK;
        int fill = enabled ? (hovered ? 0xFF263B36 : 0xFF182120) : 0xFF171919;
        graphics.fill(x, y, x + width, y + height, withAlpha(border, opacity));
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, withAlpha(fill, opacity));
        graphics.drawCenteredString(font, label, x + width / 2, y + 7,
                withAlpha(enabled ? PARCHMENT : 0xFF6F7472, opacity));
    }

    boolean mouseClicked(double mouseX, double mouseY, int button, int screenWidth, int screenHeight) {
        if (!visible || closingAt >= 0 || button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return visible;
        }
        int modalWidth = Math.max(230, Math.min(390, screenWidth - 24));
        int modalHeight = Math.max(150, Math.min(236, screenHeight - 20));
        int modalX = (screenWidth - modalWidth) / 2;
        int modalY = (screenHeight - modalHeight) / 2;
        if (!contains(mouseX, mouseY, modalX, modalY, modalWidth, modalHeight)) {
            close();
            return true;
        }

        int searchY = modalY + 31;
        int listTop = searchY + 27;
        int footerY = modalY + modalHeight - 31;
        int listBottom = footerY - 6;
        int cancelWidth = 72;
        int switchWidth = 88;
        int cancelX = modalX + modalWidth - 14 - cancelWidth;
        int switchX = cancelX - 7 - switchWidth;
        if (contains(mouseX, mouseY, cancelX, footerY, cancelWidth, 21)) {
            close();
            return true;
        }
        if (selectedSpell != null && contains(mouseX, mouseY, switchX, footerY, switchWidth, 21)) {
            switchHandler.accept(selectedSpell);
            closeImmediately();
            return true;
        }
        if (contains(mouseX, mouseY, modalX + 14, listTop, modalWidth - 28, listBottom - listTop)) {
            double contentY = mouseY - listTop + scroll;
            int rowTop = 0;
            for (MenuRow row : rows) {
                if (row.spell != null && contentY >= rowTop && contentY < rowTop + row.height) {
                    selectedSpell = row.spell.getSpellResource();
                    return true;
                }
                rowTop += row.height;
            }
        }
        return true;
    }

    boolean mouseScrolled(double delta) {
        if (!visible) {
            return false;
        }
        scroll -= delta * 22.0;
        clampScroll();
        return true;
    }

    boolean keyPressed(int keyCode, int modifiers) {
        if (!visible) {
            return false;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        if (closingAt >= 0) {
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            if (selectedSpell != null) {
                switchHandler.accept(selectedSpell);
                closeImmediately();
            }
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !query.isEmpty()) {
            query = query.substring(0, query.offsetByCodePoints(query.length(), -1));
            searchChanged();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_V && (modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
            String clipboard = Minecraft.getInstance().keyboardHandler.getClipboard();
            for (int i = 0; i < clipboard.length() && query.length() < 64; i++) {
                char character = clipboard.charAt(i);
                if (StringUtil.isAllowedChatCharacter(character)) {
                    query += character;
                }
            }
            searchChanged();
            return true;
        }
        return true;
    }

    boolean charTyped(char codePoint) {
        if (!visible || closingAt >= 0 || !StringUtil.isAllowedChatCharacter(codePoint) || query.length() >= 64) {
            return visible;
        }
        query += codePoint;
        searchChanged();
        return true;
    }

    private void searchChanged() {
        scroll = 0;
        dirty = true;
    }

    private static String visibleSearchQuery(Font font, String text, int maxWidth, boolean keepEnd) {
        if (text.isEmpty() || font.width(text) <= maxWidth) {
            return text;
        }
        if (!keepEnd) {
            return font.plainSubstrByWidth(text, maxWidth);
        }
        int start = 0;
        while (start < text.length() && font.width(text.substring(start)) > maxWidth) {
            start = text.offsetByCodePoints(start, 1);
        }
        return text.substring(start);
    }

    private void rebuildRows(LocalPlayer player) {
        rows.clear();
        String needle = query.toLowerCase(Locale.ROOT).trim();
        List<AbstractSpell> spells = new ArrayList<>(SpellRegistry.getEnabledSpells());
        spells.removeIf(spell -> spell == null || spell == SpellRegistry.none());
        spells.sort(Comparator.comparing((AbstractSpell spell) -> schoolName(spell).toLowerCase(Locale.ROOT))
                .thenComparing(spell -> spellName(spell, player).toLowerCase(Locale.ROOT))
                .thenComparing(AbstractSpell::getSpellId));

        Map<SchoolType, List<AbstractSpell>> grouped = new LinkedHashMap<>();
        for (AbstractSpell spell : spells) {
            String name = spellName(spell, player);
            String school = schoolName(spell);
            if (!needle.isEmpty() && !name.toLowerCase(Locale.ROOT).contains(needle)
                    && !spell.getSpellId().toLowerCase(Locale.ROOT).contains(needle)
                    && !school.toLowerCase(Locale.ROOT).contains(needle)) {
                continue;
            }
            grouped.computeIfAbsent(spell.getSchoolType(), ignored -> new ArrayList<>()).add(spell);
        }

        for (Map.Entry<SchoolType, List<AbstractSpell>> group : grouped.entrySet()) {
            Component schoolLabel = group.getKey() == null
                    ? Component.translatable("gui.iss_ponder.unknown_school")
                    : group.getKey().getDisplayName();
            int accent = schoolColor(group.getKey());
            rows.add(new MenuRow(null, schoolLabel, 18, accent));
            for (AbstractSpell spell : group.getValue()) {
                rows.add(new MenuRow(spell, Component.literal(spellName(spell, player)), 20, accent));
            }
        }
        contentHeight = rows.stream().mapToInt(MenuRow::height).sum();
        clampScroll();
        dirty = false;
    }

    private int spellCount() {
        return (int) rows.stream().filter(row -> row.spell != null).count();
    }

    private static String spellName(AbstractSpell spell, LocalPlayer player) {
        try {
            return spell.getDisplayName(player).getString();
        } catch (RuntimeException ignored) {
            return spell.getSpellName();
        }
    }

    private static String schoolName(AbstractSpell spell) {
        try {
            return spell.getSchoolType() == null ? "" : spell.getSchoolType().getDisplayName().getString();
        } catch (RuntimeException ignored) {
            return "";
        }
    }

    private static int schoolColor(SchoolType school) {
        if (school == null) {
            return ARCANE;
        }
        try {
            Vector3f color = school.getTargetingColor();
            return 0xFF000000 | Mth.clamp(Math.round(color.x * 255), 0, 255) << 16
                    | Mth.clamp(Math.round(color.y * 255), 0, 255) << 8
                    | Mth.clamp(Math.round(color.z * 255), 0, 255);
        } catch (RuntimeException ignored) {
            return ARCANE;
        }
    }

    private double maxScroll() {
        return Math.max(0, contentHeight - Math.max(1, viewportHeight));
    }

    private void clampScroll() {
        scroll = Mth.clamp(scroll, 0.0, maxScroll());
    }

    private float transitionProgress() {
        float opening = Mth.clamp((Util.getMillis() - openedAt) / (float) OPEN_DURATION_MS, 0.0F, 1.0F);
        float progress = opening * opening * (3.0F - 2.0F * opening);
        if (closingAt >= 0) {
            float closing = Mth.clamp((Util.getMillis() - closingAt) / (float) CLOSE_DURATION_MS, 0.0F, 1.0F);
            closing = closing * closing * (3.0F - 2.0F * closing);
            progress *= 1.0F - closing;
        }
        return progress;
    }

    private static boolean contains(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private static int withAlpha(int color, float opacity) {
        int alpha = Math.round(((color >>> 24) & 0xFF) * Mth.clamp(opacity, 0.0F, 1.0F));
        return color & 0x00FFFFFF | alpha << 24;
    }

    private record MenuRow(AbstractSpell spell, Component label, int height, int accent) {
    }
}
