import java.util.*;
import java.io.*;

/**
 * Implementation of a random event generator. The generator
 * generates random variables.
 *
 * @author Kevin Rosengren, Ian Wong, Nikola Neskovic
 * @version 11/03/16
 */
public class EventGenerator {

  private Random randomAT;
  private Random randomST;
  private double lambda;
  private double mu;
  private double c;
  private double k;
  private double alpha;
  private double beta;

  /**
   *
   * @param lambda
   * @param mu
   */
  public EventGenerator(double lambda, double mu) throws IOException {
    this.lambda = lambda;
    this.mu = mu;

    randomAT = new Random();
    randomST = new Random();

    setup();
  }

  private void setup() {
    c = 0.767 - 3.36 / lambda;
    beta = Math.PI / Math.sqrt(3.0 * lambda);
    alpha = beta * lambda;
    k = Math.log(c) - lambda - Math.log(beta);
  }

  /**
   * nextArrivalTime
   *
   * Poisson Formula from:
   * http://www.johndcook.com/blog/2010/06/14/generating-poisson-random-values/
   *
   * @return next generated arrival time
   */
  public double nextArrivalTime() throws IOException {

    double u, x, y, n, v, lhs, rhs;
    while(true) {
      u = randomAT.nextDouble();
      x = (alpha - Math.log10((1.0 - u)/u)) / beta;
      n = Math.floor(x + 0.5);
      if (n < 0)
    		continue;

      v = randomAT.nextDouble();
      y = alpha - beta * x;
      lhs = y + Math.log( v / Math.pow(1.0 + Math.exp(y), 2));
      rhs = k + n * Math.log10(lambda) - logFactorial(n);

    	if (lhs <= rhs)
    		return n;
    }

  }

  /**
   * Stirling Approximation for log factorial
   */
  private double logFactorial(double x) {
    return (x - 0.5) * Math.log10(x) - x + (0.5) * Math.log10(2 * Math.PI);
  }

  /**
   * nextServiceTime
   *
   * Using inversion method to generate exponential distribution
   *
   * @return next random service time
   */
  public double nextServiceTime() throws IOException {
    return (-mu) * Math.log(1 - randomST.nextDouble());
  }
}
