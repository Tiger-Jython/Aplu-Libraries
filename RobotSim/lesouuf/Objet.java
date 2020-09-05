
package ch.aplu.robotsim;
/*
 *  10/02/2017 D.Lesouëf
 *  Objet saisi par la pince 
 *  
 */

import java.awt.Point;

import javax.swing.JOptionPane;

//import ch.aplu.ev3.TouchSensor.SensorThread;
import ch.aplu.jgamegrid.Actor;
import ch.aplu.jgamegrid.GameGrid;
import ch.aplu.jgamegrid.Location;

public class Objet extends Part
{
  private static SensorPort port;
  private volatile boolean isRunning = false;

  String imageName;
  /*
   * modification de la position des capteurs 3 à l'avant, 3 à l'arrière
   */

  public Objet(TouchSensor ts)
  {
    super(((Obstacle)ts.getCollisionActor()).getImageName(),//fonctionne ne pas toucher!!!
      new Location((int)((ts.getCollisionActor().getX() - robot.robot.getLocation().getX())
        * Math.cos((getRobot().robot.getDirection() - ts.getCollisionActor().getDirection()) / 180 * Math.PI)
        + (ts.getCollisionActor().getY() - robot.robot.getLocation().getY())
        * Math.sin((getRobot().robot.getDirection() - ts.getCollisionActor().getDirection()) / 180 * Math.PI)),
        (int)(-(ts.getCollisionActor().getX() - robot.robot.getLocation().getX())
        * Math.sin((getRobot().robot.getDirection() - ts.getCollisionActor().getDirection()) / 180 * Math.PI)
        + (ts.getCollisionActor().getY() - robot.robot.getLocation().getY())
        * Math.cos((getRobot().robot.getDirection() - ts.getCollisionActor().getDirection()) / 180 * Math.PI))));
    imageName = ((Obstacle)ts.getCollisionActor()).getImageName();

  }

  String getImageName()
  {
    return imageName;
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
        "TouchSensor is not part of the LegoRobot.\n"
        + "Call addPart() to assemble it.",
        "Fatal Error", JOptionPane.ERROR_MESSAGE);
      if (GameGrid.getClosingMode() == GameGrid.ClosingMode.TerminateOnClose
        || GameGrid.getClosingMode() == GameGrid.ClosingMode.AskOnClose)
        System.exit(1);
      if (GameGrid.getClosingMode() == GameGrid.ClosingMode.DisposeOnClose)
        throw new RuntimeException("TouchSensor is not part of the LegoRobot.\n"
          + "Call addPart() to assemble it.");
    }
  }
}
