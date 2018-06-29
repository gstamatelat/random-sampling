import gr.james.sampling.RandomSampling;
import gr.james.sampling.WatermanSampling;

import java.util.Collection;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * Select 10 numbers at random in the range [1,100] using {@link WatermanSampling}. Each number has a 10% probability of
 * appearing in the sample.
 */
public final class SelectRandomFromRange {
    public static void main(String[] args) {
        RandomSampling<Integer> rs = new WatermanSampling<>(10, new Random());
        rs.feed(IntStream.rangeClosed(1, 100).boxed().iterator());
        Collection<Integer> sample = rs.sample();
        System.out.println(sample);
    }
}
