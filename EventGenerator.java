import java.util.*;
import java.io.*;

/**
 * Event Generator Interface. The generator
 * reads two input files; inter arrival times and
 * service times.
 *
 * @author Kevin Rosengren, Ian Wong, Nikola Neskovic
 * @version 11/03/16
 */
public class EventGenerator {

  // Read the arrival time without moving the pointer
  // Used for queue setup
  double readArrivalTime() throws IOException;

  double nextArrivalTime() throws IOException;
  double nextServiceTime() throws IOException;

  // properly close the input file streams
  void close();
}
