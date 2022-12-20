// BtDeviceInfo.java

/*
This software is part of the Aplu Bluetooth Library.
It is Open Source Free Software, so you may
- run the code for any purpose
- study how the code works and adapt it to your needs
- integrate all or parts of the code in your own programs
- redistribute copies of the code
- improve the code and release your improvements to the public
However the use of the code is entirely your responsibility.
 */


package ch.aplu.bluetooth;

import javax.bluetooth.*;

/**
 * Class to pack device information. 
 */
public class BtDeviceInfo
{
  private RemoteDevice remoteDevice;
  private DeviceClass deviceClass;

  /**
   * Creates a BtDeviceInfo instance.
   * @param dev the RemoteDevice
   * @param dc the DeviceClass
   */
  public BtDeviceInfo(RemoteDevice dev, DeviceClass dc)
  {
    remoteDevice = dev;
    deviceClass = dc;
  }
  
  /**
   * Returns the RemoteDevice part of BtDeviceInfo.
   * @return RemoteDevice
   */
  public RemoteDevice getRemoteDevice()
  {
    return remoteDevice;
  }

  /**
   * Returns the DeviceClass part of BtDeviceInfo.
   * @return DeviceClass
   */
  public DeviceClass getDeviceClass()
  {
    return deviceClass;
  }
}

