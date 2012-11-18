package it.uniroma2.jcloudstests;

import it.uniroma2.cloud.PROVIDER;
import it.uniroma2.cloud.ProviderFactory;
import it.uniroma2.cloud.util.PropertiesMap;
import it.uniroma2.cloud.util.PropertiesMap.CloudProviderProperty;
import it.uniroma2.cloud.util.ProviderHelper;
import it.uniroma2.cloud.util.ProviderHelperFactory;

import java.io.File;
import java.io.IOException;
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

	private static final PROVIDER provider = PROVIDER.AWS_EC2; // may be PROVIDER.CLOUDSTACK

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
			// //ssh chef cookbook
			// SshClient ssh =
			// computeService.getContext().getUtils().sshForNode().apply(node);
			// ssh.connect();
			// ssh.exec("date");
			// ssh.put("/tmp/mysql-1.3.0.tar.gz",
			// Payloads.newFilePayload(new
			// File("/Users/pandriani/mysql-1.3.0.tar.gz")));
			// ssh.disconnect();
			// //ssh chef cookbook
			//
			// computeService.runScriptOnNode(node.getId(), new
			// InstallChefGems(),
			// TemplateOptions.Builder.overrideLoginCredentials(LoginCredentials.builder().
			// user(p.get(CloudProviderProperty.CLOUDSTACK_IMAGE_USER)).password(p.get(CloudProviderProperty.CLOUDSTACK_IMAGE_PASSWORD)).authenticateSudo(true).build()));
			//
			// RunList runlist = RunList.builder().recipe("mysql").build();
			// Role role =
			// Role.builder().name("storage").runlist(runlist).build();
			// ChefSolo st =
			// ChefSolo.builder().cookbooksArchiveLocation("/tmp/mysql-1.3.0.tar.gz").defineRole(role).build();
			//
			// computeService.runScriptOnNode(node.getId(), st);

			String javaApp = "java";

			List<String> recipes = new ArrayList<String>();
			recipes.add(javaApp);

			ImmutableList.Builder<Statement> bootstrapBuilder = ImmutableList
					.builder();
			bootstrapBuilder.add(new InstallGit());

			for (String recipe : recipes) {
				bootstrapBuilder.add(CloneGitRepo
						.builder()
						.repository(
								"git://github.com/opscode-cookbooks/" + recipe
										+ ".git")
						.directory("/var/chef/cookbooks/" + recipe) //
						.build());
			}

			// Configure Chef Solo to bootstrap the selected recipes
			bootstrapBuilder.add(ChefSolo.builder() //
					.cookbookPath("/var/chef/cookbooks") //
					.runlist(RunList.builder().recipes(recipes).build())
					.build());

			// Build the statement that will perform all the operations
			// above
			StatementList bootstrap = new StatementList(
					bootstrapBuilder.build());

			// Run the script in the nodes of the group
			helper.runScriptOnGroup(computeService, "worker-node", bootstrap);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			computeService.getContext().close();
		}
	}

}
