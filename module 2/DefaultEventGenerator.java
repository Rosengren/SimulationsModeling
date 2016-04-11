import java.util.*;
import java.io.*;

/**
 * Implementation of a random event generator. The generator
 * generates random variables.
 *
 * @author Kevin Rosengren, Ian Wong, Nikola Neskovic
 * @version 10/04/16
 */
public class DefaultEventGenerator implements EventGenerator {

  private Random randomAT;
  private Random randomST;
  private double lambda;
  private double mu;

  /**
   *
   * @param lambda
   * @param mu
   */
  public DefaultEventGenerator(double lambda, double mu) {
    this.lambda = lambda;
    this.mu = mu;

    randomAT = new Random();
    randomST = new Random();
  }

  /**
   * nextArrivalTime
   *
   * Using inversion method to generate exponential distribution
   *
   * @return next generated arrival time
   */
  public double nextArrivalTime() {
    return (-1 / lambda) * Math.log(1 - randomAT.nextDouble());
  }

  /**
   * nextServiceTime
   *
   * Using inversion method to generate exponential distribution
   *
   * @return next random service time
   */
  public double nextServiceTime() {
    return (-1 / mu) * Math.log(1 - randomST.nextDouble());
  }
}
