import java.util.*;
import java.io.*;

/**
 * Implementation of an event generator. The generator
 * reads two input files; inter arrival times and 
 * service times.
 *
 * @author Kevin Rosengren, Ian Wong, Nikola Neskovic
 * @version 11/03/16
 */
public class EventGenerator {

  private BufferedReader interArrivalTimes = null;
  private BufferedReader serviceTimes = null;

  /**
   *
   * @param inter-arrival times input file 
   * @param service times input file 
   */
  public EventGenerator(String interArrivalTimesFile, String serviceTimesFile) throws IOException {
    interArrivalTimes = new BufferedReader(new FileReader(new File(interArrivalTimesFile)));
    serviceTimes = new BufferedReader(new FileReader(new File(serviceTimesFile)));
  }

  /**
   * nextArrivalTime
   * 
   * @return next arrival time in the inter-arrival input file.
   *         If the end of file is reached, negative infinity 
   *         is returned
   */
  public double nextArrivalTime() throws IOException {

    double nextTime = Double.NEGATIVE_INFINITY;
    String nextLine = "";

    if (interArrivalTimes != null) {
      if ((nextLine = interArrivalTimes.readLine()) != null) {
        nextTime = Double.parseDouble(nextLine);
      }
    } else {
      System.out.println("interArrivalTimes is null");
    }

    return nextTime;
  }

  /**
   * nextServiceTime
   * 
   * @return next service time in the service times input file.
   *         If the end of file is reached, negative infinity 
   *         is returned
   */
  public double nextServiceTime() throws IOException {

    double nextTime = Double.NEGATIVE_INFINITY;
    String nextLine = "";

    if (serviceTimes != null) {
      if ((nextLine = serviceTimes.readLine()) != null) {
        nextTime = Double.parseDouble(nextLine);
      }
    } else {
      System.out.println("serviceTimes is null");
    }

    return nextTime;
  }

  /**
   * close
   *
   * properly close the input file streams
   */
  public void close() {

    try {
      if (interArrivalTimes != null) {
        interArrivalTimes.close();
      }

      if (serviceTimes != null) {
        serviceTimes.close();
      }

    } catch (Exception e) {
      // ignore
    }
  }

}