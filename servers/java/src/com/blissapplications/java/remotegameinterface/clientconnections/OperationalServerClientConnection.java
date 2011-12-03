package com.blissapplications.java.remotegameinterface.clientconnections;

import com.blissapplications.java.remotegameinterface.socketworkers.OperationalProtocolDatagram;
import com.blissapplications.java.remotegameinterface.socketworkers.OperationalServerContext;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * User: tjanela
 * Date: 11/27/11
 * Time: 3:11 AM
 */
public class OperationalServerClientConnection implements Runnable,IClientConnection  {
	public static final Logger _logger = Logger.getLogger(PolicyServerClientConnection.class);

	private Socket _clientSocket;

	private InputStream _clientSocketInputStream;
	private OutputStream _clientSocketOutputStream;

	private IClientConnectionDelegate _delegate;

	public OperationalServerClientConnection(Socket socket, IClientConnectionDelegate delegate) {
		_clientSocket = socket;
		_delegate = delegate;
	}

	public void run(){
		try{
			_logger.info(String.format("Serving client %1$s...",_clientSocket.getInetAddress().getHostAddress()));
			serveClient();
			_logger.info(String.format("Client %1$s served.",_clientSocket.getInetAddress().getHostAddress()));
		}
		catch (Exception ex){
			_logger.error(String.format("Error serving client %1$s",_clientSocket.getInetAddress().getHostAddress()),ex);
		}
	}

	public void serveClient() throws Exception {
		_clientSocketInputStream = _clientSocket.getInputStream();
		_clientSocketOutputStream = _clientSocket.getOutputStream();

		ByteBuffer request = null;
		ByteBuffer response = null;

		Boolean exit = Boolean.FALSE;

		while(!exit){
			request = readRequest();

			if(request == null || request.capacity() == 0){
				exit = Boolean.TRUE;
			}

			
			response = responseForRequest(request);
			writeData(response);

		}

		_delegate.clientServed(this);
	}

	private ByteBuffer responseForRequest(ByteBuffer request) throws Exception{
		return OperationalServerContext.getOperationalServerContextInstance().handleClientRequest(this,request);
	}

	public static final int MAX_REQUEST_LENGTH = 200;
	
	public ByteBuffer readRequest() throws Exception{
		return readFromInputStream(_clientSocketInputStream,MAX_REQUEST_LENGTH);
	}

	public void writeData(ByteBuffer data) throws Exception {
		_clientSocketOutputStream.write(data.array());
	}

	protected ByteBuffer readFromInputStream(InputStream inputStream, Integer maxBytes) throws Exception{
		ByteBuffer buffer = ByteBuffer.allocate(OperationalProtocolDatagram.DATAGRAM_MIN_SIZE);
		buffer.mark();
		int codePoint;
		boolean magicFound = false;
		byte[] ourMagic = new byte[OperationalProtocolDatagram.MAGIC_FIELD_LENGTH];
		do {
			codePoint = inputStream.read();
			buffer.put((byte)(codePoint & 0xff));
			if(buffer.position() >= OperationalProtocolDatagram.DATAGRAM_MIN_SIZE){
				_logger.info(">" + buffer.position());
				buffer.position(buffer.position() - OperationalProtocolDatagram.MAGIC_FIELD_LENGTH);
				buffer.get(ourMagic, 0, OperationalProtocolDatagram.MAGIC_FIELD_LENGTH);
				if(Arrays.equals(ourMagic, OperationalProtocolDatagram.MAGIC_FIELD)){
					magicFound = true;
				}
				_logger.info("<" + buffer.position());
			}
		}	while (!magicFound && buffer.capacity() < OperationalProtocolDatagram.PAYLOAD_FIELD_MAX_LENGTH);

		buffer.reset();

		return buffer;
	}
}
