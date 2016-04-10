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
        "(1) Inter Arrival Times Input File\n" +
        "(2) Service Times Input File\n" +
        "(3) Probability p\n" +
        "(4) Probability q\n" +
        "(5) Statistics Output File\n" +
        "(6) Output Format [default = csv]");
      return;
    }

    String format = "csv";
    if (args.length > 5) {
      format = args[3];
    }

    double p = 0.0;
    double q = 0.0;
    try {
      p = Double.parseDouble(args[2]);
      q = Double.parseDouble(args[3]);
    } catch (Exception e) {
      System.out.println("Error: Could not parse probability p and/or q");
      return;
    }

    run(args[0], args[1], p, q, args[4], format);
  }

  /**
   * run
   *
   * Initialize and run Single Server Queue
   */
  public static void run(String interArrivalTimesFile,
    String serviceTimesFile, double p, double q, String outputFile, String outputFormat) throws IOException {

    EventGenerator generator = new EventGenerator(interArrivalTimesFile, serviceTimesFile);
    NetworkFeedbackQueues server = new NetworkFeedbackQueues(generator, p, q, outputFormat);
    server.run();

    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "utf-8"));

    if (outputFormat.equals("csv")) {
      out.write("Queue,Time,Future Event List,Number of Departures,Queue Size,Server Occupied,Delay");
      out.newLine();
    } else if (outputFormat.equals("delay")) {
      out.write("Delay,Number of Packets,Utilization");
      out.newLine();
    }

    // Output results to a file
    for (NetworkFeedbackQueues.Statistic stat : server.getStatistics()) {
      out.write(stat.toString());
      out.newLine();
    }

    out.close();
  }
}
