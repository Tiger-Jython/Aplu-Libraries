import ch.aplu.ev3.*; // librairie EV3Sim 
import java.awt.*;

public class ExColor
{
  int [] colorCube;
  public ExColor() {
    LegoRobot robot = new LegoRobot();
    Gear gear = new Gear();
    robot.addPart(gear);
    
    LegoLCD LCD=new LegoLCD();// Ecran du robot
    gear.setAngleFactor(180/195.0);// facteur correcteur pour le robor réel
    gear.setDistFactor(100/97.5);// facteur correcteur pour le robor réel
    
    ColorSensor cs = new ColorSensor(SensorPort.S4);
    robot.addPart(cs);
    LCD.drawString("Enter pour ",0,0);
    LCD.drawString("démarrer ",0,1);
    robot.Lancement();
    colorCube=cs.calibrateColorCube(Color.red,5,2000,true);
    gear.forward();
    while ((!cs.inColorCube(colorCube))&&(!robot.isEnterHit()));
    gear.stop();
    
    Tools.delay(2000);
    robot.exit();
  }
  public static void main(String[] args) {
    new ExColor();
  }
  static
  { RobotContext.setStartPosition(250, 200);
    RobotContext.setStartDirection(-90);
    RobotContext.useBackground("sprites/colorbar.gif");
  }
}
