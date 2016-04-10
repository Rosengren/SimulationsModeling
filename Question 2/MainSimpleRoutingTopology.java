import java.util.*;
import java.io.*;

/**
 * Implemenation of the Simulator used to initialize
 * the Network Feedback Queues.
 *
 * @author Kevin Rosengren, Ian Wong, Nikola Neskovic
 * @version 04/09/16
 */
public class MainSimpleRoutingTopology {

  /**
   * main
   *
   * @param inter-arrival input file
   * @param service times input file
   * @param statistics output file
   */
  public static void main(String[] args) throws IOException {

    if (args.length < 4) {
      System.out.println("Missing parameters:\n" +
        "(1) Lambda\n" +
        "(2) Mu\n" +
        "(3) Number of Data Points\n" +
        "(4) Number of Replicas");
      return;
    }

    String format = "csv";
    if (args.length > 4) {
      format = args[4];
    }

    double lambda = 0.0;
    double mu = 0.0;
    long dataPoints = 0;
    int replicas = 0;
    try {
      lambda = Double.parseDouble(args[0]);
      mu = Double.parseDouble(args[1]);
      dataPoints = Long.parseLong(args[2]);
      replicas = Integer.parseInt(args[3]);
    } catch (Exception e) {
      System.out.println("Error: Could not parse doubles");
      return;
    }

    System.out.println("Running simulation with:\n" +
        "\tLambda: " + lambda + "\n" +
        "\tMu: " + mu + "\n" +
        "\tData Points: " + dataPoints + "\n" +
        "\t# of Replicas: " + replicas + "\n\n");

    for (int i = 0; i < replicas; i++) {
      run(lambda, mu, dataPoints, args[3], format);
    }
  }

  /**
   * run
   *
   * Initialize and run Single Server Queue
   */
  public static void run(double lambda, double mu, long dataPoints, String outputFile, String outputFormat) throws IOException {

    EventGenerator generator = new EventGenerator(lambda, mu);
    SimpleRoutingTopology server = new SimpleRoutingTopology(generator, dataPoints, outputFormat);
    server.run();
    server.printDelays();
  }
}
