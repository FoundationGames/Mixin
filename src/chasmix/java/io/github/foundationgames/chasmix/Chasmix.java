package io.github.foundationgames.chasmix;

import io.github.foundationgames.chasmix.mixin.ChasmixApplicatorStandard;
import org.spongepowered.asm.mixin.ChasmUtil;

public final class Chasmix {
    public static final Chasmix INSTANCE = new Chasmix();

    private Chasmix() {}

    public void setupApplicatorProviders() {
        ChasmUtil.createApplicatorProviders(ChasmixApplicatorStandard::new);
    }
}
