package org.argoseven.custombarrier;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class EffectBlock extends  BlockWithEntity implements OperatorBlock, Waterloggable {
	private static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 16.0);
	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;


	public EffectBlock(Settings settings) {
		super(settings
				.noCollision()
				.nonOpaque()
				.suffocates((state, world, pos) -> false)
				.blockVision((state, world, pos) -> false)
		);
		this.setDefaultState(this.stateManager.getDefaultState().with(WATERLOGGED, false));
	}



	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		BlockPos blockPos = ctx.getBlockPos();
		World world = ctx.getWorld();
		return this.getDefaultState().with(WATERLOGGED, world.getFluidState(blockPos).getFluid() == Fluids.WATER);
	}

	@Override
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
		if (state.get(WATERLOGGED)) {
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
		return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
	}


	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.INVISIBLE;
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		if (context instanceof EntityShapeContext entityContext && entityContext.getEntity() instanceof PlayerEntity player) {
			if (player.isCreative()) {
				return VoxelShapes.fullCube();
			}
		}
		return VoxelShapes.empty();
	}

	@Override
	public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
		return 1.0F;
	}

	@Override
	public PistonBehavior getPistonBehavior(BlockState state) {
		return PistonBehavior.DESTROY;
	}

	@Override
	public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new EffectBlockEntity(ModdedRegister.EFFECT_BLOCK_ENTITY, pos, state);
	}

	@Override
	public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
		super.onEntityCollision(state, world, pos, entity);
		if (!world.isClient() &&entity instanceof LivingEntity livingEntity){
			BlockEntity be = world.getBlockEntity(pos);
			if (be instanceof EffectBlockEntity effectBlockEntity) {
				if (effectBlockEntity.getEffectId() == null) return;
				String[] tmp = effectBlockEntity.getEffectId().split(" ");

				if (tmp.length == 3 ) {
					StatusEffect statusEffect = parseStringId(tmp[0]);
					if (statusEffect == null) return;

					int duration = parseDurationOrAmplifier(tmp[1], 20);
					int amplifier = parseDurationOrAmplifier(tmp[2], 0);

					livingEntity.addStatusEffect(new StatusEffectInstance(statusEffect, duration, amplifier, false, false), null);
				}
			}
		}
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (player.isCreativeLevelTwoOp() && blockEntity instanceof EffectBlockEntity && (player.getOffHandStack().getItem() != ModdedRegister.EFFECTBLOCK_ITEM)) {
			if (!world.isClient) {
				var buf = new PacketByteBuf(io.netty.buffer.Unpooled.buffer());
				buf.writeBlockPos(pos);
				ServerPlayNetworking.send((ServerPlayerEntity) player, CustomBarrier.OPEN_EFFECTBLOCK_PACKET, buf);
			}
			return ActionResult.success(world.isClient);
		} else {
			return ActionResult.PASS;
		}
	}


	private StatusEffect parseStringId(String effectId) {
		Identifier identifier = Identifier.tryParse(effectId);
		return Registry.STATUS_EFFECT.getOrEmpty(identifier).orElse(null);
	}

	private int parseDurationOrAmplifier(String value, int defaultValue){
		if (value == null) return defaultValue;
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}
}
