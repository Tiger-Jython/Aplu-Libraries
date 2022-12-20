// MyProperties.java

package ch.aplu.packagedoc;

import java.io.*;
import java.util.*;

/**
 * Class to read library options from a property file that
 * may be modified as needed.
 */
class MyProperties extends Properties
{
  //
  private boolean verbose;
  private String propFile = "packagedoc.properties";
  private String userDir = System.getProperty("user.dir");
  private String  userHome = 
      (System.getProperty("user.home").toLowerCase().contains("%userprofile%")) ?
       ((System.getenv("USERPROFILE") == null) ? System.getProperty("java.io.tmpdir") :
      System.getenv("USERPROFILE")) : System.getProperty("user.home");
  private String fileSeparator = System.getProperty("file.separator");
  private String propertiesResourcePath = "ch/aplu/packagedoc/properties/" + propFile;
  private String propLocation = "";

  /**
   * Creates an instance with no verbose debug information.
   */
  public MyProperties()
  {
    this(false);
  }

  /**
   * Creates an instance with selectable verbose debug information.
   * @vebose if true, verbose information is written to System.out; 
   * otherwise nothing is written to System.out
   */
  public MyProperties(boolean verbose)
  {
    this.verbose = verbose;
  }

  /**
   * Searches the property file.
   * The property file is searched in the following order:<br>
   * - Application directory (user.dir)<br>
   * - Home directory (user.home)<br>
   * - library JAR foder ch/aplu/turtle/properties<br><br>
   * As soon as the property file is found, the search is canceled and the
   * content is loaded. This allows to use a personalized property file
   * without deleting or modifing the distributed
   * file in TcpJLib.jar. Consult the distributed file for more information.
   * Be careful to keep the original formatting.
   * @return true, if property file is successfully loaded; otherwise false
   */
  public boolean search()
  {
    try
    {
      // Search in current dir
      File pFile = new File(userDir + fileSeparator + propFile);
      if (pFile.isFile())
      {
        propLocation = "Properties loaded from\n   " + pFile;
        if (verbose)
          System.out.println(propLocation);
        load(new FileInputStream(pFile));
        return true;
      }

      // Search in home dir
      pFile = new File(userHome + fileSeparator + propFile);
      if (pFile.isFile())
      {
        propLocation = "Properties loaded from\n   " + pFile;
        if (verbose)
          System.out.println(propLocation);
        load(new FileInputStream(pFile));
        return true;
      }

      // Search in jar
      InputStream is = getClass().getClassLoader().
        getResourceAsStream(propertiesResourcePath);
      if (is == null)
        throw new IOException();
      load(is);
      propLocation = "Properties loaded from JAR resource";
      if (verbose)
        System.out.println(propLocation);
      return true;
    }
    catch (IOException ex)
    {
      propLocation = "Properties not found";
      if (verbose)
        System.out.println(propLocation);
      return false;  // Property file not found
    }
  }

  /**
   * Gets the property value with the given key as string.
   * @param key the key of the property
   * @return the value of the property; null, if the property file is not loaded or
   * the property is not found
   */
  public String getStringValue(String key)
  {
    if (verbose)
      System.out.print("Calling getStringValue(" + key + ")... ");
    String value = getProperty(key);
    if (value == null)
    {
      if (verbose)
        System.out.println("Not found.");
      return null;
    }
    if (verbose)
      System.out.println("value: " + value);
    return value;
  }

  /**
   * Gets the property value with the given key as int.
   * @param key the key of the property
   * @return the value of the property; null,
   * if the property file is not loaded or if the property is not found
   * or does not have integer format
   */
  public Integer getIntValue(String key)
  {
    if (verbose)
      System.out.print("Calling getIntValue(" + key + ")... ");
    String value = getProperty(key);
    if (value == null)
    {
      if (verbose)
        System.out.println("Not found.");
      return null;
    }
    int intValue;
    try
    {
      intValue = Integer.parseInt(value);
    }
    catch (NumberFormatException ex)
    {
      return null;
    }
    if (verbose)
      System.out.println("value: " + value);
    return intValue;
  }

  /**
   * Gets the property value with the given key as double.
   * @param key the key of the property
   * @return the value of the property; null
   * if the property file is not loaded or the property is not found or does not have double format
   */
  public Double getDoubleValue(String key)
  {
    if (verbose)
      System.out.print("Calling getDoubleValue(" + key + ")... ");
    String value = getProperty(key);
    if (value == null)
    {
      if (verbose)
        System.out.println("Not found.");
      return null;
    }
    double doubleValue;
    try
    {
      doubleValue = Double.parseDouble(value);
    }
    catch (NumberFormatException ex)
    {
      return null;
    }
    if (verbose)
      System.out.println("value: " + value);
    return doubleValue;
  }

  /**
   * Gets the property values with the given key as an integer array.
   * The assumend property format is (a, b, c, ...) 
   * (comma separated parantheses with allowed white spaces). All tokens
   * must have a valid integer format
   * @param key the key of the property
   * @return an array with the tokens as integers; null
   * if the property file is not loaded or the property is not found or 
   * does not have proper format
   */
  public int[] getIntArray(String key, int size)
  // null if not valid, assumed format "(a, b, c, ...)"
  {
    if (verbose)
      System.out.print("Calling getIntArray(" + key + ", " + size + ")... ");
    boolean oldVerbose = verbose;
    verbose = false;
    String value = getStringValue(key);
    verbose = oldVerbose;
    if (value == null)
    {
      if (verbose)
        System.out.println("Not found.");
      return null; // Not found
    }

    value = value.replaceAll("\\s", "");  // Remove white spaces
    if (value.length() < 2 * size + 1)  // not valid
      return null;
    value = value.substring(1, value.length() - 1);  // Remove parantheses
    String[] tokens = value.split(",");
    if (tokens.length != size)
      return null;  // not valid

    int[] v = new int[size];
    try
    {
      for (int i = 0; i < size; i++)
        v[i] = Integer.parseInt(tokens[i]);
    }
    catch (NumberFormatException ex)
    {
      return null;  // not valid
    }
    if (verbose)
      System.out.println("value: " + Arrays.toString(v));
    return v;
  }

  /**
   * Gets the property values with the given key as a double array.
   * The assumend property format is (a, b, c, ...) 
   * (comma separated parantheses with allowed white spaces). All tokens
   * must have a valid double format
   * @param key the key of the property
   * @return an array with the tokens as doubles; null
   * if the property file is not loaded or the property is not found or 
   * does not have proper format
   */
  public double[] getDoubleArray(String key, int size)
  // null if not valid, assumed format "(a, b, c, ...)"
  {
    if (verbose)
      System.out.print("Calling getDoubleArray(" + key + ", " + size + ")... ");
    boolean oldVerbose = verbose;
    verbose = false;
    String value = getStringValue(key);
    verbose = oldVerbose;
    if (value == null)
    {
      if (verbose)
        System.out.println("Not found.");
      return null; // Not found
    }
    value = value.replaceAll("\\s", "");  // Remove white spaces
    if (value.length() < 2 * size + 1)  // not valid
      return null;
    value = value.substring(1, value.length() - 1);  // Remove parantheses
    String[] tokens = value.split(",");
    if (tokens.length != size)
      return null;  // not valid

    double[] v = new double[size];
    try
    {
      for (int i = 0; i < size; i++)
        v[i] = Double.parseDouble(tokens[i]);
    }
    catch (NumberFormatException ex)
    {
      return null;  // not valid
    }
    if (verbose)
      System.out.println("value: " + Arrays.toString(v));
    return v;
  }

  /**
   * Gets the property values with the given key as string array.
   * The assumend property format is (a, b, c, ...) 
   * (comma separated parantheses with allowed white spaces).
   * @param key the key of the property
   * @return an array with the tokens as strings; null
   * if the property file is not loaded or the property is not found or 
   * does not have proper format
   */
  public String[] getStringArray(String key, int size)
  // null if not valid, assumed format "(a, b, c, ...)"
  {
    if (verbose)
      System.out.print("Calling getStringArray(" + key + ", " + size + ")... ");
    String value = getStringValue(key);
    if (value == null)
    {
      if (verbose)
        System.out.println("Not found.");
      return null; // Not found
    }
    value = value.replaceAll("\\s", "");  // Remove white spaces
    if (value.length() < 2 * size + 1)  // not valid
      return null;
    value = value.substring(1, value.length() - 1);  // Remove parantheses
    String[] tokens = value.split(",");
    if (tokens.length != size)
      return null;  // not valid
    if (verbose)
      System.out.println("value: " + Arrays.toString(tokens));
    return tokens;
  }

  public String getLocation()
  {
    return propLocation;
  }

}
