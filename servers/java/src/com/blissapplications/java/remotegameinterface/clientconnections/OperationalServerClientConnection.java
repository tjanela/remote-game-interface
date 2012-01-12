package com.blissapplications.java.remotegameinterface.clientconnections;

import com.blissapplications.java.remotegameinterface.context.OperationalServerContext;
import com.blissapplications.java.remotegameinterface.packets.OperationalProtocolPacket;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * User: tjanela
 * Date: 11/27/11
 * Time: 3:11 AM
 */
public class OperationalServerClientConnection implements Runnable,IClientConnection  {
	public static final Logger _logger = Logger.getLogger(OperationalServerClientConnection.class);

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
			try{
				request = readRequest();

				if(request == null || request.capacity() == 0){
					_logger.debug("Empty message. Exiting service loop...");
					exit = Boolean.TRUE;
					continue;
				}

				_logger.debug(String.format("Got Request: %1$s", request));
				response = responseForRequest(request);

				if(response != null){
					_logger.debug(String.format("Will respond: %1$s", response));
					writeData(response);
				}else{
					_logger.debug("Got null response.");
				}
			}catch(SocketException ex){
				_logger.error("Error serving client: ", ex);
				_delegate.clientDisconnected(this);
				exit = Boolean.TRUE;
				throw ex;
			}
			Thread.yield();
		}

		_delegate.clientServed(this);
	}

	private ByteBuffer responseForRequest(ByteBuffer request) throws Exception{
		return OperationalServerContext.getOperationalServerContextInstance().handleClientRequest(this,request);
	}

	public ByteBuffer readRequest() throws Exception{
		return readFromInputStream(_clientSocketInputStream,OperationalProtocolPacket.PACKET_MAX_SIZE);
	}

	public void writeData(ByteBuffer data) throws Exception {
		_clientSocketOutputStream.write(data.array());
	}

	protected ByteBuffer readFromInputStream(InputStream inputStream, Integer maxBytes) throws Exception{
		ByteBuffer buffer = ByteBuffer.allocate(maxBytes);
		buffer.mark();
		int codePoint;
		boolean magicFound = false;

		int packetSize = 0;
		byte[] ourMagic = new byte[OperationalProtocolPacket.MAGIC_FIELD_LENGTH];
		do {
			codePoint = inputStream.read();
			byte readByte = (byte)(codePoint & 0xff);
			buffer.put(readByte);
			if(buffer.position() >= OperationalProtocolPacket.PACKET_MIN_SIZE){
				buffer.position(buffer.position() - OperationalProtocolPacket.MAGIC_FIELD_LENGTH);
				buffer.get(ourMagic, 0, OperationalProtocolPacket.MAGIC_FIELD_LENGTH);
				if(Arrays.equals(ourMagic, OperationalProtocolPacket.MAGIC_FIELD)){
					magicFound = true;
					packetSize = buffer.position();
				}
			}
		}	while (!magicFound && buffer.position() < maxBytes && codePoint != -1);

		buffer.reset();
		
		ByteBuffer trimmedByteBuffer = ByteBuffer.allocate(packetSize);
		trimmedByteBuffer.mark();
		trimmedByteBuffer.put(buffer.array(),0,packetSize);
		trimmedByteBuffer.reset();

		return trimmedByteBuffer;
	}
}
