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
package io.github.foundationgames.chasmix.mixin;

import io.github.foundationgames.chasmix.chasm.ChasmixTransformerBuilder;
import io.github.foundationgames.chasmix.chasm.ChasmixTransformerPool;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;
import org.objectweb.asm.tree.FieldNode;
import org.spongepowered.asm.mixin.transformer.ClassInfo;
import org.spongepowered.asm.mixin.transformer.MixinApplicatorStandard;
import org.spongepowered.asm.mixin.transformer.MixinTargetContext;
import org.spongepowered.asm.mixin.transformer.TargetClassContext;

import java.util.Map;

/**
 * Reimplements mixin application in Chassembly
 */
public class ChasmixApplicatorStandard extends MixinApplicatorStandard {
    protected ChasmixTransformerBuilder transformers;

    public ChasmixApplicatorStandard(TargetClassContext context) {
        super(context);
    }

    @Override
    protected void applyMixin(MixinTargetContext mixin, MixinApplicatorStandard.ApplicatorPass pass) {
        this.transformers = new ChasmixTransformerBuilder(pass, mixin.getTargetClassInfo(), mixin.getClassInfo());
        super.applyMixin(mixin, pass);
        ChasmixTransformerPool.getCurrentPool().ifPresent(pool -> pool.add(this.transformers.createTransformer()));
    }

    @Override
    protected void mergeShadowFields(MixinTargetContext mixin) {
        for (Map.Entry<FieldNode, ClassInfo.Field> entry : mixin.getShadowFields()) {
            FieldNode shadow = entry.getKey();
            FieldNode target = this.findTargetField(shadow);
            if (target != null) {
                transformers.mergeFieldAnnotations(shadow, target);

                // Strip the FINAL flag from @Mutable fields
                if (entry.getValue().isDecoratedMutable()) {
                    transformers.stripFieldAccess(target, Opcodes.ACC_FINAL);
                }
            }
        }
    }

    @Override
    protected void mergeNewFields(MixinTargetContext mixin) {
        for (FieldNode field : mixin.getFields()) {
            FieldNode target = this.findTargetField(field);
            if (target == null) {
                // This is just a local field, so add it
                transformers.addField(field);
                mixin.fieldMerged(field);

                if (field.signature != null) {
                    if (this.mergeSignatures) {
                        SignatureVisitor sv = mixin.getSignature().getRemapper();
                        new SignatureReader(field.signature).accept(sv);
                        transformers.setFieldSignature(field, sv.toString());
                    } else {
                        transformers.setFieldSignature(field, null);
                    }
                }
            }
        }
    }
}
