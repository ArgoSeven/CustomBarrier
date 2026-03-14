package org.argoseven.custombarrier;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class CustomBarrier implements ModInitializer {
    public static final String MOD_ID = "custombarrier";
    public static final Identifier OPEN_SCREEN_PACKET = new Identifier(MOD_ID, "open_screen");
    public static final Identifier SINK_BARRIER_PACKET = new Identifier("argotweaks", "sink_barrier");

    public static final Block CUSTOM_BARRIER_BLOCK = new CustomBarrierBlock(FabricBlockSettings.of(Material.METAL).strength(4.0f).luminance(5));
    public static BlockEntityType<CustomBarrierBlockEntity> CUSTOM_BARRIER_BLOCK_ENTITY;
    private static CustomBarrierBlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CustomBarrierBlockEntity(CUSTOM_BARRIER_BLOCK_ENTITY, pos, state);
    }

    @Override
    public void onInitialize() {

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "custom_barrier"), CUSTOM_BARRIER_BLOCK);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "custom_barrier"), new CustomBarrierBlockItem(CUSTOM_BARRIER_BLOCK, new Item.Settings().group(ItemGroup.MISC)));
        CUSTOM_BARRIER_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "custom_barrier"), BlockEntityType.Builder.create(CustomBarrier::createBlockEntity, CUSTOM_BARRIER_BLOCK).build(null));


        ServerPlayNetworking.registerGlobalReceiver(SINK_BARRIER_PACKET, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            String particleId = buf.readString(32767);
            String customString = buf.readString(32767);

            server.execute(() -> {
                World world = player.getWorld();
                if (world.getBlockEntity(pos) instanceof CustomBarrierBlockEntity be) {
                    be.setParticleId(particleId);
                    be.setTags(customString);
                    be.markDirty();
                    BlockState state = world.getBlockState(pos);
                    world.updateListeners(pos, state, state, 3);
                }
            });
        });

    }
}
