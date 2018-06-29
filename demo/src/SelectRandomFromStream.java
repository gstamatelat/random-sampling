import gr.james.sampling.RandomSampling;
import gr.james.sampling.VitterXSampling;

import java.nio.charset.Charset;
import java.util.Random;
import java.util.Scanner;

/**
 * Select 5 random tokens from an input stream using {@link VitterXSampling}.
 */
public final class SelectRandomFromStream {
    public static void main(String[] args) {
        RandomSampling<String> rs = new VitterXSampling<>(5, new Random());
        System.out.println(Charset.defaultCharset().name());
        rs.feed(new Scanner(System.in, Charset.defaultCharset().name()));
        System.out.println(rs.sample());
    }
}