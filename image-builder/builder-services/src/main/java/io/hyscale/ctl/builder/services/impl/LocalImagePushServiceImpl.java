package io.hyscale.ctl.builder.services.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.hyscale.ctl.commons.models.CommandResult;
import io.hyscale.ctl.commons.utils.ObjectMapperFactory;
import io.hyscale.ctl.servicespec.commons.util.ImageUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.ctl.builder.services.command.ImageCommandGenerator;
import io.hyscale.ctl.builder.services.config.ImageBuilderConfig;
import io.hyscale.ctl.builder.services.exception.ImageBuilderErrorCodes;
import io.hyscale.ctl.builder.core.models.BuildContext;
import io.hyscale.ctl.builder.core.models.DockerImage;
import io.hyscale.ctl.builder.core.models.ImageBuilderActivity;
import io.hyscale.ctl.commons.commands.CommandExecutor;
import io.hyscale.ctl.commons.constants.ToolConstants;
import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.commons.logger.WorkflowLogger;
import io.hyscale.ctl.commons.models.Status;
import io.hyscale.ctl.builder.services.util.DockerImageUtil;
import io.hyscale.ctl.builder.services.util.ImageLogUtil;
import io.hyscale.ctl.builder.services.service.ImagePushService;
import io.hyscale.ctl.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.ctl.servicespec.commons.model.service.ServiceSpec;

@Component
public class LocalImagePushServiceImpl implements ImagePushService {

    private static final Logger logger = LoggerFactory.getLogger(LocalImagePushServiceImpl.class);

    @Autowired
    private DockerImageUtil dockerImageUtil;

    @Autowired
    private CommandExecutor commandExecutor;

    @Autowired
    private ImageCommandGenerator commandGenerator;

    @Autowired
    private ImageLogUtil imageLogUtil;

    @Autowired
    private ImageBuilderConfig imageBuilderConfig;

    /**
     * Check docker exists, If stack image as service image pull tag, If push
     * required login and push else return
     *
     * @throws HyscaleException
     */
    @Override
    public void pushImage(ServiceSpec serviceSpec, BuildContext buildContext) throws HyscaleException {

        validate(serviceSpec, buildContext);

        try {
            dockerImageUtil.isDockerRunning();
        } catch (HyscaleException e) {
            logger.error(e.toString());
            throw e;
        }
        String imageFullPath = ImageUtil.getImage(serviceSpec);
        String sourceImage = getSourceImageName(serviceSpec, buildContext);

        if (buildContext.isStackAsServiceImage()) {
            pullImage(sourceImage);
        }

        tagImage(sourceImage, imageFullPath);

        if (buildContext.getImageRegistry() == null) {
            WorkflowLogger.startActivity(ImageBuilderActivity.IMAGE_PUSH);
            WorkflowLogger.endActivity(Status.SKIPPING);
            return;
        }
        // Ignore login - updates the docker config file
//        dockerLogin(buildContext);
        WorkflowLogger.startActivity(ImageBuilderActivity.IMAGE_PUSH);
        String appName = buildContext.getAppName();
        String serviceName = buildContext.getServiceName();
        boolean verbose = buildContext.isVerbose();
        String pushImageCommand = commandGenerator.getImagePushCommand(imageFullPath);
        String logFilePath = imageBuilderConfig.getDockerPushLogDir(appName, serviceName);
        File logFile = new File(logFilePath);
        buildContext.setPushLogs(logFilePath);
        // TODO keep continuation activity for user , launch a new thread & waitFor
        boolean status = commandExecutor.execute(pushImageCommand, logFile);
        if (!status) {
            WorkflowLogger.endActivity(Status.FAILED);
            logger.error("Failed to push docker image");
        } else {
            String inspectCommand = commandGenerator.getImageInspectCommand(ImageUtil.getImage(serviceSpec));
            CommandResult result = commandExecutor.executeAndGetResults(inspectCommand);
            buildContext.setImageShaSum(getShaSum(result));
            WorkflowLogger.endActivity(Status.DONE);
        }

        if (verbose) {
            imageLogUtil.readPushLogs(appName, serviceName);
        }
        
        if (!status) {
        	throw new HyscaleException(ImageBuilderErrorCodes.FAILED_TO_PUSH_IMAGE);
        }

    }

    private void pullImage(String imageName) throws HyscaleException {
        WorkflowLogger.startActivity(ImageBuilderActivity.IMAGE_PULL);

        if (StringUtils.isBlank(imageName)) {
            WorkflowLogger.endActivity(Status.SKIPPING);
            return;
        }
        try {
            dockerImageUtil.pullImage(imageName);
        } catch (HyscaleException e) {
            WorkflowLogger.endActivity(Status.FAILED);
            logger.error(e.toString());
            throw e;
        }
        WorkflowLogger.endActivity(Status.DONE);

    }

    private void validate(ServiceSpec serviceSpec, BuildContext buildContext) throws HyscaleException {
        ArrayList<String> missingFields = new ArrayList<String>();

        if (serviceSpec == null) {
            missingFields.add("ServiceSpec");
        }
        if (buildContext == null) {
            missingFields.add("BuildContext");
        }

        if (!missingFields.isEmpty()) {
            String[] missingFieldsArr = new String[missingFields.size()];
            missingFieldsArr = missingFields.toArray(missingFieldsArr);
            throw new HyscaleException(ImageBuilderErrorCodes.FIELDS_MISSING, missingFieldsArr);
        }
    }

    private void tagImage(String sourceImage, String targetImage) throws HyscaleException {
        WorkflowLogger.startActivity(ImageBuilderActivity.IMAGE_TAG);

        if (StringUtils.isBlank(sourceImage)) {
            WorkflowLogger.endActivity(Status.SKIPPING);
            return;
        }
        try {
            dockerImageUtil.tagImage(sourceImage, targetImage);
        } catch (HyscaleException e) {
            WorkflowLogger.endActivity(Status.FAILED);
            logger.error(e.toString());
            throw e;
        }
        WorkflowLogger.endActivity(Status.DONE);

    }

    /*
     * If only stack image return stackImageName else return dockerImage name
     */
    private String getSourceImageName(ServiceSpec serviceSpec, BuildContext buildContext) throws HyscaleException {

        if (buildContext.isStackAsServiceImage()) {
            return serviceSpec.get(HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.buildSpec,
                    HyscaleSpecFields.stackImage), String.class);
        }
        DockerImage dockerImage = buildContext.getDockerImage();

        return StringUtils.isNotBlank(dockerImage.getTag())
                ? dockerImage.getName() + ToolConstants.COLON + dockerImage.getTag()
                : dockerImage.getName();
    }

//    private void dockerLogin(BuildContext buildContext) throws HyscaleException {
//        WorkflowLogger.startActivity(ImageBuilderActivity.LOGIN);
//        ImageRegistry imageRegistry = buildContext.getImageRegistry();
//        try {
//            dockerImageUtil.loginToRegistry(imageRegistry);
//        } catch (HyscaleException e) {
//            WorkflowLogger.endActivity(Status.FAILED);
//            logger.error(e.toString());
//            throw e;
//        }
//        WorkflowLogger.endActivity(Status.DONE);
//    }

    private String getShaSum(CommandResult result) {
        if (result == null || result.getExitCode() > 0 || StringUtils.isBlank(result.getCommandOutput())) {
            return null;
        }
        ObjectMapper mapper = ObjectMapperFactory.jsonMapper();
        try {
            JsonNode node = mapper.readTree(result.getCommandOutput());
            JsonNode digestNode = null;
            if (node.isArray()) {
                digestNode = node.get(0).get("RepoDigests");
            } else {
                digestNode = node.get("RepoDigests");
            }
            if (digestNode == null) {
                return null;
            }
            List<String> digestList = mapper.convertValue(digestNode, new TypeReference<List<String>>() {
            });
            return digestList.get(digestList.size() - 1);
        } catch (IOException e) {
            logger.debug("Error while processing image inspect results ", e);
        }
        return null;
    }


}
