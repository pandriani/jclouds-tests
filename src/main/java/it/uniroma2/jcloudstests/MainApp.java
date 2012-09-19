/**
 *
 * Copyright (C) 2011 Cloud Conscious, LLC. <info@cloudconscious.com>
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */

package it.uniroma2.jcloudstests;

import static org.jclouds.ec2.options.RunInstancesOptions.Builder.asType;
import static org.jclouds.scriptbuilder.domain.Statements.exec;

import it.uniroma2.jcloudstests.PropertiesReader.AWS;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jclouds.aws.domain.Region;
import org.jclouds.compute.ComputeServiceContextFactory;
import org.jclouds.ec2.EC2AsyncClient;
import org.jclouds.ec2.EC2Client;
import org.jclouds.ec2.domain.InstanceState;
import org.jclouds.ec2.domain.InstanceType;
import org.jclouds.ec2.domain.IpProtocol;
import org.jclouds.ec2.domain.KeyPair;
import org.jclouds.ec2.domain.Reservation;
import org.jclouds.ec2.domain.RunningInstance;
import org.jclouds.ec2.predicates.InstanceStateRunning;
import org.jclouds.predicates.InetSocketAddressConnect;
import org.jclouds.predicates.RetryablePredicate;
import org.jclouds.rest.RestContext;
import org.jclouds.scriptbuilder.ScriptBuilder;
import org.jclouds.scriptbuilder.domain.OsFamily;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.net.HostAndPort;

/**
 * This the Main class of an Application that demonstrates the use of the
 * EC2Client by creating a small lamp server.
 * 
 * Usage is: java MainApp accesskeyid secretkey command name where command in
 * create destroy
 * 
 */
public class MainApp {
	
    private static final boolean IS_SET_PROXY = false;

	public static String AWS_REGION = Region.EU_WEST_1;
	public static String command = "create";
	//public static String command = "destroy";
	public static String AMI = "ami-f3595f87"; //Ubuntu 12.04 LTS Precise EBS boot

	
	
	public static void main(String[] args) throws TimeoutException, IOException {
		PropertiesReader p = new PropertiesReader("uniroma2.properties");
		
		String ACCESS_KEY_ID = p.getProperty(AWS.ACCESS_KEY_ID.name());
		String SECRET_KEY = p.getProperty(AWS.SECRET_KEY.name());
		String KEY_NAME = p.getProperty(AWS.KEY_NAME.name());
		// Args
	
		// set proxy If needed 		
	
		
		
		if (IS_SET_PROXY){
			setProxy();
		}

		// Init
		RestContext<EC2Client, EC2AsyncClient> context = new ComputeServiceContextFactory()
				.createContext("aws-ec2", ACCESS_KEY_ID, SECRET_KEY)
				.getProviderSpecificContext();

		// Get a synchronous client
		EC2Client client = context.getApi();

		try {
			if (command.equals("create")) {

				KeyPair pair = createKeyPair(client, KEY_NAME);
				System.out.println("KeyPair fingerprint: " + pair);
				RunningInstance instance = createSecurityGroupKeyPairAndInstance(
						client, KEY_NAME);

				System.out.printf("instance %s ready%n", instance.getId());
				System.out.printf("ip address: %s%n", instance.getIpAddress());
				System.out.printf("dns name: %s%n", instance.getDnsName());
				System.out.printf("login identity:%n%s%n",
						pair.getKeyMaterial());

			} else if (command.equals("destroy")) {
				destroySecurityGroupKeyPairAndInstance(client, KEY_NAME);
			}
		} finally {
			// Close connecton
			context.close();
			System.exit(0);
		}

	}

	private static void setProxy(){
		try{
			PropertiesReader proxyProp = new PropertiesReader("proxy.properties");
			for (Object key : proxyProp.keySet()){
				System.setProperty(key.toString(), proxyProp.getProperty(key.toString()));
				if (key.toString().equals("http.proxyHost")){ 
					System.out.println("set proxy host: " + proxyProp.getProperty(key.toString()));
				}
			}
			
			System.out.print(" correctly");
		}catch (Exception e) {
			System.out.println("No proxy set. Check your proxy.properties file");
		}

	}

	
	private static void destroySecurityGroupKeyPairAndInstance(
			EC2Client client, String name) {
		try {
			String id = findInstanceByKeyName(client, name).getId();
			System.out.printf("%d: %s terminating instance%n",
					System.currentTimeMillis(), id);
			client.getInstanceServices().terminateInstancesInRegion(AWS_REGION,
					findInstanceByKeyName(client, name).getId());
		} catch (NoSuchElementException e) {
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			System.out.printf("%d: %s deleting keypair%n",
					System.currentTimeMillis(), name);
			client.getKeyPairServices().deleteKeyPairInRegion(AWS_REGION, name);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			System.out.printf("%d: %s deleting group%n",
					System.currentTimeMillis(), name);
			client.getSecurityGroupServices().deleteSecurityGroupInRegion(AWS_REGION,
					name);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static RunningInstance createSecurityGroupKeyPairAndInstance(
			EC2Client client, String name) throws TimeoutException {
		// create a new security group
//		createSecurityGroupAndAuthorizePorts(client, name);

		// create a new instance
		RunningInstance instance = runInstance(client, name, name);

		// await for the instance to start
		return blockUntilInstanceRunning(client, instance);
	}

	static void createSecurityGroupAndAuthorizePorts(EC2Client client,
			String name) {
		System.out.printf("%d: creating security group: %s%n",
				System.currentTimeMillis(), name);
		client.getSecurityGroupServices().createSecurityGroupInRegion(AWS_REGION,
				name, name);
		for (int port : new int[] { 80, 8080, 443, 22 }) {
			client.getSecurityGroupServices()
					.authorizeSecurityGroupIngressInRegion(AWS_REGION, name,
							IpProtocol.TCP, port, port, "0.0.0.0/0");
		}
	}

	static KeyPair createKeyPair(EC2Client client, String name) {
		System.out.printf("%d: Get key: %s%n",
				System.currentTimeMillis(), name);
		
		Set<KeyPair> keypairs = client.getKeyPairServices().describeKeyPairsInRegion(AWS_REGION, name);
		Iterator<KeyPair> it = keypairs.iterator();
		KeyPair keyPair = null;
		while (it.hasNext()) {
			keyPair = it.next();
			System.out.println(keyPair);
		}
		return keyPair;//client.getKeyPairServices().describeKeyPairsInRegion(AWS_REGION, name).iterator().next();
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
				.getInstanceServices().runInstancesInRegion(AWS_REGION, null, // allow
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
		if (!socketTester.apply(HostAndPort.fromParts(instance.getIpAddress(), 22)))
			throw new TimeoutException("timeout waiting for ssh to start: "
					+ instance.getIpAddress());

		System.out.printf("%d: %s ssh service started%n",
				System.currentTimeMillis(), instance.getIpAddress());

		System.out.printf("%d: %s awaiting http service to start%n",
				System.currentTimeMillis(), instance.getIpAddress());
		if (!socketTester.apply(HostAndPort.fromParts(instance.getIpAddress(), 80)))
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
				.getInstanceServices().describeInstancesInRegion(AWS_REGION,
						instanceId); // last parameter (ids) narrows the
		// search

		// since we refined by instanceId there should only be one instance
		return Iterables.getOnlyElement(Iterables.getOnlyElement(reservations));
	}

	private static RunningInstance findInstanceByKeyName(EC2Client client,
			final String keyName) {
		// search my account for the instance I just created
		Set<? extends Reservation<? extends RunningInstance>> reservations = client
				.getInstanceServices().describeInstancesInRegion(AWS_REGION);

		// extract all the instances from all reservations
		Set<RunningInstance> allInstances = Sets.newHashSet();
		for (Reservation<? extends RunningInstance> reservation : reservations) {
			allInstances.addAll(reservation);
		}

		// get the first one that has a keyname matching what I just created
		return Iterables.find(allInstances, new Predicate<RunningInstance>() {

			public boolean apply(RunningInstance input) {
				return input.getKeyName().equals(keyName)
						&& input.getInstanceState() != InstanceState.TERMINATED;
			}

		});
	}
}
