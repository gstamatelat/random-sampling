import java.util.Random

import gr.james.sampling.WatermanSampling

import scala.collection.JavaConverters._

/**
  * Select 10 numbers at random in the range [1,100] using [[WatermanSampling]]. Each number has a 10% probability of
  * appearing in the sample.
  */
object SelectRandomFromRange extends App {
  val rs = new WatermanSampling[Int](10, new Random)
  rs.feed((1 to 100).iterator.asJava)
  val sample = rs.sample
  println(sample)
}
