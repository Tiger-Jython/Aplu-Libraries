// GPaneBeanInfo.java

/*
 This software is part of the JEX (Java Exemplarisch) Utility Library.
 It is Open Source Free Software, so you may
 - run the code for any purpose
 - study how the code works and adapt it to your needs
 - integrate all or parts of the code in your own programs
 - redistribute copies of the code
 - improve the code and release your improvements to the public
 However the use of the code is entirely your responsibility.
 */
package ch.aplu.util;

import java.beans.*;

/**
 * A bean info class derived from SimpleBeanInfo in order to restrict the
 * visible properties of bean class GPane.
 */
public class GPaneBeanInfo extends SimpleBeanInfo
{
  /**
   * Return the descriptor of visible properties.
   */
  public PropertyDescriptor[] getPropertyDescriptors()
  {
    PropertyDescriptor pds[] = null;
    try
    {
      pds = new PropertyDescriptor[]
      {
        new PropertyDescriptor("backgroundColor", GPane.class),
        new PropertyDescriptor("penColor", GPane.class),
        new PropertyDescriptor("xmin", GPane.class),
        new PropertyDescriptor("xmax", GPane.class),
        new PropertyDescriptor("ymin", GPane.class),
        new PropertyDescriptor("ymax", GPane.class),
        new PropertyDescriptor("enableFocus", GPane.class),
      };
    }
    catch (IntrospectionException e)
    {
      e.printStackTrace();
    }
    return pds;
  }

}