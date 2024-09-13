// src/main/java/com/garentii/sort/controller/ScriptExecutionController.java
package com.garentii.sort.controller;

import com.garentii.sort.model.VulnerabilityScript;
import com.garentii.sort.service.ScriptExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/scripts")
public class ScriptExecutionController {

    @Autowired
    private ScriptExecutionService scriptExecutionService;

    @PostMapping("/generate-all-plans")
    public Map<String, Object> generateAllPlans(@RequestBody List<VulnerabilityScript> scripts) {
        List<List<Integer>> allPlans = scriptExecutionService.generateAllExecutionPlans(scripts);
        int count = scriptExecutionService.countAllExecutionPlans(scripts);
        Map<String, Object> response = new HashMap<>();
        response.put("allPlans", allPlans);
        response.put("count", count);
        return response;
    }
}