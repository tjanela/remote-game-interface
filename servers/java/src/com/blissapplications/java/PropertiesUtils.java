package com.blissapplications.java;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesUtils {

	public static Properties load(String filename) throws IOException{
		Properties result = new Properties();
		
		FileInputStream fileInputStream = new FileInputStream(filename);

		result.load(fileInputStream);
		
		return result;
	}
	
}
