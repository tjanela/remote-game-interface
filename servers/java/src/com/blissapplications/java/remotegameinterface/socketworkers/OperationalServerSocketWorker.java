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
	public static final Logger _logger = Logger.getLogger(OperationalServerSocketWorker.class);

	public Boolean _exit;

	public List<IClientConnection> _operationalServerClientConnections;

	public ServerSocket _socket;

	public OperationalServerSocketWorker(ServerSocket socket) {

		_operationalServerClientConnections = new ArrayList<IClientConnection>();
		_socket = socket;
		_exit = Boolean.FALSE;

	}

	public void run() {
		_logger.info("Running operational server...");
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
					_logger.error("Error on operational server", ex);
				}
			} catch (Exception ex) {
				_logger.error("Error on operational server", ex);
			}
		}
		_logger.info("Exiting operational server.");
	}

	public void clientServed(IClientConnection servedClient) {
		_logger.info(String.format("Client %1$s has been served by operational server.", servedClient));
		
		synchronized (this) {
			if (_operationalServerClientConnections.contains(servedClient)) {
				_operationalServerClientConnections.remove(servedClient);
			} else {
				_logger.warn(String.format("Client %1$s not found on operational server internal state! Proceeding...", servedClient));
			}
			
			try
			{
				OperationalServerContext.getOperationalServerContextInstance().handleClientDisconnection(servedClient);
			}
			catch(Exception ex)
			{
				_logger.error(String.format("Client %1$s", servedClient), ex);
			}
		}
	}
	
	public void clientDisconnected(IClientConnection disconnectedClient) {
		_logger.info(String.format("Client %1$s has disconnected.", disconnectedClient));
		synchronized (this) {
			if (_operationalServerClientConnections.contains(disconnectedClient)) {
				_operationalServerClientConnections.remove(disconnectedClient);
			} else {
				_logger.warn(String.format("Client %1$s not found on operational server internal state! Proceeding...", disconnectedClient));
			}
			try
			{
				OperationalServerContext.getOperationalServerContextInstance().handleClientDisconnection(disconnectedClient);
			}
			catch(Exception ex)
			{
				_logger.error(String.format("Client %1$s", disconnectedClient), ex);
			}
		}
	}

	public void exit() {
		_exit = Boolean.TRUE;
	}

}
