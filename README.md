# Random Sampling

A collection of algorithms in Java 8 for the problem of random sampling with a
reservoir.

Reservoir sampling is a family of randomized algorithms for randomly choosing a
sample of `k` items from a list `S` containing `n` items, where `n` is either a
very large or unknown number. Typically `n` is large enough that the list
doesn't fit into main memory. [1] In this context, the sample of `k` items will
be referred to as ***sample*** and the list `S` as ***stream***.

This package distinguishes these algorithms into two main categories: the ones
that assign a weight in each item of the source stream and the ones that don't.
These will be referred to as weighted and unweighted random sampling algorithms
respectively. In unweighted algorithms, each item in the stream has probability
`k/n` in appearing in the sample. In weighted algorithms this probability
depends on the extra parameter `weight`. Each algorithm may interpret this
parameter in a different way, for example in [2] two possible interpretations
are mentioned.

## Using

Random Sampling is published to
[jcenter](https://bintray.com/gstamatelat/random-sampling/random-sampling). You
can add a dependency from your project as follows:

Using Maven

```xml
<dependency>
  <groupId>gr.james</groupId>
  <artifactId>random-sampling</artifactId>
  <version>0.13</version>
</dependency>
```

Using Gradle

```
compile 'gr.james:random-sampling:0.13'
```

## Examples

Select 10 numbers at random in the range [1,100]. Each number has a 10%
probability of appearing in the sample.

```java
RandomSampling<Integer> rs = new WatermanSampling<>(10, new Random());
rs.feed(IntStream.rangeClosed(1, 100).boxed().iterator());
Collection<Integer> sample = rs.sample();
System.out.println(sample);
```

Select 5 random tokens from an input stream.

```java
RandomSampling<String> rs = new VitterXSampling<>(5, new Random());
rs.feed(new Scanner(System.in));
System.out.println(rs.sample());
```

Same example using Algorithm Z.

```java
RandomSampling<String> rs = new VitterZSampling<>(5, new Random());
rs.feed(new Scanner(System.in));
System.out.println(rs.sample());
```

Select 2 terms from a vocabulary, based on their weight.

```java
WeightedRandomSampling<String> rs = new EfraimidisSampling<>(2, new Random());
rs.feed("collection", 1);
rs.feed("algorithms", 2);
rs.feed("java", 2);
rs.feed("random", 3);
rs.feed("sampling", 4);
rs.feed("reservoir", 5);
System.out.println(rs.sample());
```

Unweighted random sampling using the Java 8 stream API.

```java
RandomSamplingCollector<Integer> collector = WatermanSampling.collector(5, new Random());
Collection<Integer> sample = IntStream.range(0, 20).boxed().collect(collector);
System.out.println(sample);
```

Weighted random sampling using the Java 8 stream API.

```java
WeightedRandomSamplingCollector<String> collector = ChaoSampling.weightedCollector(2, new Random());
Map<String, Double> map = new HashMap<>();
map.put("collection", 1.0);
map.put("algorithms", 2.0);
map.put("java", 2.0);
map.put("random", 3.0);
map.put("sampling", 4.0);
map.put("reservoir", 5.0);
Collection<String> sample = map.entrySet().stream().collect(collector);
System.out.println(sample);
```

## Algorithms

| Class                | Algorithm                     | Space  | Weighted |
| :------------------- | :---------------------------- | :----- | :------- |
| `WatermanSampling`   | Algorithm R by Waterman       | `O(k)` |          |
| `VitterXSampling`    | Algorithm X by Vitter         | `O(k)` |          |
| `VitterZSampling`    | Algorithm Z by Vitter         | `O(k)` |          |
| `LiLSampling`        | Algorithm L by Li             | `O(k)` |          |
| `EfraimidisSampling` | Algorithm A-Res by Efraimidis | `O(k)` | &#10004; |
| `ChaoSampling`       | Algorithm by Chao             | `O(k)` | &#10004; |

### 1 Algorithm R by Waterman

Signature: `WatermanSampling` implements `RandomSampling`

#### References
- The Art of Computer Programming, Vol II, Random Sampling and Shuffling.

### 2 Algorithm X by Vitter

Signature: `VitterXSampling` implements `RandomSampling`

#### References
- [Vitter, Jeffrey S. "Random sampling with a reservoir." ACM Transactions on Mathematical Software (TOMS) 11.1 (1985): 37-57.](https://doi.org/10.1145/3147.3165)

### 3 Algorithm Z by Vitter

Signature: `VitterZSampling` implements `RandomSampling`

#### References
- [Vitter, Jeffrey S. "Random sampling with a reservoir." ACM Transactions on Mathematical Software (TOMS) 11.1 (1985): 37-57.](https://doi.org/10.1145/3147.3165)

### 4 Algorithm L by Li

Signature: `LiLSampling` implements `RandomSampling`

#### References
- [Li, Kim-Hung. "Reservoir-sampling algorithms of time complexity O (n (1+ log (N/n)))." ACM Transactions on Mathematical Software (TOMS) 20.4 (1994): 481-493.](https://doi.org/10.1145/198429.198435)

### 5 Algorithm A-Res by Efraimidis

Signature: `EfraimidisSampling` implements `WeightedRandomSampling`

#### References
- [Efraimidis, Pavlos S., and Paul G. Spirakis. "Weighted random sampling with a reservoir." Information Processing Letters 97.5 (2006): 181-185.](https://doi.org/10.1016/j.ipl.2005.11.003)

### 6 Algorithm by Chao

Signature: `ChaoSampling` implements `WeightedRandomSampling`

#### References
- [Chao, M. T. "A general purpose unequal probability sampling plan." Biometrika 69.3 (1982): 653-656.](https://doi.org/10.2307/2336002)
- [Sugden, R. A. "Chao's list sequential scheme for unequal probability sampling." Journal of Applied Statistics 23.4 (1996): 413-421.](https://doi.org/10.1080/02664769624152)

## References

[1] [Wikipedia contributors. "Reservoir sampling." Wikipedia, The Free Encyclopedia. Wikipedia, The Free Encyclopedia, 17 Oct. 2017. Web. 21 Nov. 2017.](https://en.wikipedia.org/wiki/Reservoir_sampling)

[2] [Efraimidis, Pavlos S. "Weighted random sampling over data streams." Algorithms, Probability, Networks, and Games. Springer International Publishing, 2015. 183-195.](https://doi.org/10.1007/978-3-319-24024-4_12)
