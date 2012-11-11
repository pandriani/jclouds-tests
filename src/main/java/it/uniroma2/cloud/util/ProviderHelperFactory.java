package it.uniroma2.cloud.util;

import it.uniroma2.cloud.PROVIDER;

public class ProviderHelperFactory {

	public static ProviderHelper getProviderHelper(PROVIDER provider){
		if (provider == PROVIDER.CLOUDSTACK)
			return CloudStackProviderHelper.getInstance();
		else if (provider == PROVIDER.AWS_EC2)
			return AWSProviderHelper.getInstance();
		else
			return null;//throw new Exception("Unsupported provider: " + provider.toString());
	}

}
