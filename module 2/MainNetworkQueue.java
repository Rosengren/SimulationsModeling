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

  /**
   * main
   *
   * @param inter-arrival input file
   * @param service times input file
   * @param statistics output file
   */
  public static void main(String[] args) throws IOException {

    if (args.length < 5) {
      System.out.println("Missing parameters:\n" +
        "(1) Lambda\n" +
        "(2) Mu\n" +
        "(3) Probability p\n" +
        "(4) Probability q\n" +
        "(5) Number of Data Points\n" +
        "(6) Statistics Output File\n" +
        "(7) Output Format [default = csv]");
      return;
    }

    String format = "csv";
    if (args.length > 6) {
      format = args[6];
    }

    double p = 0.0;
    double q = 0.0;
    double lambda = 0.0;
    double mu = 0.0;
    long dataPoints = 0;
    try {
      lambda = Double.parseDouble(args[0]);
      mu = Double.parseDouble(args[1]);
      p = Double.parseDouble(args[2]);
      q = Double.parseDouble(args[3]);
      dataPoints = Long.parseLong(args[4]);
    } catch (Exception e) {
      System.out.println("Error: Could not parse doubles");
      return;
    }

    System.out.println("Running simulation with:\n" +
        "\tLambda: " + lambda + "\n" +
        "\tMu: " + mu + "\n" +
        "\tp: " + p + "\n" +
        "\tq: " + q + "\n" +
        "\tData Points: " + dataPoints);

    run(lambda, mu, p, q, dataPoints, args[5], format);
  }

  /**
   * run
   *
   * Initialize and run Single Server Queue
   */
  public static void run(double lambda, double mu, double p, double q, long dataPoints, String outputFile, String outputFormat) throws IOException {

    EventGenerator generator = new EventGenerator(lambda, mu);
    NetworkFeedbackQueues server = new NetworkFeedbackQueues(generator, p, q, dataPoints, outputFormat);
    server.run();

    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "utf-8"));

    if (outputFormat.equals("csv")) {
      out.write("Queue,Time,Future Event List,Number of Departures,Queue Size,Server Occupied,Delay");
      out.newLine();
    } else if (outputFormat.equals("delay")) {
      out.write("Delay,Number of Packets,Utilization");
      out.newLine();
    }

    server.printFrequencies();

    // Output results to a file
    for (NetworkFeedbackQueues.Statistic stat : server.getStatistics()) {
      out.write(stat.toString());
      out.newLine();
    }

    out.close();
  }
}
