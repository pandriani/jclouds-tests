package it.uniroma2.cloud.util;

import it.uniroma2.cloud.util.PropertiesMap.CloudProviderProperty;

import java.io.File;
import java.io.IOException;

import org.jclouds.compute.options.TemplateOptions;
import org.jclouds.domain.LoginCredentials;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class AWSProviderHelper extends AbstractProviderHelper implements ProviderHelper {
	private static final PropertiesMap p = PropertiesMap.getInstance();
	private static AWSProviderHelper instance = null;

	private AWSProviderHelper() {
	}

	public static AWSProviderHelper getInstance() {
		if (instance == null) {
			instance = new AWSProviderHelper();
		}
		return instance;
	}

	public LoginCredentials getLoginCredentials() {
		String privateKey = null;
		try {
			privateKey = Files.toString(
			        new File(PropertiesMap.getInstance().get(CloudProviderProperty.AWS_KEY_PATH)), Charsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return LoginCredentials.builder().user(p.get(CloudProviderProperty.AWS_AMI_USER)).privateKey(privateKey).authenticateSudo(true).build();
	}
}
