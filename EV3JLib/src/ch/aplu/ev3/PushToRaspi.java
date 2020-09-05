// PushToRaspi.java

package ch.aplu.ev3;

import java.io.*;
import ch.aplu.bluetooth.*;
import javax.bluetooth.*;

public class PushToRaspi
{
  private boolean debug = false;
  
  // ------------- Inner class TransferThread ---------------
  private class TransferThread extends Thread
  {
    public void run()
    {
      isTransferSuccessful = true;
      boolean rc = sendFile(fsrcFile, dos);
      delay(2000);
      if (!rc)
      {
        System.out.println("Error while sending file " + fsrcFile);
        isTransferSuccessful = false;
      }
      try
      {
        dos.close();
      }
      catch (Exception ex)
      {
      }
      isDone = true;
    }
  }
  // 
  //
  private File fsrcFile;
  private String serverName;
  private final String serviceName = "RaspiServer";
  private volatile boolean isDone = false;
  private volatile boolean isTransferSuccessful;
  private BluetoothClient bc;
  private DataOutputStream dos;

  public PushToRaspi(String btName, String srcFile)
  {
    serverName = btName;
    fsrcFile = new File(srcFile);
    if (!BluetoothFinder.isBluetoothSupported())
    {
      System.out.println("Sorry. Bluetooth not supported.");
      return;
    }
 
    RemoteDevice rd = BluetoothFinder.searchPreknownDevice(serverName);
    if (rd == null)
    {
      System.out.println("Sorry. Device '" + serverName + "' not paired.");
      return;
    }
    
    bc = new BluetoothClient(rd, serviceName);
    if (debug)
      bc.setVerbose(true);
    System.out.println("Please wait a moment...");

    double startTime = System.currentTimeMillis();
    int nbTrials = 1;
    int maxTrials = 10;
    while (nbTrials < maxTrials)
    {
      boolean rc = bc.connect();
      if (!rc)
        nbTrials++;
      else
        break;

    }
    if (nbTrials == maxTrials)
    {
      String msg = "\nConnection failed.\n"
        + "Possible reasons:\n"
        + "- Robot not ready\n"
        + "- Bluetooth devices not paired\n"
        + "- Wrong Bluetooth name";
      System.out.println(msg);
      return;
    }

    System.out.println("Connection established. Transferring data...");
    dos = new DataOutputStream(bc.getOutputStream());
    new TransferThread().start();

    while (!isDone)
      delay(100);

    bc.disconnect();

    if (!isTransferSuccessful)
      System.out.println("Transfer failed.\nPlease restart transfer");
    else
    {  
      System.out.println("Program successfully downloaded to /home/pi/scripts/MyApp.py.");
      System.out.println("   " + fsrcFile.length() + " bytes transferred");
      double endTime = System.currentTimeMillis();
      System.out.println("   Time elapsed: " + (int)(endTime - startTime) + " ms");
    }
  }

  private boolean sendFile(File srcFile, DataOutputStream dos)
  {
//    System.out.println("sendFile() with " + srcFile);
    InputStream in = null;
    try
    {
      in = new FileInputStream(srcFile);
      byte[] buf = new byte[256];
      int len;
      while ((len = in.read(buf)) > 0)
        dos.write(buf, 0, len);
      dos.flush();
    }
    catch (IOException ex)
    {
      System.out.println("Fatal error. IOException " + ex);
      return false;
    }
    finally
    {
      try
      {
        in.close();
      }
      catch (Exception ex)
      {
      }
    }
    return true;
  }

  private void delay(long timeout)
  {
    try
    {
      Thread.currentThread().sleep(timeout);
    }
    catch (InterruptedException ex)
    {
    }
  }
}


