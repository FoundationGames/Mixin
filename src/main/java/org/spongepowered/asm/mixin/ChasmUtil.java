package org.spongepowered.asm.mixin;

import org.spongepowered.asm.mixin.transformer.MixinApplicatorStandard;
import org.spongepowered.asm.mixin.transformer.TargetClassContext;

public final class ChasmUtil {
    private static ApplicatorProvider<MixinApplicatorStandard> standardProvider;

    private ChasmUtil() {}

    public static void createApplicatorProviders(ApplicatorProvider<MixinApplicatorStandard> provider) {
        standardProvider = provider;
    }

    public static void destroyApplicatorProviders() {
        standardProvider = null;
    }

    public static MixinApplicatorStandard getStandardApplicator(TargetClassContext ctx) {
        return standardProvider != null ? standardProvider.provide(ctx) : null;
    }

    public interface ApplicatorProvider<A extends MixinApplicatorStandard> {
        A provide(TargetClassContext ctx);
    }
}
