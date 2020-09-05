// EV3Properties.java

/*
 This software is part of the EV3JLib library.
 It is Open Source Free Software, so you may
 - run the code for any purpose
 - study how the code works and adapt it to your needs
 - integrate all or parts of the code in your own programs
 - redistribute copies of the code
 - improve the code and release your improvements to the public
 However the use of the code is entirely your responsibility.
 */
package ch.aplu.ev3;

import java.io.*;
import java.util.*;

/**
 * Class to read properties from the EV3JLib property file.
 * Many library options are defined in a property file 'ev3jlib.properties' that
 * may be modified as needed. 
 */
public class EV3Properties extends Properties
{
  private boolean verbose;
  private final String propFile = "ev3jlib.properties";
  private final String userDir = System.getProperty("user.dir");
  private final String userHome = System.getProperty("user.home");
  private final String fileSeparator = System.getProperty("file.separator");
  private final String propertiesResourcePath = "ch/aplu/ev3/properties/" + propFile;
  private String propLocation = "";

  /**
   * Searches the property file ev3lib.properties.
   * The property file is searched in the following order:<br>
   * - Application directory (user.dir)<br>
   * - Home directory (user.home)<br>
   * - library JAR folder ch/aplu/ev3/properties<br><br>
   * As soon as the property file is found, the search is canceled and the
   * content is loaded. This allows to use a personalized property file
   * without deleting or modifing the distributed
   * file in EV3JLibA.jar. Consult the distributed file for more information.
   * Be careful to keep the original formatting.
   */
  public EV3Properties()
  {
    if (!search())
      new ShowError("Could not read property file '" + propFile + "'");
  }

  private boolean search()
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

  public String getLocation()
  {
    return propLocation;
  }

  /**
   * Gets the property value with the given key as string.
   * An error dialog is shown, if the property does not exist.
   * @param key the key of the property
   * @return the value of the property or empty string, if the property is not found
   */
  public String getStringValue(String key)
  {
    String value = getProperty(key);
    if (value == null)
    {
      new ShowError("Property '" + key
        + "' not found in file '" + propFile + "'");
      return "";
    }
    return value.trim();
  }

  /**
   * Gets the property value with the given key as int.
   * An error dialog is shown, if the property does not exist or has bad format.
   * @param key the key of the property
   * @return the value of the property or 0, 
   * if the property is not found or does not have integer format
   */
  public int getIntValue(String key)
  {
    String value = getProperty(key);
    if (value == null)
    {
      new ShowError("Property '" + key
        + "' not found in file '" + propFile + "'");
      return 0;
    }
    int intValue;
    try
    {
      intValue = Integer.parseInt(value);
    }
    catch (NumberFormatException ex)
    {
      new ShowError("Illegal integer format of property '" + key
        + "' in file '" + propFile + "'");
      return 0;
    }
    return intValue;
  }

  /**
   * Gets the property value with the given key as double.
   * An error dialog is shown, if the property does not exist or has bad format.
   * @param key the key of the property
   * @return the value of the property or 0, 
   * if the property is not found or does not have double format
   */
  public double getDoubleValue(String key)
  {
    String value = getProperty(key);
    if (value == null)
    {
      new ShowError("Property '" + key
        + "' not found in file '" + propFile + "'");
      return 0;
    }
    double doubleValue;
    try
    {
      doubleValue = Double.parseDouble(value);
    }
    catch (NumberFormatException ex)
    {
      new ShowError("Illegal double format of property '" + key
        + "' in file '" + propFile + "'");
      return 0;
    }
    return doubleValue;
  }

}
