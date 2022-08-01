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
package io.github.foundationgames.chasmix;

import io.github.foundationgames.chasmix.chasm.ChasmixTransformerPool;
import io.github.foundationgames.chasmix.mixin.ChasmixBlackboard;
import io.github.foundationgames.chasmix.mixin.ChasmixApplicatorStandard;
import io.github.foundationgames.chasmix.mixin.ChasmixMixinService;
import org.objectweb.asm.tree.ClassNode;
import org.quiltmc.chasm.api.Transformer;
import org.quiltmc.chasm.lang.internal.render.Renderer;
import org.spongepowered.asm.mixin.ChasmUtil;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.mixin.transformer.Config;
import org.spongepowered.asm.mixin.transformer.DefaultExtensions;
import org.spongepowered.asm.mixin.transformer.MixinCoprocessorNestHost;
import org.spongepowered.asm.mixin.transformer.MixinProcessor;
import org.spongepowered.asm.mixin.transformer.SyntheticClassRegistry;
import org.spongepowered.asm.mixin.transformer.ext.Extensions;
import org.spongepowered.asm.service.MixinService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class Chasmix {
    public static Renderer DEBUG_PRINT_RENDERER = null;

    private final MixinProcessor mixin;

    public Chasmix() {
        var clsReg = new SyntheticClassRegistry();
        var exts = new Extensions(clsReg);
        var coprocs = new MixinCoprocessorNestHost();
        DefaultExtensions.create(MixinEnvironment.getDefaultEnvironment(), exts, clsReg, coprocs);

        this.mixin = new MixinProcessor(null, exts, null, coprocs);
    }

    public static void addMixinConfig(String configFile) {
        Mixins.getConfigs().add(Config.create(configFile));
    }

    public Optional<List<Transformer>> generateChasmTransformers(String targetName, ClassNode targetClass) {
        ChasmixTransformerPool.createPool();
        this.mixin.applyMixins(MixinEnvironment.getDefaultEnvironment(), targetName, targetClass);
        var ret = ChasmixTransformerPool.getCurrentPool();
        ChasmixTransformerPool.destroyPool();

        return ret;
    }

    public Optional<List<Transformer>> generateChasmTransformers(String targetName) throws IOException, ClassNotFoundException {
        var node = MixinService.getService().getBytecodeProvider().getClassNode(targetName);

        return generateChasmTransformers(targetName, node);
    }

    public static void provideServices() {
        System.setProperty("mixin.service", ChasmixMixinService.class.getName());
        ChasmUtil.providePropertyService(ChasmixBlackboard::new);
        ChasmUtil.createApplicatorProviders(ChasmixApplicatorStandard::new);
    }
}
