/**
 * Copyright 2019 Pramati Prism, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.hyscale.controller.directive.impl;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.utils.WindowsUtil;
import io.hyscale.controller.directive.ServiceSpecDirectiveUpdateHandler;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.PropType;

/**
 * Updates the "props" directive from the service spec before processing the
 * service spec for deployment. Update operation is like a pre-processor and
 * does not change the state of the service spec
 * 
 * @author tushart
 *
 */
@Component
public class PropsJsonHandler extends ServiceSpecDirectiveUpdateHandler<Map<String, Object>> {

	private static final Logger logger = LoggerFactory.getLogger(BuildSpecJsonHandler.class);

	@Override
	public Map<String, Object> updateObject(Map<String, Object> props) throws HyscaleException {
		if (props == null || props.isEmpty()) {
			return props;
		}
		// update file type props
		props.entrySet().forEach(each -> {
			String value = String.valueOf(each.getValue());
			if (PropType.FILE.getPatternMatcher().matcher(value).matches()) {
				each.setValue(WindowsUtil.updateToUnixFileSeparator(value));
			}
		});

		return props;
	}

	@Override
	protected String[] getPath() {
		return new String[] { HyscaleSpecFields.props };
	}

	@Override
	protected Class<Map<String, Object>> getType() {
		return (Class<Map<String, Object>>) (Class) Map.class;
	}

}
