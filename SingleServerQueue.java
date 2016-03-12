import java.util.*;
import java.io.*;

public class SingleServerQueue {

  /** generates new arrival times and service times **/
  private EventGenerator eventGenerator;

  /** Future Event List **/
  private List<Event> eventList;

  /** packets in the queue **/
  private Queue<Double> packetQueue;

  /** total number of departures **/
  private long numberOfDepartures;

  /** total number of arrivals **/
  private long numberOfArrivals;

  /** current clock time **/
  private double clock;

  /** Server in use **/
  private boolean isBusy;

  private double timeTillNextEvent;
  private double timeFree;
  private double totalServerFreeTime;
  private double totalWaitingTime;

  public SingleServerQueue(EventGenerator eventGenerator) {
    this.eventGenerator = eventGenerator;
    eventList = new ArrayList<Event>();
    packetQueue = new LinkedList<Double>();
    numberOfArrivals = 0;
    numberOfDepartures = 0;
    clock = 0.0;
    isBusy = false;
    timeTillNextEvent = 0.0;
    timeFree = 0.0;
    totalServerFreeTime = 0.0;
    totalWaitingTime = 0.0;
  }

  public void run() throws IOException {
    // initialize simulator

    // if event is Double.POSITIVE_INFINITY, then we've reached the end of the file

    arrivalEvent(); // start the simulation

    Event nextEvent;
    while (!eventList.isEmpty()) {
      nextEvent = eventList.remove(0); // first element

      if (nextEvent.type == "D") {

        if (nextEvent.time == -1) {
          System.out.println("Reached End of Service Time File");
          break;
        }

        departureEvent();
        System.out.println("New Departure Event");
      } else if (nextEvent.type == "A") {

        if (nextEvent.time == -1) {
          System.out.println("Reached End of Arrival Time File");
          break;
        }

        arrivalEvent();
        System.out.println("New Arrival Event");
      }
    }

    closeGenerator();
  }

  public String getResults() {
    return null;
    // TODO: return statistics
  }


  private void arrivalEvent() throws IOException {

    // Is LS(t) = 1 ?
    if (isBusy) {

      // Increase LQ(t) by 1
      packetQueue.add(clock);
    } else {
      // Set LS(t) = 1
      isBusy = true;

      // Generate Service Time s*;
      // Schedule new Departure event
      // at time t + s*;
      eventList.add(new Event("D", clock + eventGenerator.nextServiceTime()));
    }

    // Generate interarrival time a*;
    // Schedule next arrival event
    // at time t + a*;
    eventList.add(new Event("A", clock + eventGenerator.nextArrivalTime()));

    // Collect Statistics
    collectStatistics();

    // Return control to time-advance
    // routine to continue simulation
  }

  private void departureEvent() throws IOException {

    // Is LQ(t) > 0 ?
    if (packetQueue.size() > 0) {

      // Reduce LQ(t) by 1
      packetQueue.remove();

      // Generate service time s*;
      // Schedule new departure
      // event at time t + s*;
      eventList.add(new Event("D", clock + eventGenerator.nextServiceTime()));

    } else {

      // Set LS(t) = 0
      isBusy = false;
    }

    collectStatistics();

    // Return control to time-advance
    // routine to continue simulation
  }

  private void collectStatistics() {

  }

  private static class Event {

    /** Event Type **/
    public String type;

    /** Time to run event **/
    public double time; 

    public Event(String type, double time) {
      this.type = type;
      this.time = time;
    }
  }

  public void closeGenerator() {
    eventGenerator.close();
  }
}