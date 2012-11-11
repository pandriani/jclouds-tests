package it.uniroma2.cloud.util;


import it.uniroma2.cloud.util.PropertiesMap.CloudProviderProperty;

import org.jclouds.compute.options.TemplateOptions;
import org.jclouds.domain.LoginCredentials;

public class CloudStackProviderHelper extends AbstractProviderHelper implements ProviderHelper {

	private static final PropertiesMap p = PropertiesMap.getInstance();
	private static CloudStackProviderHelper instance = null;
	private CloudStackProviderHelper(){}
	
	public static CloudStackProviderHelper getInstance() {
		if (instance == null) {
			instance = new CloudStackProviderHelper();
		}
		return instance;
	}
	
	public LoginCredentials getLoginCredentials() {
		return LoginCredentials
				.builder()
				.user(p.get(CloudProviderProperty.CLOUDSTACK_IMAGE_USER))
				.password(
						p.get(CloudProviderProperty.CLOUDSTACK_IMAGE_PASSWORD))
				.authenticateSudo(true).build();
	}

}
