// TcpComClientListener.java

package ch.aplu.tcpcom;

/**
Callback called at state change events. 
state: TCPClient.CONNECTING, msg: IP address of server <br>
state: TCPClient.CONNECTION_FAILED, msg: IP address of server<br> 
state: TCPClient.CONNECTED, msg: IP address of server<br>
state: TCPClient.DISCONNECTED, msg: empty<br> 
state: TCPClient.MESSAGE, msg: message received from server
*/
public interface TCPClientListener
{
  void onStateChanged(String state, String msg);
}
