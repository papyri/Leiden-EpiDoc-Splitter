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
public class EpiDocSplitterTest extends TestCase {

  public EpiDocSplitterTest(String testName) {
    super(testName);
    try {
      file = loadTestFile();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  String file;

  /**
   * Test of split method, of class EpiDocSplitter.
   */
  public void testSplit_String() throws Exception {
    System.out.println("split(String in)");
    String in = file;
    EpiDocSplitter instance = new EpiDocSplitter();
    List result = instance.split(in);
    assertEquals(in, instance.join(result));
  }

  /**
   * Test of split method, of class EpiDocSplitter.
   */
  public void testSplit_File() throws Exception {
    System.out.println("split(File in)");
    File in = new File(getClass().getClassLoader().getResource("badtext.xml").toURI());
    EpiDocSplitter instance = new EpiDocSplitter();
    List<String> result = instance.split(in);
    assertEquals(file, instance.join(result));
  }

  /**
   * Test of split method, of class EpiDocSplitter.
   */
  public void testSplit_Reader() throws Exception {
    System.out.println("split(Reader in)");
    Reader reader = new InputStreamReader(getClass().getClassLoader().getResourceAsStream("badtext.xml"));
    EpiDocSplitter instance = new EpiDocSplitter();
    List result = instance.split(reader);
    assertEquals(file, instance.join(result));
  }

  /**
   * Test of join method, of class EpiDocSplitter.
   */
  public void testJoin() throws Exception {
    System.out.println("join");
    EpiDocSplitter instance = new EpiDocSplitter();
    List<String> in = instance.split(file);
    String expResult = file;
    String result = instance.join(in);
    assertEquals(expResult, result);
  }

  private String loadTestFile() throws Exception {
    Reader reader = new InputStreamReader(getClass().getClassLoader().getResourceAsStream("badtext.xml"));
    char[] buffer = new char[1024];
    int read = -1;
    StringBuilder file = new StringBuilder();
    while ((read = reader.read(buffer)) > 0) {
      file.append(buffer, 0, read);
    }
    return file.toString();
  }
}
