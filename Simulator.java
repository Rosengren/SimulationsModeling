import java.util.*;
import java.io.*;

public class Simulator {

  private static SingleServerQueue server;

  public static void main(String[] args) throws IOException {

    if (args.length < 2) {
      System.out.println("Missing parameters:\n" + 
        "(1) Inter Arrival Times Input File\n" +
        "(2) Service Times Input File\n" +
        "(3) Output File");
      return;
    }

    String interArrivalTimesFile = args[0];
    String serviceTimesFile = args[1];
    String outputFile = args[2];

    run(interArrivalTimesFile, serviceTimesFile);
  }

  public static void run(String interArrivalTimesFile,
    String serviceTimesFile) throws IOException {

    EventGenerator g = new EventGenerator(interArrivalTimesFile, serviceTimesFile);
    SingleServerQueue q = new SingleServerQueue(g);
    q.run();
  }
}