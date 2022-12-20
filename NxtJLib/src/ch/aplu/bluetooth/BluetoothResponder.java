// BluetoothResponder.java

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

import java.util.*;

/**
 * Callback declarations for Bluetooth device and service search.
 */
public interface BluetoothResponder
{
  /**
   * Callback notification methods called when the device search is finished.
   * deviceTable stores the found devices as DeviceInfo, which are pairs of the form
   * {RemoteDevice, DeviceClass}.
   * To retrieve the device name, BluetoothFinder.getDeviceName(device)
   * may be used. If no devices are found, the deviceTable size is 0.
   * Use BluetoothFinder.addBluetoothResponder() to register the notification.
   * @see BluetoothFinder
   * @see javax.bluetooth.RemoteDevice
   * @see javax.bluetooth.DeviceClass
   */
  public void notifyBluetoothDeviceSearch(Vector deviceTable);

  /**
   * Callback notification methods called when the service search is finished.
   * serviceTable stores the found service ServiceInfo which are pairs of the form
   * {RemoteDevice, ServiceRecord}.
   * To retrieve the device name, BluetoothFinder.getDeviceName(device)
   * may be used. If no services are found, the serviceTable size is 0.
   * Use BluetoothFinder.addBluetoothResponder() to register the notification.
   * @see BluetoothFinder
   * @see javax.bluetooth.RemoteDevice
   * @see javax.bluetooth.ServiceRecord
   */
  public void notifyBluetoothServiceSearch(Vector serviceTable);
}
