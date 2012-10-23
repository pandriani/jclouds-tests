package it.uniroma2.jcloudstests;

import java.io.IOException;
import java.util.Properties;

public class PropertiesReader extends Properties{

	public enum AWS {
		ACCESS_KEY_ID,
		SECRET_KEY,
		KEY_NAME,
		SECURITY_GROUP_NAME
	}

	private static final long serialVersionUID = -1582779298930647956L;

	public PropertiesReader(String resourceName) throws IOException{
		load(getClass().getClassLoader().getResourceAsStream(resourceName));
	}
	
	
	
	public static void main(String[] args) throws IOException{
			PropertiesReader p = new PropertiesReader("uniroma2.properties");
			System.out.println(p.get(AWS.ACCESS_KEY_ID));
	}
}