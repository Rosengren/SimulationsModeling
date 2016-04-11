import java.util.*;

/**
 * Implemenation of the TES model variable generator
 *
 * @author Kevin Rosengren, Ian Wong, Nikola Neskovic
 * @version 03/11/16
 */
public class TESModelGenerator {

  private static final double DEFAULT_A = 1;
  private static final double DEFAULT_B = 0;

  private double a;
  private double b;

  public TESModelGenerator() {
    a = DEFAULT_A;
    b = DEFAULT_B;
  }

  /**
   * setUniformRange
   *
   * Set the range used to generate uniformly
   * distributed random variables.
   * Range is [b, a)
   */
  public void setUniformRange(double a, double b) {
    this.a = a;
    this.b = b;
    // System.out.println("Range set to [" + b + "," + a + "]");
  }

  /**
   * generateNext
   *
   * Generate a new correlated uniformly distributed
   * random number between [0, 1)
   *
   * The formula is:
   *
   *      U'_n+1 = < U'_n + V_n+1 > , n = 0,1,2,...
   *
   * and < x > = modulo-1 operation
   */
  public double generateNext(double previous_u_prime, double v) {
    return modulo1(previous_u_prime + nextUniformRV(v));
  }

  /**
   * modulo1
   *
   * Perform modulo-1 operation on given input
   *
   *    < x > = x - floor(x)
   */
  private double modulo1(double x) {
    return x - Math.floor(x);
  }

  /**
   * stitchTransform
   *
   * Perform a stich transformation on a given
   * random variable u' and a stitching parameter xi
   *
   * @param u  : u'
   * @param xi : stitch parameter
   */
  public double stitchTransform(double u, double xi) {
    if (u >= 0 && u <= xi) {
      return u / xi;
    } else { // xi <= u < 1
      return (1.0 - u) / (1.0 - xi);
    }
  }

  /**
   * inverseTransform
   *
   * Apply the inverse transform method on an
   * exponentially distributed random variable
   *
   * @param lambda : exponential dist. parameter
   * @param r      : random variable
   */
  public double inverseExponentialTransform(double lambda, double r) {
    return (-1 / lambda) * Math.log(1 - r);
  }

  /**
   * uniformRV
   *
   * Normalizes a given random variable
   * in the range of [b, a)
   *
   * @param v : the random variable to normalize
   */
  private double nextUniformRV(double v) {
    return b + (a - b) * v;
  }
}
