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
  private static final String DEPARTURE_EVENT = "Departure";

  private static final int ANY_QUEUE = 0;
  private static final int QUEUE_ONE = 1;
  private static final int QUEUE_TWO = 2;

  private Random random;

  /** probabilities of staying in the system **/
  private double p;
  private double q;


  /** generates new arrival times and service times **/
  private EventGenerator eventGenerator;

  /** Future Event list (set) ordered by event time **/
  private TreeSet<Event> futureEventList;

  /** List of collected statistics **/
  private List<Statistic> statistics;

  /** first customer queue **/
  private Queue<Double> queue_one;

  /** second customer queue **/
  private Queue<Double> queue_two;

  /** total number of departures **/
  private long numberOfDeparturesFromServerOne;
  private long numberOfDeparturesFromServerTwo;
  private long totalNumberOfDepartures;

  /** total number of arrivals **/
  private long numberOfArrivals;

  /** current clock time **/
  private double clock;

  /** Server in use **/
  private boolean queue_one_is_busy;
  private boolean queue_two_is_busy;

  private String outputFormat;

  private double currentStartTimeOfServerOne;
  private double currentStartTimeOfServerTwo;

  private double totalServerOneFreeTime;
  private double totalServerTwoFreeTime;

  private double delay;

  private double previousServiceTime;

  private double previousArrivalTime;

  private long numOfDataPoints;

  private Map<Integer, Long> frequencies;

  /**
   * SingleServerQueue
   *
   * @param eventGenerator for generating arrival times
   *        and service times
   */
  public NetworkFeedbackQueues(EventGenerator eventGenerator, double p, double q, long numOfDataPoints, String outputFormat) {
    this.eventGenerator = eventGenerator;
    this.numOfDataPoints = numOfDataPoints;

    futureEventList = new TreeSet<Event>(new EventComparator());
    statistics = new ArrayList<Statistic>();
    queue_one = new LinkedList<Double>();
    queue_two = new LinkedList<Double>();

    this.outputFormat = outputFormat;

    clock = 0.0;
    queue_one_is_busy = false;
    queue_two_is_busy = false;
    numberOfArrivals = 0;
    numberOfDeparturesFromServerOne = 0;
    numberOfDeparturesFromServerTwo = 0;
    currentStartTimeOfServerOne = 0.0;
    currentStartTimeOfServerTwo = 0.0;
    totalServerOneFreeTime = 0.0;
    totalServerTwoFreeTime = 0.0;

    delay = 0.0;
    previousArrivalTime = 0.0;
    previousServiceTime = 0.0;

    totalNumberOfDepartures = 0;

    random = new Random();
    this.p = p;
    this.q = q;

    frequencies = new HashMap<>();
    frequencies.put(5, new Long(0));
    frequencies.put(10, new Long(0));
    frequencies.put(15, new Long(0));
    frequencies.put(20, new Long(0));
    frequencies.put(25, new Long(0));
    frequencies.put(30, new Long(0));
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

      if (totalNumberOfDepartures >= numOfDataPoints) {
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

      }
    }
  }

  /**
   * initialConditions
   *
   * start the clock at the first arrival time and
   * setup departure of first event for queue one
   */
  private void initialConditions() throws IOException {

    // Set clock to first arrival time
    clock = eventGenerator.nextArrivalTime();

    double arrivalTime = clock;


    // Set LS(t) = 1
    queue_one_is_busy = true;
    totalServerOneFreeTime += clock;
    totalServerTwoFreeTime += clock;

    // Generate Service Time s*;
    // Schedule new Departure event
    // at time t + s*;
    double serviceTime = eventGenerator.nextServiceTime();
    futureEventList.add(new Event(QUEUE_ONE, DEPARTURE_EVENT, clock + serviceTime, serviceTime));

    serviceTime = eventGenerator.nextServiceTime();
    futureEventList.add(new Event(QUEUE_TWO, DEPARTURE_EVENT, clock + serviceTime, serviceTime));

    // Generate interarrival time a*;
    // Schedule next arrival event
    // at time t + a*;
    double nextArrivalTime = eventGenerator.nextArrivalTime();
    futureEventList.add(new Event(QUEUE_ONE, ARRIVAL_EVENT, clock + nextArrivalTime, nextArrivalTime));
    nextArrivalTime = eventGenerator.nextArrivalTime();
    futureEventList.add(new Event(QUEUE_TWO, ARRIVAL_EVENT, clock + nextArrivalTime, nextArrivalTime));

    numberOfArrivals += 1;

    // delay = Math.max(0, delay + previousArrivalTime + previousServiceTime - arrivalTime);

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
  private void arrivalEvent(Event event) throws IOException {

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
        totalServerOneFreeTime += clock - currentStartTimeOfServerOne;

        // Generate service time s*;
        // Schedule new Departure event
        // at time t + s*;
        serviceTime = eventGenerator.nextServiceTime();
        futureEventList.add(new Event(QUEUE_ONE, DEPARTURE_EVENT, clock + serviceTime, serviceTime));
      }

      // Generate interarrival time a*;
      // Schedule next arrival event
      // at time t + a*;

      double nextArrivalTime = eventGenerator.nextArrivalTime();
      futureEventList.add(new Event(QUEUE_ONE, ARRIVAL_EVENT, clock + nextArrivalTime, nextArrivalTime));

    } else { // QUEUE_TWO

      // Is LS_2(t) = 1?
      if (queue_two_is_busy) {

        // Increase LQ_2(t) by 1
        serviceTime = eventGenerator.nextServiceTime();
        queue_two.add(serviceTime);
      } else {

        // Set LS_2(t) = 1
        queue_two_is_busy = true;
        totalServerTwoFreeTime += clock - currentStartTimeOfServerTwo;

        // Generate service time s*;
        // Schedule new Departure event
        // at time t + s*;
        serviceTime = eventGenerator.nextServiceTime();
        futureEventList.add(new Event(QUEUE_TWO, DEPARTURE_EVENT, clock + serviceTime, serviceTime));
      }

      // Generate interarrival time a*;
      // Schedule next arrival event
      // at time t + a*;

      double nextArrivalTime = eventGenerator.nextArrivalTime();
      futureEventList.add(new Event(QUEUE_TWO, ARRIVAL_EVENT, clock + nextArrivalTime, nextArrivalTime));
    }

    numberOfArrivals += 1;

    // delay = Math.max(0, delay + previousArrivalTime + previousServiceTime - arrivalTime);

    // collectStatistics(); // FIXME: be sure to disable this when running actual simulation

    // previousArrivalTime = arrivalTime;
    // previousServiceTime = serviceTime;

    // Return control to time-advance
    // routine to continue simulation
  }

  /**
   * departureEvent
   *
   * simulate departure event at time t = clock
   */
  private void departureEvent(Event event) throws IOException {

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

        currentStartTimeOfServerOne = clock;
      }

      // Generate p*
      // Is p >= p*?
      if (p > getProbability()) {
        // Scehdule next arrival
        // event at time t for queue two
        futureEventList.add(new Event(QUEUE_TWO, ARRIVAL_EVENT, clock, event.serviceTime));
      }

      numberOfDeparturesFromServerOne += 1;

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

        currentStartTimeOfServerTwo = clock;
      }

      // Generate q*
      // Is q >= q*?
      if (q > getProbability()) {
        // Scehdule next arrival
        // event at time t for queue one
        futureEventList.add(new Event(QUEUE_ONE, ARRIVAL_EVENT, clock, event.serviceTime));
      }

      numberOfDeparturesFromServerTwo += 1;
    }

    totalNumberOfDepartures += 1;
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

    long size = queue_one.size();
    if (size <= 5) {
      frequencies.put(5, frequencies.get(5) + 1);
    } else if (size <= 10) {
      frequencies.put(10, frequencies.get(10) + 1);
    } else if (size <= 15) {
      frequencies.put(15, frequencies.get(15) + 1);
    } else if (size <= 20) {
      frequencies.put(20, frequencies.get(20) + 1);
    } else if (size <= 25) {
      frequencies.put(25, frequencies.get(25) + 1);
    } else if (size > 25) {
      frequencies.put(30, frequencies.get(30) + 1);
    }

    Statistic statisticOne = new Statistic(
      QUEUE_ONE,
      this.clock,
      new ArrayList(this.futureEventList), // clone
      this.numberOfDeparturesFromServerOne,
      this.queue_one.size(),
      queue_one_is_busy ? 1: 0,
      delay,
      outputFormat);

    Statistic statisticTwo = new Statistic(
      QUEUE_TWO,
      this.clock,
      new ArrayList(this.futureEventList), // clone
      this.numberOfDeparturesFromServerTwo,
      this.queue_two.size(),
      queue_two_is_busy ? 1: 0,
      delay,
      outputFormat);

    statistics.add(statisticTwo);
    statistics.add(statisticOne);
  }

  /**
   * getStatistics
   *
   * return list of gathered statistics
   */
  public List<Statistic> getStatistics() {
    return statistics;
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

    @Override
    public String toString() {
      return "(" + type + "; queue " + queue + "; " + df.format(time) + ")";
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
   */
  public static class Statistic {

    private DecimalFormat df;

    private String format;

    public int queue;
    public double clock;
    public List<Event> futureEventList;
    public long numberOfDepartures;
    public long queueSize;
    public double delay;
    public int serverInUse;

    public Statistic(int queue, double clock, List<Event> futureEventList,
      long numberOfDepartures, long queueSize, int serverInUse,
      double delay, String format) {
      this.queue = queue;
      this.clock = clock;
      this.futureEventList = futureEventList;
      this.numberOfDepartures = numberOfDepartures;
      this.queueSize = queueSize;
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
        return queue +
               "," + df.format(clock) +
               ",[" + fel + "]" +
               "," + numberOfDepartures +
               "," + queueSize +
               "," + serverInUse +
               "," + delay;
      } else if (format.equals("delay")) {
        long numberOfPackets = queueSize + serverInUse;
        return df.format(delay) +
               "," + numberOfPackets;
      } else {
        return "Queue: " + queue +
               "Clock: " + df.format(clock) +
               ", Future Event List: [" + fel + "]" +
               ", Number of Departures: " + numberOfDepartures +
               ", Queue Size: " + queueSize +
               ", Server in use: " + serverInUse +
               ", Delay: " + delay;
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


  public void printFrequencies() {
    System.out.println(frequencies.toString());
  }
}
