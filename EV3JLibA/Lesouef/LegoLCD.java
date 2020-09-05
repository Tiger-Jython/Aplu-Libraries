package ch.aplu.ev3;
/*
 *  Auteur D.LESOUEF 2016-06-04
 */
import lejos.hardware.lcd.LCD;

public class LegoLCD {
	
	public LegoLCD() {
	}

	public void drawString(String text, int i, int j){	
		LCD.clear(j);
		LCD.drawString( text, i, j);
		LCD.refresh();
	}
	
	public void drawString(int n,int i, int j){
		LCD.clear(j);
		LCD.drawString(Integer.toString(n),i,j);
		LCD.refresh();
	}
	
	public void drawString(double x,int i, int j){
		LCD.clear(j);
		LCD.drawString(Double.toString(x),i,j);
		LCD.refresh();
	}
	
	public void clear(){
		LCD.clear();
	}
	
	public void refresh(){
		LCD.refresh();
	}
	
}
