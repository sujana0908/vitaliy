// src/main/java/com/garentii/sort/service/ScriptExecutionService.java
package com.garentii.sort.service;

import com.garentii.sort.model.VulnerabilityScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScriptExecutionService {
    public List<List<Integer>> generateAllExecutionPlans(List<VulnerabilityScript> scripts) {
        Map<Integer, List<Integer>> graph = new HashMap<>();
        Map<Integer, Integer> inDegree = new HashMap<>();

        // Step 1: Build graph and calculate in-degrees
        scripts.forEach(script -> {
            int scriptId = script.getScriptId();
            inDegree.putIfAbsent(scriptId, 0);
            graph.putIfAbsent(scriptId, new ArrayList<>());
            script.getDependencies().forEach(dep -> {
                graph.putIfAbsent(dep, new ArrayList<>());
                graph.get(dep).add(scriptId);
                inDegree.put(scriptId, inDegree.getOrDefault(scriptId, 0) + 1);
            });
        });

        // Find scripts with zero in-degree (no dependencies)
        Queue<Integer> queue = inDegree.entrySet().stream()
                .filter(entry -> entry.getValue() == 0)
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(LinkedList::new));

        // Step 3: Perform topological sort (execution plan)
        List<List<Integer>> allExecutionOrders = new ArrayList<>();
        backtrack(queue, new ArrayList<>(), allExecutionOrders, graph, inDegree, scripts.size());

        if (allExecutionOrders.isEmpty()) {
            throw new IllegalArgumentException("Circular dependency detected in the scripts.");
        }

        return allExecutionOrders;
    }

    private void backtrack(Queue<Integer> queue, List<Integer> currentOrder, List<List<Integer>> allOrders,
                           Map<Integer, List<Integer>> graph, Map<Integer, Integer> inDegree, int totalScripts) {
        if (currentOrder.size() == totalScripts) {
            allOrders.add(new ArrayList<>(currentOrder));
            return;
        }

        int size = queue.size();
        for (int i = 0; i < size; i++) {
            int currentScript = queue.poll();
            currentOrder.add(currentScript);

            List<Integer> nextQueue = new LinkedList<>(queue);
            for (int neighbor : graph.getOrDefault(currentScript, new ArrayList<>())) {
                inDegree.put(neighbor, inDegree.get(neighbor) - 1);
                if (inDegree.get(neighbor) == 0) {
                    nextQueue.add(neighbor);
                }
            }

            backtrack(new LinkedList<>(nextQueue), currentOrder, allOrders, graph, inDegree, totalScripts);

            currentOrder.remove(currentOrder.size() - 1);
            for (int neighbor : graph.getOrDefault(currentScript, new ArrayList<>())) {
                inDegree.put(neighbor, inDegree.get(neighbor) + 1);
            }
            queue.add(currentScript);
        }
    }

    public int countAllExecutionPlans(List<VulnerabilityScript> scripts) {
        List<List<Integer>> allPlans = generateAllExecutionPlans(scripts);
        return allPlans.isEmpty() || allPlans.get(0).isEmpty() ? 0 : allPlans.size();
    }
}