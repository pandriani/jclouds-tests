package it.uniroma2.jcloudstests;

import it.uniroma2.cloud.PROVIDER;
import it.uniroma2.cloud.ProviderFactory;
import it.uniroma2.cloud.util.PropertiesMap;
import it.uniroma2.cloud.util.PropertiesMap.CloudProviderProperty;
import it.uniroma2.cloud.util.ProviderHelper;
import it.uniroma2.cloud.util.ProviderHelperFactory;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import org.jclouds.compute.ComputeService;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.domain.TemplateBuilder;
import org.jclouds.compute.options.TemplateOptions;
import org.jclouds.domain.LoginCredentials;
import org.jclouds.io.Payloads;
import org.jclouds.scriptbuilder.statements.chef.InstallChefGems;
import org.jclouds.ssh.SshClient;

public class TestCloudStack {

	private static final PropertiesMap p = PropertiesMap.getInstance();
	private static final PROVIDER provider = PROVIDER.CLOUDSTACK;
	
	
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		
		
		ComputeService computeService = ProviderFactory.createComputeService(provider);
		ProviderHelper helper = ProviderHelperFactory.getProviderHelper(provider);
		
		Iterable<? extends NodeMetadata> nodes = helper.listRunningNodesInGroup(computeService, "worker-node");
		Iterator<? extends NodeMetadata> nodeIt = nodes.iterator();
		while(nodeIt.hasNext()){
			NodeMetadata node = nodeIt.next();
			System.out.println(node);	
		}
		TemplateBuilder templateBuilder = computeService.templateBuilder();
		TemplateOptions opts = helper.overrideLoginCredential();
		
		Template t = templateBuilder.imageId(p.get(CloudProviderProperty.CLOUDSTACK_DEFAULT_IMAGE)).smallest().options(opts).build();
		try {
			Set<? extends NodeMetadata> nodeSet = computeService.createNodesInGroup("worker-node", 1, t);
			
			Iterator<? extends NodeMetadata> itNode = nodeSet.iterator();
			while (itNode.hasNext()) {
				NodeMetadata node = (NodeMetadata) itNode.next();
				System.out.println(node);
				
				//ssh chef cookbook
				SshClient ssh = computeService.getContext().getUtils().sshForNode().apply(node);
				ssh.connect();
				ssh.exec("date");
				ssh.put("/tmp/mysql-1.3.0.tar.gz",
				    Payloads.newFilePayload(new File("/Users/pandriani/mysql-1.3.0.tar.gz")));
				ssh.disconnect();
				//ssh chef cookbook
				
				computeService.runScriptOnNode(node.getId(), new InstallChefGems(), TemplateOptions.Builder.overrideLoginCredentials(LoginCredentials.builder().
	            user(p.get(CloudProviderProperty.CLOUDSTACK_IMAGE_USER)).password(p.get(CloudProviderProperty.CLOUDSTACK_IMAGE_PASSWORD)).authenticateSudo(true).build()));
				
//				computeService.runScriptOnNode(node.getId(), ChefSolo.builder()
//				    .cookbooksArchiveLocation("/tmp/mysql-1.3.0.tar.gz")
//				    .installRecipe("mysql")
//				    .build());
				
			}
						
		} catch (RunNodesException e) {
			e.printStackTrace();
		}finally{
			computeService.getContext().close();			
		}
	}

}
