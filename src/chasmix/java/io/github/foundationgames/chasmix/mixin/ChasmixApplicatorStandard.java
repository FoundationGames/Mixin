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
    protected final ChasmixTransformerBuilder transformers;

    public ChasmixApplicatorStandard(TargetClassContext context) {
        super(context);

        this.transformers = new ChasmixTransformerBuilder(this.targetClass, context.getClassInfo());
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

    @Override
    protected void afterApply() {
        ChasmixTransformerPool.getCurrentPool().ifPresent(pool -> pool.add(this.transformers.createTransformer()));
    }
}
