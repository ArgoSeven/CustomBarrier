package org.argoseven.custombarrier.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.argoseven.custombarrier.CustomBarrierBlockEntity;
import org.lwjgl.glfw.GLFW;

public class CustomBarrierScreen extends Screen {
    private static final Identifier SINK_BARRIER_PACKET = new Identifier("argotweaks", "sink_barrier");
    private final BlockEntity blockEntity;
    private TextFieldWidget particleIdField;
    private TextFieldWidget customStringField;

    public CustomBarrierScreen(BlockEntity blockEntity) {
        super(Text.literal("Custom Barrier Settings"));
        this.blockEntity = blockEntity;
    }

    @Override
    protected void init() {
        super.init();
        if (this.client == null) return;
       CustomBarrierBlockEntity be = (CustomBarrierBlockEntity) blockEntity;
        
        this.particleIdField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, this.height / 2 - 40, 200, 20, Text.literal("Particle ID"));
        this.particleIdField.setMaxLength(32767);
        this.particleIdField.setText(be.getParticleId());
        this.addDrawableChild(particleIdField);
        
        this.customStringField = new TextFieldWidget(
            this.textRenderer,
            this.width / 2 - 100,
            this.height / 2,
            200,
            20,
            Text.literal("Custom String")
        );
        this.customStringField.setMaxLength(32767);
        this.customStringField.setText(be.getTags());
        this.addDrawableChild(customStringField);

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
        if (blockEntity instanceof CustomBarrierBlockEntity be) {
            String particleId = particleIdField.getText();
            String customString = customStringField.getText();
            var buf = PacketByteBufs.create();
            buf.writeBlockPos(be.getPos());
            buf.writeString(particleId);
            buf.writeString(customString);
            ClientPlayNetworking.send(SINK_BARRIER_PACKET, buf);
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
        drawTextWithShadow(matrices, this.textRenderer, Text.of("Particle"), this.width / 2 - 100,  this.height / 2 - 52, 16777215);
        drawTextWithShadow(matrices, this.textRenderer, Text.of("Tag"), this.width / 2 - 100,  this.height / 2 - 12, 16777215);
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
