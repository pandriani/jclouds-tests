package it.uniroma2.cloud.util;

import static com.google.common.collect.Iterables.filter;
import static org.jclouds.compute.predicates.NodePredicates.*;
import static com.google.common.base.Predicates.*;

import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.options.TemplateOptions;
import org.jclouds.domain.LoginCredentials;

public abstract class AbstractProviderHelper implements ProviderHelper {

	public TemplateOptions overrideLoginCredential() {
		return TemplateOptions.Builder
				.overrideLoginCredentials(getLoginCredentials());
	}

	public abstract LoginCredentials getLoginCredentials();

	public Iterable<? extends NodeMetadata> listRunningNodesInGroup(
			ComputeService computeService, String groupName) {
		return filter(computeService.listNodesDetailsMatching(all()),
				and(inGroup(groupName), not(TERMINATED)));

	}

}
