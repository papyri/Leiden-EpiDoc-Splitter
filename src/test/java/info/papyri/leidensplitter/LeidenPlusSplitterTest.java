/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.papyri.leidensplitter;

import java.io.*;
import java.util.List;
import junit.framework.TestCase;

/**
 *
 * @author hcayless
 */
public class LeidenPlusSplitterTest extends TestCase {

  public LeidenPlusSplitterTest(String testName) {
    super(testName);
    try {
      file = loadTestFile();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  String file;

  /**
   * Test of split method, of class LeidenPlusSplitter.
   */
  public void testSplit_String() throws Exception {
    System.out.println("split(String in)");
    String in = file;
    LeidenPlusSplitter instance = new LeidenPlusSplitter();
    List result = instance.split(in);
    String join = instance.join(result);
    assertEquals(in, join);
  }

  /**
   * Test of split method, of class LeidenPlusSplitter.
   */
  public void testSplit_File() throws Exception {
    System.out.println("split(File in)");
    File in = new File("/Users/hcayless/Development/APIS/leidensplitter/src/test/resources/leidenplus.txt");
    LeidenPlusSplitter instance = new LeidenPlusSplitter();
    List<String> result = instance.split(in);
    assertEquals(file, instance.join(result));
  }

  /**
   * Test of split method, of class LeidenPlusSplitter.
   */
  public void testSplit_Reader() throws Exception {
    System.out.println("split(Reader in)");
    Reader reader = new FileReader(new File("/Users/hcayless/Development/APIS/leidensplitter/src/test/resources/leidenplus.txt"));
    LeidenPlusSplitter instance = new LeidenPlusSplitter();
    List result = instance.split(reader);
    assertEquals(file, instance.join(result));
  }

  /**
   * Test of join method, of class LeidenPlusSplitter.
   */
  public void testJoin() throws Exception {
    System.out.println("join");
    LeidenPlusSplitter instance = new LeidenPlusSplitter();
    List<String> in = instance.split(file);
    String expResult = file;
    String result = instance.join(in);
    assertEquals(expResult, result);
  }

  private String loadTestFile() throws Exception {
    File in = new File("/Users/hcayless/Development/APIS/leidensplitter/src/test/resources/leidenplus.txt");
    Reader reader = new FileReader(in);
    char[] buffer = new char[1024];
    int read = -1;
    StringBuilder file = new StringBuilder();
    while ((read = reader.read(buffer)) > 0) {
      file.append(buffer, 0, read);
    }
    return file.toString();
  }
}
