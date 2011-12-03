package com.blissapplications.java.remotegameinterface.socketworkers;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * User: tjanela
 * Date: 11/27/11
 * Time: 4:27 AM
 */
public class OperationalProtocolDatagram {

	public static final int ID_FIELD_LENGTH = 3;
	public static final int PAYLOAD_FIELD_MAX_LENGTH = 200;
	public static final byte[] MAGIC_FIELD = {'[','!','P','U','M','P','!',']'};
	public static final int MAGIC_FIELD_LENGTH = MAGIC_FIELD.length;

	public static final int DATAGRAM_MIN_SIZE = ID_FIELD_LENGTH + MAGIC_FIELD_LENGTH;

	public OperationalProtocolDatagramType _datagramType;
	private byte[] _id = new byte[ID_FIELD_LENGTH];
	private byte[] _payload = new byte[PAYLOAD_FIELD_MAX_LENGTH];
	private byte[] _magic = new byte[MAGIC_FIELD_LENGTH];

	public byte[] getId (){return _id;}
	public byte[] getPayload(){return _payload;}
	public byte[] getMagic(){return _magic;}

	
	private static OperationalProtocolDatagram getDatagram(OperationalProtocolDatagramType type, String payload) throws Exception{
		ByteBuffer payloadByteBuffer = ByteBuffer.allocate(payload.length());
		payloadByteBuffer.put(Charset.forName("UTF-8").newEncoder().encode(CharBuffer.wrap(payload)));
		
		return getDatagram(type, payloadByteBuffer);
	}
	
	private static OperationalProtocolDatagram getDatagram(OperationalProtocolDatagramType type, ByteBuffer payload){
		return getDatagram(type, payload.array());
		
	}
	
	private static OperationalProtocolDatagram getDatagram(OperationalProtocolDatagramType type, byte[] payload){
		ByteBuffer magicByteBufffer = ByteBuffer.allocate(MAGIC_FIELD_LENGTH);
		magicByteBufffer.put(MAGIC_FIELD);
		
		OperationalProtocolDatagram datagram = new OperationalProtocolDatagram();
		datagram._datagramType = type;
		datagram._id = OperationalProtocolDatagramType.getTypeBytes(datagram._datagramType);
		datagram._payload = payload;
		datagram._magic = magicByteBufffer.array();
		
		return datagram;
	}
	
	public static OperationalProtocolDatagram  getRegisterDisplayClientResponse(OperationalServerHash hash) throws Exception{
		String encodedHash = hash.toEncodedString();
		return getDatagram(OperationalProtocolDatagramType.RegisterDisplayClientResponse, encodedHash);
	}
	
	public static OperationalProtocolDatagram  getRegisterControlClientResponse(OperationalServerHash hash) throws Exception{
		String encodedHash = hash.toEncodedString();
		return getDatagram(OperationalProtocolDatagramType.RegisterControlClientResponse, encodedHash);
	}
	
	public static OperationalProtocolDatagram  getUnregisterControlClientResponse(OperationalServerHash hash) throws Exception{
		String encodedHash = hash.toEncodedString();
		return getDatagram(OperationalProtocolDatagramType.UnregisterControlClientResponse, encodedHash);
	}
	
	public static OperationalProtocolDatagram  getUnregisterDisplayClientResponse(OperationalServerHash hash) throws Exception{
		String encodedHash = hash.toEncodedString();
		return getDatagram(OperationalProtocolDatagramType.UnregisterDisplayClientResponse, encodedHash);
	}
	
	public static OperationalProtocolDatagram  getPayloadResponse() throws Exception{
		return getDatagram(OperationalProtocolDatagramType.PayloadResponse, "");
	}

	public static OperationalProtocolDatagram decode(ByteBuffer request) throws Exception{

		OperationalProtocolDatagram decodedRequest = new OperationalProtocolDatagram();

		int payloadSize = request.capacity() - ID_FIELD_LENGTH - MAGIC_FIELD_LENGTH;
		
		request.get(decodedRequest._id, 0, ID_FIELD_LENGTH);
		request.get(decodedRequest._payload, 0, payloadSize);
		request.get(decodedRequest._magic, 0, MAGIC_FIELD_LENGTH);

		if(!Arrays.equals(MAGIC_FIELD, decodedRequest._magic)){
			throw new Exception("Cannot decode request. Invalid magic field.");
		}
		
		int datagramId = (decodedRequest._id[0] & 0x000000FF) << 16;
		datagramId |= (decodedRequest._id[1] & 0x000000FF) << 8;
		datagramId |= (decodedRequest._id[2] & 0x000000FF);
		
		decodedRequest._datagramType = OperationalProtocolDatagramType.fromInt(datagramId);

		if(decodedRequest._datagramType.equals(OperationalProtocolDatagramType.UnknownDatagram)){
			throw new Exception("Cannot decode request. Unknown Datagram.");
		}

		return decodedRequest;
	}

	public static ByteBuffer encode(OperationalProtocolDatagram datagram) throws Exception{
		ByteBuffer byteBuffer = ByteBuffer.allocate(ID_FIELD_LENGTH + datagram.getPayload().length + MAGIC_FIELD_LENGTH);

		int payloadLength = datagram.getPayload().length;
		byteBuffer.put(datagram.getId(), 0, ID_FIELD_LENGTH);
		byteBuffer.put(datagram.getPayload(), 0, payloadLength);
		byteBuffer.put(datagram.getMagic(), 0, MAGIC_FIELD_LENGTH);
		
		return byteBuffer;
	}
}
