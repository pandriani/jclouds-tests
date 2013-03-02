package it.uniroma2.jcloudstests;

import it.uniroma2.cloud.PROVIDER;
import it.uniroma2.cloud.ProviderFactory;
import it.uniroma2.cloud.util.PropertiesMap;
import it.uniroma2.cloud.util.PropertiesMap.CloudProviderProperty;
import it.uniroma2.cloud.util.ProviderHelperFactory;

import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.jclouds.ContextBuilder;
import org.jclouds.aws.domain.Region;
import org.jclouds.aws.ec2.reference.AWSEC2Constants;
import org.jclouds.cloudstack.CloudStackAsyncClient;
import org.jclouds.cloudstack.CloudStackClient;
import org.jclouds.cloudstack.domain.LoadBalancerRule;
import org.jclouds.cloudstack.features.LoadBalancerClient;
import org.jclouds.cloudstack.options.AccountInDomainOptions;
import org.jclouds.cloudstack.options.ListLoadBalancerRulesOptions;
import org.jclouds.compute.ComputeService;

import org.jclouds.ec2.EC2AsyncClient;
import org.jclouds.ec2.EC2Client;
import org.jclouds.elb.ELBApi;
import org.jclouds.elb.ELBAsyncApi;
import org.jclouds.elb.domain.Listener;
import org.jclouds.elb.domain.LoadBalancer;
import org.jclouds.elb.domain.Protocol;
import org.jclouds.elb.features.AvailabilityZoneApi;
import org.jclouds.elb.features.InstanceApi;
import org.jclouds.elb.features.LoadBalancerApi;
import org.jclouds.enterprise.config.EnterpriseConfigurationModule;
import org.jclouds.loadbalancer.LoadBalancerService;
import org.jclouds.loadbalancer.LoadBalancerServiceContext;
import org.jclouds.loadbalancer.domain.LoadBalancerMetadata;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.rest.RestContext;
import org.jclouds.sshj.config.SshjSshClientModule;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Module;

public class TestLoadBalancer {

	private static final PROVIDER provider = PROVIDER.AWS_EC2;
	private static final PropertiesMap p = PropertiesMap.getInstance();

	
	private static Iterable<Module> getModules() {
		return ImmutableSet.<Module> of(new SshjSshClientModule(),
				new SLF4JLoggingModule(), new EnterpriseConfigurationModule());
	}
	
	private static Properties configureAWSProperties(){
		Properties overrides = new Properties();
		overrides.setProperty(AWSEC2Constants.PROPERTY_EC2_CC_REGIONS,
				Region.US_EAST_1);
		return overrides;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String accessKey = p.get(CloudProviderProperty.AWS_ACCESS_KEY_ID);
		String secretKey = p.get(CloudProviderProperty.AWS_SECRET_KEY);
		String region = Region.US_EAST_1;
//		ContextBuilder builder = ContextBuilder.newBuilder("aws-elb")
//				.credentials(accessKey, secretKey).modules(getModules()).overrides(configureAWSProperties());
//		LoadBalancerService loadBalancerService = builder.build(
//				LoadBalancerServiceContext.class).getLoadBalancerService();

		
		
		
		
//		RestContext<CloudStackClient, CloudStackAsyncClient> cloudstack = (RestContext<CloudStackClient, CloudStackAsyncClient>)ProviderFactory.createRestContext(PROVIDER.CLOUDSTACK);
//		LoadBalancerClient loadbalancer = cloudstack.getApi().getLoadBalancerClient();
//		Set<LoadBalancerRule> rules = loadbalancer.listLoadBalancerRules(ListLoadBalancerRulesOptions.Builder.accountInDomain("tesisti", AccountInDomainOptions.NONE.toString()));
		
		RestContext<ELBApi, ELBAsyncApi> context = ContextBuilder
				.newBuilder("aws-elb")
				.credentials(p.get(CloudProviderProperty.AWS_ACCESS_KEY_ID),
						p.get(CloudProviderProperty.AWS_SECRET_KEY))
				.modules(getModules()).overrides(configureAWSProperties()).build();
		ELBApi elb = context.getApi();

		LoadBalancerApi loadabalancerApi = elb.getLoadBalancerApiForRegion(region);
		Listener listener = Listener.builder().instancePort(8080).instanceProtocol(Protocol.HTTP).port(80).protocol(Protocol.HTTP).build();
		String elbresult = loadabalancerApi.createListeningInAvailabilityZones("worker-node-AWS-ELB", listener, ImmutableSet.<String> of("us-east-1a"));
		System.out.println(elbresult);
		
		InstanceApi instanceApi = elb.getInstanceApiForRegion(region);
		
		
		
//		Set<? extends LoadBalancerMetadata> lbs = loadBalancerService.listLoadBalancers();
//		Iterator<? extends LoadBalancerMetadata> lbsIt = lbs.iterator();
//
//		while(lbsIt.hasNext()){
//			System.out.println(lbsIt.next());
//		}
		
	}

}
