package org.argoseven.custombarrier;

import com.mojang.datafixers.kinds.IdF;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class CustomBarrierBlock extends BlockWithEntity implements OperatorBlock, Waterloggable {
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    protected CustomBarrierBlock(Settings settings) {
        super(settings
                .noCollision()
                .nonOpaque()
                .suffocates((state, world, pos) -> false)
                .blockVision((state, world, pos) -> false)
        );

        this.setDefaultState(this.stateManager.getDefaultState().with(WATERLOGGED, false));
    }


    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CustomBarrierBlockEntity(CustomBarrier.CUSTOM_BARRIER_BLOCK_ENTITY, pos, state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (context instanceof EntityShapeContext esc) {
            Entity entity = esc.getEntity();

            if (entity instanceof PlayerEntity player) {
                if (world.getBlockEntity(pos) instanceof CustomBarrierBlockEntity be){
                    canPass(player, be.getTags());
                    if (player.hasStatusEffect(StatusEffects.LUCK)) {
                        return VoxelShapes.empty();
                    }
                }
            }
        }

        return VoxelShapes.fullCube();
    }


    /*
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return context.isHolding(CustomBarrier.CUSTOM_BARRIER_BLOCK.asItem()) ? VoxelShapes.fullCube() : VoxelShapes.empty();
    }*/


    @Override
    public boolean isTranslucent(BlockState state, BlockView world, BlockPos pos) {
        return true;
    }


    @Override
    public BlockState getStateForNeighborUpdate(
            BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos
    ) {
        if ((Boolean)state.get(WATERLOGGED)) {
            world.createAndScheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }

        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        // Vuoto: non blocca né interagisce
    }


    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (world.isClient) {
            PlayerEntity player = world.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ(), 3, false);

            if (player != null && player.hasStatusEffect(StatusEffects.LUCK)) {
                return;
            }

            DefaultParticleType particle = ParticleTypes.SCRAPE;
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof CustomBarrierBlockEntity customBE) {
                particle = getParticleById(customBE.getParticleId());
            }

            for (int i = 0; i < 8; i++) {
                double offsetX = random.nextDouble() - 0.5;
                double offsetY = random.nextDouble() - 0.5;
                double offsetZ = random.nextDouble() - 0.5;

                world.addParticle(
                        particle,
                        pos.getX() + 0.5 + offsetX,
                        pos.getY() + 0.5 + offsetY,
                        pos.getZ() + 0.5 + offsetZ,
                        0, 0, 0
                );
            }
        }
    }


    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (player.isCreativeLevelTwoOp() && blockEntity instanceof CustomBarrierBlockEntity) {
            if (!world.isClient) {
                var buf = new PacketByteBuf(io.netty.buffer.Unpooled.buffer());
                buf.writeBlockPos(pos);
                ServerPlayNetworking.send((net.minecraft.server.network.ServerPlayerEntity) player, CustomBarrier.OPEN_SCREEN_PACKET, buf);
            }
            return ActionResult.success(world.isClient);
        } else {
            return ActionResult.PASS;
        }
    }

    public static void canPass(PlayerEntity player, String tag){
        if (tag == null) {return;}
        String delimiter = ";";
        //String[] tagArray = tag.contains(delimiter) ? tag.split(delimiter) : new String[]{tag};
        if (player != null && player.getScoreboardTags() != null && player.getScoreboardTags().contains(tag.toString()) && !player.hasStatusEffect(StatusEffects.LUCK)) {
            player.setStatusEffect(new StatusEffectInstance(StatusEffects.LUCK, 10,1), null);
        }
    }

    private static DefaultParticleType getParticleById(String id) {
        if (id != null) {
            ParticleType<?> particleType = Registry.PARTICLE_TYPE.get(new Identifier(id));
            if (particleType instanceof DefaultParticleType) {
                return (DefaultParticleType) particleType;
            }
        }
        return ParticleTypes.SMOKE;
    }
}
