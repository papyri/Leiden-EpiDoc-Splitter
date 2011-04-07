/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.papyri.leidensplitter;

import java.io.Reader;
import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java.util.*;
import org.xml.sax.*;
import org.xml.sax.ext.*;
import org.xml.sax.helpers.*;

/**
 *
 * @author Hugh Cayless
 *
 * EpiDocSplitter takes an EpiDoc-style edition <div> and partitions it into
 * well-formed XML fragments of N lines each (plus a remainder).
 */
public class EpiDocSplitter {
  private ArrayList<String> results;

  public List<String> split(String in) throws Exception {
    return split(new StringReader(in));
  }

  public List<String> split(File in) throws Exception {
    return split(new FileReader(in));
  }
  
  public List<String> split(Reader reader) throws Exception {
    XMLReader xml = XMLReaderFactory.createXMLReader();
    ContentHandler handler = new SplitterContentHandler();
    xml.setContentHandler(handler);
    xml.setProperty("http://xml.org/sax/properties/lexical-handler", handler);
    xml.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
    xml.setFeature("http://xml.org/sax/features/validation", false );
    InputSource is = new InputSource(reader);
    xml.parse(is);
    return results;
  }

  public String join(List<String> in) {
    StringBuilder result = new StringBuilder();
    String chunk = in.get(0);
    if (in.size() > 1) {
      // </ab></div></div> closing the chunk is always on a newline, so we can snip at the last \n
      // (discarding the document end, in case it is a newline.
      result.append(chunk.substring(0, chunk.lastIndexOf("☃\n")));
      for (Iterator<String> i = in.listIterator(1); i.hasNext();) {
        chunk = i.next();
        chunk = chunk.substring(chunk.indexOf("☃\n") + 2);  //strip the first line, which always contains the opening elements
        if (i.hasNext()) {
          result.append(chunk.substring(0, chunk.lastIndexOf("☃\n", chunk.length() - 1)));
        } else {
          result.append(chunk);
        }
      }
    } else {
      result.append(chunk);
    }
    return result.toString();
  }
  
  private class SplitterContentHandler implements ContentHandler, LexicalHandler {
    private StringBuilder chunk;
    private String currentNS = "";
    private String currentPrefix = "";
    private boolean addNS = false;
    private Deque<List> stack = new ArrayDeque<List>();
    private boolean standalone = false;
    private int lineCount = 0;
    private boolean split = false;
    private int splitOn = 20;
    private char[] nl = new char[] {'☃', '\n'};

    @Override
    public void setDocumentLocator(Locator locator) {
      //do nothing
    }

    @Override
    public void startDocument() throws SAXException {
      results = new ArrayList();
      chunk = new StringBuilder();
    }

    @Override
    public void endDocument() throws SAXException {
      results.add(chunk.toString());
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
      if (!currentNS.equals(uri)) {
        currentNS = uri;
        currentPrefix = prefix;
        addNS = true;
      }
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
      currentNS = "";
      currentPrefix = "";
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
      if (standalone) {
        chunk.append(">");
        standalone = false;
      }
      if ("lb".equals(localName)) {
        lineCount++;
        if (lineCount % splitOn == 0) split = true;
        if (split && "ab".equals(stack.peek().get(0))) {
          split = false;
          characters(nl, 0, 2);
          Deque stackClone = ((ArrayDeque)stack).clone();
          for (List elt : stack) {
            endElement(null, (String)elt.get(0), null);
          }
          results.add(chunk.toString());
          chunk = new StringBuilder();
          for (Iterator<List> i = stackClone.descendingIterator(); i.hasNext();) {
            List elt = i.next();
            startElement(null, (String)elt.get(0), null, (Attributes)elt.get(1));
          }
          characters(nl, 0, 2);
        }
      }
      chunk.append("<");
      chunk.append(currentPrefix);
      if (currentPrefix.length() > 0) chunk.append(":");
      chunk.append(localName);
      if (addNS) {
        chunk.append(" ");
        chunk.append(" xmlns"); 
        if (currentPrefix.length() > 0) chunk.append(":");
        chunk.append(currentPrefix);
        chunk.append("=\"");
        chunk.append(currentNS);
        chunk.append("\"");
      }
      for (int i = 0; i < atts.getLength(); i++) {
        chunk.append(" ");
        chunk.append(atts.getQName(i));
        chunk.append("=\"");
        chunk.append(atts.getValue(i));
        chunk.append("\"");
      }
      List elt = new ArrayList();
      elt.add(localName);
      elt.add(new AttributesImpl(atts));
      stack.push(elt);
      standalone = true;
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
      stack.pop();
      if (standalone) {
        chunk.append("/>");
        standalone = false;
      } else {
        chunk.append("</");
        chunk.append(currentPrefix);
        if (currentPrefix.length() > 0) chunk.append(":");
        chunk.append(localName);
        chunk.append(">");
      }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
      if (standalone) {
        chunk.append(">");
        standalone = false;
      }
      chunk.append(ch, start, length);
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
      chunk.append(ch, start, length);
    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void skippedEntity(String name) throws SAXException {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void startDTD(String name, String publicId, String systemId) throws SAXException {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void endDTD() throws SAXException {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void startEntity(String name) throws SAXException {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void endEntity(String name) throws SAXException {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void startCDATA() throws SAXException {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void endCDATA() throws SAXException {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void comment(char[] ch, int start, int length) throws SAXException {
      chunk.append(ch, start, length);
    }
    
  }

}
