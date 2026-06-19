package org.argoseven.custombarrier;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
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
                .sounds(BlockSoundGroup.AMETHYST_BLOCK)
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
                    if (!player.hasStatusEffect(ModdedRegister.ETHEREAL_EFFECT) && canPass(player, be)){
                        if (!player.getWorld().isClient){
                            player.addStatusEffect(new StatusEffectInstance(ModdedRegister.ETHEREAL_EFFECT, 10, 0));
                        }
                    }
                    if (player.hasStatusEffect(ModdedRegister.ETHEREAL_EFFECT)) {
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
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        // Vuoto: non blocca né interagisce
    }

    
    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (world.isClient) {
            ParticleEffect particle = ParticleTypes.SMOKE;
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof CustomBarrierBlockEntity customBE) {
                if (customBE.isOpaque()){
                    return;
                }
                particle = getParticleById(customBE.getParticleId());
                if (particle == null) {
                    return;
                }
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

    public static boolean canPass(PlayerEntity player, CustomBarrierBlockEntity customBarrierBlockEntity) {
        String delimiter = ",";
        if (customBarrierBlockEntity == null) return false;
        boolean isValidPlayer = player != null && player.getScoreboardTags() != null;
        if (!isValidPlayer || customBarrierBlockEntity.getCheck() == null || customBarrierBlockEntity.getCheck().isEmpty()) return false;
        String check = customBarrierBlockEntity.getCheck();

        switch (customBarrierBlockEntity.getMode()) {
            case TAG:
                if (check.contains(delimiter)) {
                    String[] tagsArray = check.split(delimiter);
                    if (player.getScoreboardTags().containsAll(List.of(tagsArray))) {
                        return true;
                    }
                } else if (player.getScoreboardTags().contains(check)) {
                    return true;
                }
                break;

            case PLAYER:
                if (check.contains(delimiter)) {
                    String[] players = check.split(delimiter);
                    return  List.of(players).contains(player.getDisplayName().getString());
                }else if (Objects.equals(player.getDisplayName().getString(), customBarrierBlockEntity.getCheck())) {
                    return true;
                }
                break;
            case MAINHAND:
                if (Registry.ITEM.getId(player.getMainHandStack().getItem()).toString().equals(check)) {
                    return true;
                }
                break;
            case PREDICATE:
                if (player instanceof ServerPlayerEntity serverPlayerEntity) {
                    LootCondition condition = serverPlayerEntity.getWorld().getServer().getPredicateManager().get(Identifier.tryParse(check));
                    if (condition != null) {
                        LootContext lootContext = new LootContext.Builder(serverPlayerEntity.getWorld())
                                .parameter(LootContextParameters.THIS_ENTITY, serverPlayerEntity)
                                .parameter(LootContextParameters.ORIGIN, serverPlayerEntity.getPos())
                                .build(LootContextTypes.COMMAND);
                        if (condition.test(lootContext)) {
                            return true;
                        }
                    }
                }
                break;
            default:
                break;
        }
        return false;
    }



    private static ParticleEffect getParticleById(String id) {
        if (id == null || id.isEmpty()) return null ;
        try {
            StringReader reader = new StringReader(id);
            Identifier identifier = Identifier.fromCommandInput(reader);
            ParticleType<?> type = Registry.PARTICLE_TYPE.get(identifier);
            if (type == null) return ParticleTypes.SMOKE;
            return readParticleParameters(type, reader);
        } catch (Exception e) {
            return ParticleTypes.SMOKE;
        }
    }

    private static <T extends ParticleEffect> T readParticleParameters(ParticleType<T> type, StringReader reader) throws CommandSyntaxException {
        return type.getParametersFactory().read(type, reader);
    }
}
