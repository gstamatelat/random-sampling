# Random Sampling

A collection of algorithms in Java 8 for the problem of random sampling with a reservoir.

Reservoir sampling is a family of randomized algorithms for randomly choosing a sample of `k` items from a list `S` containing `n` items, where `n` is either a very large or unknown number. Typically `n` is large enough that the list doesn't fit into main memory. [1] In this context, the sample of `k` items will be referred to as ***sample*** and the list `S` as ***stream***.

This library distinguishes these algorithms into two main categories: the ones that assign a weight in each item of the source stream and the ones that don't. These will be referred to as weighted and unweighted random sampling algorithms respectively. In unweighted algorithms, each item in the stream has probability `k/n` in appearing in the sample. In weighted algorithms this probability depends on the extra parameter `weight`. Each algorithm may interpret this parameter in a different way, for example in [2] two possible interpretations are mentioned.

## Examples

Select 10 numbers at random in the range [1,100]. Each number has a 10% probability of appearing in the sample.
```java
final RandomSampling<Integer> rs = new WatermanSampling<>(10, new Random());
rs.feed(IntStream.rangeClosed(1, 100).boxed().iterator());
Collection<Integer> sample = rs.sample();
System.out.println(sample);
```

Select 5 random tokens from an input stream.
```java
final RandomSampling<String> rs = new VitterXSampling<>(5, new Random());
rs.feed(new Scanner(System.in));
System.out.println(rs.sample());
```

Same example using Algorithm Z.
```java
final RandomSampling<String> rs = new VitterZSampling<>(5, new Random());
rs.feed(new Scanner(System.in));
System.out.println(rs.sample());
```

Select 2 terms from a vocabulary, based on their weight.
```java
final WeightedRandomSampling<String> rs = new EfraimidisSampling<>(2, new Random());
rs.feed("collection", 1)
  .feed("algorithms", 2)
  .feed("java", 2)
  .feed("random", 3)
  .feed("sampling", 4)
  .feed("reservoir", 5);
System.out.println(rs.sample());
```

## Algorithms

### 1 Algorithm R by Waterman

The Art of Computer Programming, Vol II, Random Sampling and Shuffling.

Signature: `WatermanSampling` implements `RandomSampling`

### 2 Algorithm X by Vitter

[Vitter, Jeffrey S. "Random sampling with a reservoir." ACM Transactions on Mathematical Software (TOMS) 11.1 (1985): 37-57.](https://doi.org/10.1145/3147.3165)

Signature: `VitterXSampling` implements `RandomSampling`

### 3 Algorithm Z by Vitter

[Vitter, Jeffrey S. "Random sampling with a reservoir." ACM Transactions on Mathematical Software (TOMS) 11.1 (1985): 37-57.](https://doi.org/10.1145/3147.3165)

Signature: `VitterZSampling` implements `RandomSampling`

### 4 Algorithm A-Res by Efraimidis and Spirakis

[Efraimidis, Pavlos S., and Paul G. Spirakis. "Weighted random sampling with a reservoir." Information Processing Letters 97.5 (2006): 181-185.](https://doi.org/10.1016/j.ipl.2005.11.003)

Signature: `EfraimidisSampling` implements `WeightedRandomSampling`

### 5 Algorithm by Chao

[Chao, M. T. "A general purpose unequal probability sampling plan." Biometrika 69.3 (1982): 653-656.](https://doi.org/10.2307/2336002)

Signature: `ChaoSampling` implements `WeightedRandomSampling`

## References

[1] [Wikipedia contributors. "Reservoir sampling." Wikipedia, The Free Encyclopedia. Wikipedia, The Free Encyclopedia, 17 Oct. 2017. Web. 21 Nov. 2017.](https://en.wikipedia.org/wiki/Reservoir_sampling)

[2] [Efraimidis, Pavlos S. "Weighted random sampling over data streams." Algorithms, Probability, Networks, and Games. Springer International Publishing, 2015. 183-195.](https://doi.org/10.1007/978-3-319-24024-4_12)
