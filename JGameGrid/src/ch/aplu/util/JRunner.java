// JRunner.java

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

import java.lang.reflect.*;
import javax.swing.*;

/**
 * A helper class in order to execute a method in a separate thread
 * and to get a notification callback when the thread terminates.
 * (Simple version of SwingWorker.)
 */
public class JRunner
{
  // --------------- Inner class MyThread -----------------------
  private class MyThread extends Thread
  {
    private String methodName;

    MyThread(String methodName)
    {
      this.methodName = methodName;
    }

    public void run()
    {
      String msg = null;
      Boolean rc = null;
      boolean retCode = false;
      boolean isVoid = false;
      try
      {
        rc = (Boolean)execMethod.invoke(exposedObj, new Object[]
          {
          });
      }
      catch (IllegalAccessException ex)
      {
        msg = methodName + "() not accessible";
      }
      catch (IllegalArgumentException ex)
      {
        msg = methodName + "() is not parameterless";
      }
      catch (InvocationTargetException ex)
      {
        msg = methodName + "() throws an exception";
      }
      catch (ClassCastException ex)
      {
        msg = methodName + "() has not a void or boolean return type";
      }
      if (msg != null)
      {
        JOptionPane.showMessageDialog(null, msg,
          "Error while calling JRunner.run()",
          JOptionPane.ERROR_MESSAGE);
        System.exit(0);
      }

      try
      {
        retCode = rc.booleanValue();
      }
      catch (NullPointerException ex)
      {
        isVoid = true;
      }
      if (!isVoid)
      {
        if (doneMethod != null && !retCode)
        {
          SwingUtilities.invokeLater(
            new Runnable()
            {
              public void run()
              {
                try
                {
                  Object[] argValues = new Object[1];
                  argValues[0] = methodName;
                  doneMethod.invoke(exposedObj, argValues);
                }
                catch (Exception ex)
                {
                  JOptionPane.showMessageDialog(null, "Error while trying to invoke \"done()\".",
                    "Error while calling JRunner.run()",
                    JOptionPane.ERROR_MESSAGE);
                  System.exit(0);
                }
              }

            });
        }

        if (killedMethod != null && retCode)
        {
          SwingUtilities.invokeLater(
            new Runnable()
            {
              public void run()
              {
                try
                {
                  Object[] argValues = new Object[1];
                  argValues[0] = methodName;
                  killedMethod.invoke(exposedObj, argValues);
                }
                catch (Exception ex)
                {
                  JOptionPane.showMessageDialog(null, "Error while trying to invoke \"killed()\".",
                    "Error while calling JRunner.run()",
                    JOptionPane.ERROR_MESSAGE);
                  System.exit(0);
                }
              }

            });
        }
      }
    }

  }
  // --------------- End of inner class -------------------------

  private Class aClass;
  private MyThread myThread = null;
  private Method execMethod = null;
  private Method doneMethod = null;
  private Method killedMethod = null;
  private Object exposedObj;
  private Object monitor = new Object();
  private Object monitorInternal = new Object();
  private boolean isWaiting;
  private boolean isWaitRequested;
  private boolean isCanceled;

  /**
   * Create a JRunner instance with given reference to a class instance.
   * The class should declare a public method that will be called in a separate
   * thread by invoking run().
   */
  public JRunner(Object exposedObj)
  {
    aClass = exposedObj.getClass();
    this.exposedObj = exposedObj;
  }

  /**
   * Create a thread and invoke the parameterless method with given name.<br>
   * The thread terminates when the given method returns.<br><br>
   * The given method must have a void or boolean return type.
   * The boolean return value can be used as follows:<br><br>
   *
   * If the given method returns false and the the class declares a method
   * <code>void done(String methodName)</code>, done() is called with its
   * corresponding methodName. done() is invoked by the event dispatch
   * thread (EDT), so that Swing methods may be called directly.<br><br>
   *
   * If the given method returns true and the the class declares a method
   * <code>void killed(String methodName)</code>, killed() is called with its
   * corresponding methodName. killed() is invoked by the event dispatch
   * thread (EDT), so that Swing methods may be called directly.<br><br>
   */
  public void run(String methodName)
  {
    Method methods[] = aClass.getDeclaredMethods();
    for (int i = 0; i < methods.length; ++i)
    {
      if (methodName.equals(methods[i].getName()))
      {
        try
        {
          methods[i].setAccessible(true);
          execMethod = methods[i];
        }
        catch (Exception ex)
        {
          JOptionPane.showMessageDialog(null, "Method \"" + methodName + "()\" not found.",
            "Error while calling JRunner.run()",
            JOptionPane.ERROR_MESSAGE);
          System.exit(0);
        }
      }
    }

    if (execMethod == null)
    {
      JOptionPane.showMessageDialog(null, "Method \"" + methodName + "()\" not found.",
        "Error while calling JRunner.run()",
        JOptionPane.ERROR_MESSAGE);
      System.exit(0);
    }

    String doneMethodName = "done";
    for (int i = 0; i < methods.length; ++i)
    {
      if (doneMethodName.equals(methods[i].getName()))
      {
        try
        {
          methods[i].setAccessible(true);
          doneMethod = methods[i];
        }
        catch (Exception ex)
        {
        }
      }
    }

    String killedMethodName = "killed";
    for (int i = 0; i < methods.length; ++i)
    {
      if (killedMethodName.equals(methods[i].getName()))
      {
        try
        {
          methods[i].setAccessible(true);
          killedMethod = methods[i];
        }
        catch (Exception ex)
        {
        }
      }
    }

    myThread = new MyThread(methodName);
    myThread.start();
  }

  /**
   * Wait for the internal thread to die at most the given time (in ms).
   * @see #join()
   */
  public void join(long millis)
  {
    if (myThread != null)
    {
      try
      {
        myThread.join(millis);
      }
      catch (InterruptedException ex)
      {
      }
    }
  }

  /**
   * Block until the internal thread dies.
   * @see #join(long)
   */
  public void join()
  {
    join(0);
  }

  private void putSleepInternal()
  {
    synchronized (monitorInternal)
    {
      try
      {
        monitorInternal.wait();
      }
      catch (InterruptedException ex)
      {
      }
    }
  }

  private void wakeUpInternal()
  {
    synchronized (monitorInternal)
    {
      monitorInternal.notify();
    }
  }

  /**
   * Halt temporarily the execution of the internal thread if it was requested
   * by calling waitRequest() and block until cancel() or resume() is called.
   * Return true if cancel() is called.
   * Return false, if resume() is called.
   * @see #waitRequest()
   * @see #cancel()
   * @see #resume()
   */
  public boolean waitIfRequested()
  {
    if (isWaitRequested)
    {
      isWaiting = true;
      putSleepInternal();
      isWaiting = false;
      isWaitRequested = false;
      if (isCanceled)
        return true;
    }
    return false;
  }

  /**
   * Continue the halted thread. If the thread is not halted, clear any
   * pending waitRequest.
   * @see #waitRequest()
   */
  public void resume()
  {
    if (isWaiting)
    {
      isCanceled = false;
      wakeUpInternal();
    }
    else
      isWaitRequested = false;
  }

  /**
   * Continue the halted thread and wait for the thread to die at most the given
   * time (in ms). If the thread is not halted, clear any
   * pending waitRequest.
   * @see #cancel()
   * @see #waitRequest()
   */
  public void cancel(long millis)
  {
    if (isWaiting)
    {
      isCanceled = true;
      wakeUpInternal();
      join(millis);
    }
    else
      isWaitRequested = false;
  }

  /**
   * Continue the halted thread and block until the thread is dead.
   * If the thread is not halted, clear any pending waitRequest.
   * @see #cancel(long)
   * @see #waitRequest()
   */
  public void cancel()
  {
    cancel(0);
  }

  /**
   * Set the waitRequest flag.
   * The next time waitIfRequested() is called, the thread will halt.
   * If the thread is already halted, nothing happens.
   * @see #waitIfRequested()
   */
  public void waitRequest()
  {
    if (!isWaiting)
      isWaitRequested = true;
  }

  /**
   * Clear or set the waitRequest flag.
   * @see #waitIfRequested()
   */
  public void waitRequest(boolean wait)
  {
    isWaitRequested = wait;
  }

}
