package org.argoseven.custombarrier.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.argoseven.custombarrier.CustomBarrier;
import org.argoseven.custombarrier.EffectBlockEntity;
import org.lwjgl.glfw.GLFW;

public class EffectBlockScreen extends Screen {
    private final BlockEntity blockEntity;
    private TextFieldWidget particleIdField;

    public EffectBlockScreen(EffectBlockEntity blockEntity) {
        super(Text.literal("Custom Barrier Settings"));
        this.blockEntity = blockEntity;
    }

    @Override
    protected void init() {
        super.init();
        if (this.client == null) return;
        EffectBlockEntity be = (EffectBlockEntity) blockEntity;

        this.particleIdField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, this.height / 2 - 40, 200, 20, Text.literal("Particle ID"));
        this.particleIdField.setMaxLength(32767);
        this.particleIdField.setText(be.getEffectId());
        this.addDrawableChild(particleIdField);


        this.addDrawableChild(new ButtonWidget(
                this.width / 2 - 50,
                this.height / 2 + 40,
                100,
                20,
                Text.literal("Save"),
                (button) -> saveSettings()
        ));
    }

    private void saveSettings() {
        if (blockEntity instanceof EffectBlockEntity be) {
            String particleId = particleIdField.getText().trim();
            var buf = PacketByteBufs.create();
            buf.writeBlockPos(be.getPos());
            buf.writeString(particleId);
            ClientPlayNetworking.send(CustomBarrier.SINK_EFFECTBLOCK_PACKET, buf);
        }
        this.close();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        if (client != null) {
            this.client.setScreen(null);
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        drawTextWithShadow(matrices, this.textRenderer, Text.of("Effect"), this.width / 2 - 100,  this.height / 2 - 52, 16777215);
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
