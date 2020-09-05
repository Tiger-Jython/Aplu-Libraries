// Robot.java

/*
 This software is part of the RaspiSim library.
 It is Open Source Free Software, so you may
 - run the code for any purpose
 - study how the code works and adapt it to your needs
 - integrate all or parts of the code in your own programs
 - redistribute copies of the code
 - improve the code and release your improvements to the public
 However the use of the code is entirely your responsibility.

 Author: Aegidius Pluess, www.aplu.ch
 */
package ch.aplu.raspisim;

import ch.aplu.jgamegrid.*;
import ch.aplu.util.Monitor;
import ch.aplu.util.SoundPlayer;
import ch.aplu.util.SoundPlayerListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;
import javax.swing.JOptionPane;
import java.lang.reflect.*;
import java.io.*;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

/**
 * Class that represents a simulated Raspi brick. Parts (e.g. motors, sensors) may
 be assembled into the robot to make it doing the desired job. Each instance
 creates its own square playground (501 x 501 pixels). Some initial conditions may be modified by
 calling static methods of the class RobotContext in a static block. A typical example
 is:<br><br>
 * <code>
 <font color="#0000ff">import</font><font color="#000000">&nbsp;ch.aplu.raspisim.</font><font color="#c00000">*</font><font color="#000000">;</font><br>
 <font color="#000000"></font><br>
 <font color="#0000ff">public</font><font color="#000000">&nbsp;</font><font color="#0000ff">class</font><font color="#000000">&nbsp;Example</font><br>
 <font color="#000000">{</font><br>
 <font color="#000000">&nbsp;&nbsp;</font><font color="#0000ff">static</font><br>
 <font color="#000000">&nbsp;&nbsp;</font><font color="#000000">{</font><br>
 <font color="#000000">&nbsp;&nbsp;&nbsp;&nbsp;RobotContext.</font><font color="#000000">setStartPosition</font><font color="#000000">(</font><font color="#000000">100</font><font color="#000000">,&nbsp;</font><font color="#000000">100</font><font color="#000000">)</font><font color="#000000">;</font><br>
 <font color="#000000">&nbsp;&nbsp;&nbsp;&nbsp;RobotContext.</font><font color="#000000">setStartDirection</font><font color="#000000">(</font><font color="#000000">45</font><font color="#000000">)</font><font color="#000000">;</font><br>
 <font color="#000000">&nbsp;&nbsp;</font><font color="#000000">}</font><br>
 <font color="#000000"></font><br>
 <font color="#000000">&nbsp;&nbsp;</font><font color="#0000ff">public</font><font color="#000000">&nbsp;</font><font color="#000000">Example</font><font color="#000000">()</font><br>
 <font color="#000000">&nbsp;&nbsp;</font><font color="#000000">{</font><br>
 <font color="#000000">&nbsp;&nbsp;&nbsp;&nbsp;Robot&nbsp;robot&nbsp;</font><font color="#c00000">=</font><font color="#000000">&nbsp;</font><font color="#0000ff">new</font><font color="#000000">&nbsp;</font><font color="#000000">Robot</font><font color="#000000">()</font><font color="#000000">;</font><br>
 <font color="#000000">&nbsp;&nbsp;&nbsp;&nbsp;Gear&nbsp;gear&nbsp;</font><font color="#c00000">=</font><font color="#000000">&nbsp;</font><font color="#0000ff">new</font><font color="#000000">&nbsp;</font><font color="#000000">Gear</font><font color="#000000">()</font><font color="#000000">;</font><br>
 <font color="#000000">&nbsp;&nbsp;&nbsp;&nbsp;robot.</font><font color="#000000">addPart</font><font color="#000000">(</font><font color="#000000">gear</font><font color="#000000">)</font><font color="#000000">;</font><br>
 <font color="#000000">&nbsp;&nbsp;&nbsp;&nbsp;gear.</font><font color="#000000">forward</font><font color="#000000">(</font><font color="#000000">5000</font><font color="#000000">)</font><font color="#000000">;</font><br>
 <font color="#000000">&nbsp;&nbsp;&nbsp;&nbsp;robot.</font><font color="#000000">exit</font><font color="#000000">()</font><font color="#000000">;</font><br>
 <font color="#000000">&nbsp;&nbsp;</font><font color="#000000">}</font><br>
 <font color="#000000"></font><br>
 <font color="#000000">&nbsp;&nbsp;</font><font color="#0000ff">public</font><font color="#000000">&nbsp;</font><font color="#0000ff">static</font><font color="#000000">&nbsp;</font><font color="#0000ff">void</font><font color="#000000">&nbsp;</font><font color="#000000">main</font><font color="#000000">(</font><font color="#00008b">String</font><font color="#000000">[]</font><font color="#000000">&nbsp;args</font><font color="#000000">)</font><br>
 <font color="#000000">&nbsp;&nbsp;</font><font color="#000000">{</font><br>
 <font color="#000000">&nbsp;&nbsp;&nbsp;&nbsp;</font><font color="#0000ff">new</font><font color="#000000">&nbsp;</font><font color="#000000">Example</font><font color="#000000">()</font><font color="#000000">;</font><br>
 <font color="#000000">&nbsp;&nbsp;</font><font color="#000000">}</font><br>
 <font color="#000000">}</font><br>
 * </code><br><br>
 In principle you may remove the static header and use the program unmodified
 for the real robot using the RaspiJLib library.<br><br>

 All sprite images are loaded from subdirectory "sprites"
 of the application folder or, if not found there, from _sprites of the distribution JAR.
 */
public class Robot
{

  // ---------------- Inner class ButtonThread -------------------
  class ButtonThread extends Thread
  {
    private boolean isRunning = false;
    private final int BUTTON_LONGPRESS_DURATION = 10;  // in 200 ms units
    private KeyEvent event;

    public ButtonThread(KeyEvent e)
    {
      isRunning = true;
      event = e;
    }

    public void run()
    {
      int count = 0;
      while (isRunning && count < BUTTON_LONGPRESS_DURATION)
      {
        delay(200);
        count += 1;
      }
      if (isRunning)
        if (buttonListener != null)
          buttonListener.buttonEvent(SharedConstants.BUTTON_LONGPRESSED);
    }

    public void terminate()
    {
      isRunning = false;
      try
      {
        join(200);
      }
      catch (InterruptedException ex)
      {
      }
    }

    private void delay(long timeout)
    {
      try
      {
        Thread.sleep(timeout);
      }
      catch (InterruptedException ex)
      {
      }
    }
  }

  private class MyKeyListener implements GGKeyListener
  {
    private boolean isButtonDown = false;

    public boolean keyPressed(KeyEvent e)
    {
      if (isButtonDown) // avoid repeating key press
        return false;
      isButtonDown = true;
      switch (e.getKeyCode())
      {
        case 10:
          buttonID = BrickButton.ID_ENTER;
          break;
        case 27:
          buttonID = BrickButton.ID_ESCAPE;
          break;
        case 37:
          buttonID = BrickButton.ID_LEFT;
          break;
        case 38:
          buttonID = BrickButton.ID_UP;
          break;
        case 39:
          buttonID = BrickButton.ID_RIGHT;
          break;
        case 40:
          buttonID = BrickButton.ID_DOWN;
          break;
      }

      if (buttonListener != null)
        buttonListener.buttonEvent(SharedConstants.BUTTON_PRESSED);
      buttonThread = new ButtonThread(e);
      buttonThread.start();
      return false;
    }

    public boolean keyReleased(KeyEvent e)
    {
      buttonThread.terminate();
      if (buttonListener != null)
        buttonListener.buttonEvent(SharedConstants.BUTTON_RELEASED);
      isButtonDown = false;
      return false;
    }
  }

  // ------------------- Inner class MyRobot ---------------------------
  private class MyRobot extends Actor
    implements GGActorCollisionListener, GGExitListener
  {
    private MyRobot(Location startLocation, double startDirection)
    {
      super(true, "sprites/raspirobot.gif");  // Rotatable
      gg.setSimulationPeriod(SharedConstants.simulationPeriod);
      if (RobotContext.xLoc > 0 && RobotContext.yLoc > 0)
        gg.setLocation(RobotContext.xLoc, RobotContext.yLoc);
      gg.setBgColor(Color.white);
      gg.setTitle(title);
      gg.removeAllActors();

      int nbObstacles = RobotContext.obstacles.size();
      for (int i = 0; i < nbObstacles; i++)
      {
        gg.addActorNoRefresh(RobotContext.obstacles.get(i),
          RobotContext.obstacleLocations.get(i));
      }
      int nbTargets = RobotContext.targets.size();
      for (int i = 0; i < nbTargets; i++)
      {
        gg.addActorNoRefresh(RobotContext.targets.get(i),
          RobotContext.targetLocations.get(i));
      }
      int nbTorches = RobotContext.torches.size();
      if (nbTorches > 0)
      {
        gg.addMouseListener(
          new GGMouseListener()
          {
            public boolean mouseEvent(GGMouse mouse)
            {
              if (mouse.getEvent() == GGMouse.lPress)
              {
                Location loc = gg.toLocationInGrid(mouse.getX(), mouse.getY());
                // Search for closest
                int dclosest = 1000;
                Torch closest = null;
                for (Torch t : RobotContext.torches)
                {
                  int d = loc.getDistanceTo(t.getLocation());
                  if (d < dclosest)
                  {
                    dclosest = d;
                    closest = t;
                  }
                }
                if (dclosest < 20)
                  tActive = closest;
              }
              if (mouse.getEvent() == GGMouse.lDrag && tActive != null)
              {
                tActive.setPixelLocation(mouse.getX(), mouse.getY());
              }
              if (mouse.getEvent() == GGMouse.lRelease && tActive != null)
              {
                tActive.setPixelLocation(mouse.getX(), mouse.getY());
                tActive = null;
              }
              return true;
            }
          },
          GGMouse.lPress | GGMouse.lDrag | GGMouse.lRelease);
      }
      for (int i = 0; i < nbTorches; i++)
      {
        final Torch t = RobotContext.torches.get(i);
        gg.addActorNoRefresh(t, t.getInitialLoc());
      }

      for (Shadow shadow : RobotContext.shadows)
        gg.addActorNoRefresh(shadow, shadow.getCenter());

      gg.addActorNoRefresh(this, startLocation, startDirection);
      pos = new GGVector(getLocation().x, getLocation().y); // Double coordinates

      wheelDistance = getHeight(0) - 10;
      addActorCollisionListener(this);
      setCollisionCircle(collisionCenter, collisionRadius);

      if (RobotContext.mouseListener != null)
        gg.addMouseListener(RobotContext.mouseListener,
          GGMouse.lPress | GGMouse.lDrag | GGMouse.lRelease);

      gg.addExitListener(this);
      gg.show();
      if (RobotContext.isRun)
        gg.doRun();
      Class appClass = null;
      try
      {
        appClass = Class.forName(new Throwable().getStackTrace()[3].getClassName());
        if (appClass.toString().indexOf("TurtleRobot") != -1)
          appClass = Class.forName(new Throwable().getStackTrace()[4].getClassName());
      }
      catch (Exception ex)
      {
      }
      if (appClass != null)
        exec(appClass, gg, "_init");
    }

    public int collide(Actor actor1, Actor actor2)
    {
      gg.setTitle("Robot-Obstacle Collision");
      isCollisionInfo = true;
      if (collisionListener != null && isCollisionTriggerEnabled)
      {
        new Thread(new Runnable()
        {
          public void run()
          {
            isCollisionTriggerEnabled = false;
            collisionListener.collide();
            isCollisionTriggerEnabled = true;
          }
        }).start();
      }
      return 0;
    }

    public boolean notifyExit()
    {
      exit();
      switch (GameGrid.getClosingMode())
      {
        case TerminateOnClose:
          gg.stopGameThread();
          System.exit(0);
          break;
        case AskOnClose:
          if (JOptionPane.showConfirmDialog(gg.getFrame(),
            "Terminating program. Are you sure?",
            "Please confirm",
            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
          {
            gg.stopGameThread();
            System.exit(0);
          }
          break;
        case DisposeOnClose:
          gg.dispose();
          break;
        case NothingOnClose:
          break;
      }
      return false;
    }

    private void exec(Class appClass, GameGrid gg, String methodName)
    {
      Method execMethod = null;

      Method methods[] = appClass.getDeclaredMethods();
      for (int i = 0; i < methods.length; ++i)
      {
        if (methodName.equals(methods[i].getName()))
        {
          execMethod = methods[i];
          break;
        }
      }
      if (execMethod == null)
        return;

      execMethod.setAccessible(true);
      try
      {
        execMethod.invoke(this, new Object[]
        {
          gg
        });
      }
      catch (IllegalAccessException ex)
      {
//        System.out.println("method not accessible");
      }
      catch (IllegalArgumentException ex)
      {
        //       System.out.println("wrong parameter signature");
      }
      catch (InvocationTargetException ex)
      {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.getTargetException().printStackTrace(pw);
        JOptionPane.showMessageDialog(null, sw.toString()
          + "\n\nApplication will terminate.", "Fatal Error",
          JOptionPane.ERROR_MESSAGE);
        if (GameGrid.getClosingMode() == GameGrid.ClosingMode.TerminateOnClose
          || GameGrid.getClosingMode() == GameGrid.ClosingMode.AskOnClose)
          System.exit(0);
      }
    }

    public void reset()
    {
      pos = new GGVector(getLocationStart().x, getLocationStart().y); // Double coordinates
    }

    public void act()
    {
      synchronized (Robot.class)
      {
        String info = RobotContext.statusText;
        if (!info.equals(Robot.statusText))
        {
          Robot.statusText = info;
          gg.setStatusText(info);
        }

        if (!title.equals("") && isCollisionInfo)
        {
          gg.setTitle("");
        }
        // Add new obstacles as collision actor
        int nb = RobotContext.obstacles.size();
        if (nb > nbObstacles)
        {
          for (int i = nb - 1; i >= nbObstacles; i--)
            addCollisionActor(RobotContext.obstacles.get(i));
          nbObstacles = nb;
        }

        // ------------------ We notify light listeners -------------
        for (Part part : parts)
        {
          if (part instanceof LightSensor)
            ((LightSensor)part).notifyEvent();
        }

        // ------------------ We notify ultrasonic listeners -------------
        for (Part part : parts)
        {
          if (part instanceof UltrasonicSensor)
            ((UltrasonicSensor)part).notifyEvent();
        }

        Gear gear = (Gear)(gg.getOneActor(Gear.class));
        ArrayList<Actor> motors = gg.getActors(Motor.class);
        if (gear != null && !motors.isEmpty())
          fail("Error constructing LegoRobot" + "\nCannot add both Gear and Motor." + "\nApplication will terminate.");

        // ------------------ We have a gear --------------------
        if (gear != null)
        {
          int speed = gear.getSpeed();
          if (speed == 0)
            return;
          Gear.GearState state = gear.getState();
          double radius = gear.getRadius();
          if (state != oldGearState || radius != oldRadius)  // State change
          {
            oldGearState = state;
            oldRadius = radius;
            if (radius != 0)
            {
              if (state == state.LEFT)
              {
                initRot(-Math.abs(radius));
                // dphi = ds / r = v * dt / r, dt = simulation_period (constant)
                dphi = -SharedConstants.gearRotIncFactor * speed / radius;
              }
              if (state == state.RIGHT)
              {
                initRot(Math.abs(radius));
                dphi = SharedConstants.gearRotIncFactor * speed / radius;
              }
            }
          }
          switch (state)
          {
            case FORWARD:
              advance(SharedConstants.nbSteps * speed);
              break;
            case BACKWARD:
              advance(-SharedConstants.nbSteps * speed);
              break;
            case LEFT:
              if (gear.getRadius() == 0)
              {
                dphi = SharedConstants.gearTurnAngle * speed;
                turn(-dphi);
              }
              else
              {
                pos = getRotatedPosition(pos, rotCenter, dphi);
                setLocation(new Location((int)(pos.x), (int)(pos.y)));
                dir += dphi;
                setDirection(dir);
              }
              break;
            case RIGHT:
              if (gear.getRadius() == 0)
              {
                dphi = SharedConstants.gearTurnAngle * speed;
                turn(dphi);
              }
              else
              {
                pos = getRotatedPosition(pos, rotCenter, dphi);
                setLocation(new Location((int)(pos.x), (int)(pos.y)));
                dir += dphi;
                setDirection(dir);
              }
              break;
          }
        }

        // ------------------ We have two motors --------------
        if (!motors.isEmpty() && motors.size() == 2)
        {
          Motor motA = (Motor)motors.get(0);
          Motor motB = (Motor)motors.get(1);
          Motor motorA;
          Motor motorB;
          if (motB.getId() == 0)
          {
            motorA = motA;
            motorB = motB;
          }
          else
          {
            motorA = motB;
            motorB = motA;
          }
          int speedA = motorA.getSpeed();
          int speedB = motorB.getSpeed();
          if (speedA == 0 && speedB == 0)
            return;
          MotorState stateA = motorA.getState();
          MotorState stateB = motorB.getState();
          double radius;

          if (stateA != oldMotorStateA || stateB != oldMotorStateB || speedA != oldSpeedA || speedB != oldSpeedB)  // State change
          {
            Robot.debug("Motors state change");
            oldMotorStateA = stateA;
            oldMotorStateB = stateB;
            debug("State change. motorA: " + stateA + "; motorB:" + stateB);
            oldSpeedA = speedA;
            oldSpeedB = speedB;
            isRotationInit = true;
          }

          if (stateA == MotorState.FORWARD && stateB == MotorState.FORWARD)
          {
            Robot.debug("motorA FORWARD; motorB FORWARD");
            if (speedA == speedB)
              advance(SharedConstants.nbSteps * speedA);
            else
            {
              if (isRotationInit)
              {
                isRotationInit = false;
                sign = (speedA > speedB ? -1 : 1);
                radius = wheelDistance / 2.0 * (speedA + speedB) / Math.abs(speedB - speedA);
                initRot(sign * radius);
                rotInc = SharedConstants.motorRotIncFactor * (speedA + speedB) / radius;
              }
              double rot = sign * rotInc;
              pos = getRotatedPosition(pos, rotCenter, rot);
              setLocation(new Location((int)(pos.x), (int)(pos.y)));
              dir += rot;
              setDirection(dir);
            }
          }
          if (stateA == MotorState.BACKWARD && stateB == MotorState.BACKWARD)
          {
            Robot.debug("motorA BACKWARD; motorB FORWARD");
            if (speedA == speedB)
              advance(-SharedConstants.nbSteps * speedA);
            else
            {
              if (isRotationInit)
              {
                isRotationInit = false;
                sign = (speedA > speedB ? -1 : 1);
                radius = wheelDistance / 2.0 * (speedA + speedB) / Math.abs(speedA - speedB);
                initRot(sign * radius);
                rotInc = SharedConstants.motorRotIncFactor * (speedA + speedB) / radius;
              }
              double rot = -sign * rotInc;
              pos = getRotatedPosition(pos, rotCenter, rot);
              setLocation(new Location((int)(pos.x), (int)(pos.y)));
              dir += rot;
              setDirection(dir);
            }
          }
          if (stateA == MotorState.FORWARD && stateB == MotorState.BACKWARD)
          {
            Robot.debug("motorA FORWARD; motorB BACKWARD");
            if (speedA == speedB)
              turn(-(int)(speedA * SharedConstants.motTurnAngle));
            else
            {
              if (isRotationInit)
              {
                isRotationInit = false;
                sign = (speedA > speedB ? -1 : 1);
                radius = wheelDistance / 200.0 * Math.abs(speedA - speedB);
                initRot(sign * radius);
                rotInc = SharedConstants.motorRotIncFactor * Math.max(speedA, speedB) / (wheelDistance + radius);
              }
              double rot = -rotInc;
              pos = getRotatedPosition(pos, rotCenter, rot);
              setLocation(new Location((int)(pos.x), (int)(pos.y)));
              dir += rot;
              setDirection(dir);
            }
          }
          if (stateA == MotorState.BACKWARD && stateB == MotorState.FORWARD)
          {
            Robot.debug("motorA BACKWARD; motorB BACKWARD");
            if (speedA == speedB)
              turn((int)(speedA * SharedConstants.motTurnAngle));
            else
            {
              if (isRotationInit)
              {
                isRotationInit = false;
                sign = (speedA > speedB ? -1 : 1);
                radius = wheelDistance / 200.0 * Math.abs(speedA - speedB);
                initRot(sign * radius);
                rotInc = SharedConstants.motorRotIncFactor * Math.max(speedA, speedB) / (wheelDistance - Math.abs(radius));
              }
              double rot = rotInc;
              pos = getRotatedPosition(pos, rotCenter, rot);
              setLocation(new Location((int)(pos.x), (int)(pos.y)));
              dir += rot;
              setDirection(dir);
            }
          }
          if (stateA == MotorState.FORWARD && stateB == MotorState.STOPPED)
          {
            Robot.debug("motorA FORWARD; motorB STOPPED");
            if (isRotationInit)
            {
              isRotationInit = false;
              radius = wheelDistance / 2;
              initRot(-radius);
              rotInc = SharedConstants.motorRotIncFactor * speedA / radius;
            }
            double rot = -rotInc;
            pos = getRotatedPosition(pos, rotCenter, rot);
            setLocation(new Location((int)(pos.x), (int)(pos.y)));
            dir += rot;
            setDirection(dir);
          }
          if (stateA == MotorState.BACKWARD && stateB == MotorState.STOPPED)
          {
            Robot.debug("motorA BACKWARD; motorB STOPPED");
            if (isRotationInit)
            {
              isRotationInit = false;
              radius = wheelDistance / 2;
              initRot(-radius);
              rotInc = SharedConstants.motorRotIncFactor * speedA / radius;
            }
            double rot = rotInc;
            pos = getRotatedPosition(pos, rotCenter, rot);
            setLocation(new Location((int)(pos.x), (int)(pos.y)));
            dir += rot;
            setDirection(dir);
          }
          if (stateA == MotorState.STOPPED && stateB == MotorState.FORWARD)
          {
            Robot.debug("motorA STOPPED; motorB FORWARD");
            if (isRotationInit)
            {
              isRotationInit = false;
              radius = wheelDistance / 2;
              initRot(radius);
              rotInc = SharedConstants.motorRotIncFactor * speedB / radius;
            }
            double rot = rotInc;
            pos = getRotatedPosition(pos, rotCenter, rot);
            setLocation(new Location((int)(pos.x), (int)(pos.y)));
            dir += rot;
            setDirection(dir);
          }
          if (stateA == MotorState.STOPPED && stateB == MotorState.BACKWARD)
          {
            Robot.debug("motorA STOPPED; motorB BACKWARD");
            if (isRotationInit)
            {
              isRotationInit = false;
              radius = wheelDistance / 2;
              initRot(radius);
              rotInc = SharedConstants.motorRotIncFactor * speedB / radius;
            }
            double rot = -rotInc;
            pos = getRotatedPosition(pos, rotCenter, rot);
            setLocation(new Location((int)(pos.x), (int)(pos.y)));
            dir += rot;
            setDirection(dir);
          }
          if (stateA == MotorState.STOPPED && stateB == MotorState.STOPPED)
          {
            Robot.debug("motorA STOPPED; motorB STOPPED");
            return;
          }
        }
      }
    }

    private void advance(double d)
    {
      pos = pos.add(
        new GGVector(d * Math.cos(Math.toRadians(getDirection())),
          d * Math.sin(Math.toRadians(getDirection()))));
      setLocation(new Location((int)(pos.x + 0.5), (int)(pos.y + 0.5)));
    }

    private void initRot(double radius)
    {
      GGVector v = new GGVector(getLocation().x, getLocation().y);
      GGVector vDir = new GGVector(
        -Math.sin(Math.toRadians(getDirection())),
        +Math.cos(Math.toRadians(getDirection())));
      GGVector vCenter = v.add(vDir.mult(radius));
      rotCenter = new Point((int)vCenter.x, (int)vCenter.y);
      pos = new GGVector(getLocation().x, getLocation().y);
      dir = getDirection();
    }

    public void turn(double angle)
    {
      synchronized (Robot.class)
      {
        super.turn(angle);
        for (Part p : parts)
        {
          p.turn(angle);
          p.setLocation(getPartLocation(p));
        }
      }
    }

    public void setLocation(Location loc)
    {
      synchronized (Robot.class)
      {
        super.setLocation(loc);
        for (Part p : parts)
          p.setLocation(getPartLocation(p));
      }
    }

    public void setDirection(double dir)
    {
      synchronized (Robot.class)
      {
        super.setDirection(dir);
        for (Part p : parts)
        {
          p.setLocation(getPartLocation(p));
          p.setDirection(dir);
        }
      }
    }

    public Location getPartLocation(Part part)
    {
      Location pos = part.getPosition();
      double r = Math.sqrt(pos.x * pos.x + pos.y * pos.y);
      double phi = Math.atan2(pos.y, pos.x);
      double dir = getDirection() * Math.PI / 180;
      Location loc = new Location(
        (int)(Math.round(getX() + r * Math.cos(dir + phi))),
        (int)(Math.round(getY() + r * Math.sin(dir + phi))));
      return loc;
    }
  }
  // ---------------------- End of class MyRobot ---------------------

  // ---------------------- Class Interceptor --------------------
  private class Interceptor extends PrintStream
  {
    public Interceptor(OutputStream out)
    {
      super(out, true);
    }

    /**
     * Print a boolean value.
     */
    public void print(boolean b)
    {
      gg.setStatusText("" + b);
    }

    /**
     * Print a character.
     */
    public void print(char c)
    {
      gg.setStatusText("" + c);
    }

    /**
     * Print an array of characters.
     */
    public void print(char[] s)
    {
      StringBuffer sbuf = new StringBuffer();
      for (int i = 0; i < s.length; i++)
        sbuf.append(s[i]);
      gg.setStatusText(sbuf.toString());
    }

    /**
     * Print a double-precision floating-point number.
     */
    public void print(double d)
    {
      gg.setStatusText("" + d);
    }

    /**
     * Print a floating-point number.
     */
    public void print(float f)
    {
      gg.setStatusText("" + f);
    }

    /**
     * Print an integer.
     */
    public void print(int i)
    {
      gg.setStatusText("" + i);
    }

    /**
     * Print a long integer.
     */
    public void print(long l)
    {
      gg.setStatusText("" + l);
    }

    /**
     * Print an object.
     */
    public void print(Object obj)
    {
      gg.setStatusText(obj.toString());
    }

    /**
     * Print a string.
     */
    public void print(String s)
    {
      gg.setStatusText(s);
    }

    /**
     *  Terminate the current line by writing the line separator string.
     */
    public void println()
    {
      gg.setStatusText("\n");
    }

    /**
     * Print a boolean and then terminate the line.
     */
    public void println(boolean b)
    {
      gg.setStatusText(b + "\n");
    }

    /**
     * Print a character and then terminate the line.
     */
    public void println(char c)
    {
      gg.setStatusText(c + "\n");
    }

    /**
     * Print an array of characters and then terminate the line.
     */
    public void println(char[] s)
    {
      StringBuffer sbuf = new StringBuffer();
      for (int i = 0; i < s.length; i++)
        sbuf.append(s[i]);
      gg.setStatusText(sbuf.toString() + "\n");
    }

    /**
     * Print a double and then terminate the line.
     */
    public void println(double d)
    {
      gg.setStatusText(d + "\n");
    }

    /**
     * Print a float and then terminate the line.
     */
    public void println(float f)
    {
      gg.setStatusText(f + "\n");
    }

    /**
     * Print an integer and then terminate the line.
     */
    public void println(int i)
    {
      gg.setStatusText(i + "\n");
    }

    /**
     * Print a long and then terminate the line.
     */
    public void println(long l)
    {
      gg.setStatusText(l + "\n");
    }

    /**
     * Print an Object and then terminate the line.
     */
    public void println(Object obj)
    {
      gg.setStatusText(obj.toString() + "\n");
    }

    /**
     * Print a String and then terminate the line.
     */
    public void println(String s)
    {
      gg.setStatusText(s + "\n");
    }

    /**
     * Print a formatted string using the specified format string and varargs.
     * (See PrintStream.printf() for more information)
     * @return the PrintStream reference
     */
    public PrintStream printf(String format, Object... args)
    {
      gg.setStatusText(String.format(format, args));
      return this;
    }

    /**
     * Print a formatted string using the specified format string and varargs
     * and applying given locale during formatting.
     * (See PrintStream.printf() for more information)
     * @return the PrintStream reference
     */
    public PrintStream printf(Locale l, String format, Object... args)
    {
      gg.setStatusText(String.format(l, format, args));
      return this;
    }
  }
  // ---------------------- End of class Interceptor -------------
  /**
   * Center of a circle to detect robot-obstacle collisions
   * (pixel coordinates relative to center of robot image, default: (-13, 0)).
   */
  public static Point collisionCenter = new Point(-13, 0);
  /**
   * Radius of a circle to detect robot-obstacle collisions
   * (in pixels, default: 20).
   */
  public static int collisionRadius = 16;
  //
  private final int nbRotatableSprites = 360;
  private static GameGrid gg;
  private static MyRobot robot;
  private int nbObstacles = 0;
  private String title = "RaspiSim V"
    + SharedConstants.VERSION.substring(0, SharedConstants.VERSION.indexOf(" "));
  private ArrayList<Part> parts = new ArrayList<Part>();
  private double rotInc;
  private int currentSpeed;
  private MotorState oldMotorStateA = MotorState.STOPPED;
  private MotorState oldMotorStateB = MotorState.STOPPED;
  private int oldSpeedA = -1;
  private int oldSpeedB = -1;
  private boolean isRotationInit = true;
  private int wheelDistance;
  private Gear.GearState oldGearState = Gear.GearState.STOPPED;
  private Point rotCenter;
  private GGVector pos;
  private double dir;
  private int sign;
  private double oldRadius;
  private double dphi;
  private boolean isCollisionInfo = false;
  private CollisionListener collisionListener = null;
  private boolean isCollisionTriggerEnabled = true;
  private int buttonID;
  private ButtonListener buttonListener = null;
  private Torch tActive = null;
  private static String statusText = "";
  private ButtonThread buttonThread = null;

  /**
   * Creates a robot with its playground using defaults from RobotContext.
   */
  public Robot()
  {
    gg = new GameGrid(500, 500, 1, null,
      RobotContext.imageName, RobotContext.isNavigationBar, nbRotatableSprites);
    if (RobotContext.isError)
    {
      if (GameGrid.getClosingMode() == GameGrid.ClosingMode.TerminateOnClose
        || GameGrid.getClosingMode() == GameGrid.ClosingMode.AskOnClose)
        System.exit(1);
    }
    robot = new MyRobot(RobotContext.startLocation, RobotContext.startDirection);
    ViewingCone.init();
    gg.addKeyListener(new MyKeyListener());
    if (RobotContext.isStatusBar)
    {
      gg.addStatusBar(RobotContext.statusBarHeight);
      gg.setStatusText(RobotContext.statusText);
      PrintStream originOut = System.out;
      PrintStream interceptor = new Interceptor(originOut);
      System.setOut(interceptor);
    }
    RobotInstance.setRobot(this);
    // Add deferred parts to GameGrid
    for (Part part : RobotInstance.partsToAdd)
    {
      debug("Adding part: " + part);
      addPart(part);
    }
  }

  protected void addPart(Part part)
  {
    synchronized (Robot.class)
    {
      parts.add(part);
      gg.addActor(part, robot.getPartLocation(part), robot.getDirection());
      gg.setPaintOrder(getClass(), part.getClass());  // On top of obstacles
      gg.setActOrder(getClass());  // First
      gg.setPaintOrder(Torch.class, Shadow.class);  // Torches on top
    }
  }

  protected void removePart(Part part)
  {
    synchronized (Robot.class)
    {
      parts.remove(part);
      gg.removeActor(part);
      gg.refresh();
    }
  }

  /**
   * Returns the instance reference of the GameGrid.
   * @return the reference of the GameGrid
   */
  public static GameGrid getGameGrid()
  {
    return gg;
  }

  /**
   * Returns the current state of the underlying GameGrid window.
   * @return true, if the window not disposed; otherwise false
   */
  public boolean isConnected()
  {
    return !gg.isDisposed();
  }

  /**
   * Returns the instance reference of the MyRobot actor.
   * @return the reference of the MyRobot
   */
  public static Actor getRobot()
  {
    return robot;
  }

  /**
   * Stops any motion and performs a cleanup of all parts.
   */
  public void exit()
  {
    synchronized (Robot.class)
    {
      for (Part p : parts)
      {
        p.cleanup();
      }
    }
    Led.clearAll();
    gg.doPause();
  }

  /**
   * Returns the current library version.
   * @return a string telling the current version
   */
  public static String getVersion()
  {
    return SharedConstants.VERSION;
  }

  protected static void fail(String message)
  {
    if (GameGrid.getClosingMode() == GameGrid.ClosingMode.TerminateOnClose
      || GameGrid.getClosingMode() == GameGrid.ClosingMode.AskOnClose)
    {
      JOptionPane.showMessageDialog(null, message, "Fatal Error", JOptionPane.ERROR_MESSAGE);
      System.exit(0);
    }
    if (GameGrid.getClosingMode() == GameGrid.ClosingMode.DisposeOnClose)
      throw new RuntimeException("Fatal Error.\n" + message);
  }

  /**
   * Resets MyRobot to start location/direction.
   */
  public void reset()
  {
    Actor robot = getRobot();
    robot.reset();
    robot.setLocation(robot.getLocationStart());
    robot.setDirection(robot.getDirectionStart());
  }

  /**
   * Adds the given target in the target list and shows it at the given 
   * location. If the target is already in the target list, it is first removed.
   * @param target the target to add
   * @param x the x location of the target center
   * @param y the y location of the target center
   */
  public void addTarget(Target target, int x, int y)
  {
    synchronized (RobotContext.targets)
    {
      if (RobotContext.targets.contains(target))
      {
        removeTarget(target);
      }
      gg.addActorNoRefresh(target, new Location(x, y));
      RobotContext.targets.add(target);
      // targetLocations not used
    }
  }

  /**
   * Removes the given target from the target list and hides it.
   * If the target is not part of the target list, nothing happens.
   * @param target the target to remove
   */
  public void removeTarget(Target target)
  {
    synchronized (RobotContext.targets)
    {
      Location l = target.getLocation();
      GGBackground bg = gg.getBg();
      ViewingCone.eraseCone(bg);
      ViewingCone.eraseProximityCircle(bg);
      Color oldPaintColor = bg.getPaintColor();
      bg.setPaintColor(bg.getBgColor());
      Point[] mesh = target.getMesh();
      Point[] clearMesh = new Point[mesh.length];
      for (int i = 0; i < mesh.length; i++)
      {
        clearMesh[i] = new Point(mesh[i].x + l.x, mesh[i].y + l.y);
      }
      bg.fillPolygon(clearMesh);
      bg.setPaintColor(oldPaintColor);
      gg.removeActor(target);
      RobotContext.targets.remove(target);
    }
  }

  /**
   * Adds the given obstacle in the obstacle list and shows it at the given 
   * location. If the obstacle is already in the obstacle list, it is first removed.
   * @param obstacle the obstacle to add
   * @param x the x location of the target center
   * @param y the y location of the target center
   */
  public void addObstacle(Obstacle obstacle, int x, int y)
  {
    synchronized (RobotContext.obstacles)
    {
      if (RobotContext.obstacles.contains(obstacle))
      {
        removeObstacle(obstacle);
      }
      gg.addActorNoRefresh(obstacle, new Location(x, y));
      RobotContext.obstacles.add(obstacle);
      // obstacleLocations not used
    }
  }

  /**
   * Removes the given obstacle from the obstacle list and hides it.
   * If the obstacle is not part of the obstacle list, nothing happens.
   * @param obstacle the obstacle to remove
   */
  public void removeObstacle(Obstacle obstacle)
  {
    synchronized (RobotContext.obstacles)
    {
      gg.removeActor(obstacle);
      RobotContext.obstacles.remove(obstacle);
    }
  }

  /**
   * Registers a robot-obtacle collision listener that fires
   * the collide callback when the circumcircle of the robot overlaps
   * with an obstacle.
   * @param listener the CollisionListener to register
   */
  public void addCollisionListener(CollisionListener listener)
  {
    collisionListener = listener;
  }

  /**
   * Registers a button listener that simulates the events
   * when the brick buttons is hit. 
   * @param listener the ButtonListener to register
   */
  public void addButtonListener(ButtonListener listener)
  {
    buttonListener = listener;
  }

  /**
   * Returns true, if any of the buttons was hit.
   * Calls Thread.sleep(10) to be used in narrow loops.
   * A button listener must be started before by calling startButtonListener.
   * @return true, if a button was hit or the simulation window is closed
   */
  public boolean isButtonHit()
  {
    Tools.delay(10);
    if (gg.isDisposed())
      return true;
    return (buttonID != 0);
  }

  /**
   * Returns the button ID of the button previously hit. The button hit
   * buffer is cleared then.
   * @return the ID of the button; if the hit buffer is empty or the button listener is
   * is not running or the simulation window is closed, return 0
   */
  public int getHitButtonID()
  {
    if (gg.isDisposed())
      return 0;
    int id = buttonID;
    buttonID = 0;
    return id;
  }

  /**
   * Returns true, if the UP button was the last button hit since
   * the last call of this method. On return, the button hit is cleared.
   * Calls Thread.sleep(10) to be used in narrow loops.
   * @return true, if the up button was clicked or the simulation window is closed  
   */
  public boolean isUpHit()
  {
    Tools.delay(10);
    if (gg.isDisposed())
      return true;
    boolean pressed = (buttonID == BrickButton.ID_UP);
    if (pressed)
      buttonID = 0;
    return pressed;
  }

  /**
   * Returns true, if the DOWN button was the last button hit since
   * the last call of this method. On return, the button hit is cleared.
   * Calls Thread.sleep(10) to be used in narrow loops.
   * @return true, if the up button was clicked or the simulation window is closed
   */
  public boolean isDownHit()
  {
    Tools.delay(10);
    if (gg.isDisposed())
      return true;
    boolean pressed = (buttonID == BrickButton.ID_DOWN);
    if (pressed)
      buttonID = 0;
    return pressed;
  }

  /**
   * Returns true, if the LEFT button was the last button hit since
   * the last call of this method. On return, the button hit is cleared.
   * Calls Thread.sleep(10) to be used in narrow loops.
   * @return true, if the up button was clicked or the simulation window is closed
   */
  public boolean isLeftHit()
  {
    Tools.delay(10);
    if (gg.isDisposed())
      return true;
    boolean pressed = (buttonID == BrickButton.ID_LEFT);
    if (pressed)
      buttonID = 0;
    return pressed;
  }

  /**
   * Returns true, if the RIGHT button was the last button hit since
   * the last call of this method. On return, the button hit is cleared.
   * Calls Thread.sleep(10) to be used in narrow loops.
   * @return true, if the up button was clicked or the simulation window is closed
   */
  public boolean isRightHit()
  {
    Tools.delay(10);
    if (gg.isDisposed())
      return true;
    boolean pressed = (buttonID == BrickButton.ID_RIGHT);
    if (pressed)
      buttonID = 0;
    return pressed;
  }

  /**
   * Returns true, if the ENTER button was the last button hit since
   * the last call of this method. On return, the button hit is cleared.
   * Calls Thread.sleep(10) to be used in narrow loops.
   * @return true, if the up button was clicked or the simulation window is closed
   */
  public boolean isEnterHit()
  {
    Tools.delay(10);
    if (gg.isDisposed())
      return true;
    boolean pressed = (buttonID == BrickButton.ID_ENTER);
    if (pressed)
      buttonID = 0;
    return pressed;
  }

  /**
   * Returns true, if the ESCAPE button was the last button hit since
   * the last call of this method. On return, the button hit is cleared.
   * Calls Thread.sleep(10) to be used in narrow loops.
   * @return true, if the up button was clicked or the simulation window is closed
   */
  public boolean isEscapeHit()
  {
    Tools.delay(10);
    if (gg.isDisposed())
      return true;
    boolean pressed = (buttonID == BrickButton.ID_ESCAPE);
    if (pressed)
      buttonID = 0;
    return pressed;
  }

  private int getButtonID()
  {
    int key = gg.getKeyCode();
    switch (key)
    {
      case 10:
        return BrickButton.ID_ENTER;
      case 27:
        return BrickButton.ID_ESCAPE;
      case 37:
        return BrickButton.ID_LEFT;
      case 38:
        return BrickButton.ID_UP;
      case 39:
        return BrickButton.ID_RIGHT;
      case 40:
        return BrickButton.ID_DOWN;
      default:
        return 0;
    }
  }

  /**
   * Returns always false. For compatiblity with EV3JLib.
   * @return false
   */
  public boolean isAutonomous()
  {
    return false;
  }

  /**
   * Plays a sine tone with given frequency and duration and maximum volume.
   * The method blocks until the tone is finished.
   * @param frequency the frequency of the tone (in Hertz) (double rounded to int)
   * @param duration the duration of the tone (in Millisec) (double rounded to int)
   */
  public void playTone(double frequency, double duration)
  {
    playTone(127, frequency, duration, true);
  }

  /**
   * Plays a sine tone with given frequency and duration.
   * @param volume the sound volume (0..100) (double rounded to int)
   * @param frequency the frequency of the tone (in Hertz) (double rounded to int)
   * @param duration the duration of the tone (in Millisec) (double rounded to int)
   * @param blocking if true, the methods blocks until the tone is finished
   */
  public void playTone(double volume, double frequency, double duration, boolean blocking)
  {
    AudioFormat audioFormat = getAudioFormat();
    ByteArrayOutputStream data = new ByteArrayOutputStream();
    float sampleRate = audioFormat.getSampleRate();
    int nbFrames = (int)(duration / 1000 * sampleRate);
    double t = 0;
    double durationSec = duration / 1000;
    double dt = 1.0 / sampleRate;
    double tAttack;
    double tRelease;
    if (duration > 100)
    {
      tAttack = 0.05;
      tRelease = 0.05;
    }
    else
    {
      tAttack = durationSec / 2;
      tRelease = durationSec / 2;
    }
    int amplitude = Math.max(100, (int)(volume));
    for (int i = 0; i < nbFrames; i++)
    {
      int a;
      if (t < tAttack)
        a = (int)(t / tAttack * amplitude);  // Attack
      else if (t > durationSec - tRelease)  // Release
        a = (int)((durationSec - t) / tRelease * amplitude);
      else  // Substain
        a = amplitude;

      byte soundData = (byte)(a * Math.sin(2 * Math.PI * frequency * t));
      data.write(soundData);
      t += dt;
    }
    try
    {
      data.close();
    }
    catch (IOException ex)
    {
    }

    InputStream is
      = new ByteArrayInputStream(data.toByteArray());
    AudioInputStream audioInputStream
      = new AudioInputStream(is, audioFormat,
        data.size() / audioFormat.getFrameSize());
    try
    {
      SoundPlayer player = new SoundPlayer(audioInputStream);
      if (blocking)
      {
        player.addSoundPlayerListener(
          new SoundPlayerListener()
          {
            public void notifySoundPlayerStateChange(int reason, int mixerIndex)
            {
              switch (reason)
              {
                case 0:
//                System.out.println("Notify: start playing");
                  break;
                case 1:
//                System.out.println("Notify: resume playing");
                  break;
                case 2:
//                System.out.println("Notify: pause playing");
                  break;
                case 3:
//                System.out.println("Notify: stop playing");
                  break;
                case 4:
//                System.out.println("Notify: end of resource");
                  Tools.delay(400);
                  Monitor.wakeUp();
                  break;
              }
            }
          }
        );
      }
      player.play();
      if (blocking)
        Monitor.putSleep();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  private AudioFormat getAudioFormat()
  {
    // 8000,11025,16000,22050,44100
    float sampleRate = 22050.0F;
//   float sampleRate = 44100.0F;

    // 8,16
    int sampleSizeInBits = 8;

    // 1,2
    int channels = 1;

    // true,false
    boolean signed = true;

    // true,false
    boolean bigEndian = false;

    return new AudioFormat(sampleRate, sampleSizeInBits,
      channels, signed, bigEndian);
  }

  public static void debug(String msg)
  {
    if (SharedConstants.debug)
      System.out.println(msg);
  }
}
