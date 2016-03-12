import java.util.*;
import java.io.*;

/**
 * Implemenation of the Simulator used to initialize
 * the Single Server Queue.
 *
 * @author Kevin Rosengren, Ian Wong, Nikola Neskovic
 * @version 03/11/16
 */
public class Simulator {

  /**
   * main
   *
   * @param inter-arrival input file
   * @param service times input file
   * @param statistics output file
   */
  public static void main(String[] args) throws IOException {

    if (args.length < 3) {
      System.out.println("Missing parameters:\n" + 
        "(1) Inter Arrival Times Input File\n" +
        "(2) Service Times Input File\n" +
        "(3) Statistics Output File");
      return;
    }

    run(args[0], args[1], args[2]);
  }

  /**
   * run
   *
   * Initialize and run Single Server Queue
   */
  public static void run(String interArrivalTimesFile,
    String serviceTimesFile, String outputFile) throws IOException {

    EventGenerator generator = new EventGenerator(interArrivalTimesFile, serviceTimesFile);
    SingleServerQueue server = new SingleServerQueue(generator);
    server.run();

    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "utf-8"));
    
    // Output results to a file
    for (SingleServerQueue.Statistic stat : server.getStatistics()) {
      out.write(stat.toString());
      out.newLine();
    }
  }
}