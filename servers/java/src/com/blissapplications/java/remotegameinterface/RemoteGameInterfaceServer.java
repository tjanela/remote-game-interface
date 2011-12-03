package com.blissapplications.java.remotegameinterface;

import com.blissapplications.java.remotegameinterface.socketworkers.OperationalServerSocketWorker;
import com.blissapplications.java.remotegameinterface.socketworkers.PolicyServerSocketWorker;
import org.apache.log4j.Logger;

import java.net.*;

/**
 * User: tjanela
 * Date: 11/25/11
 * Time: 6:02 PM
 */

public class RemoteGameInterfaceServer implements IRemoteGameInterfaceServer {
	public static final Integer SERVER_OPERATIONAL_PORT = 20201;
	public static final Integer SERVER_POLICY_PORT = SERVER_OPERATIONAL_PORT + 1;

	public static final Logger _logger = Logger.getLogger(RemoteGameInterfaceServer.class);

	private static RemoteGameInterfaceServer _remoteGameInterfaceInstance;

	private Thread _operationalServerSocketThread;
	private OperationalServerSocketWorker _operationalServerSocketWorker;
	private ServerSocket _operationalServerSocket;

	private Thread _policyServerSocketThread;
	private PolicyServerSocketWorker _policyServerSocketWorker;
	private ServerSocket _policyServerSocket;

	public void start() throws Exception {

		_policyServerSocket = new ServerSocket(SERVER_POLICY_PORT);
		_operationalServerSocket = new ServerSocket(SERVER_OPERATIONAL_PORT);
		
		_policyServerSocketWorker = new PolicyServerSocketWorker(_policyServerSocket);
		_operationalServerSocketWorker = new OperationalServerSocketWorker(_operationalServerSocket);

		_policyServerSocketThread = new Thread(_policyServerSocketWorker);
		_operationalServerSocketThread = new Thread(_operationalServerSocketWorker);

		_policyServerSocketThread.start();
		_operationalServerSocketThread.start();
	}

	public void stop() throws Exception {
		
		_policyServerSocketWorker.exit();
		_operationalServerSocketWorker.exit();

		_policyServerSocket.close();
		_operationalServerSocket.close();

		_policyServerSocketThread.join();
		_operationalServerSocketThread.join();
	}

	public static RemoteGameInterfaceServer getRemoteGameInterfaceServerInstance() {
		if (_remoteGameInterfaceInstance == null) {
			_remoteGameInterfaceInstance = new RemoteGameInterfaceServer();
		}
		return _remoteGameInterfaceInstance;
	}

	private RemoteGameInterfaceServer() {}
}
