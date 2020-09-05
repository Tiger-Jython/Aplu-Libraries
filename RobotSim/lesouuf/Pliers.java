
package ch.aplu.robotsim;
/*
 *  10/02/2017 D.Lesouëf
 *  Simulation d'une pince 
 *  La saisie a lieu avec le capteur de contact
 */

import ch.aplu.jgamegrid.*;

import java.awt.*;
import javax.swing.JOptionPane;

/**
 * Class that represents a touch sensor.
 */
public class Pliers extends Part
{
  private int upwards = 0;
  private boolean isOpen = true;
  boolean grasped = false;
  Objet obj;
  private boolean Upwards;
  private boolean isRunning;
  static String[][] pince =
  {
    {
      "sprites/pinceOpen.gif", "sprites/pinceClose.gif"
    }, 
    {
      "sprites/pinceOpen_rear.gif", "sprites/pinceClose_rear.gif"
    }
  };
  private static GameGrid gg;
  /*
   * modification de la position des capteurs 3 à l'avant, 3 à l'arrière
   */
  private static final Location[][] locs = new Location[][]
  {
    {
      new Location(12, 10), new Location(12, 0), new Location(12, -10), new Location(-35, 0)
    },
    {
      new Location(-35, 10), new Location(-35, 0), new Location(-35, -10), new Location(12, 0)
    }
  };

  /**
   * Creates a sensor instance pointing downwards connected to the given port.
   * In simulation mode, the sensor detects the brightness of the background pixel.
   * The port selection determines the position of the sensor:
   * S1: right, S2: middle, S3: left, S4: rear-middle.
   * @param port the port where the sensor is plugged-in
   */
  public Pliers(int port)
  {
    this(port, false);
  }

  /**
   * Creates a sensor instance connected to the given port.
   * The port selection determines the position of the sensor:<br>
   * Downward direction: 1: right; 2: middle, 3: left , S4: rear-middle<br>
   * Upward direction: 1: rear right; 2: rear-middle, 3: rear left , S4: middle <br>
   * A sensor directed downwards detects the intensity of the background,
   * while a sensor directed upwards detects the summed intensity of all torches.
   * @param port the port where the sensor is plugged-in
   * @param upwards if true, the sensor is pointing upwards; otherwise pointing downwards
   */
  public Pliers(int port, boolean Upwards)
  {
    super(pince[Upwards ? 1 : 0], locs[Upwards ? 1 : 0][port - 1]
    );
    this.Upwards = Upwards;
  }

  public Pliers(SensorPort port, int upwards)
  {
    super(pince[upwards], port == SensorPort.S1 ? locs[upwards == 1 ? 1 : 0][0]
      : (port == SensorPort.S2 ? locs[upwards == 1 ? 1 : 0][1]
      : locs[upwards == 1 ? 1 : 0][2]));
    this.upwards = upwards;
  }

  public void open()
  {
    isOpen = true;
    this.show(0);
    if (grasped)
    {
      grasped = false;
      robot.removePart(obj);

      Obstacle obs = new Obstacle(obj.getImageName());
      //obs.setDirection(45);
      robot.addObstacle(obs, obj.getX(), obj.getY(), robot.robot.getDirection());
    }

  }

  public void open(int delay)
  {
    isOpen = true;
    this.show(0);
    if (grasped)
    {
      grasped = false;
      robot.removePart(obj);

      Obstacle obs = new Obstacle(obj.getImageName());
      //obs.setDirection(45);
      robot.addObstacle(obs, obj.getX(), obj.getY(), robot.robot.getDirection());
    }

  }

  public void close()
  {
    isOpen = false;
    this.show(1);
  }

  public void close(int delay)
  {
    isOpen = false;
    this.show(1);
  }

  public void grasp(TouchSensor ts)
  {
    this.show(1);
    obj = new Objet(ts);
    robot.removeObstacle((Obstacle)ts.getCollisionActor());//Evite la disparition de l'obstacle un instant
    robot.addPart(obj, ts.getCollisionActor().getDirection() - ((Actor)robot.robot).getDirection());//l'objet reste parallèle à lui même
    grasped = true;

  }

  public void grasp(TouchSensor ts, int delay)
  {
    this.show(1);
    obj = new Objet(ts);
    robot.removeObstacle((Obstacle)ts.getCollisionActor());//Evite la disparition de l'obstacle un instant
    robot.addPart(obj, ts.getCollisionActor().getDirection() - ((Actor)robot.robot).getDirection());//l'objet reste parallèle à lui même
    grasped = true;
  }

  protected void cleanup()
  {
    isRunning = false;
  }

  private void checkPart()
  {
    if (robot == null)
    {
      JOptionPane.showMessageDialog(null,
        "Pince is not part of the LegoRobot.\n"
        + "Call addPart() to assemble it.",
        "Fatal Error", JOptionPane.ERROR_MESSAGE);
      if (GameGrid.getClosingMode() == GameGrid.ClosingMode.TerminateOnClose
        || GameGrid.getClosingMode() == GameGrid.ClosingMode.AskOnClose)
        System.exit(1);
      if (GameGrid.getClosingMode() == GameGrid.ClosingMode.DisposeOnClose)
        throw new RuntimeException("Pince is not part of the LegoRobot.\n"
          + "Call addPart() to assemble it.");
    }
  }
}
