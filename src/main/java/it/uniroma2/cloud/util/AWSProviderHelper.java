package it.uniroma2.cloud.util;

import it.uniroma2.cloud.util.PropertiesMap.CloudProviderProperty;

import java.io.File;
import java.io.IOException;

import org.jclouds.aws.domain.Region;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.domain.TemplateBuilder;
import org.jclouds.domain.LoginCredentials;
import org.jclouds.ec2.compute.options.EC2TemplateOptions;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class AWSProviderHelper extends AbstractProviderHelper implements
		ProviderHelper {
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
					new File(p.get(CloudProviderProperty.AWS_KEY_PATH)),
					Charsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return LoginCredentials.builder()
				.user(p.get(CloudProviderProperty.AWS_AMI_USER))
				.privateKey(privateKey).authenticateSudo(true).build();
	}

	@Override
	public Template getTemplate(ComputeService computeService) {
		TemplateBuilder templateBuilder = computeService.templateBuilder();
		Template t = templateBuilder
				.imageId(
						Region.US_EAST_1
								+ "/"
								+ p.get(CloudProviderProperty.AWS_DEFAULT_IMAGE))
				.smallest()
				.options(
						EC2TemplateOptions.Builder
								.keyPair(
										p.get(CloudProviderProperty.AWS_KEY_NAME))
								.overrideLoginCredentials(getLoginCredentials()))
				.build();
		return t;
	}
}
