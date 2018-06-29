import gr.james.sampling.EfraimidisSampling;
import gr.james.sampling.WeightedRandomSampling;

import java.util.Random;

/**
 * Select 2 terms from a vocabulary using {@link EfraimidisSampling}, based on their weight.
 */
public final class SelectWeightedFromVocabulary {
    public static void main(String[] args) {
        WeightedRandomSampling<String> rs = new EfraimidisSampling<>(2, new Random());
        rs.feed("collection", 1)
                .feed("algorithms", 2)
                .feed("java", 2)
                .feed("random", 3)
                .feed("sampling", 4)
                .feed("reservoir", 5);
        System.out.println(rs.sample());
    }
}
