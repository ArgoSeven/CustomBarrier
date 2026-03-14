package org.argoseven.custombarrier;

import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CustomBarrierBlockItem extends BlockItem {
    public CustomBarrierBlockItem(Block block, Item.Settings settings) {
        super(block, settings);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        if (stack.hasNbt()) {
            return true;
        }
        return super.hasGlint(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (stack.hasNbt()) {
            String particleId = stack.getNbt().getCompound("BlockEntityTag").getString("particleId");
            String customString = stack.getNbt().getCompound("BlockEntityTag").getString("customString");

            tooltip.add(Text.literal(particleId).styled(style -> style.withColor(Formatting.GRAY).withItalic(true)));
            tooltip.add(Text.literal(customString).styled(style -> style.withColor(Formatting.GREEN).withItalic(true)));
        }
        super.appendTooltip(stack, world, tooltip, context);
    }
}