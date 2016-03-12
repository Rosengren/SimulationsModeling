import java.util.*;
import java.io.*;

public class EventGenerator {

  private BufferedReader interArrivalTimes = null;
  private BufferedReader serviceTimes = null;

  public EventGenerator(String interArrivalTimesFile, String serviceTimesFile) throws IOException {
    interArrivalTimes = new BufferedReader(new FileReader(new File(interArrivalTimesFile)));
    serviceTimes = new BufferedReader(new FileReader(new File(serviceTimesFile)));
  }

  public double nextArrivalTime() throws IOException {

    double nextTime = -1;
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

  public double nextServiceTime() throws IOException {

    double nextTime = -1;
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