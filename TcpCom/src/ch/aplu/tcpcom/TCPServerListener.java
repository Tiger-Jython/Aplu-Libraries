// TcpComServerListener.java

package ch.aplu.tcpcom;

/**
Callback called at state change events.<br> 
state: TCPServer.PORT_IN_USE, msg: port<br> 
state: TCPServer.CONNECTED, msg: IP address of client<br>
state: TCPServer.LISTENING, msg: port<br> 
state: TCPSever.TERMINATED, msg: empty<br> 
state: TCPServer.MESSAGE, msg: message received from client<br>
*/
public interface TCPServerListener
{
  void onStateChanged(String state, String msg);
}
