package org.argoseven.custombarrier;

import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
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
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
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

import java.util.List;
import java.util.Objects;

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
        return new CustomBarrierBlockEntity(ModdedRegister.CUSTOM_BARRIER_BLOCK_ENTITY, pos, state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (context instanceof EntityShapeContext esc) {
            Entity entity = esc.getEntity();

            if (entity instanceof PlayerEntity player) {
                if (world.getBlockEntity(pos) instanceof CustomBarrierBlockEntity be){
                    canPass(player, be);
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
    public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
        return 1.0F;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockPos blockPos = ctx.getBlockPos();
        World world = ctx.getWorld();
        return (BlockState)((BlockState)((BlockState)this.getDefaultState().with(WATERLOGGED, world.getFluidState(blockPos).getFluid() == Fluids.WATER)));
    }
    
    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if ((Boolean)state.get(WATERLOGGED)) {
            world.createAndScheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        return state;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return (Boolean)state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
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
                world.addParticle(particle, pos.getX() + 0.5 + offsetX, pos.getY() + 0.5 + offsetY, pos.getZ() + 0.5 + offsetZ, 0, 0, 0);
            }
        }
    }



    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (player.isCreativeLevelTwoOp() && blockEntity instanceof CustomBarrierBlockEntity && (player.getOffHandStack().getItem() != ModdedRegister.CUSTOM_BARRIER_ITEM)) {
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

    public static void canPass(PlayerEntity player, CustomBarrierBlockEntity customBarrierBlockEntity) {
        String delimiter = ",";
        if (customBarrierBlockEntity == null) return;
        boolean isValidPlayer = player != null && player.getScoreboardTags() != null && !player.hasStatusEffect(StatusEffects.LUCK);
        if (!isValidPlayer) return;

        switch (customBarrierBlockEntity.getMode()) {
            case TAG:
                String tag = customBarrierBlockEntity.getCheck();
                if (tag.contains(delimiter)) {
                    String[] tagsArray = tag.split(delimiter);
                    if (player.getScoreboardTags().containsAll(List.of(tagsArray))){
                        player.setStatusEffect(new StatusEffectInstance(StatusEffects.LUCK, 10,1), null);
                    }
                }else if (player.getScoreboardTags().contains(tag)) {
                    player.setStatusEffect(new StatusEffectInstance(StatusEffects.LUCK, 10,1), null);
                }
                break;
            case PLAYER:
                if (Objects.equals(player.getDisplayName().getString(), customBarrierBlockEntity.getCheck())) {
                    player.setStatusEffect(new StatusEffectInstance(StatusEffects.LUCK, 10,1), null);
                }
                break;
            case PREDICATE:
                if (player instanceof ServerPlayerEntity serverPlayerEntity) {
                    LootCondition condition = serverPlayerEntity.getWorld().getServer().getPredicateManager().get(Identifier.tryParse(customBarrierBlockEntity.getCheck()));
                    if (condition != null) {
                        LootContext lootContext = new LootContext.Builder(serverPlayerEntity.getWorld())
                                .parameter(LootContextParameters.THIS_ENTITY, serverPlayerEntity)
                                .parameter(LootContextParameters.ORIGIN, serverPlayerEntity.getPos())
                                .build(LootContextTypes.COMMAND);
                        boolean match = condition.test(lootContext);
                        if (match) {
                            player.setStatusEffect(new StatusEffectInstance(StatusEffects.LUCK, 10,1), null);
                        }
                    }
                }
                break;
            default:
                break;
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
