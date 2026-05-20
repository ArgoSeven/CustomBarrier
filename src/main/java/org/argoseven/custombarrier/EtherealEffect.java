package org.argoseven.custombarrier;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class EtherealEffect extends StatusEffect {
    public EtherealEffect() {
        super(StatusEffectCategory.BENEFICIAL, 0x98D982);
    }

    @Override
    public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        super.onRemoved(entity, attributes, amplifier);
    }
}
