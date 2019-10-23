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
					logger.error("Error while closing the output stream {}", ex);
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
