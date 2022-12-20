# tcpcom.py
# TJ version

'''
 This software is part of the TCPCom library.
 It is Open Source Free Software, so you may
 - run the code for any purpose
 - study how the code works and adapt it to your needs
 - integrate all or parts of the code in your own programs
 - redistribute copies of the code
 - improve the code and release your improvements to the public
 However the use of the code is entirely your responsibility.
 '''

from threading import Thread
import thread
import socket
import urllib
import urllib2
import time
import sys
import urllib2
import json
from dbtable import *

TCPCOM_VERSION = "1.32 - March 17, 2019"

# ================================== Server ================================
# ---------------------- class TimeoutThread ------------------------
class TimeoutThread(Thread):
    def __init__(self, server, timeout):
        Thread.__init__(self)
        self.server = server
        self.timeout = timeout
        self.count = 0
        
    def run(self):
        TCPServer.debug("TimeoutThread starting")
        self.isRunning = True
        isTimeout = False
        while self.isRunning:
            time.sleep(0.01)
            self.count += 1
            if self.count == 100 * self.timeout:
                self.isRunning = False
                isTimeout = True
        if isTimeout:        
            TCPServer.debug("TimeoutThread terminated with timeout")
            self.server.disconnect()
        else:         
            TCPServer.debug("TimeoutThread terminated without timeout")
                
    def reset(self):
        self.count = 0

    def stop(self):
        self.isRunning = False

# ---------------------- class TCPServer ------------------------
class TCPServer(Thread):
    '''
    Class that represents a TCP socket based server.
    '''
    isVerbose = False
    PORT_IN_USE = "PORT_IN_USE"
    CONNECTED = "CONNECTED"
    LISTENING = "LISTENING"
    TERMINATED = "TERMINATED"
    MESSAGE = "MESSAGE"

    def __init__(self, port, stateChanged, endOfBlock = '\0', isVerbose = False):
        '''
        Creates a TCP socket server that listens on TCP port
        for a connecting client. The server runs in its own thread, so the
        constructor returns immediately. State changes invoke the callback
        onStateChanged().
        @param port: the IP port where to listen (0..65535)
        @param stateChange: the callback function to register
        @param endOfBlock: character indicating end of a data block (default: '\0')
        @param isVerbose: if true, debug messages are written to System.out, default: False
        '''
        Thread.__init__(self)
        self.port = port
        self.endOfBlock = endOfBlock
        self.timeout = 0
        self.stateChanged = stateChanged
        TCPServer.isVerbose = isVerbose
        self.isClientConnected = False
        self.terminateServer = False
        self.isServerRunning = False
        self.start()
        try:
            registerFinalizeFunction(self.terminate()) # only defined in TJ
        except:
            pass

    def setTimeout(self, timeout):
        '''
        Sets the maximum time (in seconds) to wait in blocking recv() for an incoming message. If the timeout is exceeded, the link to the client is disconnected.
        (timeout <= 0: no timeout).
        '''
        if timeout <= 0:
            self.timeout = 0
        else:
            self.timeout = timeout

    def run(self):
        TCPServer.debug("TCPServer thread started")
        HOSTNAME = "" # Symbolic name meaning all available interfaces
        self.conn = None
        self.serverSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.serverSocket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)  # close port when process exits
        TCPServer.debug("Socket created")
        try:
            self.serverSocket.bind((HOSTNAME, self.port))
        except socket.error as msg:
            print "Fatal error while creating TCPServer: Bind failed.", msg[0], msg[1]
            sys.exit()
        try:    
            self.serverSocket.listen(10)
        except:
            print "Fatal error while creating TCPServer: Port", self.port, "already in use"
            try:
                self.stateChanged(TCPServer.PORT_IN_USE, str(self.port))
            except Exception, e:
               print "Caught exception in TCPServer.PORT_IN_USE:", e
            sys.exit()

        try:
            self.stateChanged(TCPServer.LISTENING, str(self.port))
        except Exception, e:
            print "Caught exception in TCPServer.LISTENING:", e

        self.isServerRunning = True
                
        while True:
            TCPServer.debug("Calling blocking accept()...")
            conn, self.addr = self.serverSocket.accept()
            if self.terminateServer:
                self.conn = conn
                break
            if self.isClientConnected:
                TCPServer.debug("Returning form blocking accept(). Client refused")
                try:
                    conn.shutdown(socket.SHUT_RDWR)
                except:
                    pass
                conn.close()
                continue
            self.conn = conn
            self.isClientConnected = True
            self.socketHandler = ServerHandler(self, self.endOfBlock)
            self.socketHandler.setDaemon(True)  # necessary to terminate thread at program termination
            self.socketHandler.start()
            try: 
                self.stateChanged(TCPServer.CONNECTED, self.addr[0])
            except Exception, e:
                print "Caught exception in TCPServer.CONNECTED:", e
        self.conn.close()
        self.serverSocket.close()
        self.isClientConnected = False
        try:
            self.stateChanged(TCPServer.TERMINATED, "")
        except Exception, e:
            print "Caught exception in TCPServer.TERMINATED:", e
        self.isServerRunning = False
        TCPServer.debug("TCPServer thread terminated")
        
    def terminate(self):
        '''
        Closes the connection and terminates the server thread.
        Releases the IP port.
        '''
        TCPServer.debug("Calling terminate()")
        if not self.isServerRunning:
            TCPServer.debug("Server not running")
            return
        self.terminateServer = True
        TCPServer.debug("Disconnect by a dummy connection...")
        if self.conn != None:
            self.conn.close()
            self.isClientConnected = False
        client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        client_socket.connect(('localhost', self.port))  # dummy connection to get out of accept()
        
    def disconnect(self):
        '''
        Closes the connection with the client and enters
        the LISTENING state
        '''
        TCPServer.debug("Calling Server.disconnect()")
        if self.isClientConnected:
            self.isClientConnected = False
            try:
                self.stateChanged(TCPServer.LISTENING, str(self.port))
            except Exception, e:
                print "Caught exception in TCPServer.LISTENING:", e
            TCPServer.debug("Shutdown socket now")
            try:
                self.conn.shutdown(socket.SHUT_RDWR)
            except:
                pass
            self.conn.close()

    def sendMessage(self, msg):
        '''
        Sends the information msg to the client (as String, the character endOfBlock (defaut: ASCII 0) serves as end of
        string indicator, it is transparently added and removed)
        @param msg: the message to send
        '''
        TCPServer.debug("sendMessage() with msg: " + msg)
        if not self.isClientConnected:
            TCPServer.debug("Not connected")
            return
        try:
            self.conn.sendall(msg + self.endOfBlock)    
        except:
            TCPClient.debug("Exception in sendMessage()")

    def isConnected(self):
        '''
        Returns True, if a client is connected to the server.
        @return: True, if the communication link is established
        '''
        return self.isClientConnected
    
    def loopForever(self):
        '''
        Blocks forever with little processor consumption until a keyboard interrupt is detected.
        '''
        try:
            while True:
                time.sleep(1)
        except KeyboardInterrupt:
            pass
        self.terminate()

    def isTerminated(self):
        '''
        Returns True, if the server is in TERMINATED state.
        @return: True, if the server thread is terminated
        '''
        return self.terminateServer

    @staticmethod
    def debug(msg):
        if TCPServer.isVerbose:
            print "   TCPServer-> " + msg
 
    @staticmethod
    def getVersion():
        '''
        Returns the library version.
        @return: the current version of the library
        '''
        return TCPCOM_VERSION

    @staticmethod
    # Hack should work on all platforms
    def getIPAddress():
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        try:
            # doesn't even have to be reachable
            s.connect(('10.255.255.255', 1))
            IP = s.getsockname()[0]
        except:
            IP = '127.0.0.1'
        finally:
            s.close()
        return IP
   
# ---------------------- class ServerHandler ------------------------
class ServerHandler(Thread):
    def __init__(self, server, endOfBlock):
        Thread.__init__(self)
        self.server = server
        self.endOfBlock = endOfBlock

    def run(self):
        TCPServer.debug("ServerHandler started")
        timeoutThread = None
        if self.server.timeout > 0:
            timeoutThread = TimeoutThread(self.server, self.server.timeout)
            timeoutThread.start()
        bufSize = 4096
        try:
            while True:
                data = ""
                reply = ""
                isRunning = True
                while not reply[-1:] == self.endOfBlock:
                    TCPServer.debug("Calling blocking conn.recv()")
                    reply = self.server.conn.recv(bufSize)
                    TCPServer.debug("Returned from conn.recv() with " + str(reply))
                    if reply == None or len(reply) == 0: # Client disconnected
                        TCPServer.debug("conn.recv() returned None")
                        isRunning = False
                        break
                    data += reply
                if not isRunning:
                    break
                TCPServer.debug("Received msg: " + data + "; len: " + str(len(data)))
                junk = data.split(self.endOfBlock)  # more than 1 message may be received if
                                         # transfer is fast. data: xxxx<eob>yyyyy<eol>zzz<eob>
                for i in range(len(junk) - 1):
                    try:
                        self.server.stateChanged(TCPServer.MESSAGE, junk[i]) # eol is not included
                    except Exception, e:
                        print "Caught exception in TCPServer.MESSAGE:", e
                if not self.server.isClientConnected: # Callback disconnected the client
                     if timeoutThread != None:
                        timeoutThread.stop()
                     TCPServer.debug("Callback disconnected client. ServerHandler terminated")
                     return
                if timeoutThread != None:
                    timeoutThread.reset() 
        except:  # May happen if client peer is resetted
            TCPServer.debug("Exception from blocking conn.recv(), Msg: " + str(sys.exc_info()[0]) + \
              " at line # " +  str(sys.exc_info()[-1].tb_lineno))
        self.server.disconnect()
        if timeoutThread != None:
            timeoutThread.stop()
        TCPServer.debug("ServerHandler terminated")


# ================================== Client ================================
# -------------------------------- class TCPClient --------------------------
class TCPClient():
    '''
    Class that represents a TCP socket based client.
    '''
    isVerbose = False
    CONNECTING = "CONNECTING"
    SERVER_OCCUPIED = "SERVER_OCCUPIED"
    CONNECTION_FAILED = "CONNECTION_FAILED"
    CONNECTED = "CONNECTED"
    DISCONNECTED = "DISCONNECTED"
    MESSAGE = "MESSAGE"

    def __init__(self, ipAddress, port, stateChanged, isVerbose = False):
        '''
        Creates a TCP socket client prepared for a connection with a
        TCPServer at given address and port.
        @param host: the IP address of the host
        @param port: the IP port where to listen (0..65535)
        @param stateChanged: the callback function to register
        @param isVerbose: if true, debug messages are written to System.out
        '''
        self.isClientConnected = False
        self.isClientConnecting = False
        self.ipAddress = ipAddress
        self.port = port
        self.stateChanged = stateChanged
        self.checkRefused = False
        self.isRefused = False

        TCPClient.isVerbose = isVerbose
                  
    def sendMessage(self, msg, responseTime = 0):
        '''
        Sends the information msg to the server (as String, the character \0
        (ASCII 0) serves as end of string indicator, it is transparently added
        and removed).  For responseTime > 0 the method blocks and waits
        for maximum responseTime seconds for a server reply.
        @param msg: the message to send
        @param responseTime: the maximum time to wait for a server reply (in s)
        @return: the message or null, if a timeout occured
        '''
        TCPClient.debug("sendMessage() with msg = " + msg)
        if not self.isClientConnected:
            TCPClient.debug("sendMessage(): Connection closed.")
            return None
        reply = None
        try:
            msg += "\0";  # Append \0
            rc = self.sock.sendall(msg)
            if responseTime > 0:
                reply = self._waitForReply(responseTime)  # Blocking
        except:
            TCPClient.debug("Exception in sendMessage()")
            self.disconnect()
    
        return reply
    
    def _waitForReply(self, responseTime):
        TCPClient.debug("Calling _waitForReply()")
        self.receiverResponse = None
        startTime = time.time()
        while self.isClientConnected and self.receiverResponse == None and time.time() - startTime < responseTime:
            time.sleep(0.01)
        if self.receiverResponse == None:
            TCPClient.debug("Timeout while waiting for reply")
        else:    
            TCPClient.debug("Response = " + self.receiverResponse + " time elapsed: " + str(int(1000 * (time.time() - startTime))) + " ms")
        return self.receiverResponse

    def connect(self, timeout = 0):
        '''
        Creates a connection to the server (blocking until timeout).
        @param timeout: the maximum time (in s) for the connection trial  (0: for default timeout)
        @return: True, if the connection is established; False, if the server
        is not available or occupied
        '''
        if timeout == 0:
            timeout = None
        try:
            self.stateChanged(TCPClient.CONNECTING, self.ipAddress + ":" + str(self.port))
        except Exception, e:
            print "Caught exception in TCPClient.CONNECTING:", e
        try:
            self.isClientConnecting = True
            host = (self.ipAddress, self.port)
            if self.ipAddress == "localhost" or self.ipAddress == "127.0.0.1":
                timeout = None  # do not use timeout for local host, to avoid error message "java.net..."
            self.sock = socket.create_connection(host, timeout)
            self.sock.settimeout(None)
            self.isClientConnecting = False
            self.isClientConnected = True
        except:
            self.isClientConnecting = False
            try:
                self.stateChanged(TCPClient.CONNECTION_FAILED, self.ipAddress + ":" + str(self.port))
            except Exception, e:
                print "Caught exception in TCPClient.CONNECTION_FAILED:", e
            TCPClient.debug("Connection failed.")
            return False
        ClientHandler(self)

        # Check if connection is refused
        self.checkRefused = True
        self.isRefused = False
        startTime = time.time()
        while time.time() - startTime < 2 and not self.isRefused:
            time.sleep(0.001)
        if self.isRefused:
            TCPClient.debug("Connection refused")
            try:
                self.stateChanged(TCPClient.SERVER_OCCUPIED, self.ipAddress + ":" + str(self.port))
            except Exception, e:
                print "Caught exception in TCPClient.SERVER_OCCUPIED:", e
            return False

        try:
            self.stateChanged(TCPClient.CONNECTED, self.ipAddress + ":" + str(self.port))
        except Exception, e:
            print "Caught exception in TCPClient.CONNECTED:", e
        TCPClient.debug("Successfully connected")
        return True

    def disconnect(self):
        '''
        Closes the connection with the server.
        '''
        TCPClient.debug("Client.disconnect()")
        if not self.isClientConnected:
            TCPClient.debug("Connection already closed")
            return
        self.isClientConnected = False
        TCPClient.debug("Closing socket")
        try: # catch Exception "transport endpoint is not connected"
            self.sock.shutdown(socket.SHUT_RDWR)
        except:
            pass
        self.sock.close()

    def isConnecting(self):
        '''
        Returns True during a connection trial.
        @return: True, while the client tries to connect
        '''
        return self.isClientConnecting

    def isConnected(self):
        '''
        Returns True of client is connnected to the server.
        @return: True, if the connection is established
        '''
        return self.isClientConnected
    
    @staticmethod
    def debug(msg):
        if TCPClient.isVerbose:
            print "   TCPClient-> " + msg

    @staticmethod
    def getVersion():
        '''
        Returns the library version.
        @return: the current version of the library
        '''
        return TCPCOM_VERSION

# -------------------------------- class ClientHandler ---------------------------
class ClientHandler(Thread):
    def __init__(self, client):
        Thread.__init__(self)
        self.client = client
        self.start()
                
    def run(self):
        TCPClient.debug("ClientHandler thread started")
        while True:
            try:
                junk = self.readResponse().split("\0")
                # more than 1 message may be received 
                # if transfer is fast. data: xxxx\0yyyyy\0zzz\0
                for i in range(len(junk) - 1):
                    try:
                        self.client.stateChanged(TCPClient.MESSAGE, junk[i])
                    except Exception, e:
                        print "Caught exception in TCPClient.MESSAGE:", e
            except:    
                TCPClient.debug("Exception in readResponse() Msg: " + str(sys.exc_info()[0]) + \
                  " at line # " +  str(sys.exc_info()[-1].tb_lineno))
                if self.client.checkRefused:
                    self.client.isRefused = True
                break
        try:
            self.client.stateChanged(TCPClient.DISCONNECTED, "")
        except Exception, e:
            print "Caught exception in TCPClient.DISCONNECTED:", e
        TCPClient.debug("ClientHandler thread terminated")

    def readResponse(self):
        TCPClient.debug("Calling readResponse")
        bufSize = 4096
        data = ""
        while not data[-1:]  ==  "\0":
            try:
                reply = self.client.sock.recv(bufSize)  # blocking
                if len(reply) == 0:
                    TCPClient.debug("recv returns null length")
                    raise Exception("recv returns null length")
            except:
                TCPClient.debug("Exception from blocking conn.recv(), Msg: " + str(sys.exc_info()[0]) + \
                  " at line # " +  str(sys.exc_info()[-1].tb_lineno))
                raise Exception("Exception from blocking sock.recv()")
            data += reply
            self.receiverResponse = data[:-1]
        return data


# -------------------------------- class HTTPServer --------------------------
class HTTPServer(TCPServer):
    
    def getHeader1(self):
       return "HTTP/1.1 501 OK\r\nServer: " + self.serverName + "\r\nConnection: Closed\r\n"
   
    def getHeader2(self):
        return "HTTP/1.1 200 OK\r\nServer: " + self.serverName + "\r\nContent-Length: %d\r\nContent-Type: text/html\r\nConnection: Closed\r\n\r\n"

    def onStop(self):
        self.terminate()
    
    def __init__(self, requestHandler, serverName = "PYSERVER", port = 80, isVerbose = False):
        '''

        Creates a HTTP server (inherited from TCPServer) that listens for a connecting client on given port (default = 80). 
        Starts a thread that handles and returns HTTP GET requests. The HTTP respoonse header reports the given server name 
        (default: "PYSERVER")
        
        requestHandler() is a callback function called when a GET request is received.
        Signature:  msg, stateHandler = requestHandler(clientIP, filename, params)
        
        Parameters:                
            clientIP: the client's IP address in dotted format
            filename: the requested filename with preceeding '/'
            params: a tuple with format: ((param_key1, param_value1), (param_key2, param_value2), ...)  (all items are strings)
        
        Return values:
            msg: the HTTP text response (the header is automatically created) 
            stateHandler: a callback function that is invoked immediately after the reponse is sent.
              If stateHandler = None, nothing is done. The function may include longer lasting server 
              actions or a wait time, if sensors are not immediately ready for a new measurement.
            
        Call terminate() to stop the server. The connection is closed by the server at the end of each response. If the client connects,
        but does not send a request within 5 seconds, the connection is closed by the server.
        '''
        try:
            registerStopFunction(self.onStop)
        except:
            pass # registerStopFunction not defined (e.g. on Raspberry Pi)
    
        TCPServer.__init__(self, port, stateChanged = self.onStateChanged, endOfBlock = '\n', isVerbose = isVerbose)
        self.serverName = serverName
        self.requestHandler = requestHandler
        self.port = port
        self.verbose = isVerbose
        self.timeout = 5
        self.clientIP = ""
    
    def getClientIP(self):
        '''
        Returns the dotted IP of a connected client. If no client is connected, returns empty string.
        '''
        return self.clientIP                

    def onStateChanged(self, state, msg):
        if state == "CONNECTED":
            self.clientIP = msg
            self.debug("Client " + msg + " connected.")
        elif state == "DISCONNECTED":
            self.clientIP = ""
            self.debug("Client disconnected.")
        elif state == "LISTENING":
            self.clientIP = ""
            self.debug("LISTENING")
        elif state == "MESSAGE":
            self.debug("request: " + msg)
            if len(msg) != 0:
                filename, params = self._parseURL(msg)
                if filename == None:
                    self.sendMessage(self.getHeader1())
                else:
                    text, stateHandler = self.requestHandler(self.clientIP, filename, params)
                    self.sendMessage(self.getHeader2() % (len(text)))
                    self.sendMessage(text)
                    if stateHandler != None:
                        try:
                            stateHandler()
                        except Exception, e:
                            print "Exception in stateHandler():", str(e)
            else:
                self.sendMessage(self.getHeader1())
            self.disconnect()
          
    def _parseURL(self, request):
        lines = request.split('\n') # split lines
        params = []
        for line in lines:
            if line[0:4] == 'GET ': # only GET request
                url = line.split()[1].strip() # split at spaces and take second item
                i = url.find('?')  # check for params
                if i != -1: # params given
                    filename = url[0:i].strip()  # include leading /
                    params = []
                    urlParam = url[i + 1:]
                    for item in urlParam.split('&'): # split parameters
                        i = item.find('=')
                        key = item[0:i]
                        value = item[i+1:]
                        params.append((key, value))
                    return filename, tuple(params)    
                return url.strip(), tuple([])
        return None, tuple([])   

    def debug(self, msg):
        if self.verbose:
            print("   HTTPServer-> " + msg)

    @staticmethod
    def getServerIP():
        '''
        Returns the server's IP address (static method).
        '''
        return TCPServer.getIPAddress()

# -------------------------------- class HTTPClient --------------------------
class HTTPClient:
    @staticmethod
    def getRequest(url, data = None):
        try:
            if data != None and data != {}:
               url_values = urllib.urlencode(data)
               url = url + '?' + url_values
            request = urllib2.Request(url)
            response = urllib2.urlopen(request)
            return response.read()
        except urllib2.HTTPError as e:
            print "HTTP error:", e.code
            return e.read()

    @staticmethod
    def postRequest(url, data = None):
        try:
            if data != None and data != {}:
               url_values = urllib.urlencode(data)
            else:
               url_values = ""
            request = urllib2.Request(url, url_values)
            response = urllib2.urlopen(request)
            return response.read()
        except urllib2.HTTPError as e:
            print "HTTP error:", e.code
            return e.read()
    
    @staticmethod
    def deleteRequest(url, data = ""):
        try:
            request = urllib2.Request(url, data)
            request.get_method = lambda: 'DELETE'
            response = urllib2.urlopen(request)
            return response.read()
        except urllib2.HTTPError as e:
            print "HTTP error:", e.code
            return e.read()
    
    @staticmethod
    def pushover(token_key, user_key, title, message):
        '''
        Sends a push request to the Pushover host api.pushover.net with given token key, user key, title and message.
        '''
        PUSHOVER_HOST = 'api.pushover.net'
        PUSHOVER_QUERY = '/1/messages.json'
        url = 'https://' + PUSHOVER_HOST + PUSHOVER_QUERY
        
        data = {}
        data['token'] = token_key
        data['user'] = user_key
        data['title'] = title
        data['message'] = message
        return HTTPClient.postRequest(url, data)

    @staticmethod    
    def toUrl(string, encode):
        return urllib.urlencode(data)
    
    @staticmethod
    def extractJSON(response):
        try:
            message = response[response.find('{'):response.rfind('}') + 1]
            return json.loads(message)
        except Exception as ex:
            return None

# -------------------------------- class ThingSpeakChannel --------------------------
class ThingSpeakChannel:
    _host = "https://api.thingspeak.com"
    _maxEntries = 8
    
    def __init__(self, channelId, readKey, writeKey, userKey, isVerbose = False):
        '''
        Creates an instance of the ThinkSpeakChannel class for communication with 
        an established channel on the cloud server www.thingspeak.com. 
        The parameters correspond to the values received from the server during the channel definition. 
        With verbose = True, debug information is written to the console.
        userKey is assigned to the account and not to the channel (User API Key).
        '''
        self._channelId = channelId
        self._readKey = readKey
        self._writeKey = writeKey
        self._userKey = userKey
        self._labels = []
        self._isVerbose = isVerbose

    def getData(self, nbEntries = -1, includeDate = True):
        '''
        Returns the last nbEntries records as list of dictionaries [{fieldId: value} ...] (fieldId = 1..8). 
        For nbEntries = -1, all records are returned. 
        For includeDate = True, the field 'data', which contains the date-time of the record's creation, 
        is also returned. All values are strings. If an error occurs, an empty list is returned.
        '''
        inquiry = "/channels/" + str(self._channelId) + \
            "/feeds.json?api_key=" + self._readKey + "&results=" + (str(nbEntries) if nbEntries != -1 else "8000")
        url = ThingSpeakChannel._host + inquiry    
        self._debug("getData(); url " + url)
        reply = HTTPClient.getRequest(url)
        self._debug("reply: " + reply)
        dataDict = HTTPClient.extractJSON(reply)
        self._debug("dataDict: " + str(dataDict))
        if dataDict != None and 'status' in dataDict.keys() and (dataDict['status'] == '400' or dataDict['status'] == '404'):
            print "Bad request"
            return []
        if dataDict == None:
            return []
        self._labels.clear()
        for label in range(1, ThingSpeakChannel._maxEntries + 1):
            try:
                dataDict['channel']['field' + str(label)]
                self._labels.append(label)
            except:
                pass    
    
        self._debug("labels: " + str(self._labels))
        
        tableList = []
        nbFeeds = len(dataDict['feeds'])
        self._debug("nbFeeds: " + str(nbFeeds))
        for entryId in range(0, nbFeeds):
            values = {}
            for label in self._labels:
                try:
                    value = str(dataDict['feeds'][entryId]['field' + str(label)])
                    if includeDate:
                        date = str(dataDict['feeds'][entryId]['created_at'])
                        values['date'] = date.replace('T', ' ').replace('Z', '')
                    values[label] = value
                except:
                    pass
            if values == {}:
                break    
            tableList.append(values)
        self._debug("tableList: " + str(tableList))
        return tableList

    def getTable(self, nbEntries = -1, includeDate = True):
        '''
        Returns the last nbEntries records as instance of DbTable with string entries. 
        For nbEntries = -1, all records are returned. 
        For includeDate = True, the field 'data', which contains the date-time of the record's creation, 
        is also returned. If an error occurs, an empty table is returned.
        '''   
        dataList = self.getData(nbEntries, includeDate)
        if dataList == []:
            return DbTable()
        fieldNames = []
        keys = dataList[0].keys()
        for key in keys:
            if key != "date":
                attr = "field" + str(key)
            else:
                attr = str(key)
            fieldNames.append(attr)
        tbl = DbTable(fieldNames)
        for entry in dataList:
            tbl.insert(entry.values())
        return tbl

    def show(self, nbEntries = -1, includeDate = True):
        '''
        Prints the content of getTable() to the console.
        '''
        tbl = self.getTable(nbEntries, includeDate)
        print tbl

    def getLastValue(self, fieldId = 1):
        '''
        Returns the value (string) of the last record with given fieldId (1..8). 
        In case of error or if the table is empty, None is returned.
        '''
        lastData = self.getData(1)
        if lastData == None or lastData == []: #empty channel
            return None
        return lastData[0][fieldId]

    def getFields(self):
        '''
        Returns a dictionary with the fields defined in the channel and their names: 
        {'field1': <field name 1>, 'field2' :, <field name 2> ..}. 
        In case of error, None is returned.
        '''
        inquiry = "/channels/" + str(self._channelId) + \
            "/feeds.json?api_key=" + self._readKey + "&results=1"
        url = ThingSpeakChannel._host + inquiry    
        self._debug("getFields(); url " + url)
        reply = HTTPClient.getRequest(url)
        dataDict = HTTPClient.extractJSON(reply)
        if dataDict != None and 'status' in dataDict.keys() and (dataDict['status'] == '400' or dataDict['status'] == '404'):
            print "Bad request"
            return []
        fieldNames = {}
        for label in range(1, ThingSpeakChannel._maxEntries + 1):
            try:
                key = 'field' + str(label)
                value = dataDict['channel'][key]
                fieldNames[key] = value
            except:
                pass    
        return fieldNames    
    
    def insert(self, data, timeout = 20):
        '''
        Makes a table entry and returns true on success. 
        data is either a single value (number or string) or a dictionary {fieldId: value, ...} 
        for the entry in multiple fields. Fields that do not receive a value are set to None. 
        For free accounts, a new entry is only allowed every 15 s; 
        if the server is not ready yet, the attempt is repeated every second until the timeout (in s) 
        is reached. Then False is returned.
        '''
        if type(data) == dict:
            self._debug("inserting from dictionary: " + str(data))
            query = ""
            for key in data.keys():
                query = query + "&field" + str(key) + "=" + str(data[key])
                inquiry = "/update/?api_key=" + self._writeKey + query
        else:
            self._debug("inserting single value: " + str(data))
            inquiry = "/update/?api_key=" + self._writeKey + "&field1=" + str(data)
        nbTrials = 0
        while nbTrials < int(timeout):
            rc = self._insert(inquiry)
            if rc == -1:
                return True
            elif rc == 1:
                return False
            nbTrials += 1
            self._debug("insertion failed. nTrials: " + str(nbTrials))
            time.sleep(1)
        return False    
    
    def _insert(self, inquiry):             
        try:
            url = ThingSpeakChannel._host + inquiry    
            self._debug("_insert; url " + url)
            reply = HTTPClient.getRequest(url)
            self._debug("reply: " + reply)
            if reply == '0':
                return 0
            dataDict = HTTPClient.extractJSON(reply)
            if dataDict != None and 'status' in dataDict.keys() and (dataDict['status'] == '400' or dataDict['status'] == '404'):
                print "Bad request"
                return 1
        except:
            return 1
        return -1

    def clear(self):
        '''
        Deletes all table entries and returns true on success.
        '''
        inquiry = "/channels/" + str(self._channelId) + "/feeds.json?api_key=" + self._userKey
        url = ThingSpeakChannel._host + inquiry    
        self._debug("clear(); url " + url)
        reply = HTTPClient.deleteRequest(url)
        self._debug("reply: " + reply)
        dataDict = HTTPClient.extractJSON(reply)
        if dataDict != None and 'status' in dataDict.keys() and (dataDict['status'] == '400' or dataDict['status'] == '404'):
            print "Bad request"
            return False
        return True
        
    def _debug(self, msg):
        if self._isVerbose:
            print "---> Debug:", msg   
