import java.text.DecimalFormat;
import java.util.*;
import java.io.*;

/**
 * Random Number Generator
 * generates an output file containing a specified number
 * of random variables with a given interval.
 *
 * @author Kevin Rosengren, Ian Wong, Nikola Neskovic
 * @version 03/11/16
 */
public class RandomNumberGenerator {

  private static Random random;
  private static DecimalFormat df;

  public static void main(String[] args) throws Exception {
    if (args.length < 4) {
      System.out.println("Missing Parameters.\n" +
      "(1) lower limit\n" +
      "(2) upper limit\n" +
      "(3) number of random variables\n" +
      "(4) output file\n");
      return;
    }

    generate(Integer.parseInt(args[0]),
             Integer.parseInt(args[1]),
             Integer.parseInt(args[2]),
             args[3]);

  }

  public static void generate(int lower, int upper, int total, String outputFile) throws IOException {

    random = new Random();
    df = new DecimalFormat("#.#########");

    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "utf-8"));
 
    for (int i = 0; i < total; i++) {
      out.write(df.format(lower + (upper - lower) * random.nextDouble()));
      out.newLine();
    }

    out.close();
  }
}