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
package io.hyscale.commons.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Map;

import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.exception.CommonErrorCode;
import io.hyscale.commons.exception.HyscaleException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

@Component
public class MustacheTemplateResolver {

	private static final Logger logger = LoggerFactory.getLogger(MustacheTemplateResolver.class);

	private MustacheFactory mustacheFactory;

	public MustacheTemplateResolver() {
		this.mustacheFactory = new DefaultMustacheFactory();
	}

	public String resolveTemplate(String templateFile, Map<String, Object> context) throws HyscaleException {
		if(StringUtils.isBlank(templateFile)){
			throw new HyscaleException(CommonErrorCode.FAILED_TO_RESOLVE_TEMPLATE,templateFile);
		}
		if(context == null || context.isEmpty()){
			throw new HyscaleException(CommonErrorCode.TEMPLATE_CONTEXT_NOT_FOUND,templateFile);
		}
		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			Writer out = new OutputStreamWriter(outputStream,
					Charset.forName(ToolConstants.CHARACTER_ENCODING).newEncoder());
			Mustache m = mustacheFactory.compile(templateFile);
			m.execute(out, context);
			if (out != null) {
				try {
					out.flush();
					out.close();
				} catch (IOException e) {
					HyscaleException ex = new HyscaleException(CommonErrorCode.FAILED_TO_RESOLVE_TEMPLATE, templateFile);
					logger.error("Error while closing the output stream {}", ex.getMessage());
				}
			}
			String populatedTemplate = new String(outputStream.toByteArray(),
					Charset.forName(ToolConstants.CHARACTER_ENCODING));
			return populatedTemplate;
		} catch (IOException e) {
			HyscaleException ex = new HyscaleException(CommonErrorCode.FAILED_TO_RESOLVE_TEMPLATE, templateFile);
			logger.error("Error while resolving template {}", templateFile, ex);
			throw ex;
		}
	}

}
