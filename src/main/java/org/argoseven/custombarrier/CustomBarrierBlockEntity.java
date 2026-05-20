package org.argoseven.custombarrier;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class CustomBarrierBlockEntity extends BlockEntity {

    private String particleId = "minecraft:scrape";
    private String check = "";
    private BarrierMode mode = BarrierMode.TAG;

    public CustomBarrierBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public String getParticleId() {
        return particleId;
    }

    public void setParticleId(String id) {
        this.particleId = id;
        markDirty();
        if (world != null) {
            BlockState state = world.getBlockState(pos);
            world.updateListeners(pos, state, state, 3);
        }
    }

    public String getCheck() {
        return check;
    }

    public void setCheck(String str) {
        this.check = str != null ? str : "";
        markDirty();
        if (world != null) {
            BlockState state = world.getBlockState(pos);
            world.updateListeners(pos, state, state, 3);
        }
    }

    public void setMode(BarrierMode mode) {
        this.mode = mode != null ? mode : BarrierMode.TAG;
        markDirty();
        if (world != null) {
            BlockState state = world.getBlockState(pos);
            world.updateListeners(pos, state, state, 3);
        }
    }

    public BarrierMode getMode() {return mode;}

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putString("particleId", particleId);
        nbt.putString("customString", check);
        nbt.putString("mode", mode.name());
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.particleId = nbt.getString("particleId");
        this.check = nbt.getString("customString");
        if (nbt.contains("mode")) {
            this.mode = BarrierMode.valueOf(nbt.getString("mode"));
        }
    }

    @Nullable
    @Override
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound nbt = new NbtCompound();
        writeNbt(nbt);
        return nbt;
    }
}
