package gr.james.sampling;

import java.util.Random;

public class Benchmark {

    private static final Random random = new Random();

    private static final WatermanSampling<Object> waterman = new WatermanSampling<>(10, random);
    private static final VitterXSampling<Object> vitterx = new VitterXSampling<>(10, random);
    private static final VitterZSampling<Object> vitterz = new VitterZSampling<>(10, random);
    private static final EfraimidisSampling<Object> efraimidis = new EfraimidisSampling<>(10, random);

    public static void main(String[] args) {
        System.out.printf("%10s %5d ms%n", "Waterman", performance(waterman) / 1000000);
        System.out.printf("%10s %5d ms%n", "VitterX", performance(vitterx) / 1000000);
        System.out.printf("%10s %5d ms%n", "VitterZ", performance(vitterz) / 1000000);
        System.out.printf("%10s %5d ms%n", "Efraimidis", performance(efraimidis) / 1000000);
    }

    private static long performance(RandomSampling<Object> alg) {
        final long start = System.nanoTime();
        if (alg instanceof UnweightedRandomSampling) {
            for (int i = 0; i < 100000000; i++) {
                ((UnweightedRandomSampling<Object>) alg).feed(i);
            }
        } else if (alg instanceof WeightedRandomSampling) {
            for (int i = 0; i < 100000000; i++) {
                ((WeightedRandomSampling<Object>) alg).feed(i, 1.0);
            }
        }
        return System.nanoTime() - start;
    }

}
