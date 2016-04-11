import java.text.DecimalFormat;
import java.util.*;
import java.io.*;

/**
 * Implementation of a Network Feedback Queue
 *
 * @author Kevin Rosengren, Ian Wong, Nikola Neskovic
 * @version 09/04/16
 */
public class NetworkFeedbackQueues {

  private static final String ARRIVAL_EVENT = "Arrival";
  private static final String FEEDBACK_EVENT = "Feedback";
  private static final String DEPARTURE_EVENT = "Departure";

  private static final int QUEUE_ONE = 0;
  private static final int QUEUE_TWO = 1;

  /** random number generator for probabilities **/
  private Random random;

  /** probabilities of staying in the system **/
  private double p, q;

  /** generates new arrival times and service times **/
  private EventGenerator eventGenerator_one;
  private EventGenerator eventGenerator_two;

  /** Future Event list (set) ordered by event time **/
  private TreeSet<Event> futureEventList;

  /** customer queues **/
  private Queue<Double> queue_one;
  private Queue<Double> queue_two;

  /** current clock time **/
  private double clock;

  /** Server in use **/
  private boolean queue_one_is_busy;
  private boolean queue_two_is_busy;

  /** How many departures before exiting **/
  private long numberOfDepartures;

  /** number of departures so far **/
  private long totalNumberOfDepartures;

  private Map<Integer, Long> queue_one_histogram;
  private Map<Integer, Long> queue_two_histogram;
  private int[] bins = {5, 10, 15, 20, 25, 30};

  /** used to calculate delays **/
  private double[] delay;
  private double[] totalDelay;
  private double[] delayCount;
  private double[] previousServiceTime;
  private double[] previousArrivalTime;

  /**
   * SingleServerQueue
   *
   * @param eventGenerator for generating arrival times
   *        and service times
   */
  public NetworkFeedbackQueues(EventGenerator eventGenerator_one,
      EventGenerator eventGenerator_two, double p, double q, long numberOfDepartures) {
    this.eventGenerator_one = eventGenerator_one;
    this.eventGenerator_two = eventGenerator_two;
    this.numberOfDepartures = numberOfDepartures;

    futureEventList = new TreeSet<Event>(new EventComparator());
    queue_one = new LinkedList<Double>();
    queue_two = new LinkedList<Double>();

    clock = 0.0;
    queue_one_is_busy = false;
    queue_two_is_busy = false;

    delay = new double[]{0.0, 0.0};
    totalDelay = new double[]{0.0, 0.0};
    delayCount = new double[]{0, 0};
    previousArrivalTime = new double[]{0.0, 0.0};
    previousServiceTime = new double[]{0.0, 0.0};

    totalNumberOfDepartures = 0;

    random = new Random();
    this.p = p;
    this.q = q;

    queue_one_histogram = new HashMap<>();
    queue_two_histogram = new HashMap<>();

    // Create bins in hashmap
    for (int i = 0; i < bins.length; i++) {
      queue_one_histogram.put(bins[i], new Long(0));
      queue_two_histogram.put(bins[i], new Long(0));
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

      if (totalNumberOfDepartures >= numberOfDepartures) {
        System.out.println("Finished simulation.");
        break;
      } else {
        // Advance clock to next event time
        clock = nextEvent.time;
      }

      if (nextEvent.type.equals(DEPARTURE_EVENT)) {
        departureEvent(nextEvent);
      } else if (nextEvent.type.equals(ARRIVAL_EVENT)) {
        arrivalEvent(nextEvent);
      } else if (nextEvent.type.equals(FEEDBACK_EVENT)) {
        feedbackEvent(nextEvent);
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
    clock = eventGenerator_one.nextArrivalTime();

    double arrivalTime = clock;

    // Set LS(t) = 1
    queue_one_is_busy = true;
    queue_two_is_busy = true;

    // Generate Service Time s*;
    // Schedule new Departure event
    // at time t + s*;
    double serviceTime = eventGenerator_one.nextServiceTime();
    futureEventList.add(new Event(QUEUE_ONE, DEPARTURE_EVENT, clock + serviceTime, serviceTime));
    updateDelays(QUEUE_ONE, arrivalTime, serviceTime);

    serviceTime = eventGenerator_two.nextServiceTime();
    futureEventList.add(new Event(QUEUE_TWO, DEPARTURE_EVENT, clock + serviceTime, serviceTime));
    updateDelays(QUEUE_TWO, arrivalTime, serviceTime);

    // Generate interarrival time a*;
    // Schedule next arrival event
    // at time t + a*;
    double nextArrivalTime = eventGenerator_one.nextArrivalTime();
    futureEventList.add(new Event(QUEUE_ONE, ARRIVAL_EVENT, clock + nextArrivalTime, nextArrivalTime));

    nextArrivalTime = eventGenerator_two.nextArrivalTime();
    futureEventList.add(new Event(QUEUE_TWO, ARRIVAL_EVENT, clock + nextArrivalTime, nextArrivalTime));

    collectStatistics();

    // Return control to time-advance
    // routine to continue simulation
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
        serviceTime = eventGenerator_one.nextServiceTime();
        queue_one.add(serviceTime);
      } else {

        // Set LS_1(t) = 1
        queue_one_is_busy = true;

        // Generate service time s*;
        // Schedule new Departure event
        // at time t + s*;
        serviceTime = eventGenerator_one.nextServiceTime();
        futureEventList.add(new Event(QUEUE_ONE, DEPARTURE_EVENT, clock + serviceTime, serviceTime));
      }

      // Generate interarrival time a*;
      // Schedule next arrival event
      // at time t + a*;
      double nextArrivalTime = eventGenerator_one.nextArrivalTime();
      futureEventList.add(new Event(QUEUE_ONE, ARRIVAL_EVENT, clock + nextArrivalTime, nextArrivalTime));

      updateDelays(QUEUE_ONE, arrivalTime, serviceTime);

    } else { // QUEUE_TWO

      // Is LS_2(t) = 1?
      if (queue_two_is_busy) {

        // Increase LQ_2(t) by 1
        serviceTime = eventGenerator_two.nextServiceTime();
        queue_two.add(serviceTime);
      } else {

        // Set LS_2(t) = 1
        queue_two_is_busy = true;

        // Generate service time s*;
        // Schedule new Departure event
        // at time t + s*;
        serviceTime = eventGenerator_two.nextServiceTime();
        futureEventList.add(new Event(QUEUE_TWO, DEPARTURE_EVENT, clock + serviceTime, serviceTime));
      }

      // Generate interarrival time a*;
      // Schedule next arrival event
      // at time t + a*;
      double nextArrivalTime = eventGenerator_two.nextArrivalTime();
      futureEventList.add(new Event(QUEUE_TWO, ARRIVAL_EVENT, clock + nextArrivalTime, nextArrivalTime));

      updateDelays(QUEUE_TWO, arrivalTime, serviceTime);
    }

    // Return control to time-advance
    // routine to continue simulation
  }

  /**
   * feedbackEvent
   *
   * simulate feedback event at time t = clock
   */
  private void feedbackEvent(Event event) {

    double serviceTime = 0.0;
    double arrivalTime = clock;

    if (event.queue == QUEUE_ONE) {

      // Is LS_1(t) = 1?
      if (queue_one_is_busy) {

        // Increase LQ_1(t) by 1
        serviceTime = eventGenerator_one.nextServiceTime();
        queue_one.add(serviceTime);
      } else {

        // Set LS_1(t) = 1
        queue_one_is_busy = true;

        // Generate service time s*;
        // Schedule new Departure event
        // at time t + s*;
        serviceTime = eventGenerator_one.nextServiceTime();
        futureEventList.add(new Event(QUEUE_ONE, DEPARTURE_EVENT, clock + serviceTime, serviceTime));
      }

    } else { // QUEUE_TWO

      // Is LS_2(t) = 1?
      if (queue_two_is_busy) {

        // Increase LQ_2(t) by 1
        serviceTime = eventGenerator_two.nextServiceTime();
        queue_two.add(serviceTime);
      } else {

        // Set LS_2(t) = 1
        queue_two_is_busy = true;

        // Generate service time s*;
        // Schedule new Departure event
        // at time t + s*;
        serviceTime = eventGenerator_two.nextServiceTime();
        futureEventList.add(new Event(QUEUE_TWO, DEPARTURE_EVENT, clock + serviceTime, serviceTime));
      }
    }

    // Return control to time-advance
    // routine to continue simulation
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

      // Generate p*
      // Is p >= p*?
      if (p > getProbability()) {
        // Scehdule next arrival
        // event at time t for queue two
        futureEventList.add(new Event(QUEUE_TWO, FEEDBACK_EVENT, clock, event.serviceTime));

        updateDelays(QUEUE_TWO, clock, event.serviceTime);
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

      // Generate q*
      // Is q >= q*?
      if (q > getProbability()) {
        // Scehdule next arrival
        // event at time t for queue one
        futureEventList.add(new Event(QUEUE_ONE, FEEDBACK_EVENT, clock, event.serviceTime));

        updateDelays(QUEUE_ONE, clock, event.serviceTime);
      }
    }

    totalNumberOfDepartures += 1;
    collectStatistics();

    // Return control to time-advance
    // routine to continue simulation
  }

  /**
   * collectStatistics
   *
   * collect the queue size frequency
   * at the current clock time of
   * the simulation
   */
  private void collectStatistics() {

    for (int i = 0; i < bins.length - 1; i++) {
      if (queue_one.size() <= bins[i]) {
        queue_one_histogram.put(bins[i], queue_one_histogram.get(bins[i]) + 1);
        break;
      }
    }

    for (int i = 0; i < bins.length - 1; i++) {
      if (queue_two.size() <= bins[i]) {
        queue_two_histogram.put(bins[i], queue_two_histogram.get(bins[i]) + 1);
        break;
      }
    }

    if (queue_one.size() > bins[bins.length - 1]) {
      queue_one_histogram.put(bins[bins.length - 1], queue_one_histogram.get(bins[bins.length - 1]) + 1);
    }

    if (queue_two.size() > bins[bins.length - 1]) {
      queue_two_histogram.put(bins[bins.length - 1], queue_two_histogram.get(bins[bins.length - 1]) + 1);
    }
  }

  private double getProbability() {
    return random.nextDouble();
  }

  /**
   * Event
   *
   * Object representing an event
   * @param type of event (ex: Arrival, Departure)
   * @param time that event takes place
   */
  private static class Event {

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

  public void printHistogramQueues() {
  }

  public void printResults() {
    // Histogram Queues
    System.out.println("QUEUE ONE: " + queue_one_histogram.toString());
    System.out.println("QUEUE TWO: " + queue_two_histogram.toString());

    // Delays
    System.out.println("Average delay for queue 1: " + totalDelay[0] / delayCount[0]);
    System.out.println("Average delay for queue 2: " + totalDelay[1] / delayCount[1]);
  }

  private void updateDelays(int i, double arrivalTime, double serviceTime) {
    delay[i] = Math.max(0, delay[i] + previousArrivalTime[i] + previousServiceTime[i] - arrivalTime);
    totalDelay[i] += delay[i];
    delayCount[i] += 1;
    previousArrivalTime[i] = arrivalTime;
    previousServiceTime[i] = serviceTime;
  }
}
