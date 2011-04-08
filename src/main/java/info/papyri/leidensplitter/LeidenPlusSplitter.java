package info.papyri.leidensplitter;

import java.io.Reader;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * @author Hugh Cayless
 *
 * LeidenPlusSplitter takes a Leiden+ style edition div and partitions it into
 * well-formed fragments of N lines each (plus a remainder).
 */
public class LeidenPlusSplitter {

  private static Map<String, String> tokens = new HashMap<String, String>();
  private static Map<String, String> tokensRev = new HashMap<String, String>();
  private static Pattern linenum = Pattern.compile("^\\s*[0-9]+[/\\\\,a-zA-Z0-9]*[ms0-9]*\\.");
  private int splitOn = 20;

  public LeidenPlusSplitter() {
    init();
  }

  public LeidenPlusSplitter(int chunkSize) {
    init();
    splitOn = chunkSize;
  }

  private void init() {
    tokens.put("<S=", "open-div.edition");
    tokens.put("<D=", "open-div");
    tokens.put("=D>", "close-div");
    tokens.put("<=", "open-ab");
    tokens.put("=>", "close-ab");
    tokens.put("<:", "open-app");
    tokens.put(":>", "close-app");
    tokens.put("|_", "open-supplied.undefined");
    tokens.put("_|", "close-supplied.undefined");
    tokens.put("_[", "open-supplied.parallel.lost");
    tokens.put("]_", "close-supplied.parallellost");
    tokens.put("|_", "open-supplied");
    tokens.put("_|", "close-supplied");
    tokens.put("(", "open-expan");
    tokens.put(")", "close-expan");
    for (String key : tokens.keySet()) {
      tokensRev.put(tokens.get(key), key);
    }
  }

  public List<String> split(String in) throws Exception{
    return split(new StringReader(in));
  }

  public List<String> split(File in) throws Exception {
    return split(new FileReader(in));
  }

  public List<String> split(Reader in) throws Exception {
    BufferedReader reader = new BufferedReader(in);
    List<String> result = new ArrayList<String>();
    StringBuilder out = new StringBuilder();
    Deque<String> elements = new ArrayDeque<String>();
    String[] elts = new String[50];
    boolean split = false;
    int linecount = 0;
    String lang = "";
    String line = reader.readLine();
    while (line != null) {
      out.append(line);
      String next = reader.readLine();
      if (next != null) out.append("\n");
      if (line.startsWith("<S=")) {
        lang = line.substring(3, line.indexOf('<', 3));
      }
      // figure out stack state at the end of the line
      if (line.length() > elts.length) {
        elts = new String[line.length()];
      }

      for (String key : tokens.keySet()) {
        int index = -1;
        while ((index = line.indexOf(key, index + 1)) >= 0) {
          elts[index] = tokens.get(key);
        }
      }
      for (int i = 0; i < line.length(); i++) {
        if (elts[i] != null) {
          if (elts[i].startsWith("open-")) {
            elements.push(elts[i]);
          }
          if (elts[i].startsWith("close-") && elements.peek().equals(elts[i].replace("close-", "open-"))) {
            elements.pop();
          }
          elts[i] = null;
        }
      }
      /* if the line begins with a line number,
       * 1) increment linecount
       * 2) peek at elements and see if we're inside an ab
       * 3) if linecount mod 20 == 0, then split the file.
       */
      Matcher m = linenum.matcher(line);
      /* if this is a numbered line, increment the counter, and split the
       * file if we're at a break point, specified by splitOn.
       */
      if (m.find()) {
        linecount++;
        if (linecount % splitOn == 0) {
          split = true;
        }
        if (split && "open-ab".equals(elements.peek())) {
          out.append("☃\n"); // ☃\n before closer is crucial, as we'll snip there when we re-join the chunks
          for (String el : elements) {
            String close = tokensRev.get(el.replace("open-", "close-"));
            if (close != null) {
              out.append(close);
            }
          }
          result.add(out.toString());
          out = new StringBuilder();
          for (Iterator<String> i = elements.descendingIterator(); i.hasNext();) {
            String token = i.next();
            out.append(tokensRev.get(token));
            if ("open-div.edition".equals(token)) {
              out.append(lang);
            }
            if ("open-div".equals(token)) {
              out.append(".fake");
            }
          }
          out.append("☃\n");
          split = false;
        }
      }
      line = next;
    }
    result.add(out.toString());
    return result;
  }

  public String join(List<String> in) {
    StringBuilder result = new StringBuilder();
    String chunk = in.get(0);
    if (in.size() > 1) {
      // =>=D> closing the chunk will always be followed by a ☃\n, so we can snip there.
      result.append(chunk.substring(0, chunk.lastIndexOf("☃\n")));
      for (Iterator<String> i = in.listIterator(1); i.hasNext();) {
        chunk = i.next();
        chunk = chunk.substring(chunk.indexOf("☃\n") + 2);  //strip the first line (the '<S=.grc<D=.fake<=' part)
        if (i.hasNext()) {
          result.append(chunk.substring(0, chunk.lastIndexOf("☃\n")));
        } else {
          result.append(chunk);
        }
      }
    } else {
      result.append(chunk);
    }
    return result.toString();
  }
}
