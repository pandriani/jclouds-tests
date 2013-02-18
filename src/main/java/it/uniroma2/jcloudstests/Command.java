package it.uniroma2.jcloudstests;

import it.uniroma2.cloud.PROVIDER;
import it.uniroma2.cloud.ProviderFactory;
import it.uniroma2.cloud.util.PropertiesMap;
import it.uniroma2.cloud.util.PropertiesMap.CloudProviderProperty;
import it.uniroma2.cloud.util.ProviderHelper;
import it.uniroma2.cloud.util.ProviderHelperFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jclouds.compute.ComputeService;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.RunScriptOnNodesException;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.domain.TemplateBuilder;
import org.jclouds.compute.options.TemplateOptions;
import org.jclouds.domain.LoginCredentials;
import org.jclouds.io.Payloads;
import org.jclouds.scriptbuilder.domain.Statement;
import org.jclouds.scriptbuilder.domain.StatementList;
import org.jclouds.scriptbuilder.domain.Statements;
import org.jclouds.scriptbuilder.domain.chef.Role;
import org.jclouds.scriptbuilder.domain.chef.RunList;
import org.jclouds.scriptbuilder.statements.chef.ChefSolo;
import org.jclouds.scriptbuilder.statements.chef.InstallChefGems;
import org.jclouds.scriptbuilder.statements.git.CloneGitRepo;
import org.jclouds.scriptbuilder.statements.git.InstallGit;

import org.jclouds.ssh.SshClient;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class Command {

	private static final PROVIDER provider = PROVIDER.AWS_EC2; // may be
																// PROVIDER.CLOUDSTACK
	private static final String WORKERNODE = "worker-node";
	private ComputeService computeService;
	private ProviderHelper helper;

	public Command(ComputeService computeService, ProviderHelper helper) {
		super();
		this.computeService = computeService;
		this.helper = helper;
	}

	public void printNodeGroup(String group) {
		Iterable<? extends NodeMetadata> nodes = helper
				.listRunningNodesInGroup(computeService, group);
		Iterator<? extends NodeMetadata> nodeIt = nodes.iterator();
		while (nodeIt.hasNext()) {
			NodeMetadata node = nodeIt.next();
			System.out.println(node);
			System.out.println(node.getPublicAddresses().iterator().next());
		}
	}

	public void createInstances(String group, int howMany)
			throws RunNodesException {
		Template t = helper.getTemplate(computeService);
		Set<? extends NodeMetadata> nodeSet = computeService
				.createNodesInGroup(group, howMany, t);

		Iterator<? extends NodeMetadata> itNode = nodeSet.iterator();
		while (itNode.hasNext()) {
			NodeMetadata node = (NodeMetadata) itNode.next();
			System.out.println(node);
			System.out.println(node.getPublicAddresses().iterator().next());
		}
	}


	public void installWorkerNodes() throws RunScriptOnNodesException {
		helper.runScriptOnGroup(
				computeService,
				WORKERNODE,
				Statements.newStatementList(
						Statements.exec("export CATALINA_OPTS=\"-Xms256m -Xmx512m\""),
						Statements.exec("apt-get -y update"),
						Statements.exec("apt-get -y install tomcat7"),
						Statements
								.exec("wget https://s3.amazonaws.com/TesiAndrianiFiorentino/imagetranscoder.war"),
						Statements
								.exec("mv imagetranscoder.war /var/lib/tomcat7/webapps")));
	}
	
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) {
		ComputeService computeService = ProviderFactory
				.createComputeService(provider);
		ProviderHelper helper = ProviderHelperFactory
				.getProviderHelper(provider);
		try {
			Command cmd = new Command(computeService, helper);
			//cmd.printNodeGroup(WORKERNODE);
			cmd.createInstances(WORKERNODE, 2);
			cmd.installWorkerNodes();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			computeService.getContext().close();
		}
	}

}
