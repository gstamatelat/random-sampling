import gr.james.sampling.RandomSampling;
import gr.james.sampling.VitterXSampling;

import java.util.Random;
import java.util.Scanner;

/**
 * Select 5 random tokens from an input stream using {@link VitterXSampling}.
 */
public final class SelectRandomFromStreamX {
    public static void main(String[] args) {
        RandomSampling<String> rs = new VitterXSampling<>(5, new Random());
        rs.feed(new Scanner(System.in));
        System.out.println(rs.sample());
    }
}
