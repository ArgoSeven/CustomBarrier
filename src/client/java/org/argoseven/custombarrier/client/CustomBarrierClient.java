package org.argoseven.custombarrier.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.util.math.BlockPos;
import org.argoseven.custombarrier.CustomBarrier;
import org.argoseven.custombarrier.CustomBarrierBlockEntity;
import org.argoseven.custombarrier.EffectBlockEntity;
import org.argoseven.custombarrier.ModdedRegister;
import org.argoseven.custombarrier.client.render.CustomBarrierBlockEntityRenderer;
import org.argoseven.custombarrier.client.render.EffectBlockTileRenderer;

import static org.argoseven.custombarrier.CustomBarrier.OPEN_SCREEN_PACKET;

public class CustomBarrierClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockEntityRendererFactories.register(ModdedRegister.CUSTOM_BARRIER_BLOCK_ENTITY, CustomBarrierBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(ModdedRegister.EFFECT_BLOCK_ENTITY, EffectBlockTileRenderer::new);


        ClientPlayNetworking.registerGlobalReceiver(OPEN_SCREEN_PACKET, (client, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            if (client.player != null && client.player.getWorld() != null) {
                var blockEntity = client.player.getWorld().getBlockEntity(pos);
                if (blockEntity instanceof CustomBarrierBlockEntity) {
                    client.execute(() -> {
                        client.setScreen(new CustomBarrierScreen((CustomBarrierBlockEntity) blockEntity));
                    });
                }
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(CustomBarrier.OPEN_EFFECTBLOCK_PACKET, (client, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            if (client.player != null && client.player.getWorld() != null) {
                var blockEntity = client.player.getWorld().getBlockEntity(pos);
                if (blockEntity instanceof EffectBlockEntity) {
                    client.execute(() -> {
                        client.setScreen(new EffectBlockScreen((EffectBlockEntity) blockEntity));
                    });
                }
            }
        });


    }
}
