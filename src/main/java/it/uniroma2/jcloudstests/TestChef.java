package it.uniroma2.jcloudstests;

import it.uniroma2.cloud.PROVIDER;
import it.uniroma2.cloud.ProviderFactory;
import it.uniroma2.cloud.util.ProviderHelper;
import it.uniroma2.cloud.util.ProviderHelperFactory;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.jclouds.ContextBuilder;
import org.jclouds.chef.ChefContext;
import org.jclouds.chef.ChefService;
import org.jclouds.chef.config.ChefProperties;
import org.jclouds.chef.util.RunListBuilder;
import org.jclouds.compute.ComputeService;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.scriptbuilder.domain.OsFamily;
import org.jclouds.scriptbuilder.domain.Statement;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import com.google.inject.Module;

public class TestChef {

	private static final PROVIDER provider = PROVIDER.AWS_EC2;

	public static void main(final String[] args) throws Exception {
		// Group for the virtual machines
		String group = "worker-node";

		// Connect to the cloud provider

		ComputeService computeService = ProviderFactory
				.createComputeService(provider);
		ProviderHelper helper = ProviderHelperFactory
				.getProviderHelper(provider);

		ChefContext chefContext = helper.buildChefContext();
		ChefService chef = chefContext.getChefService();

		try {
			// Build the runlist for the deployed nodes
			System.out
					.println("Configuring node runlist in the Chef server...");
			List<String> runlist = new RunListBuilder().addRecipe("java")
					.addRecipe("tomcat").addRecipe("myapp").build();
			chef.updateBootstrapConfigForGroup(runlist, group);
			//chef.updateRunListForGroup(runlist, group);
			Statement chefBootstrap = chef.createBootstrapScriptForGroup(group);

			 helper.runScriptOnGroup(computeService, "worker-node",
			 chefBootstrap.render(OsFamily.UNIX));

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			chefContext.close();
		}
	}
}
