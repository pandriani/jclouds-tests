package it.uniroma2.cloud.util;


import it.uniroma2.cloud.util.PropertiesMap.CloudProviderProperty;

import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.domain.TemplateBuilder;
import org.jclouds.compute.options.TemplateOptions;
import org.jclouds.domain.LoginCredentials;

public class CloudStackProviderHelper extends AbstractProviderHelper implements ProviderHelper {

	private static final PropertiesMap p = PropertiesMap.getInstance();
	private static CloudStackProviderHelper instance = null;
	private CloudStackProviderHelper(){}
	
	public static CloudStackProviderHelper getInstance() {
		if (instance == null) {
			instance = new CloudStackProviderHelper();
		}
		return instance;
	}
	
	@Override
	public LoginCredentials getLoginCredentials() {
		return LoginCredentials
				.builder()
				.user(p.get(CloudProviderProperty.CLOUDSTACK_IMAGE_USER))
				.password(
						p.get(CloudProviderProperty.CLOUDSTACK_IMAGE_PASSWORD))
				.authenticateSudo(true).build();
	}

	@Override
	public Template getTemplate(ComputeService computeService) {
		TemplateBuilder templateBuilder = computeService.templateBuilder();
		TemplateOptions opts = buildTemplateOptions();

		Template t = templateBuilder
				.imageId(p.get(CloudProviderProperty.CLOUDSTACK_DEFAULT_IMAGE))
				.smallest().options(opts.inboundPorts(getPortsToBeOpened())).build();
		return t;
	}
	
	public TemplateOptions buildTemplateOptions() {
		return TemplateOptions.Builder
				.overrideLoginCredentials(getLoginCredentials());
	}


}
