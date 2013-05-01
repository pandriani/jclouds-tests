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
import org.jclouds.scriptbuilder.domain.StatementList;
import org.jclouds.scriptbuilder.domain.Statements;
import org.jclouds.scriptbuilder.statements.ruby.InstallRuby;
import org.jclouds.scriptbuilder.statements.ruby.InstallRubyGems;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import com.google.inject.Module;

public class TestChef {

	private static final PROVIDER provider = PROVIDER.CLOUDSTACK;

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
			List<String> runlist = new RunListBuilder().addRecipe("zip").addRecipe("timezone")
					.addRecipe("java").addRecipe("tomcat").addRecipe("myapp")
					.build();
			chef.updateBootstrapConfigForGroup(runlist, group);
			// chef.updateRunListForGroup(runlist, group);
			Statement chefBootstrap = chef.createBootstrapScriptForGroup(group);
			
			//START FIX FOR OLD CHEF VERSION
			helper.runScriptOnGroup(computeService, "worker-node",
					new StatementList(InstallRuby.builder().build(),
							InstallRubyGems.builder().build()));
			helper.runScriptOnGroup(
					computeService,
					"worker-node",
					Statements.newStatementList(
							Statements.exec("gem sources -c"), 
//							Statements.exec("rvm --force gemset delete tmp1"), 
							Statements.exec("gem install net-ssh -v 2.2.2 --no-rdoc --no-ri"), 
							Statements.exec("gem install net-ssh-gateway -v 1.1.0 --no-rdoc --no-ri"),
							Statements.exec("gem install net-ssh-multi -v 1.1 --no-rdoc --no-ri")));
			//END FIX FOR OLD CHEF VERSION
			
			helper.runScriptOnGroup(computeService, "worker-node",
					chefBootstrap);

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			chefContext.close();
		}
	}
}
