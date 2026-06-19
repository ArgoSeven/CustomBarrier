package org.argoseven.custombarrier.client.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import org.argoseven.custombarrier.EffectBlockEntity;
import org.argoseven.custombarrier.EffectBlockItem;

public class EffectBlockTileRenderer implements BlockEntityRenderer<EffectBlockEntity> {
    private static final Box RENDER_BOX = new Box(0, 0, 0, 1, 1, 1);
    public EffectBlockTileRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    public void render(EffectBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity localPlayer = client.player;
        if (localPlayer == null || (!(localPlayer.getStackInHand(Hand.MAIN_HAND).getItem() instanceof EffectBlockItem) && !(localPlayer.getStackInHand(Hand.OFF_HAND).getItem() instanceof EffectBlockItem))) return;

        matrices.push();
        VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getLines());
        WorldRenderer.drawBox(matrices, buffer, RENDER_BOX, 0.0F, 1.0F, 0.0F, 0.6F);
        matrices.pop();
    }
}