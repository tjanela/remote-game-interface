package com.blissapplications.java.remotegameinterface.clientconnections;

import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

/**
 * User: tjanela
 * Date: 11/26/11
 * Time: 1:57 AM
 */
public class PolicyServerClientConnection implements Runnable, IClientConnection{
	public static final Logger _logger = Logger.getLogger(PolicyServerClientConnection.class);

	public static final String POLICY_REQUEST_FILE = "policies/request.xml";
	public static final String POLICY_RESPONSE_FILE = "policies/response.xml";

	private Socket _clientSocket;

	private InputStream _clientSocketInputStream;
	private OutputStream _clientSocketOutputStream;

	private String _policyRequestContents;
	private String _policyResponseContents;

	private IClientConnectionDelegate _delegate;

	public PolicyServerClientConnection(Socket socket, IClientConnectionDelegate delegate) {
		_clientSocket = socket;
		_delegate = delegate;
	}

	public void run(){
		try{
			_logger.info(String.format("Serving client %1$s...",_clientSocket.getInetAddress().getHostAddress()));
			serveClient();
			_logger.info(String.format("Client %1$s served.",_clientSocket.getInetAddress().getHostAddress()));
		}
		catch (SocketTimeoutException ex){
			_logger.warn("Policy client socket read timed out. Closing connection.");
		}
		catch (Exception ex){
			_logger.error(String.format("Error serving client %1$s",_clientSocket.getInetAddress().getHostAddress()),ex);
		}
	}

	public void serveClient() throws Exception {
		_policyRequestContents = readFile(POLICY_REQUEST_FILE);
		_policyResponseContents = readFile(POLICY_RESPONSE_FILE);
		_clientSocketInputStream = _clientSocket.getInputStream();
		_clientSocketOutputStream = _clientSocket.getOutputStream();

		String request = readRequest();

		if (_policyRequestContents.equals(request)) {
			writeData(Charset.forName("UTF-8").newEncoder().encode(CharBuffer.wrap(_policyResponseContents)));
		}
		
		_clientSocketOutputStream.write(0);
		_clientSocketOutputStream.flush();
		_clientSocket.close();

		_delegate.clientServed(this);
	}

	public String readRequest() throws Exception{
		return readFromInputStream(_clientSocketInputStream,_policyRequestContents.length());
	}

	protected String readFromInputStream(InputStream inputStream, Integer maxBytes) throws Exception{
		StringBuffer buffer = new StringBuffer();
		int codePoint;
		boolean zeroByteRead = false;

			do {
				codePoint = inputStream.read();

				if (codePoint == 0 || codePoint == -1) {
					zeroByteRead = true;
				} else if (Character.isValidCodePoint(codePoint)) {
					buffer.appendCodePoint(codePoint);
				}
			}
			while (!zeroByteRead && buffer.length() < maxBytes);
		return buffer.toString();
	}


	public void writeData(ByteBuffer data) throws Exception {
		_clientSocketOutputStream.write(data.array());
	}

	private static String readFile(String path) throws IOException {
		FileInputStream stream = new FileInputStream(new File(path));
		try {
			FileChannel fc = stream.getChannel();
			MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
			/* Instead of using default, pass in a decoder. */
			return Charset.defaultCharset().decode(bb).toString();
		} finally {
			stream.close();
		}
	}

}
