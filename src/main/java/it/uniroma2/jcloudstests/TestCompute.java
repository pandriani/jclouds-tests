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

public class TestCompute {

	private static final PROVIDER provider = PROVIDER.AWS_EC2; // may be
																// PROVIDER.CLOUDSTACK

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		ComputeService computeService = ProviderFactory
				.createComputeService(provider);
		ProviderHelper helper = ProviderHelperFactory
				.getProviderHelper(provider);

		Iterable<? extends NodeMetadata> nodes = helper
				.listRunningNodesInGroup(computeService, "worker-node");
		Iterator<? extends NodeMetadata> nodeIt = nodes.iterator();
		while (nodeIt.hasNext()) {
			NodeMetadata node = nodeIt.next();
			System.out.println(node);
		}

		Template t = helper.getTemplate(computeService);

		try {
			Set<? extends NodeMetadata> nodeSet = computeService
					.createNodesInGroup("worker-node", 1, t);

			Iterator<? extends NodeMetadata> itNode = nodeSet.iterator();
			while (itNode.hasNext()) {
				NodeMetadata node = (NodeMetadata) itNode.next();
				System.out.println(node);
			}

			helper.runScriptOnGroup(computeService, "worker-node", Statements
					.newStatementList(Statements.exec("date"),
							Statements.exec("apt-get -y install tomcat7"), 
							Statements.exec("wget https://s3.amazonaws.com/TesiAndrianiFiorentino/imagetranscoder.war"),
							Statements.exec("mv imagetranscoder.war /var/lib/tomcat7/webapps")));

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			computeService.getContext().close();
		}
	}

}
