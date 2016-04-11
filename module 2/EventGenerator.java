/**
 * Event Generator Interface
 * Random event generator.
 *
 * @author Kevin Rosengren, Ian Wong, Nikola Neskovic
 * @version 10/04/16
 */
public interface EventGenerator {
  double nextArrivalTime();
  double nextServiceTime();
}
