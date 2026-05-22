package org.argoseven.custombarrier.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.argoseven.custombarrier.BarrierMode;
import org.argoseven.custombarrier.CustomBarrierBlockEntity;
import org.lwjgl.glfw.GLFW;

public class CustomBarrierScreen extends Screen {
    private static final Identifier SINK_BARRIER_PACKET = new Identifier("argotweaks", "sink_barrier");
    private final BlockEntity blockEntity;
    private TextFieldWidget particleIdField;
    private TextFieldWidget customStringField;
    private BarrierMode currentMode;
    private CheckboxWidget opaqueCheckbox;


    public CustomBarrierScreen(CustomBarrierBlockEntity blockEntity) {
        super(Text.literal("Custom Barrier Settings"));
        this.blockEntity = blockEntity;
        this.currentMode = blockEntity.getMode();
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

        this.opaqueCheckbox = new CheckboxWidget(
                this.width / 2 + 105,
                this.height / 2 - 40,
                20,20,
                Text.of("Opaque"),
                be.isOpaque()
        );
        this.addDrawableChild(this.opaqueCheckbox);
        this.customStringField = new TextFieldWidget(
            this.textRenderer,
            this.width / 2 - 100,
            this.height / 2,
            200,
            20,
            Text.literal("Custom String")
        );
        this.customStringField.setMaxLength(32767);
        this.customStringField.setText(be.getCheck());
        this.addDrawableChild(customStringField);

        CyclingButtonWidget<BarrierMode> modeButton = CyclingButtonWidget.builder(
                        (BarrierMode mode) -> Text.literal(mode.name())
                )
                .values(BarrierMode.values())
                .initially(this.currentMode)
                .omitKeyText()
                .build(
                        this.width / 2 + 105,
                        this.height / 2,
                        60,
                        20,
                        Text.empty(),
                        (button, value) -> {
                            this.currentMode = value;
                        }
                );
        this.addDrawableChild(modeButton);

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
            boolean opaque = opaqueCheckbox.isChecked();
            var buf = PacketByteBufs.create();
            buf.writeBlockPos(be.getPos());
            buf.writeString(particleId);
            buf.writeString(customString);
            buf.writeEnumConstant(currentMode);
            buf.writeBoolean(opaque);
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
        drawTextWithShadow(matrices, this.textRenderer, Text.of("Check"), this.width / 2 - 100,  this.height / 2 - 12, 16777215);
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
