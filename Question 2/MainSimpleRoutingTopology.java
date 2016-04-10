import java.util.*;
import java.io.*;

/**
 * Implemenation of the Simulator used to initialize
 * the Simple Rounting Topology.
 *
 * @author Kevin Rosengren, Ian Wong, Nikola Neskovic
 * @version 10/04/16
 */
public class MainSimpleRoutingTopology {

  /**
   * main
   *
   * @param lambda
   * @param mu
   * @param Number of data points to generate
   * @param Number of replicas
   */
  public static void main(String[] args) throws IOException {

    if (args.length < 4) {
      System.out.println("Missing parameters:\n" +
        "(1) Lambda\n" +
        "(2) Mu\n" +
        "(3) Routing Strategy (RR or STQ)\n" +
        "(4) Number of Data Points\n" +
        "(5) Number of Replicas");
      return;
    }

    double lambda = 0.0;
    double mu = 0.0;
    long dataPoints = 0;
    int replicas = 0;

    try {
      lambda = Double.parseDouble(args[0]);
      mu = Double.parseDouble(args[1]);
      dataPoints = Long.parseLong(args[3]);
      replicas = Integer.parseInt(args[4]);
    } catch (Exception e) {
      System.out.println("Error: Could not parse doubles");
      return;
    }

    String routingStrategy = SimpleRoutingTopology.ROUND_ROBIN; // default
    if (args[2].equals("STQ")) {
      routingStrategy = SimpleRoutingTopology.SHORTEST_TOTAL_QUEUE;
    }

    System.out.println("Running simulation with:\n" +
        "\tLambda: " + lambda + "\n" +
        "\tMu: " + mu + "\n" +
        "\tRouting Strategy: " + routingStrategy + "\n" +
        "\tData Points: " + dataPoints + "\n" +
        "\t# of Replicas: " + replicas + "\n\n");

    // Run simulations
    for (int i = 0; i < replicas; i++) {
      run(lambda, mu, dataPoints, routingStrategy);
    }
  }

  /**
   * run
   *
   * Initialize and run Single Server Queue
   */
  public static void run(double lambda, double mu, long dataPoints, String routingStrategy) {

    EventGenerator generator = new EventGenerator(lambda, mu);
    SimpleRoutingTopology server = new SimpleRoutingTopology(generator, dataPoints, routingStrategy);
    server.run();
    server.printResults();
  }
}
