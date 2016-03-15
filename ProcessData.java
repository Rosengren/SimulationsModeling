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

  private static final double[] intervals = new double[]{0.1,0.01,0.5};
  private static final int[] lambdas = new int[]{1,3,5,7,9};
  private static final int mu = 10;
  private static final double xi = 0.7;

  public void run() throws IOException {

    String inFolder = OUTPUT_FOLDER + File.separator + INPUT_SIMS_FOLDER + File.separator;

    String outFolder = OUTPUT_FOLDER + File.separator + OUTPUT_PROCESSED_FOLDER + File.separator;
    String outFile = "";
    String combinedOutFile = "";
    String inFile = "";

    List<Map<Integer, Row>> utilMapList;
    Map<Integer, Row> utilMap;

    BufferedReader replica;
    BufferedWriter out;
    BufferedWriter combinedOut;

    for (int lambda :lambdas) {
      // lambda = 9;
      for (double interval : intervals) {
        // interval = 0.5;
        utilMapList = new ArrayList<Map<Integer, Row>>();

        for (int i = 0; i < 20; i++) {
          outFile = outFolder + "sim-" + lambda + "-" + mu + "-" + interval + File.separator + "replica-result-" + i + ".csv";

          inFile = inFolder + "sim-" + lambda + "-" + mu + "-" + interval + File.separator + "replica-" + i + FILE_EXT;
          replica = new BufferedReader(new FileReader(new File(inFile)));

          replica.readLine(); // skip headers

          utilMap = new HashMap<Integer, Row>();

          // fill map
          for (int j = 0; j < 101; j++) {
            utilMap.put(j, new Row());
          }

          String line;

          double totalUtilization = 0.0;
          double count = 0;
          while ((line = replica.readLine()) != null) {


            String[] data = line.split(",");


            // Double utilDouble = Double.parseDouble(data[2]) * 100;
            Double utilDouble = Double.parseDouble(data[2]);
            totalUtilization += utilDouble;
            count += 1;
            // Double packetDelay = Double.parseDouble(data[0]);
            // double numOfPackets = Double.parseDouble(data[1]);

            // int util = utilDouble.intValue();
            // utilMap.get(util).addBoth(packetDelay, numOfPackets);

          }

          double avgUtilization = totalUtilization / count;
          System.out.println("AVG Utilization = " + avgUtilization);

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

          // utilMapList.add(utilMap);
          // return;

        // }

        // TODO: Since we are gathering each replica,
        // output a new file with the average of all the replicas
        // Can probably also calculate the confidence intervals

        // Handle utilMapList Here

        // Map<Integer, Row> combinedResults = new HashMap<Integer, Row>();
        // for (int j = 0; j < 101; j++) {
        //   combinedResults.put(j, new Row());
        // }

        // for (int j = 0; j < 101; j++) {

        //   double packetSum = 0.0;
        //   double delaySum = 0.0;
        //   List<Double> packets = new ArrayList<Double>();
        //   List<Double> delays = new ArrayList<Double>();
        //   for (Map<Integer, Row> map : utilMapList) { // each replica

        //     Row rep = map.get(j);
        //     combinedResults.get(j).addBoth(rep.packetDelay, rep.packetsInSystem, rep.delayCount, rep.packetCount);

        //     if (rep.packetCount > 0) {
        //       packetSum += rep.packetsInSystem / rep.packetCount;
        //       packets.add(rep.packetsInSystem / rep.packetCount);
        //     } else {
        //       packets.add(0.0);
        //     }

        //     if (rep.delayCount > 0) {
        //       delaySum += rep.packetDelay / rep.delayCount;
        //       delays.add(rep.packetDelay / rep.delayCount);
        //     } else {
        //       delays.add(0.0);
        //     }
        //   }

        //   // calculate Packet Interval
        //   double packetAvg = packetSum / NUMBER_OF_REPLICAS;

        //   double packetSDSum = 0;
        //   for (double packet : packets) {
        //     packetSDSum += Math.pow(packet - packetAvg, 2); 
        //   }

        //   double pktSSD = Math.sqrt(packetSDSum / (NUMBER_OF_REPLICAS - 1));
        //   combinedResults.get(j).setPktInteval((T_STATISTIC_19_0975 * pktSSD) / Math.sqrt(NUMBER_OF_REPLICAS - 1));

          // System.out.println("Packets: " + packets.toString());
          // System.out.println("Packet Sum: " + packetSum + " packet average: " + packetAvg + " packetSDSum: " + packetSDSum
          //   + " pktSSD: " + pktSSD);


          // calculate delay Interval
          // double delayAvg = delaySum / NUMBER_OF_REPLICAS;

          // double delaySDSum = 0;
          // for (Double delay : delays) {
          //   delaySDSum += Math.pow(delay - delayAvg, 2);
          // }

          // double delaySSD = Math.sqrt(delaySDSum / (NUMBER_OF_REPLICAS - 1));
          // combinedResults.get(j).setDelayInterval((T_STATISTIC_19_0975  * delaySSD) / Math.sqrt(NUMBER_OF_REPLICAS - 1));
        
        }

        

        // combinedOutFile = outFolder + "sim-" + lambda + "-" + mu + "-" + interval + File.separator + "replica-result-combined-" + lambda + "-" + mu + "-" + interval + ".csv";
        // combinedOut = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(combinedOutFile), "utf-8"));

        // combinedOut.write("Utilization,Average Packet Delay,Avg Pkt Confidence,Packets in System,Pkt in System Confidence");
        // combinedOut.newLine();

        // for (int utilKey : combinedResults.keySet()) {
        //   combinedOut.write(utilKey + "," + combinedResults.get(utilKey).getCSV(true));
        //   combinedOut.newLine();
        // }

        // combinedOut.close();
        // System.out.println("Finished combining: " + combinedOutFile);
        return;
      }
    }
  }

  private class Row {

    public double packetsInSystem;
    public long packetCount;
    public double packetDelay;
    public long delayCount;
    public double pktInteval;
    public double delayInterval;

    public Row() {
      packetsInSystem = 0.0;
      packetDelay = 0.0;
      delayCount = 0;
      packetCount = 0;
      pktInteval = 0.0;
      delayInterval = 0.0;
    }

    public void addBoth(double packetDelay, double packetsInSystem) {
      this.packetDelay += packetDelay;
      this.packetsInSystem += packetsInSystem;
      this.delayCount += 1;
      this.packetCount += 1;
    }

    public void addBoth(double packetDelay, double packetsInSystem, long delayCount, long packetCount) {
      this.packetDelay += packetDelay;
      this.packetsInSystem += packetsInSystem;
      this.delayCount += delayCount;
      this.packetCount += packetCount;
    }

    public void setPktInteval(double interval) {
      pktInteval = interval;
    }

    public void setDelayInterval(double interval) {
      delayInterval = interval;
    }

    public String getCSV(boolean withSSD) {

      double avgPacketDelay = 0.0;
      double avgPacketCount = 0.0;

      if (delayCount > 0) {
        avgPacketDelay = packetDelay / delayCount;
      }

      if (packetCount > 0) {
        avgPacketCount = packetsInSystem / packetCount;
      }

      if (withSSD) {
        return avgPacketDelay + "," + delayInterval + ","
          + avgPacketCount + "," + pktInteval;
      } else {
        return avgPacketDelay + "," + packetsInSystem;
      }
    }
  }
}