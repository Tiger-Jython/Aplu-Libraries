// GGProperties.java

/*
This software is part of the JGameGrid package.
It is Open Source Free Software, so you may
- run the code for any purpose
- study how the code works and adapt it to your needs
- integrate all or parts of the code in your own programs
- redistribute copies of the code
- improve the code and release your improvements to the public
However the use of the code is entirely your responsibility.

Author: Aegidius Pluess, www.aplu.ch
 */
package ch.aplu.jgamegrid;

import java.io.*;
import java.util.*;

/**
 * Helper class to simplify storage and retrieval of persistent data.
 * The data stored can be retrieved at the next program execution.
 */
public class GGProperties
{
  private String propertiesPath;
  private Properties props = new Properties();
  private File pFile = null;
  private boolean isLoaded = false;

  /**
   * Creates a GGProperties instance with given path to the properties file.
   * Getter methods will try first to load the properties file from the application
   * jar archive; if not found there, it is searched in the file system. Setter methods
   * assumes the properties file in the file system and create or modify it.
   * @param propertiesPath the path to the properties file
   */
  public GGProperties(String propertiesPath)
  {
    this.propertiesPath = propertiesPath;
    pFile = new File(propertiesPath);
  }

  private boolean load()
  {
    if (isLoaded)
      return true;
    if (loadFromResource())
    {
      isLoaded = true;
      return true;
    }
    if (loadFromFile())
    {
      isLoaded = true;
      return true;
    }
    isLoaded = false;
    return false;
  }

  private boolean loadFromFile()
  {
    if (!pFile.isFile())
      return false;
    try
    {
      FileInputStream ifs = new FileInputStream(pFile);
      if (ifs == null)
        return false;
      props.load(ifs);
      ifs.close();
    }
    catch (IOException ex)
    {
      return false;
    }
    return true;
  }

  private boolean loadFromResource()
  {
    try
    {
      InputStream is = getClass().getResourceAsStream(propertiesPath);
      if (is == null)
        return false;
      props.load(is);
      is.close();
    }
    catch (IOException ex)
    {
      return false;

    }
    return true;
  }

  /**
   * Gets the property value with the given key as string.
   * If necessary, the properties file is loaded now.
   * @param key the key of the property
   * @return the value of the property or null, if the property is not found
   */
  public String getStringValue(String key)
  {
    if (!isLoaded)
    {
      if (!load())
        return null;
    }
    String value = props.getProperty(key);
    if (value == null)
      return null;
    return value;
  }

  /**
   * Gets the property value with the given key as int.
   * If necessary, the properties file is loaded now.
   * @param key the key of the property
   * @return the value of the property or null, 
   * if the property is not found or does not have integer format
   */
  public Integer getIntValue(String key)
  {
    if (!isLoaded)
    {
      if (!load())
        return null;
    }
    String value = props.getProperty(key);
    if (value == null)
      return null;
    int intValue;
    try
    {
      intValue = Integer.parseInt(value);
    }
    catch (NumberFormatException ex)
    {
      return null;
    }
    return intValue;
  }

  /**
   * Gets the property value with the given key as double.
   * If necessary, the properties file is loaded now.
   * @param key the key of the property
   * @return the value of the property or null, 
   * if the property is not found or does not have double format
   */
  public Double getDoubleValue(String key)
  {
    if (!isLoaded)
    {
      if (!load())
        return null;
    }
    String value = props.getProperty(key);
    if (value == null)
      return null;
    double doubleValue;
    try
    {
      doubleValue = Double.parseDouble(value);
    }
    catch (NumberFormatException ex)
    {
      return null;
    }
    return doubleValue;
  }

  /**
   * If the properties file exists and has write access, creates or modifies 
   * the property value with given key.  If the file does not exist, it is created now.
   * The file is closed after each write operation.
   * @param key the key to create or modify
   * @param value the String value associated to the key
   * @return true, if operation is successful; otherwise false
   */
  public boolean setStringProperty(String key, String value)
  {
    if (pFile.canWrite())  // Don't set property if not writable
      return false;
    props.setProperty(key, value);
    try
    {
      FileOutputStream fos = new FileOutputStream(pFile);
      props.store(fos, null);
      fos.close();
    }
    catch (IOException ex)
    {
      return false;
    }
    return true;
  }

  /**
   * If the properties file exists and has write access, creates or modifies 
   * the property value with given key.  If the file does not exist, it is created now.
   * The file is closed after each write operation.
   * @param key the key to create or modify
   * @param value the integer value associated to the key
   * @return true, if operation is successful; otherwise false
   */
  public boolean setIntProperty(String key, int value)
  {
    return setStringProperty(key, new Integer(value).toString());
  }

  /**
   * If the properties file exists and has write access, creates or modifies 
   * the property value with given key.  If the file does not exist, it is created now.
   * The file is closed after each write operation.
   * @param key the key to create or modify
   * @param value the double value associated to the key
   * @return true, if operation is successful; otherwise false
   */
  public boolean setDoubleProperty(String key, double value)
  {
    return setStringProperty(key, new Double(value).toString());
  }

  /**
   * If the properties file exists and has write access, remove the property
   * with given key. The file is modified and closed.
   * @param key the key to remove
   * @return true, if operation is successful; otherwise false
   */
  public boolean remove(String key)
  {
    if (pFile.canWrite())
      return false;
    props.remove(key);
    try
    {
      FileOutputStream fos = new FileOutputStream(pFile);
      props.store(fos, null);
      fos.close();
    }
    catch (IOException ex)
    {
      return false;
    }
    return true;
  }

  /**
   * If the properties file has write access, removes it.
   * @return true, if operation is successful; otherwise false
   */
  public boolean removeAll()
  {
    if (pFile.canWrite()) 
      return false;

    return pFile.delete();
  }
}
