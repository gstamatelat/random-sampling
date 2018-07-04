import java.util.{Random, Scanner}

import gr.james.sampling.VitterXSampling

/**
  * Select 5 random tokens from an input stream using [[VitterXSampling]].
  */
object SelectRandomFromStreamX extends App {
  val rs = new VitterXSampling[String](5, new Random)
  rs.feed(new Scanner(System.in))
  System.out.println(rs.sample)
}
