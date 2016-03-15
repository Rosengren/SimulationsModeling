import java.util.*;
import java.io.*;

public class ProcessData {

  private static final String BASE_FILE_NAME = "replica-";
  private static final String FILE_EXT = ".csv";

  private static final double T_STATISTIC_19_0975 = 2.093;
  private static final String INPUT_SIMS_FOLDER = "sims";
  private static final String OUTPUT_FOLDER = "replicas";
  private static final String OUTPUT_PROCESSED_FOLDER = "processed";
  private static final int NUMBER_OF_REPLICAS = 20;

  private static final double[] intervals = new double[]{0.01,0.1,0.5};
  private static final int[] lambdas = new int[]{1,3,5,7,9};
  private static final int mu = 10;
  private static final double xi = 0.7;

  public void run() throws IOException {

    System.out.println("File,Utilization,Packet Delay,Packet Delay CI, Packets in System, Packets in System CI");

    String inFolder = OUTPUT_FOLDER + File.separator + INPUT_SIMS_FOLDER + File.separator;

    String outFolder = OUTPUT_FOLDER + File.separator + OUTPUT_PROCESSED_FOLDER + File.separator;
    String outFile = "";
    String combinedOutFile = "";
    String inFile = "";

    BufferedReader replica;
    BufferedWriter out;
    BufferedWriter combinedOut;
    
    for (double interval : intervals) {
      for (int lambda :lambdas) {

        double totalReplicaUtilization = 0.0;
        double totalReplicaPacketDelay = 0.0;
        double totalReplicaPacketsInSystem = 0.0;

        List<Double> replicaAvgPacketDelayList = new ArrayList<Double>();
        List<Double> replicaAvgPacketsInSystemList = new ArrayList<Double>();

        for (int i = 0; i < NUMBER_OF_REPLICAS; i++) {
          outFile = outFolder + "sim-" + lambda + "-" + mu + "-" + interval + File.separator + "replica-result-" + i + ".csv";

          inFile = inFolder + "sim-" + lambda + "-" + mu + "-" + interval + File.separator + "replica-" + i + FILE_EXT;
          replica = new BufferedReader(new FileReader(new File(inFile)));

          replica.readLine(); // skip headers

          String line;

          double totalUtilization = 0.0;
          double totalPacketDelay = 0.0;
          double totalPacketsInSystem = 0.0;

          double count = 0;

          double max = 0;
          while ((line = replica.readLine()) != null) {


            String[] data = line.split(",");

            Double utilDouble = Double.parseDouble(data[2]);
            totalUtilization += utilDouble;

            Double packetDelay = Double.parseDouble(data[0]);
            totalPacketDelay += packetDelay;

            if (packetDelay > max) {
              max = packetDelay;
            }
            Double numOfPackets = Double.parseDouble(data[1]);
            totalPacketsInSystem += numOfPackets;

            count += 1;
          }

          double avgUtilization = totalUtilization / count;
          double avgPacketDelay = totalPacketDelay / count;

          double avgPacketsInSystem = totalPacketsInSystem / count;

          replicaAvgPacketsInSystemList.add(avgPacketsInSystem);
          replicaAvgPacketDelayList.add(avgPacketDelay);

          replica.close();

          // OUTPUT HERE
          // out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), "utf-8"));

          // out.write("Utilization,Average Packet Delay,Packets in System");
          // out.newLine();

          // for (int utilKey : utilMap.keySet()) {
          //   out.write(utilKey + "," + utilMap.get(utilKey).getCSV(false));
          //   out.newLine();
          // }

          // out.close();
          // System.out.println("Finished with: " + outFile);

          totalReplicaUtilization += avgUtilization;
          totalReplicaPacketDelay += avgPacketDelay;
          totalReplicaPacketsInSystem += avgPacketsInSystem;
        }

        double avgReplicaUtilization = totalReplicaUtilization / NUMBER_OF_REPLICAS;

        double avgReplicaPacketDelay = totalReplicaPacketDelay / NUMBER_OF_REPLICAS;
        double avgReplicaPacketsInSystem = totalReplicaPacketsInSystem / NUMBER_OF_REPLICAS;

        // CALCULATE CONFIDENCE HERE:

        double sumPacketsInSystem = 0.0;
        double sumPacketDelay = 0.0;
        for (int j = 0; j < NUMBER_OF_REPLICAS; j++) {
          sumPacketsInSystem += Math.pow(replicaAvgPacketsInSystemList.get(j) - avgReplicaPacketsInSystem, 2);
          sumPacketDelay += Math.pow(replicaAvgPacketDelayList.get(j) - avgReplicaPacketDelay, 2);
        }

        double ssdPacketsInSystem = Math.sqrt(sumPacketsInSystem / (NUMBER_OF_REPLICAS - 1));
        double ssdPacketDelay = Math.sqrt(sumPacketDelay / (NUMBER_OF_REPLICAS - 1));

        double packetsInSystemCI = (T_STATISTIC_19_0975 * ssdPacketsInSystem) / Math.sqrt(NUMBER_OF_REPLICAS - 1);
        double packetDelayCI = (T_STATISTIC_19_0975 * ssdPacketDelay) / Math.sqrt(NUMBER_OF_REPLICAS - 1);

       System.out.println(outFile + 
               "," + avgReplicaUtilization + 
               "," + avgReplicaPacketDelay + 
               "," + packetDelayCI +
               "," + avgReplicaPacketsInSystem +
               "," + packetsInSystemCI); 
      }
    }
  }


  public void runOccupencies() throws IOException {

    System.out.println("File,Number of Packets,Number of Occurences,Confidence Interval");

    String inFolder = OUTPUT_FOLDER + File.separator + INPUT_SIMS_FOLDER + File.separator;

    String outFolder = OUTPUT_FOLDER + File.separator + OUTPUT_PROCESSED_FOLDER + File.separator;
    String outFile = "";
    String combinedOutFile = "";
    String inFile = "";

    BufferedReader replica;
    BufferedWriter out;
    
    for (double interval : intervals) {
      for (int lambda :lambdas) {


        // TODO: add replica here
        List<Map<Long, Double>> packetOccurencesList = new ArrayList<Map<Long, Double>>();

        for (int i = 0; i < NUMBER_OF_REPLICAS; i++) {
          outFile = outFolder + "sim-" + lambda + "-" + mu + "-" + interval + File.separator + "replica-result-" + i + ".csv";

          inFile = inFolder + "sim-" + lambda + "-" + mu + "-" + interval + File.separator + "replica-" + i + FILE_EXT;
          replica = new BufferedReader(new FileReader(new File(inFile)));

          replica.readLine(); // skip headers

          String line;

          // Map of <# of packets, occurence>
          Map<Long, Double> packetOccurences = new HashMap<Long, Double>();
          while ((line = replica.readLine()) != null) {


            String[] data = line.split(",");

            Long numOfPackets = Long.parseLong(data[1]);
            if (packetOccurences.containsKey(numOfPackets)) {
              packetOccurences.put(numOfPackets, packetOccurences.get(numOfPackets) + 1.0);
            } else {
              packetOccurences.put(numOfPackets, 1.0);
            }
          }

          // 1 Replica done
          packetOccurencesList.add(packetOccurences);
        }

        // Create Bins
        Map<Long, Double> replicasPacketOccurences = new HashMap<Long, Double>();
        for (long j = 0; j < 20; j++) {
          
          double count = 0;
          for (Map<Long, Double> po : packetOccurencesList) {
            if (po.containsKey(j)) {
              count += po.get(j);            
            }
          }
          replicasPacketOccurences.put(j, count);
        }

        // Calculate Confidence Intervals

        Map<Long, Double> confidenceIntervals = new HashMap<Long, Double>();
        for (long j = 0; j < 20; j++) {

          double meanOccurrence = replicasPacketOccurences.get(j) / NUMBER_OF_REPLICAS;

          double sum = 0.0;
          for (Map<Long, Double> po : packetOccurencesList) {
            if (po.containsKey(j)) {
              sum += Math.pow(po.get(j) - meanOccurrence,2);
            }
          }

          double s = Math.sqrt(sum / (NUMBER_OF_REPLICAS - 1));

          double ci = T_STATISTIC_19_0975 * s / Math.sqrt(NUMBER_OF_REPLICAS - 1);
          confidenceIntervals.put(j, ci);
        }

        System.out.println(outFile);

        for (Map.Entry<Long, Double> entry : replicasPacketOccurences.entrySet()) {
          double avgOccurences = entry.getValue() / NUMBER_OF_REPLICAS;
          System.out.println(entry.getKey() + "," 
            + avgOccurences + ","
            + confidenceIntervals.get(entry.getKey()));
        }

        System.out.println("--------------------------------------------------");
      }
    }

  }
}