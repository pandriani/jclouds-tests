package it.uniroma2.cloud;

import it.uniroma2.cloud.util.PropertiesMap;
import it.uniroma2.cloud.util.PropertiesMap.CloudProviderProperty;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.jclouds.ContextBuilder;
import org.jclouds.aws.domain.Region;
import org.jclouds.aws.ec2.reference.AWSEC2Constants;
import org.jclouds.chef.ChefContext;
import org.jclouds.chef.config.ChefProperties;
import org.jclouds.cloudstack.CloudStackAsyncClient;
import org.jclouds.cloudstack.CloudStackClient;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.ec2.EC2AsyncClient;
import org.jclouds.ec2.EC2Client;
import org.jclouds.enterprise.config.EnterpriseConfigurationModule;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.rest.RestContext;
import org.jclouds.sshj.config.SshjSshClientModule;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import com.google.inject.Module;

public class ProviderFactory {

	private static final PropertiesMap p = PropertiesMap.getInstance();

	public static ComputeService createComputeService(PROVIDER provider) {
		if (provider == PROVIDER.AWS_EC2)
			return createComputeServiceAWS(provider);
		else if (provider == PROVIDER.CLOUDSTACK)
			return createComputeServiceCloudstack(provider);
		else
			return null;
	}

	public static RestContext<?, ?> createRestContext(PROVIDER provider) {
		if (provider == PROVIDER.AWS_EC2)
			return createRestContextAWS(provider);
		else if (provider == PROVIDER.CLOUDSTACK)
			return createRestContextCloudStack(provider);
		else
			return null;
	}

	private static RestContext<CloudStackClient, CloudStackAsyncClient> createRestContextCloudStack(
			PROVIDER provider) {

		RestContext<CloudStackClient, CloudStackAsyncClient> context = ContextBuilder
				.newBuilder(provider.toString())
				.credentials(p.get(CloudProviderProperty.CLOUDSTACK_API_KEY),
						p.get(CloudProviderProperty.CLOUDSTACK_SECRET_KEY))
				.modules(getModules())
				.endpoint(p.get(CloudProviderProperty.CLOUDSTACK_URL)).build();

		return context;
	}

	private static RestContext<EC2Client, EC2AsyncClient> createRestContextAWS(
			PROVIDER provider) {
		RestContext<EC2Client, EC2AsyncClient> context = ContextBuilder
				.newBuilder(provider.toString())
				.credentials(p.get(CloudProviderProperty.AWS_ACCESS_KEY_ID),
						p.get(CloudProviderProperty.AWS_SECRET_KEY))
				.modules(getModules()).overrides(configureAWSProperties())
				.build();

		return context;
	}

	private static ComputeService createComputeServiceCloudstack(
			PROVIDER provider) {
		String endpoint = p.get(CloudProviderProperty.CLOUDSTACK_URL);
		String apiKey = p.get(CloudProviderProperty.CLOUDSTACK_API_KEY);
		String secretKey = p.get(CloudProviderProperty.CLOUDSTACK_SECRET_KEY);
		ContextBuilder builder = ContextBuilder.newBuilder(provider.toString())
				.credentials(apiKey, secretKey).modules(getModules())
				.endpoint(endpoint);
		ComputeService computeService = builder.build(
				ComputeServiceContext.class).getComputeService();
		return computeService;
	}

	public static ChefContext createChefContext() throws IOException {
		String chefClientName = p.get(CloudProviderProperty.CHEF_CLIENT_NAME);
		String pemFile = System.getProperty("user.home") + "/.chef/"
				+ chefClientName + ".pem";
		String clientCredential = Files.toString(new File(pemFile),
				Charsets.UTF_8);
		
		Properties chefConfig = new Properties();
		chefConfig.put(ChefProperties.CHEF_VALIDATOR_NAME, chefClientName);
		chefConfig.put(ChefProperties.CHEF_VALIDATOR_CREDENTIAL,
				clientCredential);

		ChefContext chefContext = ContextBuilder
				.newBuilder("chef")
				.endpoint((p.get(CloudProviderProperty.CHEF_SERVER_URL).toString()))
				.credentials(chefClientName,
						clientCredential)
				.modules(ImmutableSet.<Module> of(new SLF4JLoggingModule()))
				.overrides(chefConfig).build();
		return chefContext;
	}

	private static ComputeService createComputeServiceAWS(PROVIDER provider) {
		String accessKey = p.get(CloudProviderProperty.AWS_ACCESS_KEY_ID);
		String secretKey = p.get(CloudProviderProperty.AWS_SECRET_KEY);
		ContextBuilder builder = ContextBuilder.newBuilder(provider.toString())
				.credentials(accessKey, secretKey).modules(getModules())
				.overrides(configureAWSProperties());
		ComputeService computeService = builder.build(
				ComputeServiceContext.class).getComputeService();
		return computeService;
	}

	private static Iterable<Module> getModules() {
		return ImmutableSet.<Module> of(new SshjSshClientModule(),
				new SLF4JLoggingModule(), new EnterpriseConfigurationModule());
	}

	private static Properties configureAWSProperties() {
		Properties overrides = new Properties();
//		overrides.setProperty(AWSEC2Constants.PROPERTY_EC2_CC_REGIONS,
//				Region.US_EAST_1);
		return overrides;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		@SuppressWarnings("unchecked")
		RestContext<CloudStackClient, CloudStackAsyncClient> cloudStackContext = (RestContext<CloudStackClient, CloudStackAsyncClient>) ProviderFactory
				.createRestContext(PROVIDER.CLOUDSTACK);
		System.out.println(cloudStackContext.getDescription());

		@SuppressWarnings("unchecked")
		RestContext<EC2Client, EC2AsyncClient> awsContext = (RestContext<EC2Client, EC2AsyncClient>) ProviderFactory
				.createRestContext(PROVIDER.AWS_EC2);
		System.out.println(awsContext.getDescription());
	}

}
