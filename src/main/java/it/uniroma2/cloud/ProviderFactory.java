package it.uniroma2.cloud;

import it.uniroma2.cloud.util.PropertiesMap;
import it.uniroma2.cloud.util.PropertiesMap.CloudProviderProperty;

import org.jclouds.ContextBuilder;
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

import com.google.common.collect.ImmutableSet;
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
				.modules(getModules()).build();

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

	private static ComputeService createComputeServiceAWS(PROVIDER provider) {
		String accessKey = p.get(CloudProviderProperty.AWS_ACCESS_KEY_ID);
		String secretKey = p.get(CloudProviderProperty.AWS_SECRET_KEY);
		ContextBuilder builder = ContextBuilder.newBuilder(provider.toString())
				.credentials(accessKey, secretKey).modules(getModules());
		ComputeService computeService = builder.build(
				ComputeServiceContext.class).getComputeService();
		return computeService;
	}

	private static Iterable<Module> getModules() {
		return ImmutableSet.<Module> of(new SshjSshClientModule(),
				new SLF4JLoggingModule(), new EnterpriseConfigurationModule());
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		RestContext<CloudStackClient, CloudStackAsyncClient> cloudStackContext = (RestContext<CloudStackClient, CloudStackAsyncClient>) ProviderFactory
				.createRestContext(PROVIDER.CLOUDSTACK);
		System.out.println(cloudStackContext.getDescription());

		RestContext<EC2Client, EC2AsyncClient> awsContext = (RestContext<EC2Client, EC2AsyncClient>) ProviderFactory
				.createRestContext(PROVIDER.AWS_EC2);
		System.out.println(awsContext.getDescription());
	}

}
