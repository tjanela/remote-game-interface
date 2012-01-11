package com.blissapplications.java.remotegameinterface.packets;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.apache.log4j.Logger;

import com.blissapplications.java.remotegameinterface.context.OperationalServerHash;

/**
 * User: tjanela
 * Date: 11/27/11
 * Time: 4:27 AM
 */
public class OperationalProtocolPacket {
	private static Logger _logger = Logger.getLogger(OperationalProtocolPacket.class);
	public static final int ID_FIELD_LENGTH = 3;
	
	public static final String ERROR_ALREADY_REGISTERED_PAYLOAD = "ALREADY_REGISTERED";
	public static final String ERROR_UNKNOWN_HASH_PAYLOAD = "UNKNOWN_HASH";
	public static final String ERROR_GENERIC_PAYLOAD = "ERROR";
	
	public static final byte[] MAGIC_FIELD = {'[','!','P','U','M','P','!',']'};
	public static final int MAGIC_FIELD_LENGTH = MAGIC_FIELD.length;
	
	public static final byte[] ERROR_PAYLOAD = {'[','E','R','R',']'};
	public static final int ERROR_PAYLOAD_LENGTH = ERROR_PAYLOAD.length;

	public static final int PACKET_MIN_SIZE = ID_FIELD_LENGTH + MAGIC_FIELD_LENGTH;
	public static final int PACKET_MAX_SIZE = 200;
	public static final int PAYLOAD_MAX_SIZE = PACKET_MAX_SIZE - MAGIC_FIELD_LENGTH - ID_FIELD_LENGTH;

	public OperationalProtocolPacketType _packetType;
	private byte[] _id = new byte[ID_FIELD_LENGTH];
	private byte[] _payload = new byte[PAYLOAD_MAX_SIZE];
	private byte[] _magic = new byte[MAGIC_FIELD_LENGTH];

	public byte[] getId (){return _id;}
	public byte[] getPayload(){return _payload;}
	public byte[] getMagic(){return _magic;}

	
	private static OperationalProtocolPacket getPacket(OperationalProtocolPacketType type, String payload) throws Exception{
		ByteBuffer payloadByteBuffer = ByteBuffer.allocate(payload.length());
		payloadByteBuffer.put(Charset.forName("UTF-8").newEncoder().encode(CharBuffer.wrap(payload)));
		
		return getPacket(type, payloadByteBuffer);
	}
	
	private static OperationalProtocolPacket getPacket(OperationalProtocolPacketType type, ByteBuffer payload){
		return getPacket(type, payload.array());
		
	}
	
	private static OperationalProtocolPacket getPacket(OperationalProtocolPacketType type, byte[] payload){
		ByteBuffer magicByteBufffer = ByteBuffer.allocate(MAGIC_FIELD_LENGTH);
		magicByteBufffer.put(MAGIC_FIELD);
		
		OperationalProtocolPacket datagram = new OperationalProtocolPacket();
		datagram._packetType = type;
		datagram._id = OperationalProtocolPacketType.getTypeBytes(datagram._packetType);
		datagram._payload = payload;
		datagram._magic = magicByteBufffer.array();
		
		return datagram;
	}
	
	public static OperationalProtocolPacket getErrorDatagram(OperationalProtocolPacketType type){
		return getPacket(type, ERROR_PAYLOAD);
	}
	
	public static OperationalProtocolPacket  getUnregisterDisplayClientRequest(OperationalServerHash hash) throws Exception{
		String encodedHash = hash.toEncodedString();
		return getPacket(OperationalProtocolPacketType.UnregisterDisplayClientRequest, encodedHash);
	}
	
	public static OperationalProtocolPacket  getUnregisterControlClientRequest(OperationalServerHash hash) throws Exception{
		String encodedHash = hash.toEncodedString();
		return getPacket(OperationalProtocolPacketType.UnregisterControlClientRequest, encodedHash);
	}
	
	public static OperationalProtocolPacket  getRegisterDisplayClientResponse(OperationalServerHash hash) throws Exception{
		String encodedHash = hash.toEncodedString();
		return getPacket(OperationalProtocolPacketType.RegisterDisplayClientResponse, encodedHash);
	}
	
	public static OperationalProtocolPacket getRegisterDisplayClientErrorResponse(String payload) throws Exception{
		return getPacket(OperationalProtocolPacketType.RegisterDisplayClientResponse,payload);
	}
	
	public static OperationalProtocolPacket  getRegisterControlClientResponse(OperationalServerHash hash) throws Exception{
		String encodedHash = hash.toEncodedString();
		return getPacket(OperationalProtocolPacketType.RegisterControlClientResponse, encodedHash);
	}
	public static OperationalProtocolPacket getRegisterControlClientErrorResponse(String payload) throws Exception{
		return getPacket(OperationalProtocolPacketType.RegisterControlClientResponse,payload);
	}
	
	
	public static OperationalProtocolPacket  getUnregisterControlClientResponse(OperationalServerHash hash) throws Exception{
		String encodedHash = hash.toEncodedString();
		return getPacket(OperationalProtocolPacketType.UnregisterControlClientResponse, encodedHash);
	}
	
	public static OperationalProtocolPacket  getUnregisterDisplayClientResponse(OperationalServerHash hash) throws Exception{
		String encodedHash = hash.toEncodedString();
		return getPacket(OperationalProtocolPacketType.UnregisterDisplayClientResponse, encodedHash);
	}
	
	public static OperationalProtocolPacket  getPayloadResponse() throws Exception{
		return getPacket(OperationalProtocolPacketType.PayloadResponse, "");
	}

	public static OperationalProtocolPacket decode(ByteBuffer request) throws Exception{

		OperationalProtocolPacket decodedRequest = new OperationalProtocolPacket();

		int payloadSize = request.capacity() - ID_FIELD_LENGTH - MAGIC_FIELD_LENGTH;
		
		if(payloadSize < 0){
			_logger.error("Error decoding packet " + request.toString());
			return null;
		}
		
		decodedRequest._payload = new byte[payloadSize];
		
		request.get(decodedRequest._id, 0, ID_FIELD_LENGTH);
		request.get(decodedRequest._payload, 0, payloadSize);
		request.get(decodedRequest._magic, 0, MAGIC_FIELD_LENGTH);

		if(!Arrays.equals(MAGIC_FIELD, decodedRequest._magic)){
			throw new Exception("Cannot decode request. Invalid magic field.");
		}
		
		int datagramId = (decodedRequest._id[0] & 0x000000FF) << 16;
		datagramId |= (decodedRequest._id[1] & 0x000000FF) << 8;
		datagramId |= (decodedRequest._id[2] & 0x000000FF);
		
		decodedRequest._packetType = OperationalProtocolPacketType.fromInt(datagramId);

		if(decodedRequest._packetType.equals(OperationalProtocolPacketType.UnknownPacket)){
			throw new Exception("Cannot decode request. Unknown Datagram.");
		}

		return decodedRequest;
	}

	public static ByteBuffer encode(OperationalProtocolPacket packet) throws Exception{
		ByteBuffer byteBuffer = ByteBuffer.allocate(ID_FIELD_LENGTH + packet.getPayload().length + MAGIC_FIELD_LENGTH);

		int payloadLength = packet.getPayload().length;
		byteBuffer.put(packet.getId(), 0, ID_FIELD_LENGTH);
		byteBuffer.put(packet.getPayload(), 0, payloadLength);
		byteBuffer.put(packet.getMagic(), 0, MAGIC_FIELD_LENGTH);
		
		return byteBuffer;
	}
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		byte byte1 = getId()[0];
		byte byte2 = getId()[1];
		byte byte3 = getId()[2];
		
		String payload = new String(getPayload());
		String magic = new String(getMagic());
		
		sb.append("[").append("0x").append(String.format("%1$02X", byte1)).append(String.format("%1$02X", byte2)).append(String.format("%1$02X", byte3)).append("]");
		
		sb.append("[").append(payload).append("]");
		
		sb.append("[").append(magic).append("]");
		
		return sb.toString();
	}
}
