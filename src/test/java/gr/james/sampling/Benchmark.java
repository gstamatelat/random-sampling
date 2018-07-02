package gr.james.sampling;

import java.util.Random;

public class Benchmark {

    private static final Random random = new Random();

    private static final WatermanSampling<Object> waterman = new WatermanSampling<>(10, random);
    private static final VitterXSampling<Object> vitterx = new VitterXSampling<>(10, random);
    private static final VitterZSampling<Object> vitterz = new VitterZSampling<>(10, random);
    private static final LiLSampling<Object> lil = new LiLSampling<>(10, random);
    private static final EfraimidisSampling<Object> efraimidis = new EfraimidisSampling<>(10, random);
    private static final ChaoSampling<Object> chao = new ChaoSampling<>(10, random);

    public static void main(String[] args) {
        System.out.printf("%10s %5d ms%n", "Waterman", performance(waterman) / 1000000);
        System.out.printf("%10s %5d ms%n", "VitterX", performance(vitterx) / 1000000);
        System.out.printf("%10s %5d ms%n", "VitterZ", performance(vitterz) / 1000000);
        System.out.printf("%10s %5d ms%n", "LiL", performance(lil) / 1000000);
        System.out.printf("%10s %5d ms%n", "Efraimidis", performance(efraimidis) / 1000000);
        System.out.printf("%10s %5d ms%n", "Chao", performance(chao) / 1000000);
    }

    private static long performance(RandomSampling<Object> alg) {
        final long start = System.nanoTime();
        for (int i = 0; i < 100000000; i++) {
            alg.feed(i);
        }
        return System.nanoTime() - start;
    }

}
