package it.uniroma2.jcloudstests;

import static org.jclouds.ec2.options.RunInstancesOptions.Builder.asType;
import static org.jclouds.scriptbuilder.domain.Statements.exec;
import it.uniroma2.cloud.PROVIDER;
import it.uniroma2.cloud.ProviderFactory;
import it.uniroma2.cloud.util.ProviderHelper;
import it.uniroma2.cloud.util.ProviderHelperFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jclouds.aws.domain.Region;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.ComputeMetadata;
import org.jclouds.compute.domain.ExecResponse;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.ec2.EC2Client;
import org.jclouds.ec2.domain.InstanceType;
import org.jclouds.ec2.domain.KeyPair;
import org.jclouds.ec2.domain.Reservation;
import org.jclouds.ec2.domain.RunningInstance;
import org.jclouds.ec2.predicates.InstanceStateRunning;
import org.jclouds.predicates.InetSocketAddressConnect;
import org.jclouds.predicates.RetryablePredicate;
import org.jclouds.scriptbuilder.ScriptBuilder;
import org.jclouds.scriptbuilder.domain.OsFamily;
import org.jclouds.scriptbuilder.statements.git.InstallGit;

import com.google.common.collect.Iterables;
import com.google.common.net.HostAndPort;

public class TestAWSComputeScript {

	public static String AWS_DEFAULT_REGION = Region.US_EAST_1;
	public static String AMI = "ami-834cf1ea"; // Alestic Ubuntu 12.04 LTS
												// Precise instance store

	public static void main(String[] args) throws TimeoutException, IOException {

		ComputeService computeService = ProviderFactory
				.createComputeService(PROVIDER.AWS_EC2);
		ProviderHelper helper = ProviderHelperFactory.getProviderHelper(PROVIDER.AWS_EC2);

		try {

			Set<? extends ComputeMetadata> cmSet = computeService.listNodes();
			// Iterator<? extends ComputeMetadata> it = cmSet.iterator();
			// while (it.hasNext()) {
			// ComputeMetadata computeMetadata = (ComputeMetadata) it.next();
			// System.out.println(computeMetadata);
			// }

			NodeMetadata nodeMetadata = computeService
					.getNodeMetadata("us-east-1/i-0fa83873");
			System.out.println(nodeMetadata);

			ExecResponse resp = computeService.runScriptOnNode(
					nodeMetadata.getId(), new InstallGit(),
					helper.overrideLoginCredential());
			System.out.println(resp.getOutput());

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// Close connecton
			computeService.getContext().close();
			System.exit(0);
		}

	}

	private static RunningInstance createSecurityGroupKeyPairAndInstance(
			EC2Client client, String name) throws TimeoutException {

		// create a new instance
		RunningInstance instance = runInstance(client, name, name);

		// await for the instance to start
		return blockUntilInstanceRunning(client, instance);
	}

	static KeyPair getKeyPair(EC2Client client, String name) {
		System.out
				.printf("%d: Get key: %s%n", System.currentTimeMillis(), name);

		Set<KeyPair> keypairs = client.getKeyPairServices()
				.describeKeyPairsInRegion(AWS_DEFAULT_REGION, name);
		Iterator<KeyPair> it = keypairs.iterator();
		KeyPair keyPair = null;
		while (it.hasNext()) {
			keyPair = it.next();
			System.out.println(keyPair);
		}
		return keyPair;
	}

	static RunningInstance runInstance(EC2Client client,
			String securityGroupName, String keyPairName) {
		String script = new ScriptBuilder() // lamp install script
				.addStatement(exec("runurl run.alestic.com/apt/upgrade"))//
				.addStatement(exec("runurl run.alestic.com/install/lamp"))//
				.addStatement(exec("apt-get -y install openjdk-6-jdk"))// no
																		// license
																		// agreement!
				.render(OsFamily.UNIX);

		System.out.printf("%d: running instance%n", System.currentTimeMillis());

		Reservation<? extends RunningInstance> reservation = client
				.getInstanceServices().runInstancesInRegion(AWS_DEFAULT_REGION,
						null, // allow
						// ec2
						// to
						// chose
						// an
						// availability
						// zone
						AMI, // alestic ami allows auto-invoke of
								// user data scripts
						1, // minimum instances
						1, // maximum instances
						asType(InstanceType.M1_SMALL) // smallest instance size
								.withKeyName(keyPairName) // key I created above
								.withSecurityGroup(securityGroupName) // group I
																		// created
																		// above
								.withUserData(script.getBytes())); // script to
																	// run as
																	// root

		return Iterables.getOnlyElement(reservation);

	}

	static RunningInstance blockUntilInstanceRunning(EC2Client client,
			RunningInstance instance) throws TimeoutException {
		// create utilities that wait for the instance to finish
		RetryablePredicate<RunningInstance> runningTester = new RetryablePredicate<RunningInstance>(
				new InstanceStateRunning(client), 180, 5, TimeUnit.SECONDS);

		System.out.printf("%d: %s awaiting instance to run %n",
				System.currentTimeMillis(), instance.getId());
		if (!runningTester.apply(instance))
			throw new TimeoutException("timeout waiting for instance to run: "
					+ instance.getId());

		instance = findInstanceById(client, instance.getId());

		RetryablePredicate<HostAndPort> socketTester = new RetryablePredicate<HostAndPort>(
				new InetSocketAddressConnect(), 300, 1, TimeUnit.SECONDS);
		System.out.printf("%d: %s awaiting ssh service to start%n",
				System.currentTimeMillis(), instance.getIpAddress());
		if (!socketTester.apply(HostAndPort.fromParts(instance.getIpAddress(),
				22)))
			throw new TimeoutException("timeout waiting for ssh to start: "
					+ instance.getIpAddress());

		System.out.printf("%d: %s ssh service started%n",
				System.currentTimeMillis(), instance.getIpAddress());

		System.out.printf("%d: %s awaiting http service to start%n",
				System.currentTimeMillis(), instance.getIpAddress());
		if (!socketTester.apply(HostAndPort.fromParts(instance.getIpAddress(),
				80)))
			throw new TimeoutException("timeout waiting for http to start: "
					+ instance.getIpAddress());

		System.out.printf("%d: %s http service started%n",
				System.currentTimeMillis(), instance.getIpAddress());
		return instance;
	}

	private static RunningInstance findInstanceById(EC2Client client,
			String instanceId) {
		// search my account for the instance I just created
		Set<? extends Reservation<? extends RunningInstance>> reservations = client
				.getInstanceServices().describeInstancesInRegion(
						AWS_DEFAULT_REGION, instanceId); // last parameter (ids)
															// narrows the
		// search

		// since we refined by instanceId there should only be one instance
		return Iterables.getOnlyElement(Iterables.getOnlyElement(reservations));
	}
}
