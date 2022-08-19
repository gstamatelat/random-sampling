package gr.james.sampling;

import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Tests for {@link RandomSamplingUtils#randomSelection(int, int, Random)}.
 */
public class RandomSamplingUtilsTest {
    /**
     * Check correctness of {@link RandomSamplingUtils#randomSelection(int, int, Random) randomSelection} for n=6 and
     * k=3. The total number of 3-tuples should be 60 with equal probability of inclusion.
     */
    @Test
    public void randomSelectionCorrectness() {
        final int REPS = 200000000;
        final Random rng = new Random();
        final Map<List<Integer>, Long> frequencies = new HashMap<>();
        for (int i = 0; i < REPS; i++) {
            final int[] a = RandomSamplingUtils.randomSelection(5, 3, rng);
            final List<Integer> aList = Arrays.stream(a).boxed().collect(Collectors.toList());
            frequencies.put(aList, frequencies.getOrDefault(aList, 0L) + 1);
        }
        Assert.assertEquals(60, frequencies.size());
        final long firstFrequency = frequencies.values().iterator().next();
        for (long v : frequencies.values()) {
            Assert.assertEquals(1.0, (double) firstFrequency / v, 1.0e-2);
        }
    }
}
