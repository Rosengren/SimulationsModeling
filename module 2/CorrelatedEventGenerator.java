import java.util.*;

/**
 * Implementation of a correlated random event generator.
 *
 * @author Kevin Rosengren, Ian Wong, Nikola Neskovic
 * @version 10/04/16
 */
public class CorrelatedEventGenerator implements EventGenerator {

  private TESModelGenerator generator;

  private Random randomAT;
  private Random randomST;
  private double lambda;
  private double mu;
  private double xi;

  private double previous_AT_U_prime;
  private double previous_ST_U_prime;

  /**
   *
   * @param lambda
   * @param mu
   */
  public CorrelatedEventGenerator(double lambda, double mu, double xi, double interval) {
    this.lambda = lambda;
    this.mu = mu;
    this.xi = xi;

    randomAT = new Random();
    randomST = new Random();

    generator = new TESModelGenerator();
    generator.setUniformRange(interval, -interval);

    previous_AT_U_prime = randomAT.nextDouble();
    previous_ST_U_prime = randomST.nextDouble();
  }

  /**
   * nextArrivalTime
   *
   * Using TES Model Generator to generate
   * correlated exponentially distributed
   * random variables.
   *
   * @return next generated arrival time
   */
  public double nextArrivalTime() {
    double u_prime = generator.generateNext(previous_AT_U_prime, randomAT.nextDouble());
    double u_n = generator.stitchTransform(u_prime, xi);
    previous_AT_U_prime = u_prime;

    return generator.inverseExponentialTransform(lambda, u_n);
  }

  /**
   * nextServiceTime
   *
   * Using TES Model Generator to generate
   * correlated exponentially distributed
   * random variables.
   *
   * @return next random service time
   */
  public double nextServiceTime() {
    double u_prime = generator.generateNext(previous_ST_U_prime, randomST.nextDouble());
    double u_n = generator.stitchTransform(u_prime, xi);
    previous_ST_U_prime = u_prime;

    return generator.inverseExponentialTransform(mu, u_n);
  }

}
