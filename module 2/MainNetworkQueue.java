import java.util.*;
import java.io.*;

/**
 * Implemenation of the Simulator used to initialize
 * the Network Feedback Queues.
 *
 * @author Kevin Rosengren, Ian Wong, Nikola Neskovic
 * @version 04/09/16
 */
public class MainNetworkQueue {

  private static final double XI = 0.7;
  private static final double INTERVAL = 0.01;

  /**
   * main
   *
   * @param inter-arrival input file
   * @param service times input file
   * @param statistics output file
   */
  public static void main(String[] args) throws IOException {

    if (args.length < 6) {
      System.out.println("Missing parameters:\n" +
        "(1) Lambda\n" +
        "(2) Mu\n" +
        "(3) Probability p\n" +
        "(4) Probability q\n" +
        "(5) Number of departures\n" +
        "(6) Number of replicas\n" +
        "(7) Event Generation Type (DEF or COR)");
      return;
    }

    String generatorType = "DEF";
    if (args.length > 6) {
      if (args[6].equals("COR")) {
        generatorType = "COR";
      }
    }

    double p = 0.0;
    double q = 0.0;
    double lambda = 0.0;
    double mu = 0.0;
    long departures = 0;
    int replicas = 0;
    try {
      lambda = Double.parseDouble(args[0]);
      mu = Double.parseDouble(args[1]);
      p = Double.parseDouble(args[2]);
      q = Double.parseDouble(args[3]);
      departures = Long.parseLong(args[4]);
      replicas = Integer.parseInt(args[5]);
    } catch (Exception e) {
      System.out.println("Error: Could not parse doubles");
      return;
    }

    System.out.println("Running simulation with:\n" +
        "\tLambda: " + lambda + "\n" +
        "\tMu: " + mu + "\n" +
        "\tp: " + p + "\n" +
        "\tq: " + q + "\n" +
        "\t# of Departures: " + departures);

    run(generatorType, lambda, mu, p, q, departures, replicas);

  }

  /**
   * run
   *
   * Initialize and run Single Server Queue
   */
  public static void run(String generatorType, double lambda, double mu, double p, double q, long departures, int replicas) {

    EventGenerator generator1, generator2;

    try {
    BufferedWriter writer = new BufferedWriter(new FileWriter(new File("output.txt").getAbsoluteFile()));
    
    for (int i = 0; i < replicas; i++) {
      if (generatorType.equals("COR")) {
        generator1 = new CorrelatedEventGenerator(lambda, mu, XI, INTERVAL);
        generator2 = new CorrelatedEventGenerator(lambda, mu, XI, INTERVAL);
      } else {
        generator1 = new DefaultEventGenerator(lambda, mu);
        generator2 = new DefaultEventGenerator(lambda, mu);
      }

      NetworkFeedbackQueues server = new NetworkFeedbackQueues(generator1, generator2, p, q, departures);
      server.run();

      System.out.println("\n");
      writer.write(server.printResults());
    }

    } catch (Exception e) {
  	  e.printStackTrace();
    }
  }
}
