package com.p1nero.iss_ponder.client;

import com.p1nero.iss_ponder.ISSPonderMod;
import com.p1nero.iss_ponder.config.ClientConfig;
import io.redspace.ironsspellbooks.api.item.IScroll;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import net.createmod.ponder.enums.PonderKeybinds;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber(modid = ISSPonderMod.MOD_ID, value = Dist.CLIENT)
public final class ClientScrollPonderHandler {
    private static final int HOLD_TICKS = 12;
    private static ItemStack hoveredStack = ItemStack.EMPTY;
    private static long lastTooltipAt;
    private static int holdTicks;

    private ClientScrollPonderHandler() {
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        ItemStack stack = event.getItemStack();
        SpellData spellData = getPreviewableSpell(stack);
        if (event.getEntity() == null || minecraft.screen == null || ClientPreviewController.isPreviewOpen()
                || spellData == null) {
            return;
        }

        if (!ItemStack.isSameItemSameComponents(hoveredStack, stack)) {
            hoveredStack = stack.copy();
            holdTicks = 0;
        }
        lastTooltipAt = Util.getMillis();

        // Reference: PonderKeybinds.PONDER is the same configurable key used by Ponder's own tooltip flow.
        Component prompt;
        if (holdTicks == 0) {
            prompt = Component.translatable("tooltip.iss_ponder.hold_to_preview", PonderKeybinds.PONDER.message())
                    .withStyle(ChatFormatting.DARK_GRAY);
        } else {
            int bars = 18;
            int filled = Math.min(bars, Math.round((holdTicks / (float) HOLD_TICKS) * bars));
            prompt = Component.literal(ChatFormatting.GRAY + "|".repeat(filled)
                    + ChatFormatting.DARK_GRAY + "|".repeat(bars - filled));
        }
        event.getToolTip().add(Math.min(1, event.getToolTip().size()), prompt);

        if (ClientConfig.SHOW_SCROLL_SPELL_DESCRIPTION.get()) {
            try {
                AbstractSpell spell = spellData.getSpell();
                String descriptionKey = spell.getComponentId() + ".guide";
                if (I18n.exists(descriptionKey)) {
                    event.getToolTip().add(Math.min(2, event.getToolTip().size()),
                            Component.translatable(descriptionKey).withStyle(ChatFormatting.GRAY));
                }
            } catch (RuntimeException exception) {
                ISSPonderMod.LOGGER.debug("Could not render spell scroll description", exception);
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (ClientPreviewController.isPending() || ClientPreviewController.isPreviewOpen()) {
            holdTicks = 0;
            return;
        }

        boolean tooltipActive = !hoveredStack.isEmpty() && Util.getMillis() - lastTooltipAt < 200;
        if (tooltipActive && PonderKeybinds.PONDER.isDown()) {
            holdTicks++;
            if (holdTicks >= HOLD_TICKS) {
                SpellData spellData = getPreviewableSpell(hoveredStack);
                if (spellData != null) {
                    ClientPreviewController.request(spellData.getSpell().getSpellResource(), spellData.getLevel());
                }
                hoveredStack = ItemStack.EMPTY;
                holdTicks = 0;
            }
        } else {
            holdTicks = Math.max(0, holdTicks - 2);
            if (!tooltipActive) {
                hoveredStack = ItemStack.EMPTY;
            }
        }
    }

    @SubscribeEvent
    public static void hideHud(RenderGuiEvent.Pre event) {
        if (ClientPreviewController.isPreviewOpen()) {
            event.setCanceled(true);
        }
    }

    @Nullable
    private static SpellData getPreviewableSpell(ItemStack stack) {
        // Reference: Iron's IScroll and ISpellContainer APIs. Keep validation here and repeat it on the server;
        // add-ons and malformed item NBT must never be allowed to send an unchecked spell into the cast pipeline.
        if (!(stack.getItem() instanceof IScroll) || !ISpellContainer.isSpellContainer(stack)) {
            return null;
        }
        try {
            ISpellContainer container = ISpellContainer.get(stack);
            if (container == null || container.isEmpty() || container.getActiveSpellCount() < 1) {
                return null;
            }
            SpellData data = container.getSpellAtIndex(0);
            AbstractSpell spell = data == null ? null : data.getSpell();
            if (spell == null || spell == SpellRegistry.none() || !spell.isEnabled()
                    || spell.getSpellResource() == null || data.getLevel() < spell.getMinLevel()
                    || data.getLevel() > spell.getMaxLevel()) {
                return null;
            }
            return data;
        } catch (RuntimeException exception) {
            ISSPonderMod.LOGGER.debug("Ignoring malformed spell scroll tooltip data", exception);
            return null;
        }
    }
}
