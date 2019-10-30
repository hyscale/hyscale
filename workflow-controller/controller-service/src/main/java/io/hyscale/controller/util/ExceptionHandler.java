package io.hyscale.controller.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.hyscale.commons.config.SetupConfig;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.controller.activity.ControllerActivity;
import picocli.CommandLine;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.ParseResult;

/**
 * Utility Class to handle unexpected exception
 * @author tushart
 */
@Component
public class ExceptionHandler implements IExecutionExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);

	@Override
	public int handleExecutionException(Exception ex, CommandLine commandLine, ParseResult parseResult)
			throws Exception {

		logger.error("Unexpected error: ", ex);
		WorkflowLogger.footer();
		WorkflowLogger.error(ControllerActivity.UNEXPECTED_ERROR, SetupConfig.getMountPathOf(SetupConfig.getToolLogDir()));
		WorkflowLogger.footer();

		return 0;
	}
}
