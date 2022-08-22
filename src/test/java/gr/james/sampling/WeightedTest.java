package gr.james.sampling;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Tests for the {@link Weighted} class.
 */
public class WeightedTest {
    /**
     * In this test, we sort a list of Weighted objects, all of which have the same weight.
     * <p>
     * After the sorting, we make sure that each element is strictly lower than its successor. With this, we test for
     * two things: 1. There exist no duplicate elements, 2. The compareTo method is deterministic and consistent.
     */
    @Test
    public void compareToConsistency() {
        // Number of items in the list
        final int COUNT = 2000000;
        // Create the list
        final List<Weighted<Integer>> weightedList = new ArrayList<>();
        for (int i = 0; i < COUNT; i++) {
            weightedList.add(new Weighted<>(0, 0));
        }
        // Sort the list
        weightedList.sort(null);
        // Test the list
        for (int i = 0; i < COUNT - 1; i++) {
            final Weighted<Integer> x = weightedList.get(i);
            final Weighted<Integer> y = weightedList.get(i + 1);
            Assert.assertTrue(x.compareTo(y) < 0);
            Assert.assertNotSame(x, y);
            Assert.assertNotEquals(x, y);
        }
    }

    /**
     * In a list of Weighted objects with same weight, sorting them should have the same effect as shuffling the
     * original objects.
     */
    @Test
    public void compareToShuffling() {
        // Number of items in the list
        final int COUNT = 5;
        final int PERMUTATIONS = 120;
        final int REPS = 24000000;
        // Frequency map
        final Map<List<Integer>, Integer> frequencies = new HashMap<>();
        // Do the experiments
        for (int i = 0; i < REPS; i++) {
            // Create the list
            final List<Weighted<Integer>> weightedList = new ArrayList<>();
            for (int k = 0; k < COUNT; k++) {
                weightedList.add(new Weighted<>(k, 0));
            }
            // Sort the list
            weightedList.sort(null);
            // Add list of objects to frequencies
            final List<Integer> ll = weightedList.stream().map(x -> x.object).collect(Collectors.toList());
            frequencies.merge(ll, 1, Integer::sum);
        }
        // Tests
        Assert.assertEquals(PERMUTATIONS, frequencies.size());
        for (int v : frequencies.values()) {
            Assert.assertEquals(1.0, 1.0 * v * PERMUTATIONS / REPS, 1e-2);
        }
    }
}
