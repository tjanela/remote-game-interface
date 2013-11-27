package com.blissapplications.java.remotegameinterface.socketworkers;

import com.blissapplications.java.remotegameinterface.clientconnections.IClientConnection;
import com.blissapplications.java.remotegameinterface.clientconnections.OperationalServerClientConnection;
import com.blissapplications.java.remotegameinterface.context.OperationalServerContext;

import org.apache.log4j.Logger;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: tjanela
 * Date: 11/27/11
 * Time: 3:27 AM
 */
public class OperationalServerSocketWorker extends ServerSocketWorker {
	public static Logger _logger;

	public Boolean _exit;

	public List<IClientConnection> _operationalServerClientConnections;

	public ServerSocket _socket;

	public OperationalServerSocketWorker(ServerSocket socket) {

		_operationalServerClientConnections = new ArrayList<IClientConnection>();
		_socket = socket;
		_exit = Boolean.FALSE;

	}

	public void run() {
		getLogger().info("Running operational server...");
		while (!_exit) {
			try {
				Socket operationalClient = _socket.accept();

				OperationalServerClientConnection clientConnection = new OperationalServerClientConnection(operationalClient, this);
				Thread clientThread = new Thread(clientConnection);

				synchronized (this) {
					clientThread.start();
					_operationalServerClientConnections.add(clientConnection);
				}

			} catch (SocketException ex) {
				if (!_exit) {
          getLogger().error("Error on operational server", ex);
				}
			} catch (Exception ex) {
        getLogger().error("Error on operational server", ex);
			}
		}
    getLogger().info("Exiting operational server.");
	}

	public void clientServed(IClientConnection servedClient) {
    getLogger().info(String.format("Client has been served by operational server."));
		
		synchronized (this) {
			if (_operationalServerClientConnections.contains(servedClient)) {
				_operationalServerClientConnections.remove(servedClient);
			} else {
        getLogger().warn(String.format("Client not found on operational server internal state! Proceeding..."));
			}
			
			try
			{
				OperationalServerContext.getOperationalServerContextInstance().handleClientDisconnection(servedClient);
			}
			catch(Exception ex)
			{
        getLogger().error(String.format("Client Exception on clientServed(IClientConnection):"), ex);
			}
		}
	}
	
	public void clientDisconnected(IClientConnection disconnectedClient) {
    getLogger().info(String.format("Client %1$s has disconnected.", disconnectedClient));
		synchronized (this) {
			if (_operationalServerClientConnections.contains(disconnectedClient)) {
				_operationalServerClientConnections.remove(disconnectedClient);
			} else {
        getLogger().warn(String.format("Client not found on operational server internal state! Proceeding..."));
			}
			try
			{
				OperationalServerContext.getOperationalServerContextInstance().handleClientDisconnection(disconnectedClient);
			}
			catch(Exception ex)
			{
        getLogger().error(String.format("Client Exception on clientDisconnected(IClientConnection):"), ex);
			}
		}
	}

	public void exit() {
		_exit = Boolean.TRUE;
	}

  private static Logger getLogger(){
    if(_logger == null){
      _logger= Logger.getLogger(OperationalServerSocketWorker.class);
    }
    return _logger;
  }

}
