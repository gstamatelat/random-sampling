import gr.james.sampling.RandomSampling;
import gr.james.sampling.VitterZSampling;

import java.nio.charset.Charset;
import java.util.Random;
import java.util.Scanner;

/**
 * Select 5 random tokens from an input stream using {@link VitterZSampling}.
 */
public final class SelectRandomFromStreamZ {
    public static void main(String[] args) {
        RandomSampling<String> rs = new VitterZSampling<>(5, new Random());
        rs.feed(new Scanner(System.in, Charset.defaultCharset().name()));
        System.out.println(rs.sample());
    }
}
