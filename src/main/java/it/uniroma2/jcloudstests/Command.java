package it.uniroma2.jcloudstests;

import it.uniroma2.cloud.PROVIDER;
import it.uniroma2.cloud.ProviderFactory;
import it.uniroma2.cloud.util.ProviderHelper;
import it.uniroma2.cloud.util.ProviderHelperFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import org.jclouds.compute.ComputeService;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.RunScriptOnNodesException;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.Template;
import org.jclouds.scriptbuilder.domain.OsFamily;
import org.jclouds.scriptbuilder.domain.Statements;

public class Command {

	private static final PROVIDER provider = PROVIDER.AWS_EC2; // may be
																// PROVIDER.CLOUDSTACK
	private static final String WORKERNODE = "worker-node";
	private static final String MONITORING = "monitoring";
	private static final String CLIENT = "client";

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

	public void installWorkerNodes(PROVIDER provider)
			throws RunScriptOnNodesException {
		helper.runScriptOnGroup(
				computeService,
				WORKERNODE,
				Statements
						.newStatementList(
								Statements
										.exec("echo 'export CATALINA_OPTS=\"-Xms256m -Xmx512m\"' >> $HOME/.bashrc"),
								Statements.exec("echo 'export CLOUD_PROVIDER="
										+ provider.name()
										+ "' >> $HOME/.bashrc"),
								Statements
										.exec("echo 'export CATALINA_OPTS=\"-Xms256m -Xmx512m\"' >> $HOME/.profile"),
								Statements.exec("echo 'export CLOUD_PROVIDER="
										+ provider.name()
										+ "' >> $HOME/.profile"),
								Statements.exec("source $HOME/.profile"),
								Statements.exec("source $HOME/.bashrc"),
								Statements.exec("apt-get -y update"),
								Statements.exec("apt-get -y install unzip"),
								Statements
										.exec("wget https://s3.amazonaws.com/TesiAndrianiFiorentino/hyperic-sigar-1.6.4.zip"),
								Statements
										.exec("unzip hyperic-sigar-1.6.4.zip"),
								Statements
										.exec("mv hyperic-sigar-1.6.4/sigar-bin/lib/* /lib"),
								Statements.exec("apt-get -y install tomcat7"),
								Statements
										.exec("wget https://s3.amazonaws.com/TesiAndrianiFiorentino/imagetranscoder.war"),
								Statements
										.exec("mv imagetranscoder.war /var/lib/tomcat7/webapps"))
						.render(OsFamily.UNIX));
	}

	public void installClientNodes(PROVIDER provider)
			throws RunScriptOnNodesException {
		helper.runScriptOnGroup(
				computeService,
				CLIENT,
				Statements
						.newStatementList(
								Statements.exec("apt-get -y update"),
								Statements
										.exec("apt-get install openjdk-7-jdk"),
								Statements.exec("apt-get -y install unzip"),
								Statements
										.exec("wget https://s3.amazonaws.com/TesiAndrianiFiorentino/imgs.zip"),
								Statements.exec("unzip imgs.zip"),
								Statements.exec("mv imgs ~/"),
								Statements
										.exec("wget https://s3.amazonaws.com/TesiAndrianiFiorentino/client-0.0.1-SNAPSHOT-jar-with-dependencies.jar"))
						.render(OsFamily.UNIX));
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
			// cmd.printNodeGroup(WORKERNODE);
			cmd.createInstances(WORKERNODE, 2);
			cmd.installWorkerNodes(provider);

			//cmd.installClientNodes(provider);
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			computeService.getContext().close();
		}
	}

}
