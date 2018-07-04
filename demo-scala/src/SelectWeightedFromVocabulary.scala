import java.util.Random

import gr.james.sampling.EfraimidisSampling

/**
  * Select 2 terms from a vocabulary using [[EfraimidisSampling]], based on their weight.
  */
object SelectWeightedFromVocabulary extends App {
  val rs = new EfraimidisSampling[String](2, new Random)
  rs.feed("collection", 1)
  rs.feed("algorithms", 2)
  rs.feed("java", 2)
  rs.feed("random", 3)
  rs.feed("sampling", 4)
  rs.feed("reservoir", 5)
  System.out.println(rs.sample)
}
