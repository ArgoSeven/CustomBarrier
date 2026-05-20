package org.argoseven.custombarrier;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CustomBarrier implements ModInitializer {
    public static final String MOD_ID = "custombarrier";
    public static final Identifier OPEN_SCREEN_PACKET = new Identifier(MOD_ID, "open_screen");
    public static final Identifier SINK_BARRIER_PACKET = new Identifier("argotweaks", "sink_barrier");


    @Override
    public void onInitialize() {
        ModdedRegister.register();

        ServerPlayNetworking.registerGlobalReceiver(SINK_BARRIER_PACKET, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            String particleId = buf.readString(32767);
            String customString = buf.readString(32767);
            BarrierMode mode = buf.readEnumConstant(BarrierMode.class);
            server.execute(() -> {
                World world = player.getWorld();
                if (world.getBlockEntity(pos) instanceof CustomBarrierBlockEntity be) {
                    be.setParticleId(particleId);
                    be.setCheck(customString);
                    be.setMode(mode);
                    be.markDirty();
                    BlockState state = world.getBlockState(pos);
                    world.updateListeners(pos, state, state, 3);
                }
            });
        });

    }
}
