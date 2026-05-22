package org.argoseven.custombarrier;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

import static org.argoseven.custombarrier.CustomBarrier.MOD_ID;

public class ModdedRegister {

    public static final Block CUSTOM_BARRIER_BLOCK = new CustomBarrierBlock(FabricBlockSettings.of(Material.GLASS).luminance(3).strength(-1.0F, 3600000.0F).dropsNothing());
    public static final Item CUSTOM_BARRIER_ITEM = new CustomBarrierBlockItem(CUSTOM_BARRIER_BLOCK, new Item.Settings().group(ItemGroup.MISC));
    public static BlockEntityType<CustomBarrierBlockEntity> CUSTOM_BARRIER_BLOCK_ENTITY;
    public static final StatusEffect ETHEREAL_EFFECT =  new EtherealEffect();

    private static CustomBarrierBlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CustomBarrierBlockEntity(CUSTOM_BARRIER_BLOCK_ENTITY, pos, state);
    }

    public static void register() {
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "custom_barrier"), CUSTOM_BARRIER_BLOCK);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "custom_barrier"), CUSTOM_BARRIER_ITEM);
        Registry.register(Registry.STATUS_EFFECT, new Identifier(CustomBarrier.MOD_ID, "ethereal_effect"), ETHEREAL_EFFECT);
        CUSTOM_BARRIER_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "custom_barrier"), BlockEntityType.Builder.create(ModdedRegister::createBlockEntity, CUSTOM_BARRIER_BLOCK).build(null));
    }
}
