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

import org.quiltmc.chasm.lang.api.ast.IntegerNode;
import org.quiltmc.chasm.lang.api.ast.MapNode;
import org.quiltmc.chasm.lang.api.ast.Node;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ChassemblyUtil {
    private ChassemblyUtil() {}

    public static MapNode transformation(MapNode target, Node apply) {
        var map = new LinkedHashMap<String, Node>();
        map.put("target", target);
        map.put("apply", apply);
        return new MapNode(map);
    }

    public static MapNode transformation(MapNode target, Map<String, Node> sources, Node apply) {
        var map = new LinkedHashMap<String, Node>();
        map.put("target", target);
        map.put("sources", new MapNode(sources));
        map.put("apply", apply);
        return new MapNode(map);
    }

    public static MapNode target(Node node, Node start, Node end) {
        var map = new LinkedHashMap<String, Node>();
        map.put("node", node);
        map.put("start", start);
        map.put("end", end);
        return new MapNode(map);
    }

    public static MapNode target(Node node, int start, int end) {
        return target(node, new IntegerNode(start), new IntegerNode(end));
    }

    public static MapNode target(Node node) {
        var map = new LinkedHashMap<String, Node>();
        map.put("node", node);
        return new MapNode(map);
    }
}
