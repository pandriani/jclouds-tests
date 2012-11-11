package it.uniroma2.cloud.util;

import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.options.TemplateOptions;
import org.jclouds.domain.LoginCredentials;

public interface ProviderHelper {

	TemplateOptions overrideLoginCredential();
	LoginCredentials getLoginCredentials();
	
	Iterable<? extends NodeMetadata> listRunningNodesInGroup(ComputeService computeService, String groupName);
}
