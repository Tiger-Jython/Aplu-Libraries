import ch.aplu.ev3.*; // librairie EV3Sim 

public class ExMove
{
  public ExMove() {
    LegoRobot robot = new LegoRobot();
    Gear gear = new Gear();
    robot.addPart(gear);
    LegoLCD LCD=new LegoLCD();// Ecran du robot
    gear.setAngleFactor(180/195.0);// facteur correcteur pour le robor réel
    gear.setDistFactor(100/97.5);// facteur correcteur pour le robor réel 
    LCD.drawString("Enter pour ",0,0);
    LCD.drawString("démarrer ",0,1);
    robot.Lancement();//Appuyer sur une touche pour démarrer
    
    for (int i=0;i<6;i++)
    {
      gear.forward(100.0);
      gear.right(60.0);
    }
    Tools.delay(2000);
    robot.exit(); 
  }
  
  public static void main(String[] args) {
    
    new ExMove();
  }

  static
  { RobotContext.setStartPosition(250, 200);
    RobotContext.setStartDirection(-90);
  }
}
