package io.github.foundationgames.chasmix.chasm;

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
import org.quiltmc.chasm.lang.api.ast.StringNode;
import org.quiltmc.chasm.lang.internal.render.Renderer;
import org.spongepowered.asm.mixin.transformer.ClassInfo;
import org.spongepowered.asm.util.Annotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class ChasmixTransformerBuilder {
    private final String id;
    private final MapNode transformer = new MapNode(new HashMap<>());
    private final ListNode transformations = new ListNode(new ArrayList<>());

    private final Set<String> declaredUtils = new HashSet<>();

    public ChasmixTransformerBuilder(ClassNode target, ClassInfo mixin) {
        this.id = mixin.getClassName().toLowerCase(Locale.ROOT).replace('.', '_');

        var map = transformer.getEntries();
        map.put("mxn", Node.parse("classes[c -> c = \""+mixin.getName()+"\"][0]"));
        map.put("trg", Node.parse("classes[c -> c = \""+target.name.replace('.', '/')+"\"][0]"));
        map.put("transformations", transformations);
    }

    /**
     * Chassembly port of {@code Annotations.merge(from, to, type, name)}
     */
    protected void declareAnnotationMergers() {
        if (!declaredUtils.contains("ann_mergers")) {
            transformer.getEntries().put("merging_anns", new ListNode(
                    Arrays.stream(Annotations.MERGEABLE_MIXIN_ANNOTATIONS)
                            .map(c -> new StringNode(c.getName().replace('.', '/')))
                            .collect(Collectors.toList())));
            transformer.getEntries().put("ann_merges", new LambdaNode("desc", Node.parse("len(merging_anns[e -> e = desc]) > 0")));
            transformer.getEntries().put("merge_anns",
                    new LambdaNode("args", Node.parse(
                            "args.to[t -> !(ann_merges(t) && len(args.from[f -> f.descriptor = t.descriptor]) > 0)] + " +
                            "args.from[f -> ann_merges(f) && len(args.to[t -> t.descriptor = f.descriptor]) > 0]")));

            declaredUtils.add("ann_mergers");
        }
    }

    public void mergeFieldAnnotations(FieldNode from, FieldNode to) {
        declareAnnotationMergers();

        var srcs = new HashMap<String, Node>();
        srcs.put("a_to", new StringNode("target"));
        srcs.put("a_from", Node.parse("mxn.fields[f -> f.name = \""+from.name+"\"][0].annotations"));
        this.transformations.getEntries().add(ChassemblyUtil.transformation(
                ChassemblyUtil.target(Node.parse("trg.fields[f -> f.name = \""+to.name+"\"][0].annotations"), new IntegerNode(0), Node.parse("len(node) - 1")), srcs,
                new LambdaNode("args", Node.parse("merge_anns({to: args.sources.a_to, from: args.sources.a_from})"))));
    }

    public void addField(FieldNode field) {
        this.transformations.getEntries().add(ChassemblyUtil.transformation(
                ChassemblyUtil.target(Node.parse("trg.fields"), 0, 0),
                new LambdaNode("x", Node.parse("[mxn.fields[f -> f.name = \""+field.name+"\"][0]]"))));
    }

    public void setFieldSignature(FieldNode field, String signature) {
        this.transformations.getEntries().add(ChassemblyUtil.transformation(
                ChassemblyUtil.target(Node.parse("trg.fields[f -> f.name = \""+field.name+"\"][0].signature")),
                new LambdaNode("x", signature != null ? new StringNode(signature) : NullNode.INSTANCE)));
    }

    public void stripFieldAccess(FieldNode field, int access) {
        var srcs = new HashMap<String, Node>();
        srcs.put("access", new StringNode("target"));
        this.transformations.getEntries().add(ChassemblyUtil.transformation(
                ChassemblyUtil.target(Node.parse("trg.fields[f -> f.name = \""+field.name+"\"][0].access")), srcs,
                new LambdaNode("args", Node.parse("args.sources.access & ~" + access))));
    }

    public Transformer createTransformer() {
        return new ChasmLangTransformer(this.id, this.transformer);
    }

    public String render(Renderer renderer) {
        return renderer.render(this.transformer);
    }
}
