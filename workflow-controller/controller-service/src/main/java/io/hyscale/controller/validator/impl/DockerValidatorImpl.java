package io.hyscale.controller.validator.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import io.hyscale.builder.core.models.ImageBuilderActivity;
import io.hyscale.builder.services.exception.ImageBuilderErrorCodes;
import io.hyscale.commons.commands.CommandExecutor;
import io.hyscale.commons.commands.provider.ImageCommandProvider;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.controller.validator.Validator;

public class DockerValidatorImpl<I> extends Validator<I>{
    private static final Logger logger = LoggerFactory.getLogger(DockerValidatorImpl.class);
    
    @Autowired
    private ImageCommandProvider commandGenerator;

	@Override
	public boolean preValidate(I processInput) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean postValidate(I processInput){
		// TODO Auto-generated method stub
		
		 String command = commandGenerator.dockerVersion();
	        logger.debug("Docker Installed check command: {}", command);
	        boolean success = CommandExecutor.execute(command);
	        if (!success) {
	        	return false;
	        }
	        command = commandGenerator.dockerImages();
	        logger.debug("Docker Daemon running check command: {}", command);
	        success = CommandExecutor.execute(command);
	        if (!success) {
	            WorkflowLogger.error(ImageBuilderActivity.DOCKER_DAEMON_NOT_RUNNING);
	            return false;
	        }
		return true;
	}

	@Override
	public void preProcess(I processInput) throws HyscaleException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postProcess(I processInput) throws HyscaleException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onError(I processInput, Throwable th) {
		// TODO Auto-generated method stub
		
	}

}
