package org.argoseven.custombarrier.client.render;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;
import org.argoseven.custombarrier.CustomBarrierBlockEntity;

public class CustomBarrierBlockEntityRenderer implements BlockEntityRenderer<CustomBarrierBlockEntity> {

    public CustomBarrierBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public void render(CustomBarrierBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (!entity.isOpaque()) return;

        renderPortal(matrices, vertexConsumers, entity, tickDelta, light, overlay);
    }

    private void renderSides(CustomBarrierBlockEntity entity, Matrix4f matrix, VertexConsumer vertexConsumer) {
        float bottom = 0.0F;
        float top = 1.0F;
        this.renderSide(entity, matrix, vertexConsumer, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, Direction.SOUTH);
        this.renderSide(entity, matrix, vertexConsumer, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, Direction.NORTH);
        this.renderSide(entity, matrix, vertexConsumer, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, Direction.EAST);
        this.renderSide(entity, matrix, vertexConsumer, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, Direction.WEST);
        this.renderSide(entity, matrix, vertexConsumer, 0.0F, 1.0F, bottom, bottom, 0.0F, 0.0F, 1.0F, 1.0F, Direction.DOWN);
        this.renderSide(entity, matrix, vertexConsumer, 0.0F, 1.0F, top, top, 1.0F, 1.0F, 0.0F, 0.0F, Direction.UP);
    }

    private void renderSide(CustomBarrierBlockEntity entity, Matrix4f model, VertexConsumer vertices, float x1, float x2, float y1, float y2, float z1, float z2, float z3, float z4, Direction side) {
            vertices.vertex(model, x1, y1, z1).next();
            vertices.vertex(model, x2, y1, z2).next();
            vertices.vertex(model, x2, y2, z3).next();
            vertices.vertex(model, x1, y2, z4).next();
    }

    private void renderPortal(MatrixStack matrices, VertexConsumerProvider vertexConsumers, CustomBarrierBlockEntity entity, float tickDelta, int light, int overlay) {
        if (entity.getWorld() == null) return;
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        this.renderSides(entity, matrix4f, vertexConsumers.getBuffer(RenderLayer.getEndGateway()));
    }
}
