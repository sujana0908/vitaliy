package com.garentii.sort;

import com.garentii.sort.model.VulnerabilityScript;
import com.garentii.sort.service.ScriptExecutionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ScriptExecutionServiceTest {

    @Autowired
    private ScriptExecutionService scriptExecutionService;

    private List<VulnerabilityScript> sampleScripts;

    private List<VulnerabilityScript> chainDependencyScripts;

    private List<VulnerabilityScript> noDependencyScripts;


    @BeforeEach
    public void setUp() {
        sampleScripts = new ArrayList<>(Arrays.asList(
                new VulnerabilityScript(1, Arrays.asList(2, 3)),
                new VulnerabilityScript(2, new ArrayList<>()),
                new VulnerabilityScript(3, new ArrayList<>()),
                new VulnerabilityScript(4, Collections.singletonList(1)),
                new VulnerabilityScript(5, Collections.singletonList(1))
        ));
        chainDependencyScripts = new ArrayList<>(Arrays.asList(
                new VulnerabilityScript(1, Collections.singletonList(2)),
                new VulnerabilityScript(2, Collections.singletonList(3)),
                new VulnerabilityScript(3, Collections.singletonList(4)),
                new VulnerabilityScript(4, Collections.singletonList(5)),
                new VulnerabilityScript(5, new ArrayList<>())
        ));
        noDependencyScripts = new ArrayList<>(Arrays.asList(
                new VulnerabilityScript(1, new ArrayList<>()),
                new VulnerabilityScript(2, new ArrayList<>()),
                new VulnerabilityScript(3, new ArrayList<>())
        ));
    }

    @Test
    public void testGenerateAllExecutionPlans() {
        // Act
        List<List<Integer>> executionPlans = scriptExecutionService.generateAllExecutionPlans(sampleScripts);

        // Assert
        assertEquals(4, executionPlans.size());

        // Expected orders: [2, 3, 1, 4, 5] or [2, 3, 1, 5, 4] or [3, 2, 1, 4, 5] or [3, 2, 1, 5, 4]
        List<Integer> validPlan1 = Arrays.asList(2, 3, 1, 4, 5);
        List<Integer> validPlan2 = Arrays.asList(2, 3, 1, 5, 4);
        List<Integer> validPlan3 = Arrays.asList(3, 2, 1, 4, 5);
        List<Integer> validPlan4 = Arrays.asList(3, 2, 1, 5, 4);

        assertTrue(executionPlans.contains(validPlan1));
        assertTrue(executionPlans.contains(validPlan2));
        assertTrue(executionPlans.contains(validPlan3));
        assertTrue(executionPlans.contains(validPlan4));
    }

    @Test
    public void testCountAllExecutionPlans() {
      // Act
        int count = scriptExecutionService.countAllExecutionPlans(sampleScripts);
        // Assert
        assertEquals(4, count);
    }


    @Test
    public void testCircularDependency() {
        List<VulnerabilityScript> circularScripts = new ArrayList<>(Arrays.asList(
                new VulnerabilityScript(1, Arrays.asList(2)),
                new VulnerabilityScript(2, Arrays.asList(3)),
                new VulnerabilityScript(3, Arrays.asList(1))  // Circular dependency
        ));

        // Depending on implementation, the service may handle circular dependency by throwing an exception
        // Or return an empty list indicating no valid execution plans
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            scriptExecutionService.generateAllExecutionPlans(circularScripts);
        });

        String expectedMessage = "Circular dependency detected in the scripts.";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testSingleScript() {
        List<VulnerabilityScript> singleScript = Collections.singletonList(
                new VulnerabilityScript(1, Collections.emptyList())
        );
        List<List<Integer>> executionPlans = scriptExecutionService.generateAllExecutionPlans(singleScript);
        // Only one possible plan
        assertEquals(1, executionPlans.size());
        assertEquals(Arrays.asList(1), executionPlans.get(0));
    }

    @Test
    public void testNoDependencies() {
        List<VulnerabilityScript> noDepScripts = new ArrayList<>(Arrays.asList(
                new VulnerabilityScript(1, Collections.emptyList()),
                new VulnerabilityScript(2, Collections.emptyList()),
                new VulnerabilityScript(3, Collections.emptyList())
        ));

        List<List<Integer>> executionPlans = scriptExecutionService.generateAllExecutionPlans(noDepScripts);

        // There should be 6 possible plans: 3! = 6
        assertEquals(6, executionPlans.size());
    }@Test
    public void testGenerateAllExecutionPlans_ChainDependencies() {
        // Act
        List<List<Integer>> executionPlans = scriptExecutionService.generateAllExecutionPlans(chainDependencyScripts);

        // Assert
        assertEquals(1, executionPlans.size());

        // Expected order: [5, 4, 3, 2, 1]
        List<Integer> expectedPlan = Arrays.asList(5, 4, 3, 2, 1);
        assertEquals(expectedPlan, executionPlans.get(0));
    }

    @Test
    public void testCountAllExecutionPlans_ChainDependencies() {
        // Act
        int count = scriptExecutionService.countAllExecutionPlans(chainDependencyScripts);

        // Assert
        assertEquals(1, count);
    }
    @Test
    public void testGenerateAllExecutionPlans_NoDependencies() {
        // Act
        List<List<Integer>> executionPlans = scriptExecutionService.generateAllExecutionPlans(noDependencyScripts);

        // Assert
        assertEquals(6, executionPlans.size()); // 3! = 6 possible plans for 3 independent scripts

        // Expected valid permutations
        List<List<Integer>> expectedPlans = Arrays.asList(
                Arrays.asList(1, 2, 3),
                Arrays.asList(1, 3, 2),
                Arrays.asList(2, 1, 3),
                Arrays.asList(2, 3, 1),
                Arrays.asList(3, 1, 2),
                Arrays.asList(3, 2, 1)
        );

        // Ensure all valid permutations are generated
        assertTrue(executionPlans.containsAll(expectedPlans));
    }

    @Test
    public void testCountAllExecutionPlans_NoDependencies() {
        // Act
        int count = scriptExecutionService.countAllExecutionPlans(noDependencyScripts);

        // Assert
        assertEquals(6, count); // 6 possible plans
    }




}

