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
  }

  /**
   * nextArrivalTime
   *
   * @return next generated arrival time
   */
  public double nextArrivalTime() throws IOException {
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
