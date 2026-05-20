package org.argoseven.custombarrier.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.math.BlockPos;
import org.argoseven.custombarrier.BarrierMode;
import org.argoseven.custombarrier.CustomBarrierBlockEntity;

import static org.argoseven.custombarrier.CustomBarrier.OPEN_SCREEN_PACKET;

public class CustomBarrierClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
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
    }
}
