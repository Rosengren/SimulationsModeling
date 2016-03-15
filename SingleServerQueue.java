import java.text.DecimalFormat;
import java.util.*;
import java.io.*;

/**
 * Implementation of a Single Server Simulation
 * with a First-In-First-Out Queue
 *
 * @author Kevin Rosengren, Ian Wong, Nikola Neskovic
 * @version 11/03/16
 */
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

  /** current clock time **/
  private double clock;

  /** Server in use **/
  private boolean isBusy;

  private String outputFormat;

  private double currentStartTime;

  private double totalServerFreeTime;

  private double delay;

  private double previousServiceTime;

  private double previousArrivalTime;


  /**
   * SingleServerQueue
   *
   * @param eventGenerator for generating arrival times
   *        and service times
   */
  public SingleServerQueue(EventGenerator eventGenerator, String outputFormat) {
    this.eventGenerator = eventGenerator;

    futureEventList = new TreeSet<Event>(new EventComparator());
    statistics = new ArrayList<Statistic>();
    queue = new LinkedList<Double>();

    this.outputFormat = outputFormat;

    clock = 0.0;
    isBusy = false;
    numberOfArrivals = 0;
    numberOfDepartures = 0;
    currentStartTime = 0.0;
    totalServerFreeTime = 0.0;

    delay = 0.0;
    previousArrivalTime = 0.0;
    previousServiceTime = 0.0;
  }

  /**
   * run
   *
   * run the simulation.
   * simulation ends when an event in the
   * futureEventList has time t = -1
   */
  public void run() throws IOException {

    initialConditions(); // start simulation

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
   * initialConditions
   *
   * start the clock at the first arrival time
   * and setup departure of first event
   */
  private void initialConditions() throws IOException {

    // Set clock to first arrival time
    clock = eventGenerator.nextArrivalTime();

    double arrivalTime = clock;


    // Set LS(t) = 1
    isBusy = true;
    totalServerFreeTime += clock;

    // Generate Service Time s*;
    // Schedule new Departure event
    // at time t + s*;
    double serviceTime = eventGenerator.nextServiceTime();
    futureEventList.add(new Event(DEPARTURE_EVENT, clock + serviceTime));

    // Generate interarrival time a*;
    // Schedule next arrival event
    // at time t + a*;
    futureEventList.add(new Event(ARRIVAL_EVENT, clock + eventGenerator.nextArrivalTime()));

    numberOfArrivals += 1;

    delay = Math.max(0, delay + previousArrivalTime + previousServiceTime - arrivalTime);

    collectStatistics();

    previousArrivalTime = arrivalTime;
    previousServiceTime = serviceTime;

    // Return control to time-advance
    // routine to continue simulation
  }

  /**
   * arrivalEvent
   *
   * simulate arrival event at time t = clock
   */
  private void arrivalEvent() throws IOException {

    double serviceTime = 0.0;
    double arrivalTime = clock;

    // Is LS(t) = 1 ?
    if (isBusy) {

      // Increase LQ(t) by 1
      serviceTime = eventGenerator.nextServiceTime();
      queue.add(serviceTime);
    } else {

      // Set LS(t) = 1
      isBusy = true;
      totalServerFreeTime += clock - currentStartTime;

      // Generate Service Time s*;
      // Schedule new Departure event
      // at time t + s*;
      serviceTime = eventGenerator.nextServiceTime();
      futureEventList.add(new Event(DEPARTURE_EVENT, clock + serviceTime));
    }

    // Generate interarrival time a*;
    // Schedule next arrival event
    // at time t + a*;
    futureEventList.add(new Event(ARRIVAL_EVENT, clock + eventGenerator.nextArrivalTime()));

    numberOfArrivals += 1;

    delay = Math.max(0, delay + previousArrivalTime + previousServiceTime - arrivalTime);

    // collectStatistics();

    previousArrivalTime = arrivalTime;
    previousServiceTime = serviceTime;

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
      double serviceTime = queue.remove();

      // Generate service time s*;
      // Schedule new departure
      // event at time t + s*;
      futureEventList.add(new Event(DEPARTURE_EVENT, clock + serviceTime));

    } else {

      // Set LS(t) = 0
      isBusy = false;

      currentStartTime = clock;
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
      serverUtilization = (clock - totalServerFreeTime) / clock;
    }


    Statistic statistic = new Statistic(
      this.clock,
      new ArrayList(this.futureEventList), // clone
      this.numberOfDepartures,
      this.queue.size(),
      serverUtilization,
      isBusy ? 1: 0,
      delay,
      outputFormat);

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
   * Object representing an event
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
      return "(" + type + "; " + df.format(time) + ")";
    }
  }

  /**
   * Statistic
   *
   * Statistics of the simulation
   * at a given clock time.
   * @param current time
   * @param future event list
   * @param number of departures
   * @param size of customer queue
   * @param server utilization
   */
  public static class Statistic {

    private DecimalFormat df;

    private String format;

    public double clock;
    public List<Event> futureEventList;
    public long numberOfDepartures;
    public long queueSize;
    public double serverUtilization;
    public double delay;
    public int serverInUse;

    public Statistic(double clock, List<Event> futureEventList,
      long numberOfDepartures, long queueSize, double serverUtilization, int serverInUse,
      double delay, String format) {
      this.clock = clock;
      this.futureEventList = futureEventList;
      this.numberOfDepartures = numberOfDepartures;
      this.queueSize = queueSize;
      this.serverUtilization = serverUtilization;
      this.serverInUse = serverInUse;
      this.delay = delay;
      this.format = format;

      df = new DecimalFormat("#.#########");
    }

    @Override
    public String toString() {
      String fel = "";
      for (Event e : futureEventList) {
        fel += e.toString() + "; ";
      }

      if (format.equals("csv") || format.equals("csv-no-header")) {
        return df.format(clock) +
               ",[" + fel + "]" + 
               "," + numberOfDepartures +
               "," + queueSize +
               "," + serverInUse +
               "," + delay +
               "," + df.format(serverUtilization);
      } else if (format.equals("delay")) {
        long numberOfPackets = queueSize + serverInUse;
        return df.format(delay) +
               "," + numberOfPackets +
               "," + df.format(serverUtilization);
      } else {
        return "Clock: " + df.format(clock) + 
               ", Future Event List: [" + fel + "]" + 
               ", Number of Departures: " + numberOfDepartures +
               ", Queue Size: " + queueSize +
               ", Server in use: " + serverInUse +
               ", Delay: " + delay +
               ", Server Utilization: " + df.format(serverUtilization);
      }
    }
  }

  /**
   * EventComparator
   *
   * Used for sorting events by time
   */
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