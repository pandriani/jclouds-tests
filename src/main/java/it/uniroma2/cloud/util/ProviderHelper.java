package it.uniroma2.cloud.util;

import java.io.IOException;

import org.jclouds.chef.ChefContext;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.RunScriptOnNodesException;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.Template;
import org.jclouds.domain.LoginCredentials;
import org.jclouds.scriptbuilder.domain.Statement;

public interface ProviderHelper {

	LoginCredentials getLoginCredentials() throws IOException;

	Iterable<? extends NodeMetadata> listRunningNodesInGroup(
			ComputeService computeService, String groupName);

	void runScriptOnGroup(ComputeService compute, String groupName,
			Statement command) throws RunScriptOnNodesException;

	void runScriptOnInstance(ComputeService compute,
			String instanceName, String command)
			throws RunScriptOnNodesException;

	Template getTemplate(ComputeService computeService);
	
	ChefContext buildChefContext() throws IOException;
}
