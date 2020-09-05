// TurtleRunner.java

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
package ch.aplu.turtle;

import java.awt.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.jar.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * TurtleRunner contains a public static void main() entry point and is
 * able to create an instance of another class (by Java reflection), so that its
 * default contructor is invoked. Mainly used to avoid the ugly public static void main()
 * method in beginner's programs. The class name can be provided in three ways:<br>
 * - as parameter of the main() method<br>
 * - as an entry with key MainClass in a mainclass.properties file (located in the root of the application jar)<br>
 * - all classes in the class path that implement the TurtleProgram interface (if
 * more than one class is found, a selection box is displayed).<br>
 */
public class TurtleRunner
{
  private ArrayList<String> classNames = new ArrayList<String>();
  private boolean isReady = true;
  private String selectedItem;

  private TurtleRunner()
  {
    String className = getMainFromProperties();
    if (className != null)
      createInstance(className);
    else
    {
      className = getMainFromInterface();
      if (className != null)
        createInstance(className);
      else
        System.out.println("Class name of main class not found");
    }
  }

  private TurtleRunner(String className)
  {
    createInstance(className);
  }

  private void createInstance(String className)
  {
    try
    {
      Class clazz = Class.forName(className);
      Constructor constructor = clazz.getDeclaredConstructor();
      constructor.setAccessible(true);
      constructor.newInstance();
    }
    catch (Exception ex)
    {
      System.out.println("Failed to create instance of class " + className);
    }
  }

  private String getMainFromProperties()
  {
    try
    {
      InputStream is = getClass().getClassLoader().
        getResourceAsStream("mainclass.properties");
      if (is == null)
        return null;
      Properties props = new Properties();
      props.load(is);
      String value = props.getProperty("MainClass");
      return value; // null if not found
    }
    catch (IOException ex)
    {
      return null;
    }
  }

  private void getAllResources()
  {
    String[] classPaths =
      System.getProperty("java.class.path").split(File.pathSeparator);
    for (String classPath : classPaths)
      findResources(classPath);
  }

  private void findResources(String classPath)
  {
    if (classPath.endsWith(".jar"))
      findJarResources(classPath);
    else
      checkDirectory(new File(classPath), "");
  }

  private void checkDirectory(File directory, String directoryName)
  {
    for (File file : directory.listFiles())
      checkResource(file, directoryName);
  }

  private void checkResource(File file, String directoryName)
  {
    if (!file.exists())
      return;

    String fullName = file.getName();
    if (!directoryName.isEmpty())
      fullName = directoryName + '/' + fullName;

    if (file.isDirectory())
      checkDirectory(file, fullName);
    else
      checkResource(fullName);
  }

  private void findJarResources(String jarFile)
  {
    try
    {
      JarInputStream jarStream = new JarInputStream(new FileInputStream(jarFile));
      JarEntry element = jarStream.getNextJarEntry();
      while (element != null)
      {
        checkResource(element.getName());
        element = jarStream.getNextJarEntry();
      }
    }
    catch (IOException ex)
    {
      System.out.println(ex);
    }
  }

  private void checkResource(String resourceName)
  {
    if (resourceName.endsWith(".class") && !resourceName.contains("$"))
    {
      String className = resourceName.substring(0, resourceName.length() - 6);
      className = className.replace("/", ".");
      classNames.add(className);
    }
  }

  private String getMainFromInterface()
  {
    getAllResources();
    ArrayList<String> list = new ArrayList<String>();
    for (String className : classNames)
    {
      try
      {
        Class clazz = loadClass(className);
 //       System.out.println("\nfound: " + clazz.getName());
        Class[] interfaces = clazz.getInterfaces();
        for (Class c : interfaces)
        {
//          System.out.println("  -   implements: " + c.getName());
          if (c.getName().equals("ch.aplu.turtle.TurtleProgram"))
          {
 //           System.out.println(clazz.getName() + " implements TurtleProgram");
            list.add(clazz.getName());
          }
        }
      }
      catch (Exception ex)
      {
        return null;
      }
    }
    if (list.isEmpty())
      return null;
    else if (list.size() == 1)
      return list.get(0);
    else
      return selectOneItem(list);
  }

  private String selectOneItem(final ArrayList<String> items)
  {
    final JFrame frame = new JFrame();
    EventQueue.invokeLater(new Runnable()
    {
      public void run()
      {
        frame.setTitle("Select Main Class");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        JPanel contentPane = (JPanel)frame.getContentPane();

        final String[] itemArray = new String[items.size()];
        for (int i = 0; i < itemArray.length; i++)
          itemArray[i] = items.get(i);

        JList list = new JList(itemArray);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addListSelectionListener(new ListSelectionListener()
        {
          public void valueChanged(ListSelectionEvent evt)
          {
            if (isReady)
            {
              selectedItem = itemArray[evt.getFirstIndex()];
              isReady = false;
            }
            else
            {
              isReady = true;
              wakeUp();
            }
          }
        });
        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.
          setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.
          setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        contentPane.add(scrollPane, BorderLayout.CENTER);
        frame.setVisible(true);
      }
    });

    putSleep();
    EventQueue.invokeLater(new Runnable()
    {
      public void run()
      {
        frame.dispose();
      }
    });

    return selectedItem;
  }

  private static Class loadClass(String className) throws Exception
  {
    try
    {
      return Class.forName(className);
    }
    catch (NoClassDefFoundError ex)
    {
      return Class.forName(className, false,
        Thread.currentThread().getContextClassLoader());
    }
    catch (ExceptionInInitializerError ex)
    {
      return Class.forName(className, false,
        Thread.currentThread().getContextClassLoader());
    }
  }

  private void putSleep()
  {
    synchronized (this)
    {
      try
      {
        wait();
      }
      catch (InterruptedException ex)
      {
      }
    }
  }

  private void wakeUp()
  {
    synchronized (this)
    {
      notify();
    }
  }
  
  /**
   * Program entry point. If no argument is given, the class name of the
   * instance to load is searched in the mainclass.properties file 
   * (key MainClass) located in the application jar root. If not found,
   * the class path is scanned for classes that implement the TurtleProgram
   * interface.
   * @param args the name of the class whereof an instance is created
   */
  public static void main(String[] args)
  {
    if (args.length == 1)
      new TurtleRunner(args[0]);
    else
      new TurtleRunner();
  }
}
