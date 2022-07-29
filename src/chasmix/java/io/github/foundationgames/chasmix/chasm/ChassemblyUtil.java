package io.github.foundationgames.chasmix.chasm;

import org.quiltmc.chasm.lang.api.ast.IntegerNode;
import org.quiltmc.chasm.lang.api.ast.MapNode;
import org.quiltmc.chasm.lang.api.ast.Node;

import java.util.HashMap;
import java.util.Map;

public final class ChassemblyUtil {
    private ChassemblyUtil() {}

    public static MapNode transformation(MapNode target, Node apply) {
        var map = new HashMap<String, Node>();
        map.put("target", target);
        map.put("apply", apply);
        return new MapNode(map);
    }

    public static MapNode transformation(MapNode target, Map<String, Node> sources, Node apply) {
        var map = new HashMap<String, Node>();
        map.put("target", target);
        map.put("sources", new MapNode(sources));
        map.put("apply", apply);
        return new MapNode(map);
    }

    public static MapNode target(Node node, Node start, Node end) {
        var map = new HashMap<String, Node>();
        map.put("node", node);
        map.put("start", start);
        map.put("end", end);
        return new MapNode(map);
    }

    public static MapNode target(Node node, int start, int end) {
        return target(node, new IntegerNode(start), new IntegerNode(end));
    }

    public static MapNode target(Node node) {
        var map = new HashMap<String, Node>();
        map.put("node", node);
        return new MapNode(map);
    }
}
