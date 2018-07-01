import gr.james.sampling.EfraimidisSampling;
import gr.james.sampling.WeightedRandomSampling;

import java.util.Random;

/**
 * Select 2 terms from a vocabulary using {@link EfraimidisSampling}, based on their weight.
 */
public final class SelectWeightedFromVocabulary {
    public static void main(String[] args) {
        WeightedRandomSampling<String> rs = new EfraimidisSampling<>(2, new Random());
        rs.feed("collection", 1);
        rs.feed("algorithms", 2);
        rs.feed("java", 2);
        rs.feed("random", 3);
        rs.feed("sampling", 4);
        rs.feed("reservoir", 5);
        System.out.println(rs.sample());
    }
}
