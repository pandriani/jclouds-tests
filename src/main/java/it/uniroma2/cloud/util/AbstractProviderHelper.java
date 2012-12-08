package it.uniroma2.cloud.util;

import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.filter;
import static org.jclouds.compute.options.TemplateOptions.Builder.overrideLoginCredentials;
import static org.jclouds.compute.predicates.NodePredicates.TERMINATED;
import static org.jclouds.compute.predicates.NodePredicates.all;
import static org.jclouds.compute.predicates.NodePredicates.inGroup;

import java.util.Map;
import java.util.Map.Entry;

import org.jclouds.compute.ComputeService;
import org.jclouds.compute.RunScriptOnNodesException;
import org.jclouds.compute.domain.ExecResponse;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.predicates.NodePredicates;
import org.jclouds.domain.LoginCredentials;
import org.jclouds.scriptbuilder.domain.Statement;

public abstract class AbstractProviderHelper implements ProviderHelper {

	public abstract LoginCredentials getLoginCredentials();

	public Iterable<? extends NodeMetadata> listRunningNodesInGroup(
			ComputeService computeService, String groupName) {
		return filter(computeService.listNodesDetailsMatching(all()),
				and(inGroup(groupName), not(TERMINATED)));
	}

	public void runScriptOnGroup(ComputeService compute, String groupName,
			Statement command) throws RunScriptOnNodesException {
		// when you run commands, you can pass options to decide whether
		// to run it as root, supply or own credentials vs from cache,
		// and wrap in an init script vs directly invoke
		Map<? extends NodeMetadata, ExecResponse> execResponses = compute
				.runScriptOnNodesMatching(//
						inGroup(groupName), // predicate used to select nodes
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

	public abstract Template getTemplate(ComputeService computeService);
	
	protected int[] getPortsToBeOpened() {
		return new int[] { 80, 443, 22, 8080, 8443 };
	}


}
