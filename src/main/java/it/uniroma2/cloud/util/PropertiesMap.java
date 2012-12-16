package it.uniroma2.cloud.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.google.common.collect.Maps;

public class PropertiesMap {

	public enum CloudProviderProperty {
		// AWS
		AWS_ACCESS_KEY_ID, AWS_SECRET_KEY, AWS_KEY_NAME, AWS_KEY_PATH, AWS_AMI_USER, AWS_DEFAULT_IMAGE,
		//Other zones
		//EU_WEST
		AWS_KEY_NAME_EU_WEST, AWS_KEY_PATH_EU_WEST, AWS_DEFAULT_IMAGE_EU_WEST,

		// Cloudstack
		CLOUDSTACK_URL, CLOUDSTACK_API_KEY, CLOUDSTACK_SECRET_KEY,CLOUDSTACK_DEFAULT_IMAGE,CLOUDSTACK_IMAGE_USER, CLOUDSTACK_IMAGE_PASSWORD,

		//Chef Server
		CHEF_SERVER_CLOUDSTACK, CHEF_SERVER_AWS, CHEF_CLIENT_NAME,
	}

	private static PropertiesMap instance = null;
	private HashMap<String, String> map;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private PropertiesMap() {
		try {
			Properties p = new Properties();
			p.load(getClass().getClassLoader().getResourceAsStream(
					"uniroma2.properties"));
			this.map = Maps.newHashMap((Map) p);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static PropertiesMap getInstance() {
		if (instance == null) {
			instance = new PropertiesMap();
		}
		return instance;
	}

	public String get(CloudProviderProperty p) {
		return map.get(p.name());
	}

	public static void main(String[] args) {
		System.out.println(PropertiesMap.getInstance().get(
				CloudProviderProperty.AWS_ACCESS_KEY_ID));
	}

}