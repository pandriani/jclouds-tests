package it.uniroma2.cloud.util;

import org.jclouds.compute.ComputeService;
import org.jclouds.compute.RunScriptOnNodesException;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.Template;
import org.jclouds.domain.LoginCredentials;
import org.jclouds.scriptbuilder.domain.Statement;

public interface ProviderHelper {

	LoginCredentials getLoginCredentials();
	
	Iterable<? extends NodeMetadata> listRunningNodesInGroup(ComputeService computeService, String groupName);
	
	void runScriptOnGroup(ComputeService compute, String groupName, Statement command) throws RunScriptOnNodesException;
	
	Template getTemplate(ComputeService computeService);
}
