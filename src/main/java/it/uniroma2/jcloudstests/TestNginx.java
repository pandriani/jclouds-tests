package it.uniroma2.jcloudstests;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import it.uniroma2.cloud.PROVIDER;
import it.uniroma2.cloud.ProviderFactory;
import it.uniroma2.cloud.util.PropertiesMap;
import it.uniroma2.cloud.util.ProviderHelper;
import it.uniroma2.cloud.util.ProviderHelperFactory;
import it.uniroma2.cloud.util.PropertiesMap.CloudProviderProperty;

import org.jclouds.compute.ComputeService;
import org.jclouds.compute.RunScriptOnNodesException;
import org.jclouds.compute.domain.ComputeMetadata;
import org.jclouds.compute.domain.ExecResponse;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.NodeMetadataBuilder;
import org.jclouds.domain.LoginCredentials;
import org.jclouds.io.Payload;
import org.jclouds.io.Payloads;
import org.jclouds.ssh.SshClient;

import com.google.common.collect.Iterables;

import static com.google.common.collect.Iterables.*;
import static com.google.common.collect.Iterables.filter;
import static org.jclouds.compute.options.TemplateOptions.Builder.overrideLoginCredentials;
import static org.jclouds.compute.predicates.NodePredicates.withIds;

;

public class TestNginx {

	/**
	 * @param args
	 * @throws RunScriptOnNodesException
	 */
	public static void main(String[] args) throws RunScriptOnNodesException {
		String lbId = "1485e57a-626b-46ac-ab88-26eb9e87581f";
		PropertiesMap p = PropertiesMap.getInstance();
		ComputeService computeService = ProviderFactory
				.createComputeService(PROVIDER.CLOUDSTACK);
		ProviderHelper helper = ProviderHelperFactory
				.getProviderHelper(PROVIDER.CLOUDSTACK);
		NodeMetadata lbNodeMetadata = computeService.getNodeMetadata(lbId);

		SshClient client = computeService
				.getContext()
				.utils()
				.sshForNode()
				.apply(NodeMetadataBuilder
						.fromNodeMetadata(lbNodeMetadata)
						.credentials(
								LoginCredentials
										.builder()
										.user(p.get(CloudProviderProperty.CLOUDSTACK_IMAGE_USER))
										.password(
												p.get(CloudProviderProperty.CLOUDSTACK_IMAGE_PASSWORD))
										.authenticateSudo(true).build())
						.build());
		try {
			client.connect();
			client.put(
					"/home/tesisti/newNginx",
					Payloads.newStringPayload(
							"upstream backend {\n" +
//							"  server 192.168.3.50:8080;\n" +
							"  server 192.168.3.52:8080;\n" +
							"}\n" +
							"server {\n" +
							"  location / {\n" + 
							"    proxy_pass  http://backend;\n" +
							"  }\n" +
							"}\n"));
		} finally {
			if (client != null)
				client.disconnect();
		}
		
		helper.runScriptOnInstance(computeService, lbId,"cp /home/tesisti/newNginx /etc/nginx/sites-enabled/cloudstacklb"); 
		helper.runScriptOnInstance(computeService, lbId,"kill -HUP `cat /var/run/nginx.pid`"); //OPPURE "service nginx reload"
		
	}
}
