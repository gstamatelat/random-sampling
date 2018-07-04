import java.util.{Random, Scanner}

import gr.james.sampling.VitterZSampling

/**
  * Select 5 random tokens from an input stream using [[VitterZSampling]].
  */
object SelectRandomFromStreamZ extends App {
  val rs = new VitterZSampling[String](5, new Random)
  rs.feed(new Scanner(System.in))
  System.out.println(rs.sample)
}
