package it.uniroma2.cloud.util;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.filter;
import static org.jclouds.compute.options.TemplateOptions.Builder.overrideLoginCredentials;
import static org.jclouds.compute.predicates.NodePredicates.all;
import static org.jclouds.compute.predicates.NodePredicates.runningInGroup;
import it.uniroma2.cloud.util.PropertiesMap.CloudProviderProperty;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.jclouds.ContextBuilder;
import org.jclouds.chef.ChefContext;
import org.jclouds.chef.config.ChefProperties;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.RunScriptOnNodesException;
import org.jclouds.compute.domain.ExecResponse;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.Template;
import org.jclouds.domain.LoginCredentials;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.scriptbuilder.domain.Statement;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import com.google.inject.Module;

public abstract class AbstractProviderHelper implements ProviderHelper {

	public abstract LoginCredentials getLoginCredentials();

	public Iterable<? extends NodeMetadata> listRunningNodesInGroup(
			ComputeService computeService, String groupName) {
		return filter(computeService.listNodesDetailsMatching(all()),
				runningInGroup(groupName));
	}

	public void runScriptOnGroup(ComputeService compute, String groupName,
			String command) throws RunScriptOnNodesException {
		// when you run commands, you can pass options to decide whether
		// to run it as root, supply or own credentials vs from cache,
		// and wrap in an init script vs directly invoke
		Map<? extends NodeMetadata, ExecResponse> execResponses = compute
				.runScriptOnNodesMatching(//
						runningInGroup(groupName), // predicate used to select
													// nodes
						command, // what you actually intend to run
						overrideLoginCredentials(getLoginCredentials()) // use
																		// the
																		// local
																		// user
																		// &
								// ssh key
								.runAsRoot(true)); // don't attempt to run as
													// root (sudo)

		for (Entry<? extends NodeMetadata, ExecResponse> response : execResponses
				.entrySet()) {
			System.out.printf(
					"<< node %s: %s%n",
					response.getKey().getId(),
					concat(response.getKey().getPrivateAddresses(), response
							.getKey().getPublicAddresses()));
			System.out.printf("<<     %s%n", response.getValue());
		}
	}

	public void runScriptOnInstance(ComputeService compute,
			String instanceName, String command)
			throws RunScriptOnNodesException {
		// when you run commands, you can pass options to decide whether
		// to run it as root, supply or own credentials vs from cache,
		// and wrap in an init script vs directly invoke
		ExecResponse response = compute.runScriptOnNode(//
				instanceName, // predicate used to select nodes
				command, // what you actually intend to run
				overrideLoginCredentials(getLoginCredentials()) // use
																// the
																// local
																// user
																// &
						// ssh key
						.runAsRoot(true)); // don't attempt to run as
											// root (sudo)

		System.out.printf("<< node %s: %s%n", instanceName);
		System.out.printf("<<     %s%n", response.getOutput());
	}

	public ChefContext buildChefContext() throws IOException {
		PropertiesMap p = PropertiesMap.getInstance();

		String endpoint = getChefURL();

		String chefClientName = p.get(CloudProviderProperty.CHEF_CLIENT_NAME);
		String pemFile = System.getProperty("user.home") + "/.chef/"
				+ chefClientName + ".pem";
		String clientCredential = Files.toString(new File(pemFile),
				Charsets.UTF_8);

		Properties chefConfig = new Properties();
		chefConfig.put(ChefProperties.CHEF_VALIDATOR_NAME, chefClientName);
		chefConfig.put(ChefProperties.CHEF_VALIDATOR_CREDENTIAL,
				clientCredential);

		ChefContext chefContext = ContextBuilder.newBuilder("chef")
				.endpoint(endpoint)
				.credentials(chefClientName, clientCredential)
				.modules(ImmutableSet.<Module> of(new SLF4JLoggingModule()))
				.overrides(chefConfig).build();
		return chefContext;
	}

	protected abstract String getChefURL();

	public abstract Template getTemplate(ComputeService computeService);

	protected int[] getPortsToBeOpened() {
		return new int[] { 80, 443, 22, 8080, 8443 };
	}

}
