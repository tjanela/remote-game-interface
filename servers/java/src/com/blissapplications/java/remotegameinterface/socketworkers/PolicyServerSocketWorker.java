package com.blissapplications.java.remotegameinterface.socketworkers;

import com.blissapplications.java.remotegameinterface.clientconnections.IClientConnection;
import com.blissapplications.java.remotegameinterface.clientconnections.PolicyServerClientConnection;
import org.apache.log4j.Logger;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: tjanela
 * Date: 11/27/11
 * Time: 3:19 AM
 */
public class PolicyServerSocketWorker extends ServerSocketWorker {
	public static final Logger _logger = Logger.getLogger(PolicyServerSocketWorker.class);

	public Boolean _exit;

	public List<IClientConnection> _policyServerClientConnections;

	public ServerSocket _socket;

	public PolicyServerSocketWorker(ServerSocket socket){

		_policyServerClientConnections = new ArrayList<IClientConnection>();
		_socket = socket;
		_exit = Boolean.FALSE;
		
	}

		public void run() {
			_logger.info("Running policy server...");
			while (!_exit) {
				try {
					Socket policyClient = _socket.accept();

					PolicyServerClientConnection clientConnection = new PolicyServerClientConnection(policyClient, this);
					Thread clientThread = new Thread(clientConnection);

					synchronized (this) {
						clientThread.start();
						_policyServerClientConnections.add(clientConnection);
					}

				} catch (SocketException ex) {
					if (!_exit) {
						_logger.error("Error on policy server", ex);
					}
				} catch (Exception ex) {
					_logger.error("Error on policy server", ex);
				}
			}
			_logger.info("Exiting policy server.");
		}

		public void clientServed(IClientConnection servedClient) {
			_logger.info(String.format("Client %1$s has been served by policy server.", servedClient));
			synchronized (this) {
				if (_policyServerClientConnections.contains(servedClient)) {
					_policyServerClientConnections.remove(servedClient);
				} else {
					_logger.warn(String.format("Client %1$s not found on policy server internal state! Proceeding...", servedClient));
				}
			}
		}
	public void exit(){
		_exit = Boolean.TRUE;
	}
}