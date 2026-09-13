package org.neofabric.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Deterministic topological resolver for required mod dependencies. */
public final class ModDependencyResolver {
    public List<String> resolve(Map<String, List<ModDependency>> dependencies) {
        Map<String, List<ModDependency>> graph = new LinkedHashMap<>(dependencies);
        for (List<ModDependency> required : graph.values()) {
            for (ModDependency dependency : required) {
                if (!graph.containsKey(dependency.id())) {
                    throw new IllegalArgumentException("Missing required mod dependency: " + dependency.id());
                }
            }
        }
        List<String> ordered = new ArrayList<>();
        Set<String> visiting = new HashSet<>();
        Set<String> visited = new HashSet<>();
        for (String id : graph.keySet()) visit(id, graph, visiting, visited, ordered);
        return List.copyOf(ordered);
    }

    private void visit(String id, Map<String, List<ModDependency>> graph, Set<String> visiting,
            Set<String> visited, List<String> ordered) {
        if (visited.contains(id)) return;
        if (!visiting.add(id)) throw new IllegalArgumentException("Mod dependency cycle detected at: " + id);
        for (ModDependency dependency : graph.getOrDefault(id, List.of())) {
            visit(dependency.id(), graph, visiting, visited, ordered);
        }
        visiting.remove(id);
        visited.add(id);
        ordered.add(id);
    }
}
