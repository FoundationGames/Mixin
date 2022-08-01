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
package io.github.foundationgames.chasmix.chasm;

import io.github.foundationgames.chasmix.Chasmix;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.quiltmc.chasm.api.Transformer;
import org.quiltmc.chasm.internal.transformer.ChasmLangTransformer;
import org.quiltmc.chasm.lang.api.ast.IntegerNode;
import org.quiltmc.chasm.lang.api.ast.LambdaNode;
import org.quiltmc.chasm.lang.api.ast.ListNode;
import org.quiltmc.chasm.lang.api.ast.MapNode;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.ast.NullNode;
import org.quiltmc.chasm.lang.api.ast.ReferenceNode;
import org.quiltmc.chasm.lang.api.ast.StringNode;
import org.quiltmc.chasm.lang.internal.render.Renderer;
import org.spongepowered.asm.mixin.transformer.ClassInfo;
import org.spongepowered.asm.mixin.transformer.MixinApplicatorStandard;
import org.spongepowered.asm.util.Annotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ChasmixTransformerBuilder {
    private final String id;
    private final Map<String, Node> transformer = new LinkedHashMap<>();
    private final ListNode transformations = new ListNode(new ArrayList<>());

    private final Set<String> declaredUtils = new HashSet<>();

    public ChasmixTransformerBuilder(MixinApplicatorStandard.ApplicatorPass pass, ClassInfo target, ClassInfo mixin) {
        this.id = (mixin.getClassName().replace('.', '_') + "-" + pass.name()).toLowerCase(Locale.ROOT);

        transformer.put("mxn", Node.parse("classes[c -> c.name = \""+mixin.getName()+"\"][0]"));
        transformer.put("trg", Node.parse("classes[c -> c.name = \""+ target.getName()+"\"][0]"));
        // transformations are added at the very end
    }

    /**
     * Chassembly port of {@code Annotations.merge(from, to, type, name)}
     */
    protected void declareAnnotationMergers() {
        if (!declaredUtils.contains("ann_mergers")) {
            transformer.put("merging_anns", new ListNode(
                    Arrays.stream(Annotations.MERGEABLE_MIXIN_ANNOTATIONS)
                            .map(c -> new StringNode(c.getName().replace('.', '/')))
                            .collect(Collectors.toList())));
            transformer.put("ann_merges", new LambdaNode("ann", Node.parse("len(merging_anns[e -> e = ann.descriptor]) > 0")));
            transformer.put("merge_anns",
                    new LambdaNode("args", Node.parse(
                            "args.to[t -> !(ann_merges(t) && len(args.from[f -> f.descriptor = t.descriptor]) > 0)] + " +
                            "args.from[f -> ann_merges(f) && len(args.to[t -> t.descriptor = f.descriptor]) > 0]")));

            declaredUtils.add("ann_mergers");
        }
    }

    public void mergeFieldAnnotations(FieldNode from, FieldNode to) {
        declareAnnotationMergers();

        var srcs = new LinkedHashMap<String, Node>();
        srcs.put("anns", Node.parse("{node: target.node}"));
        this.transformations.getEntries().add(ChassemblyUtil.transformation(
                ChassemblyUtil.target(Node.parse("trg.fields[f -> f.name = \""+to.name+"\"][0].annotations"), new IntegerNode(0), Node.parse("2 * len(node) - 1")), srcs,
                new LambdaNode("args", Node.parse("merge_anns({to: args.sources.anns, from: mxn.fields[f -> f.name = \""+from.name+"\"][0].annotations})"))));
    }

    public void addField(FieldNode field) {
        this.transformations.getEntries().add(ChassemblyUtil.transformation(
                ChassemblyUtil.target(Node.parse("trg.fields"), 0, 0),
                new LambdaNode("x", Node.parse("[mxn.fields[f -> f.name = \""+field.name+"\"][0]]"))));
    }

    public void setFieldSignature(FieldNode field, String signature) {
        this.transformations.getEntries().add(ChassemblyUtil.transformation(
                ChassemblyUtil.target(Node.parse("trg.fields[f -> f.name = \""+field.name+"\"][0].signature")),
                new LambdaNode("x", (Node)(Object)(signature != null ? new StringNode(signature) : NullNode.INSTANCE))));
    }

    public void stripFieldAccess(FieldNode field, int access) {
        var srcs = new LinkedHashMap<String, Node>();
        srcs.put("access", Node.parse("{node: target.node}"));
        this.transformations.getEntries().add(ChassemblyUtil.transformation(
                ChassemblyUtil.target(Node.parse("trg.fields[f -> f.name = \""+field.name+"\"][0].access")), srcs,
                new LambdaNode("args", Node.parse("args.sources.access & " + ~access))));
    }

    public Transformer createTransformer() {
        this.transformer.put("transformations", this.transformations);
        var transformerNode = new MapNode(this.transformer);

        if (Chasmix.DEBUG_PRINT_RENDERER != null) {
            System.out.println(Chasmix.DEBUG_PRINT_RENDERER.render(transformerNode));
        }

        return new ChasmLangTransformer(this.id, transformerNode);
    }
}
