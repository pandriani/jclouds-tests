package it.uniroma2.cloud.util;

import it.uniroma2.cloud.util.PropertiesMap.CloudProviderProperty;

import java.io.File;
import java.io.IOException;

import org.jclouds.aws.domain.Region;
import org.jclouds.chef.ChefContext;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.domain.TemplateBuilder;
import org.jclouds.domain.LoginCredentials;
import org.jclouds.ec2.compute.options.EC2TemplateOptions;
import org.jclouds.ec2.domain.AvailabilityZoneInfo;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class AWSProviderHelper extends AbstractProviderHelper implements
		ProviderHelper {

	private static String DEFAULT_REGION = Region.US_EAST_1;
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

	@Override
	public LoginCredentials getLoginCredentials() {
		LoginCredentials lc = null;
		try {
			lc = getLoginCredentials(DEFAULT_REGION);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lc;
	}

	@Override
	public Template getTemplate(ComputeService computeService) {
		return getTemplate(computeService, DEFAULT_REGION);
	}

	public Template getTemplate(ComputeService computeService, String region) {
		TemplateBuilder templateBuilder = computeService.templateBuilder();
		Template t = null;
		String awsRegion = null;
		String imageId = null;
		String keyName = null;
		if (region == DEFAULT_REGION) {
			awsRegion = DEFAULT_REGION;
			imageId = p.get(CloudProviderProperty.AWS_DEFAULT_IMAGE);
			keyName = p.get(CloudProviderProperty.AWS_KEY_NAME);
		} else if (region == Region.EU_WEST_1) {
			awsRegion = region;
			imageId = p.get(CloudProviderProperty.AWS_DEFAULT_IMAGE_EU_WEST);
			keyName = p.get(CloudProviderProperty.AWS_KEY_NAME_EU_WEST);
		} else
			return null;

		try {
			t = templateBuilder
					.imageId(awsRegion + "/" + imageId)
					.smallest()
					.options(
							EC2TemplateOptions.Builder
									.keyPair(keyName)
									.overrideLoginCredentials(
											getLoginCredentials(awsRegion))
									.inboundPorts(getPortsToBeOpened()))
					.build();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return t;
	}

	public LoginCredentials getLoginCredentials(String region)
			throws IOException {
		String keyPath = null;
		if (region == DEFAULT_REGION) {
			keyPath = p.get(CloudProviderProperty.AWS_KEY_PATH);
		} else if (region == Region.EU_WEST_1) {
			keyPath = p.get(CloudProviderProperty.AWS_KEY_PATH_EU_WEST);
		} else
			keyPath = null;

		String privateKey = Files.toString(new File(keyPath), Charsets.UTF_8);
		return LoginCredentials.builder()
				.user(p.get(CloudProviderProperty.AWS_AMI_USER))
				.privateKey(privateKey).authenticateSudo(true).build();

	}

	@Override
	protected String getChefURL() {
		PropertiesMap p = PropertiesMap.getInstance();
		return 	p.get(CloudProviderProperty.CHEF_SERVER_AWS);
	}
}
