import ch.aplu.ev3.*; // librairie EV3Sim 

public class ExGyro
{
  public ExGyro() {
    LegoRobot robot = new LegoRobot();
    Gear gear = new Gear();
    robot.addPart(gear);
    
    LegoLCD LCD=new LegoLCD();// Ecran du robot
    gear.setAngleFactor(180/195.0);// facteur correcteur pour le robor réel
    gear.setDistFactor(100/97.5);// facteur correcteur pour le robor réel
    
    GyroSensor gs = new GyroSensor(SensorPort.S4);
    robot.addPart(gs);
    LCD.drawString("Enter pour ",0,0);
    LCD.drawString("démarrer ",0,1);
    robot.Lancement();
    
    gear.left();
    LCD.drawString("v="+gs.getVelocity(),0,2);
    while((gs.getValue()<300)&&(!robot.isEnterHit()))LCD.drawString("angle="+gs.getValue(),0,4);
    gear.stop();
    
    Tools.delay(2000);
    robot.exit();
  }
  public static void main(String[] args) {
    new ExGyro();
  }
  static
  { RobotContext.setStartPosition(250, 200);
    RobotContext.setStartDirection(-90);
  }
}
