// DocExtractor.java

/* Extracts information in format:
 <tr>
 <td width="28%">forward(distance), fd(distance)</td>
 <td width="62%">bewegt Turtle vorw&auml;rts </td>
 </tr>
 */
package ch.aplu.packagedoc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class DocExtractor
{
  private static boolean debug = false;
  private final static String ls = System.getProperty("line.separator");

  /**
   * Enables/Disables debug information written to System.out.
   * @param enable if true, verbose information is written to System.out 
   * (default: false)
   */
  public static void setDebug(boolean enable)
  {
    debug = enable;
  }

  /**
   * Extracts documentation for a given method from a html formatted
   * doc file.
   * @param home the home directory in the JAR file (relative, use '/' separator)
   * @param library the name of the library
   * @param language the language 'de', 'en', 'fr', 'it' appended with
   * preceeding _ to the library name
   * @param entry the name of the method (without parameter parenthesis)
   * @return the information formatted as html table, empty if information
   * is not found. More than one entry is searched (for overloaded methods).
   */
  public static String getDoc(String home, String library, String language,
    String entry)
  {
    String filename = home + "/" + library + "_" + language + ".html";
    if (debug)
      System.out.println("filename: " + filename);

    // ---------- Search resource in jar --------
    String text = getResource(filename);
    if (text == null)
    {
      if (debug)
        System.out.println("resource not found");
      return null;
    }
    if (debug)
      System.out.println("resource successfully loaded");

    // Append opening parameter bracket
    String item = entry + "(";
    String extracted = "";
    int searchIndex = 0;
    while (true)
    {
      int itemloc = text.indexOf(item, searchIndex);
      if (itemloc == -1)
      {
        if (debug && extracted.length() == 0)
          System.out.println("item '" + item + "' not found");
        return extracted;
      }
      if (debug)
        System.out.println("itemloc = " + itemloc);
      extracted += extract(text, itemloc);
      searchIndex = itemloc + item.length();
    }
  }

  // Returns null, if not found
  private static String extract(String text, int loc)
  {
    // ---------- Search for previous <tr> -----------
    int startloc = loc;
    boolean found = false;
    while (startloc > 3)
    {
      String tag = text.substring(startloc - 4, startloc);
      if (tag.equals("<tr>"))
      {
        found = true;
        break;
      }
      startloc -= 1;
    }
    if (!found)
    {
      if (debug)
        System.out.println("startloc = (not found)");
      return null;
    }
    if (debug)
      System.out.println("startloc = " + startloc);
    startloc -= 4;

    // ---------- Search for next </tr> -----------
    int endloc = text.indexOf("</tr>", loc);
    if (endloc == -1)
    {
      if (debug)
        System.out.println("endloc = (not found)");
      return null;
    }
    if (debug)
      System.out.println("endloc = " + endloc);
    endloc += 5;

    String extracted = text.substring(startloc, endloc);
    extracted += ls;  // Append ls
    if (debug)
      System.out.println("extracted:\n" + extracted);
    return extracted;
  }

  // Returns null, if error
  private static String getResource(String filename)
  {
    BufferedReader br = null;
    URL url = Thread.currentThread().getContextClassLoader().
      getResource(filename);
    if (url == null)
      return null;
    try
    {
      InputStream is = url.openStream();
      br = new BufferedReader(new InputStreamReader(is));
    }
    catch (IOException ex)
    {
      return null;
    }

    StringBuilder stringBuilder = new StringBuilder();
    String line;
    try
    {
      while ((line = br.readLine()) != null)
      {
        stringBuilder.append(line);
        stringBuilder.append(ls);
      }
      return stringBuilder.toString();
    }
    catch (IOException ex)
    {
      return null;
    }
    finally
    {
      try
      {
        br.close();
      }
      catch (Exception ex)
      {
        return null;
      }
    }
  }
}
