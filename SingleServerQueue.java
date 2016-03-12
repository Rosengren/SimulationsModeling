import java.text.DecimalFormat;
import java.util.*;
import java.io.*;

public class SingleServerQueue {

  private static final String ARRIVAL_EVENT = "Arrival";
  private static final String DEPARTURE_EVENT = "Departure";

  /** generates new arrival times and service times **/
  private EventGenerator eventGenerator;

  /** Future Event list (set) ordered by event time **/
  private TreeSet<Event> futureEventList;

  /** List of collected statistics **/
  private List<Statistic> statistics;

  /** customer queue **/
  private Queue<Double> queue;

  /** total number of departures **/
  private long numberOfDepartures;

  /** total number of arrivals **/
  private long numberOfArrivals;

  /** total time server is in use **/
  private double totalServerUsageTime;

  /** current clock time **/
  private double clock;

  /** Server in use **/
  private boolean isBusy;


  /**
   * SingleServerQueue
   *
   * @param eventGenerator for generating arrival times
   *        and service times
   */
  public SingleServerQueue(EventGenerator eventGenerator) {
    this.eventGenerator = eventGenerator;

    futureEventList = new TreeSet<Event>(new EventComparator());
    statistics = new ArrayList<Statistic>();
    queue = new LinkedList<Double>();

    clock = 0.0;
    isBusy = false;
    numberOfArrivals = 0;
    numberOfDepartures = 0;
    totalServerUsageTime = 0.0;
  }

  /**
   * run
   *
   * run the simulation.
   * simulation ends when an event in the
   * futureEventList has time t = -1
   */
  public void run() throws IOException {

    arrivalEvent(); // start the simulation

    Event nextEvent;
    while (!futureEventList.isEmpty()) {
      nextEvent = futureEventList.pollFirst(); // first element

      if (nextEvent.time < 0) {
        System.out.println("Reached end of events list");
        break;
      } else {

        // Advance clock to next event time
        clock = nextEvent.time;
      }

      if (nextEvent.type.equals(DEPARTURE_EVENT)) {
        departureEvent();

      } else if (nextEvent.type.equals(ARRIVAL_EVENT)) {
        arrivalEvent();

      }
    }

    closeGenerator();
  }

  /**
   * arrivalEvent
   *
   * simulate arrival event at time t = clock
   */
  private void arrivalEvent() throws IOException {

    // Is LS(t) = 1 ?
    if (isBusy) {

      // Increase LQ(t) by 1
      queue.add(clock);
    } else {
      // Set LS(t) = 1
      isBusy = true;

      // Generate Service Time s*;
      // Schedule new Departure event
      // at time t + s*;
      double serviceTime = clock + eventGenerator.nextServiceTime();
      totalServerUsageTime += serviceTime;
      futureEventList.add(new Event(DEPARTURE_EVENT, serviceTime));
    }

    // Generate interarrival time a*;
    // Schedule next arrival event
    // at time t + a*;
    futureEventList.add(new Event(ARRIVAL_EVENT, clock + eventGenerator.nextArrivalTime()));

    numberOfArrivals += 1;
    collectStatistics();

    // Return control to time-advance
    // routine to continue simulation
  }

  /**
   * departureEvent
   *
   * simulate departure event at time t = clock
   */
  private void departureEvent() throws IOException {

    // Is LQ(t) > 0 ?
    if (queue.size() > 0) {

      // Reduce LQ(t) by 1
      queue.remove();

      // Generate service time s*;
      // Schedule new departure
      // event at time t + s*;
      futureEventList.add(new Event(DEPARTURE_EVENT, clock + eventGenerator.nextServiceTime()));

    } else {

      // Set LS(t) = 0
      isBusy = false;
    }

    numberOfDepartures += 1;
    collectStatistics();

    // Return control to time-advance
    // routine to continue simulation
  }

  /**
   * collectStatistics
   *
   * collect and store statistics
   * at the current clock time of
   * the simulation
   */
  private void collectStatistics() {

    double serverUtilization = 0.0;

    if (clock != 0) {
      // Server Utilization = Time server is busy / total running time
      serverUtilization = totalServerUsageTime / clock;
    }


    Statistic statistic = new Statistic(
      this.clock,
      new ArrayList(this.futureEventList), // clone
      this.numberOfDepartures,
      this.queue.size(),
      serverUtilization);

    // System.out.println("Gathering Statistics: " + statistic.toString());
    statistics.add(statistic);
  }

  /**
   * getStatistics
   *
   * return list of gathered statistics
   */  
  public List<Statistic> getStatistics() {
    return statistics;
  }


  /**
   * Event
   *
   * object representing an event
   * @param type of event (ex: Arrival, Departure)
   * @param time that event takes place
   */
  private static class Event {

    private DecimalFormat df;

    /** Event Type **/
    public String type;

    /** Time to run event **/
    public double time; 

    public Event(String type, double time) {
      this.type = type;
      this.time = time;

      df = new DecimalFormat("#.#########");
    }

    @Override
    public String toString() {
      return "(" + type + ", " + df.format(time) + ")";
    }
  }

  public static class Statistic {

    private DecimalFormat df;

    public double clock;
    public List<Event> futureEventList;
    public long numberOfDepartures;
    public long queueSize;
    public double serverUtilization;

    public Statistic(double clock, List<Event> futureEventList,
      long numberOfDepartures, long queueSize, double serverUtilization) {
      this.clock = clock;
      this.futureEventList = futureEventList;
      this.numberOfDepartures = numberOfDepartures;
      this.queueSize = queueSize;
      this.serverUtilization = serverUtilization;

      df = new DecimalFormat("#.#########");
    }

    @Override
    public String toString() {
      String fel = "";
      for (Event e : futureEventList) {
        fel += e.toString() + ", ";
      }
      return "Clock: " + df.format(clock) + 
             ", Future Event List: [" + fel + "]" + 
             ", Number of Departures: " + numberOfDepartures +
             ", Queue Size: " + queueSize +
             ", Server Utilization: " + df.format(serverUtilization);
    }
  }

  private static class EventComparator implements Comparator<Event> {
    public int compare(Event one, Event two) {
      return Double.compare(one.time, two.time);
    }
  }

  /**
   * closeGenerator
   *
   * close file input streams
   */
  public void closeGenerator() {
    eventGenerator.close();
  }
}