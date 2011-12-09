package com.blissapplications.java.remotegameinterface.context;


import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

import org.apache.log4j.Logger;


/**
 * User: tjanela
 * Date: 11/27/11
 * Time: 4:05 AM
 */
public class OperationalServerHash {
	public static final int HASH_SIZE = 6;
	private static String _alphabet = "0123456789abcdefghijklmnopqrstuvwxyz";
	private static Logger _logger = Logger.getLogger(OperationalServerHash.class);
	private char[] hash;
	private static Random _random;
	
	static{
		try{
			_random = SecureRandom.getInstance("SHA1PRNG");
		}catch(Exception ex){
			_logger.error("Couldn't create SecureRandom: ", ex);
		}
		
	}
	
	private OperationalServerHash(){
		hash = new char[HASH_SIZE];
		for(int i = 0; i < HASH_SIZE ; i++)
		{
			hash[i] = _alphabet.charAt(_random.nextInt(_alphabet.length()));
		}
		
	}

	private OperationalServerHash(String encodedString) throws Exception{
		hash = new char[HASH_SIZE];
		encodedString.getChars(0, HASH_SIZE, hash, 0);
	}

	public static OperationalServerHash newHash(){
		return new OperationalServerHash();
	}

	public static OperationalServerHash fromEncodedString(String string) throws Exception{
		return new OperationalServerHash(string);
	}
	
	public static OperationalServerHash fromByteArray(byte[] byteArray) throws Exception{
		String stringRepresentation = Charset.forName("UTF-8").newDecoder().decode(ByteBuffer.wrap(byteArray)).toString();
		return new OperationalServerHash(stringRepresentation);
	}

	public String toEncodedString(){
		return new String(hash);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof OperationalServerHash)) return false;

		OperationalServerHash that = (OperationalServerHash) o;
		if (!Arrays.equals(hash,that.hash)) return false;
		return true;
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public String toString(){
		return new String(hash);
	}
}
