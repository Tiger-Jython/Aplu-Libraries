// BtServiceInfo.java

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
 * Class to pack service information. 
 */
public class BtServiceInfo
{
  private RemoteDevice remoteDevice;
  private ServiceRecord serviceRecord;

  /**
   * Creates a BtServiceInfo instance.
   * @param dev the RemoteDevice
   * @param sr the ServiceRecord
   */
  public BtServiceInfo(RemoteDevice dev, ServiceRecord sr)
  {
    remoteDevice = dev;
    serviceRecord = sr;
  }

  /**
   * Returns the RemoteDevice part of BtServiceInfo.
   * @return RemoteDevice
   */
  public RemoteDevice getRemoteDevice()
  {
    return remoteDevice;
  }

  /**
   * Returns the ServiceRecord part of BtServiceInfo.
   * @return ServiceRecord
   */
  public ServiceRecord getServiceRecord()
  {
    return serviceRecord;
  }
}


