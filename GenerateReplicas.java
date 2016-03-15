import java.util.*;
import java.io.*;

public class GenerateReplicas {

  private static final String OUTPUT_FOLDER = "replicas";
  private static final String OUTPUT_TIMES_FOLDER = "times";
  private static final String OUTPUT_SIMS_FOLDER = "sims";
  private static final int NUMBER_OF_REPLICAS = 20;
  private static final int NUMBER_OF_PACKETS = 110000;

  private static final double[] intervals = new double[]{0.1,0.01,0.5};
  private static final int[] lambdas = new int[]{1,3,5,7,9};
  private static final int mu = 10;
  private static final double xi = 0.7;

  public static void main(String[] args) throws IOException {

    // 1 - Generate Random Variable sets
    // generateRandomVariables();
    
    // 2 - Generate Correlated Exponential Data
    // generateTraffic();

    // 3 - Run the Simulations
    runSimulations();
  }

  public static void generateRandomVariables() throws IOException {

    System.out.println("Generating Random Variables");
    for (int i = 0; i < NUMBER_OF_REPLICAS; i++) {
      RandomNumberGenerator.generate(0, 1, 
        NUMBER_OF_PACKETS, 
        OUTPUT_FOLDER + File.separator + "randomIA" + i);

      RandomNumberGenerator.generate(0, 1,
        NUMBER_OF_PACKETS,
        OUTPUT_FOLDER + File.separator + "randomST" + i);
    }
  }

  public static void generateTraffic() throws IOException {

    System.out.println("Generating Traffic");

    for (int i = 0; i < NUMBER_OF_REPLICAS; i++) {
      for (double interval : intervals) {

        // Always Use 0.5 intervals for Service Times
        // TrafficGenerator.generateTimes(interval, -interval, xi,
        TrafficGenerator.generateTimes(0.5, -0.5, xi,
          mu, OUTPUT_FOLDER + File.separator + "randomST" + i,
          OUTPUT_FOLDER + File.separator + OUTPUT_TIMES_FOLDER + File.separator + "replica-" + i + "-st-" + mu + "-" + interval + ".csv");

        for (int lambda : lambdas) {
          TrafficGenerator.generateTimes(interval, -interval, xi,
            lambda, OUTPUT_FOLDER + File.separator + "randomIA" + i,
            OUTPUT_FOLDER + File.separator + OUTPUT_TIMES_FOLDER + File.separator + "replica-" + i + "-ia-" + lambda + "-" + interval + ".csv");

        }
      }      
    }
  }

  public static void runSimulations() throws IOException {

    System.out.println("Running Simulations");

    // replica-0-ia-1-0.01
    // replica- (0-19) -ai- (1,3,5,7,9) - (0.01,0.1,0.5) .csv

    String folder = OUTPUT_FOLDER + File.separator + OUTPUT_TIMES_FOLDER + File.separator;
    String outFolder = OUTPUT_FOLDER + File.separator + OUTPUT_SIMS_FOLDER + File.separator;
    String iaFile = "";
    String stFile = "";
    String outFile = "";

    for (int i = 0; i < NUMBER_OF_REPLICAS; i++) {

      for (double interval : intervals) {
        stFile = folder + "replica-" + i + "-st-" + mu + "-" + interval + ".csv";

        for (int lambda :lambdas) {
          iaFile = folder + "replica-" + i +"-ia-" + lambda + "-" + interval + ".csv";

          // outFile = outFolder + "replica-" + i + "-sim-" + lambda + "-" + mu + "-" + interval + ".csv";
          outFile = outFolder + "sim-"  + lambda + "-" + mu + "-" + interval + File.separator + "replica-" + i + ".csv";
          Simulator.run(iaFile, stFile, outFile, "delay");
          System.out.println("Ran sim: " + i + " - interval: " + interval + " lambda: " + lambda + " mu: " + mu);
        }  
      }
    }
  }
}