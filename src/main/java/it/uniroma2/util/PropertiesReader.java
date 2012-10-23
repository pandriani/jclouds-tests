package it.uniroma2.util;

import java.io.IOException;
import java.util.Properties;

public class PropertiesReader extends Properties{

	public enum CloudProviders {
		AWS_ACCESS_KEY_ID,
		AWS_SECRET_KEY,
		AWS_KEY_NAME,
		AWS_SECURITY_GROUP_NAME,
		
		CLOUDSTACK_URL,
		CLOUDSTACK_API_KEY,
		CLOUDSTACK_SECRET_KEY
	}
	


	private static final long serialVersionUID = -1582779298930647956L;

	public PropertiesReader(String resourceName) throws IOException{
		load(getClass().getClassLoader().getResourceAsStream(resourceName));
	}
	
	
	
	public static void main(String[] args) throws IOException{
			PropertiesReader p = new PropertiesReader("uniroma2.properties");
			System.out.println(p.get(CloudProviders.AWS_ACCESS_KEY_ID));
	}
}