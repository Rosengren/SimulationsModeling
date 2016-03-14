import java.text.DecimalFormat;
import java.util.*;
import java.io.*;

/**
 * Implementation of a traffic generator.
 *
 * @author Kevin Rosengren, Ian Wong, Nikola Neskovic
 * @version 03/11/16
 */
public class TrafficGenerator {

  private static TESModelGenerator generator;

  /**
   * Generate Random Variables
   *
   * @param a  : upperLimit
   * @param b  : lowerLimit
   * @param xi : stitching parameter
   * @param ld/mu  : inter-arrival or service-times parameter
   * @param input  : file containing random variables
   * @param output : file to output generated values
   */
  public static void main(String[] args) {
     generator = new TESModelGenerator();

    if (args.length < 6) {
      System.out.println("Missing Parameters. Must specify the following:\n" +
        "a      = upper limit of random variable interval\n" +
        "b      = lower limit of random variable interval\n" +
        "xi     = stitching parameter\n" +
        "ld/mu  = lambda or mu parameter\n" +
        "input  = filename containing random variables [0, 1)\n" +
        "output = filename to output results");
      return;
    }

    double a;
    double b;
    double xi;
    double lambda;
    String inputFile = args[4];
    String outputFile = args[5];

    try {
      a = Double.parseDouble(args[0]);
    } catch(Exception e) {
      System.out.println("Invalid upper limit (a): " + args[0]);
      return;
    }

    try {
      b = Double.parseDouble(args[1]);
    } catch (Exception e) {
      System.out.println("Invalid lower limit (b): " + args[1]);
      return;
    }

    if (b > a) {
      System.out.println("Error: the lower limit (b = " + b + ") is greate than upper limit (a = " + a + ")");
      return;
    }

    try {
      xi = Double.parseDouble(args[2]);
    } catch (Exception e) {
      System.out.println("Invalid stitching parameter (xi): " + args[2]);
      return;
    }

    try {
      lambda = Double.parseDouble(args[3]);
    } catch(Exception e) {
      System.out.println("Invalid lambda or mu value: " + args[3]);
      return;
    }

    generateTimes(a, b, xi, lambda, inputFile, outputFile);
  }

  /**
   * generateTimes
   *
   * Generate the services times from a given
   * set of random variables
   *
   * @param a  : upper limit of random variable range
   * @param b  : lower limit of random variable range
   * @param xi : stiching parameter
   * @param lambda : exponential distribution parameter
   * @param inputFile  : source of random variables
   * @param outputFile : destination of generated times
   */
  public static void generateTimes(double a, double b, double xi, 
    double lambda, String inputFile, String outputFile) {
    // Set Range
    generator.setUniformRange(a, b);

    DecimalFormat df = new DecimalFormat("#.#########");

    BufferedReader in = null;
    BufferedWriter out = null;
    try {
      in = new BufferedReader(new FileReader(new File(inputFile)));
      out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "utf-8"));

      out.write("Interval = [" + b + ", " + a + "), xi = " + xi + ", lambda = " + lambda + ", in: " + inputFile);
      out.newLine();

      String line = in.readLine();
      double previous_U_prime = Double.parseDouble(line);
      double previous_u_n = generator.stitchTransform(previous_U_prime, xi);
      double previous_inverse = generator.inverseExponentialTransform(lambda, previous_u_n);
      out.write(df.format(previous_inverse));
      out.newLine();

      double rv;
      while ((line = in.readLine()) != null) {
        rv = Double.parseDouble(line);
        double u_prime = generator.generateNext(previous_U_prime, rv);
        double u_n = generator.stitchTransform(u_prime, xi);
        double inverse = generator.inverseExponentialTransform(lambda, u_n);

        out.write(df.format(inverse));
        out.newLine();

        previous_U_prime = u_prime;
      }

    } catch (Exception e) {
      System.out.println(e.getMessage());
    } finally {

      try {
        if (in != null) {
          in.close();
        }

        if (out != null) {
          out.close();
        }
      } catch (Exception e) {
        // ignore
      }
    }
  }
}