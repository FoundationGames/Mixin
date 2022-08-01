/*
 * This file is part of Mixin, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.asm.mixin;

import com.google.common.base.Supplier;
import org.spongepowered.asm.mixin.transformer.MixinApplicatorStandard;
import org.spongepowered.asm.mixin.transformer.TargetClassContext;
import org.spongepowered.asm.service.IGlobalPropertyService;

public final class ChasmUtil {
    private ChasmUtil() {}

    /* --------------------- */

    private static ApplicatorProvider<MixinApplicatorStandard> standardProvider;

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

    /* --------------------- */

    private static Supplier<IGlobalPropertyService> propertySvc;

    public static void providePropertyService(Supplier<IGlobalPropertyService> provider) {
        propertySvc = provider;
    }

    public static void destroyPropertyServiceProvider() {
        propertySvc = null;
    }

    public static IGlobalPropertyService getPropertyService() {
        return propertySvc != null ? propertySvc.get() : null;
    }
}
