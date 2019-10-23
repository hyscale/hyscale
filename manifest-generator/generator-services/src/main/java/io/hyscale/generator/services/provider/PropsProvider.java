package io.hyscale.generator.services.provider;

import com.fasterxml.jackson.core.type.TypeReference;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.Props;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class PropsProvider {

	private static final Logger logger = LoggerFactory.getLogger(PropsProvider.class);

	public static Props getProps(ServiceSpec serviceSpec) throws HyscaleException {
		Props props = new Props();
		try {
			TypeReference<Map<String, String>> mapTypeReference = new TypeReference<Map<String, String>>() {
			};
			Map<String, String> propsMap = serviceSpec.get(HyscaleSpecFields.props, mapTypeReference);

			if (propsMap != null && !propsMap.isEmpty()) {
				props.setProps(propsMap);
			}
		} catch (HyscaleException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Error while fetching map props ", e);
		}
		return props;
	}
}
