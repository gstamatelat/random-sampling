package gr.james.sampling;

import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

public class ChaoSamplingTest {

    private static final Random RANDOM = new Random();

    private static final int STREAM = 10;
    private static final int SAMPLE = 5;
    private static final int REPS = 10000000;

    /**
     * In {@link ChaoSampling} the appearance probability must be proportional to the item weight
     */
    @Test
    public void correctness() {
        final int[] d = new int[STREAM];
        for (int reps = 0; reps < REPS; reps++) {
            final ChaoSampling<Integer> es = new ChaoSampling<>(SAMPLE, RANDOM);
            for (int i = 0; i < STREAM; i++) {
                es.feed(i, i + 1);
            }
            for (int s : es.sample()) {
                d[s]++;
            }
        }
        final double diff = 2.0 * (REPS * SAMPLE) / (STREAM * (STREAM + 1));
        for (int i = 0; i < d.length - 1; i++) {
            Assert.assertEquals("ChaoSamplingTest.correctness",
                    1.0, (d[i + 1] - d[i]) / diff, 1e-2);
        }
    }

}
