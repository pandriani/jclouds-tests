package it.uniroma2.jcloudstests;

import it.uniroma2.util.PropertiesReader;
import it.uniroma2.util.PropertiesReader.CloudProviders;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import org.jclouds.ContextBuilder;
import org.jclouds.cloudstack.CloudStackAsyncClient;
import org.jclouds.cloudstack.CloudStackClient;
import org.jclouds.cloudstack.CloudStackDomainAsyncClient;
import org.jclouds.cloudstack.CloudStackDomainClient;
import org.jclouds.cloudstack.CloudStackGlobalAsyncClient;
import org.jclouds.cloudstack.CloudStackGlobalClient;
import org.jclouds.cloudstack.domain.Capabilities;
import org.jclouds.cloudstack.domain.SecurityGroup;
import org.jclouds.cloudstack.domain.VirtualMachine;
import org.jclouds.cloudstack.features.ConfigurationClient;
import org.jclouds.cloudstack.features.HypervisorClient;
import org.jclouds.cloudstack.features.NetworkClient;
import org.jclouds.cloudstack.features.SecurityGroupClient;
import org.jclouds.cloudstack.features.VirtualMachineClient;
import org.jclouds.enterprise.config.EnterpriseConfigurationModule;

import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.providers.ProviderMetadata;
import org.jclouds.providers.Providers;
import org.jclouds.rest.RestContext;
import org.jclouds.sshj.config.SshjSshClientModule;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;

public class TestCloudStack {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		PropertiesReader p = new PropertiesReader("uniroma2.properties");

		String CLOUDSTACK_URL = p.getProperty(CloudProviders.CLOUDSTACK_URL
				.name());
		String CLOUDSTACK_API_KEY = p
				.getProperty(CloudProviders.CLOUDSTACK_API_KEY.name());
		String CLOUDSTACK_SECRET_KEY = p
				.getProperty(CloudProviders.CLOUDSTACK_SECRET_KEY.name());
		System.out.println(CLOUDSTACK_URL);

		Iterable<Module> modules = ImmutableSet.<Module> of(
				new SshjSshClientModule(), new SLF4JLoggingModule(),
				new EnterpriseConfigurationModule());

		RestContext<CloudStackClient, CloudStackAsyncClient> context = ContextBuilder
				.newBuilder("cloudstack")
				.credentials(CLOUDSTACK_API_KEY, CLOUDSTACK_SECRET_KEY)
				.modules(modules).endpoint(CLOUDSTACK_URL).build();

		System.out.println(context.getId());

		// Get a synchronous client
		CloudStackClient client = context.getApi();

		ConfigurationClient configurationClient = client.getConfigurationClient();
		Capabilities caps = configurationClient.listCapabilities();
		System.out.println(caps.toString());

		VirtualMachineClient vmClient = client.getVirtualMachineClient();
		Set<VirtualMachine> vms = vmClient.listVirtualMachines();
		Iterator<VirtualMachine> vmIt = vms.iterator();
		while(vmIt.hasNext()){
			VirtualMachine vm = vmIt.next();
			System.out.println(vm);
			System.out.println(vm.getName());
		}
	}

}
