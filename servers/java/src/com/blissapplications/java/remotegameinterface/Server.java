package com.blissapplications.java.remotegameinterface;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.IOException;

/**
 * User: tjanela
 * Date: 11/25/11
 * Time: 5:48 PM
 */
public class Server {

	public static Logger _logger;
	public static Boolean _exit;

	public static void startServer() throws Exception{
		_logger.info("Starting Remote Game Interface Server...");
		RemoteGameInterfaceServer.getRemoteGameInterfaceServerInstance().start();
		_logger.info("Remote Game Interface Server Started.");
	}

	public static void stopServer() throws Exception{
		_logger.info("Stopping Remote Game Interface Server...");
		RemoteGameInterfaceServer.getRemoteGameInterfaceServerInstance().stop();
		_logger.info("Remote Game Interface Server Stopped.");
	}

	public static void main(String[] args) {


		PropertyConfigurator.configure(args.length == 0 ? "properties/config.properties" : args[0]);

		_exit = Boolean.FALSE;
		_logger = Logger.getLogger(Server.class.getName());

		Boolean serverStarted = Boolean.FALSE;

		try{
			startServer();
			serverStarted = Boolean.TRUE;
		}catch (Exception ex){
			_logger.error("Error starting Game Interface Server...", ex);
			_exit = Boolean.TRUE;
		}
		printHelp();
		while (!_exit){
			try {
				/*int key = System.in.read();

				if(key == -1){
					_exit = Boolean.TRUE;
					continue;
				}

				switch ((char)key){
					case 'h':
						printHelp();
						break;
					case 'q':
						_exit = Boolean.TRUE;
						break;
				}*/
				
			} catch (Exception e) {
				_logger.error("Error on loop...",e);
			}
		}
		
		try{
			if(serverStarted){
				stopServer();
			}
		}catch(Exception ex){
			_logger.error("Error stopping Game Interface Server...", ex);
		}
		_logger.info("Exiting Remote Game Interface Server!");
	}

	private static void printHelp() {
		System.out.println("All commands need enter at the end");
		System.out.println("'h' - > Help");
		System.out.println("'q' - > Quit");
	}

}
