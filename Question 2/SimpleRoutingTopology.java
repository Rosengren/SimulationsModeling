import java.text.DecimalFormat;
import java.util.*;
import java.io.*;

/**
 * Implementation of a Simple Routing Topology
 *
 * @author Kevin Rosengren, Ian Wong, Nikola Neskovic
 * @version 09/04/16
 */
public class SimpleRoutingTopology {

  /** Routing Strategies **/
  public static final String ROUND_ROBIN = "RR";
  public static final String SHORTEST_TOTAL_QUEUE = "STQ";

  private static final String ARRIVAL_EVENT = "Arrival";
  private static final String DEPARTURE_EVENT = "Departure";

  private static final int QUEUE_ONE = 1;
  private static final int QUEUE_TWO = 2;

  private Random random;

  /** generates new arrival times and service times **/
  private EventGenerator eventGenerator;

  /** Future Event list (set) ordered by event time **/
  private TreeSet<Event> futureEventList;

  /** customer queues **/
  private Queue<Double> queue_one;
  private Queue<Double> queue_two;

  /** total number of departures **/
  private long totalNumberOfDepartures;

  /** total number of arrivals **/
  private long numberOfArrivals;

  /** current clock time **/
  private double clock;

  /** Server in use **/
  private boolean queue_one_is_busy;
  private boolean queue_two_is_busy;

  private double[] delay;
  private double[] totalDelay;
  private double[] delayCount;
  private double[] previousServiceTime;
  private double[] previousArrivalTime;

  private long numOfDataPoints;

  private boolean arrivalUp;

  private String routingStrategy;

  /**
   * SingleServerQueue
   *
   * @param eventGenerator for generating arrival times
   *        and service times
   */
  public SimpleRoutingTopology(EventGenerator eventGenerator, long numOfDataPoints, String routingStrategy) {

    this.eventGenerator = eventGenerator;
    this.numOfDataPoints = numOfDataPoints;

    futureEventList = new TreeSet<Event>(new EventComparator());
    queue_one = new LinkedList<Double>();
    queue_two = new LinkedList<Double>();

    clock = 0.0;
    queue_one_is_busy = false;
    queue_two_is_busy = false;
    numberOfArrivals = 0;

    delay = new double[]{0.0, 0.0};
    totalDelay = new double[]{0.0, 0.0};
    delayCount = new double[]{0, 0};
    previousArrivalTime = new double[]{0.0, 0.0};
    previousServiceTime = new double[]{0.0, 0.0};

    totalNumberOfDepartures = 0;

    random = new Random();

    arrivalUp = false;

    if (routingStrategy.equals(ROUND_ROBIN) || routingStrategy.equals(SHORTEST_TOTAL_QUEUE)) {
      this.routingStrategy = routingStrategy;
    } else {
      System.out.println("Error: invalid routing strategy: " + routingStrategy);
      return;
    }
  }

  /**
   * run
   *
   * run the simulation.
   * simulation ends when an event in the
   * futureEventList has time t = -1
   */
  public void run() {

    initialConditions(); // start simulation

    Event nextEvent;
    while (!futureEventList.isEmpty()) {
      nextEvent = futureEventList.pollFirst(); // first element

      if (totalNumberOfDepartures >= numOfDataPoints) {
        // System.out.println("Finished simulation.");
        break;
      } else {
        // Advance clock to next event time
        clock = nextEvent.time;
      }

      if (nextEvent.type.equals(DEPARTURE_EVENT)) {
        departureEvent(nextEvent);

      } else if (nextEvent.type.equals(ARRIVAL_EVENT)) {
        arrivalEvent(nextEvent);

      }
    }
  }

  /**
   * initialConditions
   *
   * start the clock at the first arrival time and
   * setup departure of first event for queue one
   */
  private void initialConditions() {

    // Set clock to first arrival time
    clock = eventGenerator.nextArrivalTime();

    double arrivalTime = clock;

    // Set LS(t) = 1
    queue_one_is_busy = true;

    // Generate Service Time s*;
    // Schedule new Departure event
    // at time t + s*;
    double serviceTime = eventGenerator.nextServiceTime();
    futureEventList.add(new Event(QUEUE_ONE, DEPARTURE_EVENT, clock + serviceTime, serviceTime));

    // Generate interarrival time a*;
    // Schedule next arrival event
    // at time t + a*;
    double nextArrivalTime = eventGenerator.nextArrivalTime();
    futureEventList.add(new Event(QUEUE_ONE, ARRIVAL_EVENT, clock + nextArrivalTime, nextArrivalTime));

    numberOfArrivals += 1;

    updateDelays(QUEUE_ONE, arrivalTime, serviceTime);

    // Return control to time-advance
    // routine to continue simulation
  }

  /**
   * chooseQueue
   *
   * determines which queue the customer
   * should be placed in.
   *
   * @return queue number
   */
  private int chooseQueue() {

    if (routingStrategy.equals(SHORTEST_TOTAL_QUEUE)) {
      if (!queue_one_is_busy) {
        return QUEUE_ONE;
      } else if (!queue_two_is_busy) {
        return QUEUE_TWO;
      } else if (queue_one.size() <= queue_two.size()) {
        return QUEUE_ONE;
      } else {
        return QUEUE_TWO;
      }
    } else { // Round Robin
      if (arrivalUp) {
        arrivalUp = false;
        return QUEUE_ONE;
      } else {
        arrivalUp = true;
        return QUEUE_TWO;
      }
    }
  }

  /**
   * arrivalEvent
   *
   * simulate arrival event at time t = clock
   */
  private void arrivalEvent(Event event) {

    double serviceTime = 0.0;
    double arrivalTime = clock;

    if (event.queue == QUEUE_ONE) {

      // Is LS_1(t) = 1?
      if (queue_one_is_busy) {

        // Increase LQ_1(t) by 1
        serviceTime = eventGenerator.nextServiceTime();
        queue_one.add(serviceTime);
      } else {

        // Set LS_1(t) = 1
        queue_one_is_busy = true;

        // Generate service time s*;
        // Schedule new Departure event
        // at time t + s*;
        serviceTime = eventGenerator.nextServiceTime();
        futureEventList.add(new Event(QUEUE_ONE, DEPARTURE_EVENT, clock + serviceTime, serviceTime));
      }

      updateDelays(QUEUE_ONE, arrivalTime, serviceTime);

    } else { // QUEUE_TWO

      // Is LS_2(t) = 1?
      if (queue_two_is_busy) {

        // Increase LQ_2(t) by 1
        serviceTime = eventGenerator.nextServiceTime();
        queue_two.add(serviceTime);
      } else {

        // Set LS_2(t) = 1
        queue_two_is_busy = true;

        // Generate service time s*;
        // Schedule new Departure event
        // at time t + s*;
        serviceTime = eventGenerator.nextServiceTime();
        futureEventList.add(new Event(QUEUE_TWO, DEPARTURE_EVENT, clock + serviceTime, serviceTime));
      }

      updateDelays(QUEUE_TWO, arrivalTime, serviceTime);
    }


    // Generate interarrival time a*;
    // Schedule next arrival event
    // at time t + a*;
    double nextArrivalTime = eventGenerator.nextArrivalTime();
    futureEventList.add(new Event(chooseQueue(), ARRIVAL_EVENT, clock + nextArrivalTime, nextArrivalTime));

    numberOfArrivals += 1;

    // Return control to time-advance
    // routine to continue simulation
  }

  private void updateDelays(int i, double arrivalTime, double serviceTime) {
    i-=1;
    delay[i] = Math.max(0, delay[i] + previousArrivalTime[i] + previousServiceTime[i] - arrivalTime);
    totalDelay[i] += delay[i];
    delayCount[i] += 1;
    previousArrivalTime[i] = arrivalTime;
    previousServiceTime[i] = serviceTime;
  }

  /**
   * departureEvent
   *
   * simulate departure event at time t = clock
   */
  private void departureEvent(Event event) {

    // Which queue is the event for?
    if (event.queue == QUEUE_ONE) {

      // Is LQ(t) > 0?
      if (queue_one.size() > 0) {

        // Reduce LQ_1(t) by 1
        double serviceTime = queue_one.remove();

        // Generate service time s*;
        // Schedule new departure
        // event at time t + s*;
        futureEventList.add(new Event(QUEUE_ONE, DEPARTURE_EVENT, clock + serviceTime, serviceTime));

      } else { // LQ_1(t) <= 0

        // Set LS_1(t) = 0
        queue_one_is_busy = false;
      }

    } else { // QUEUE_TWO

      // Is LQ_2(t) > 0?
      if (queue_two.size() > 0) {

        // Reduce LQ_2(t) by 1
        double serviceTime = queue_two.remove();

        // Generate service time s*;
        // Schedule new departure
        // event at time t + s*;
        futureEventList.add(new Event(QUEUE_TWO, DEPARTURE_EVENT, clock + serviceTime, serviceTime));

      } else { // LQ_2(t) <= 0

        // Set LS_2(t) = 0
        queue_two_is_busy = false;
      }

    }

    totalNumberOfDepartures += 1;

    // Return control to time-advance
    // routine to continue simulation
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

    /** Time to service **/
    public double serviceTime;

    /** Which queue this event is for **/
    public int queue;

    public Event(int queue, String type, double time, double serviceTime) {
      this.type = type;
      this.time = time;
      this.serviceTime = serviceTime;
      this.queue = queue;

      df = new DecimalFormat("#.#########");
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

  public void printResults() {
    System.out.println("Average delay for queue 1: " + totalDelay[0] / delayCount[0]);
    System.out.println("Average delay for queue 2: " + totalDelay[1] / delayCount[1]);
  }
}
