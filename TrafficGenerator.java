import java.util.*;
import java.io.*;

public class TrafficGenerator {

  private static TESModelGenerator generator;

  /**
   * Generate Random Variables
   *
   * @param type   : generate inter-arrival times (it)
   *                  or service times (st)
   * @param a      : upperLimit
   * @param b      : lowerLimit
   * @param xi     : stitching parameter
   * @param ld/mu  : inter-arrival or service-times 
   *                  parameter
   * @param input  : file containing random variables
   * @param output : file to output generated values
   */
  public static void main(String[] args) {
     generator = new TESModelGenerator();

    if (args.length < 7) {
      System.out.println("Missing Parameters. Must specify the following:\n" +
        "type   = 'st' for service-times or 'it' for inter-arrival times\n" +
        "a      = upper limit of random variable interval\n" +
        "b      = lower limit of random variable interval\n" +
        "xi     = stitching parameter\n" +
        "ld/mu  = lambda or mu parameter depending on type\n" +
        "input  = filename containing random variables [0, 1)\n" +
        "output = filename to output results");
      return;
    }

    double a;
    double b;
    double xi;
    double poissonParam;
    String inputFile = args[5];
    String outputFile = args[6];

    try {
      a = Double.parseDouble(args[1]);
    } catch(Exception e) {
      System.out.println("Invalid upper limit (a): " + args[1]);
      return;
    }

    try {
      b = Double.parseDouble(args[2]);
    } catch (Exception e) {
      System.out.println("Invalid lower limit (b): " + args[2]);
      return;
    }

    try {
      xi = Double.parseDouble(args[3]);
    } catch (Exception e) {
      System.out.println("Invalid stitching parameter (xi): " + args[3]);
      return;
    }

    try {
      poissonParam = Double.parseDouble(args[4]);
    } catch(Exception e) {
      System.out.println("Invalid lambda or mu value: " + args[4]);
      return;
    }

    String type = args[0];
    if (type.equals("st")) {
      generateServiceTimes(a, b, xi, poissonParam, inputFile, outputFile);
    } else if (type.equals("ai")) {
      generateInterArrivalTimes(a, b, xi, poissonParam, inputFile, outputFile);
    } else {
      System.out.println("Invalid type : " + args[0] + ". Must be 'st' or 'ia'");
    }
  }

  /**
   * generateInterArrivalTimes
   *
   * Output correlated random variables to
   * output file 
   */
  public static void generateInterArrivalTimes(
    double a, double b, double xi, double lambda,
    String inputFile, String outputFile) {

  }

  /**
   * generatedServiceTimes
   *
   * Generate the services times from a given
   * set of random variables
   *
   */
  public static void generateServiceTimes(
    double a, double b, double xi, double mu,
    String inputFile, String outputFile) {

    double lambda = 1.0 / mu;

    // Set Range
    generator.setUniformRange(a, b);


    BufferedReader in = null;
    BufferedWriter out = null;
    try {
      in = new BufferedReader(new FileReader(new File(inputFile)));
      out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "utf-8"));

      String line = in.readLine();
      double previousRV = Double.parseDouble(line);
      double rv, s;
      while ((line = in.readLine()) != null) {

        rv = Double.parseDouble(line);

        double u_prime = generator.generateNext(previousRV, rv);
        double u_n = generator.stitchTransform(u_prime, xi);
        double inverse = generator.inverseExponentialTransform(lambda, u_n);

        out.write(Double.toString(inverse));
        out.newLine();

        previousRV = rv;
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