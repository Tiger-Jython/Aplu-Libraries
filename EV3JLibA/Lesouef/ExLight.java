import ch.aplu.ev3.*;

public class ExLight
{
  public ExLight() {
    LegoRobot robot = new LegoRobot();
    Gear gear = new Gear();
    robot.addPart(gear);
    LegoLCD LCD=new LegoLCD();// Ecran du robot
    gear.setAngleFactor(180/195.0);// facteur correcteur pour le robor réel
    gear.setDistFactor(100/97.5);// facteur correcteur pour le robor réel
    
    LightSensor ls = new LightSensor(SensorPort.S4);
    robot.addPart(ls);
    ls.activate(true);
    
    LCD.drawString("Enter pour ",0,0);
    LCD.drawString("démarrer ",0,1);
    robot.Lancement();//
    
    int black=ls.calibrate(300,true);// nécessaire pour l'ev3 
    gear.backward();
    while ((ls.getValue()>black)&&(!robot.isEnterHit()))
    LCD.drawString(ls.getValue(),0,4);
    gear.stop();
    
    Tools.delay(2000);
    robot.exit();
  }
  public static void main(String[] args) {
    new ExLight();
  }
  
  static
  { RobotContext.setStartPosition(250, 200);
    RobotContext.setStartDirection(-90);
    RobotContext.useBackground("sprites/whiteCircle2.gif");
  }
}
