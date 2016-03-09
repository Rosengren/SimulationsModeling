import java.util.*;

public class TESModelGenerator {

  private static final double DEFAULT_A = 0;
  private static final double DEFAULT_B = 1;

  private Random random;
  private double a;
  private double b;

  public TESModelGenerator() {
    random = new Random();
    a = DEFAULT_A;
    b = DEFAULT_B;
  }

  /**
   * setUniformRange
   *
   * set the range used to generate uniformly
   * distributed random variables.
   * Range is [a, b)
   */
  public void setUniformRange(double a, double b) {
    this.a = a;
    this.b = b;
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
  public double generateNext(double previousRV) {
    return modulo1(previousRV + nextUniformRV());
  }

  /**
   * modulo1
   *
   * Performs modulo-1 operation on given input
   *
   *    < x > = x - floor(x)
   */
  private double modulo1(double x) {
    return x - Math.floor(x);
  }

  /**
   * stitchTransform
   *
   * perform a stich transformation on a given 
   * random variablue u' and a stitching parameter xi
   */
  public double strichTransform(double u, double xi) {
    if (u >= 0 && u <= xi) {
      return u / xi;
    } else { // xi <= u < 1
      return (1 - u) / (1 - xi);
    }
  }

  /**
   * uniformRV
   *
   * generate a uniform variable
   * in the range of [a, b)
   *
   */
  private double nextUniformRV() {
    return a + (b - a) * random.nextDouble();
  }
}