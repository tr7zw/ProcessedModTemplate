package dev.tr7zw.util;

import java.util.List;
import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.PoseStack;

import lombok.AllArgsConstructor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
// spotless:off 
//#if MC < 12102
//$$ import com.mojang.blaze3d.vertex.Tesselator;
//#endif
//#if MC >= 12000
import net.minecraft.client.gui.GuiGraphics;
//#else
//$$ import net.minecraft.client.gui.screens.Screen;
//$$ import com.mojang.blaze3d.systems.RenderSystem;
//$$ import net.minecraft.client.renderer.GameRenderer;
//$$ import net.minecraft.client.gui.GuiComponent;
//#endif
// spotless:on
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

@AllArgsConstructor
public class RenderContext {

    @SuppressWarnings("unused")
    private final static Minecraft minecraft = Minecraft.getInstance();

    //#if MC >= 12000
    private final GuiGraphics guiGraphics;
    //#else
    //$$ private final Screen screen;
    //$$ private final PoseStack pose;
    //#endif

    public PoseStack pose() {
        //#if MC >= 12000
        return guiGraphics.pose();
        //#else
        //$$ return pose;
        //#endif
    }

    public void drawSpecial(Consumer<MultiBufferSource> consumer) {
        //#if MC >= 12102
        guiGraphics.drawSpecial(consumer);
        //#elseif MC >= 12100
        //$$ consumer.accept(guiGraphics.bufferSource());
        //$$ guiGraphics.bufferSource().endBatch();
        //#else
        //$$ net.minecraft.client.renderer.MultiBufferSource.BufferSource bs = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        //$$ consumer.accept(bs);
        //$$ bs.endBatch();
        //#endif
    }

    public void blit(ResourceLocation atlasLocation, int x, int y, float uOffset, float vOffset, int width, int height,
            int textureWidth, int textureHeight) {
        //#if MC >= 12102
        guiGraphics.blit(t -> RenderType.guiTextured(t), atlasLocation, x, y, uOffset, vOffset, width, height, textureWidth, textureHeight);
        //#elseif MC >= 12000
        //$$ guiGraphics.blit(atlasLocation, x, y, y, uOffset, vOffset, width, height, textureWidth, textureHeight);
        //#elseif MC > 11700
        //$$ RenderSystem.setShader(GameRenderer::getPositionTexShader);
        //$$ RenderSystem.setShaderTexture(0, atlasLocation);
        //$$ screen.blit(pose, x, y, 0, uOffset, vOffset, width, height, textureWidth, textureHeight);
        //#else
        //$$ minecraft.getTextureManager().bind(atlasLocation);
        //$$ screen.blit(pose, x, y, 0, uOffset, vOffset, width, height, textureWidth, textureHeight);
        //#endif
    }

    public void blit(ResourceLocation atlasLocation, int x, int y, int uOffset, int vOffset, int uWidth, int vHeight) {
        //#if MC >= 12102
        guiGraphics.blit(t -> RenderType.guiTextured(t), atlasLocation, x, y, (float)uOffset, (float)vOffset, uWidth, vHeight, 64, 64);
        //#elseif MC >= 12000
        //$$ guiGraphics.blit(atlasLocation, x, y, uOffset, vOffset, uWidth, vHeight);
        //#elseif MC > 11700
        //$$ RenderSystem.setShader(GameRenderer::getPositionTexShader);
        //$$ RenderSystem.setShaderTexture(0, atlasLocation);
        //$$ screen.blit(pose, x, y, uOffset, vOffset, uWidth, vHeight);
        //#else
        //$$ minecraft.getTextureManager().bind(atlasLocation);
        //$$ screen.blit(pose, x, y, uOffset, vOffset, uWidth, vHeight);
        //#endif
    }

    public void blit(ResourceLocation atlasLocation, int x, int y, int blitOffset, float uOffset, float vOffset,
            int uWidth, int vHeight, int textureWidth, int textureHeight) {
        //#if MC >= 12102
        //TODO blitOffset
        guiGraphics.blit(t -> RenderType.guiTextured(t), atlasLocation, x, y, uOffset, vOffset, uWidth, vHeight, textureWidth,
                textureHeight);
        //#elseif MC >= 12000
        //$$ guiGraphics.blit(atlasLocation, x, y, blitOffset, uOffset, vOffset, uWidth, vHeight, textureWidth,
        //$$        textureHeight);
        //#elseif MC > 11700
        //$$ RenderSystem.setShader(GameRenderer::getPositionTexShader);
        //$$ RenderSystem.setShaderTexture(0, atlasLocation);
        //$$ GuiComponent.blit(pose, x, y, blitOffset, uOffset, vOffset, uWidth, vHeight, textureWidth, textureHeight);
        //#else
        //$$ minecraft.getTextureManager().bind(atlasLocation);
        //$$ GuiComponent.blit(pose, x, y, blitOffset, uOffset, vOffset, uWidth, vHeight, textureWidth, textureHeight);
        //#endif
    }

    public void blitSprite(ResourceLocation hotbarOffhandLeftSprite, int x, int y, int width, int height) {
        //#if MC >= 12102
        guiGraphics.blitSprite(t -> RenderType.guiTextured(t), hotbarOffhandLeftSprite, x, y, width, height);
        //#elseif MC >= 12002
        //$$ guiGraphics.blitSprite(hotbarOffhandLeftSprite, x, y, width, height);
        //#else
        //$$ throw new java.lang.RuntimeException();
        //#endif
    }

    public void renderTooltip(Font font, List<FormattedCharSequence> split, int x, int y) {
        //#if MC >= 12000
        guiGraphics.renderTooltip(font, split, x, y);
        //#else
        //$$ screen.renderTooltip(pose, split, x, y);
        //#endif
    }

    public void renderTooltip(Font font, MutableComponent translatable, int x, int y) {
        //#if MC >= 12000
        guiGraphics.renderTooltip(font, translatable, x, y);
        //#else
        //$$ screen.renderTooltip(pose, translatable, x, y);
        //#endif
    }

    public void fill(int minX, int minY, int maxX, int maxY, int color) {
        //#if MC >= 12000
        guiGraphics.fill(minX, minY, maxX, maxY, color);
        //#else
        //$$ GuiComponent.fill(pose, minX, minY, maxX, maxY, color);
        //#endif
    }

    public void renderFakeItem(ItemStack itemStack, int x, int y) {
        //#if MC >= 12000
        guiGraphics.renderFakeItem(itemStack, x, y);
        //#elseif MC > 11903
        //$$ minecraft.getItemRenderer().renderAndDecorateFakeItem(pose, itemStack, x, y);
        //#else
        //$$ minecraft.getItemRenderer().renderAndDecorateFakeItem(itemStack, x, y);
        //#endif
    }

    public void renderItemDecorations(Font font, ItemStack itemStack, int x, int y) {
        //#if MC >= 12000
        guiGraphics.renderItemDecorations(font, itemStack, x, y);
        //#elseif MC > 11903
        //$$ minecraft.getItemRenderer().renderGuiItemDecorations(pose, font, itemStack, x, y);
        //#else
        //$$ minecraft.getItemRenderer().renderGuiItemDecorations(font, itemStack, x, y);
        //#endif
    }

    public void renderItem(Player player, ItemStack itemStack, int x, int y, int seed) {
        //#if MC >= 12000
        guiGraphics.renderItem(player, itemStack, x, y, seed);
        //#elseif MC > 11903
        //$$ minecraft.getItemRenderer().renderAndDecorateItem(pose, player, itemStack, x, y, seed);
        //#elseif MC > 11700
        //$$ minecraft.getItemRenderer().renderAndDecorateItem(player, itemStack, x, y, seed);
        //#else
        //$$ minecraft.getItemRenderer().renderAndDecorateItem(player, itemStack, x, y);
        //#endif
    }

    public void drawString(Font font, Component name, int x, int y, int color) {
        //#if MC >= 12000
        guiGraphics.drawString(font, name, x, y, color);
        //#else
        //$$ screen.drawString(pose, font, name, x, y, color);
        //#endif
    }

    public void drawCenteredString(Font font, Component name, int x, int y, int color) {
        //#if MC >= 12000
        guiGraphics.drawCenteredString(font, name, x, y, color);
        //#else
        //$$ screen.drawCenteredString(pose, font, name, x, y, color);
        //#endif
    }

}
