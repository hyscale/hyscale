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
package io.hyscale.controller.util;

import java.util.List;
import java.util.Objects;


import io.hyscale.controller.service.ReplicaProcessingService;
import io.hyscale.deployer.services.deployer.Deployer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.builder.core.models.BuildContext;
import io.hyscale.builder.services.util.ImageLogUtil;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.AuthConfig;
import io.hyscale.commons.utils.HyscaleStringUtil;
import io.hyscale.controller.activity.ControllerActivity;
import io.hyscale.controller.constants.WorkflowConstants;
import io.hyscale.controller.model.WorkflowContext;
import io.hyscale.deployer.services.model.DeployerActivity;
import io.hyscale.deployer.services.model.ReplicaInfo;
import io.hyscale.deployer.services.exception.DeployerErrorCodes;
import io.hyscale.deployer.services.util.DeployerLogUtil;

/**
 * Utility to fetch deployment logs of the service specified.
 */
@Component
public class LoggerUtility {

    private static final Logger logger = LoggerFactory.getLogger(LoggerUtility.class);

    @Autowired
    private ImageLogUtil imageLogUtil;

    @Autowired
    private DeployerLogUtil deployerLogUtil;

    @Autowired
    private ServiceLogsInputHandler serviceLogsInputHandler;

    @Autowired
    private Deployer deployer;

    @Autowired
    private ReplicaProcessingService replicaProcessingService;

    /**
     * Deployment logs
     * User can provide a replica for which he wants to see the logs,
     * in which case if replica exists its logs are shown to the user.
     * If the provided replica does not exist, available replicas are printed and the flow stops.
     * In case user doesn't provide a replica, and there are more than 1 replica
     * user is prompted to choose from the available replicas.
     * Chosen replica logs are shown to the user.
     *
     * @param context
     * @throws HyscaleException
     */
    public void deploymentLogs(WorkflowContext context) throws HyscaleException {
        String appName = context.getAppName();
        String serviceName = context.getServiceName();
        String namespace = context.getNamespace();
        String replicaName = (String) context.getAttribute(WorkflowConstants.REPLICA_NAME);

        Boolean isTail = (Boolean) context.getAttribute(WorkflowConstants.TAIL_LOGS);
        isTail = (isTail == null) ? false : isTail;
        Integer lines = (Integer) context.getAttribute(WorkflowConstants.LINES);
        AuthConfig authConfig = context.getAuthConfig();

        String selectedPod = null;
        try {
            selectedPod = validateAndGetReplicaName(authConfig, appName, serviceName, replicaName, namespace);
        } catch (HyscaleException e) {
            context.setFailed(true);
            WorkflowLogger.error(ControllerActivity.CAUSE, e.getMessage());
            throw e;
        }
        if (StringUtils.isBlank(selectedPod)) {
            return;
        }
        try {
            WorkflowLogger.header(ControllerActivity.SERVICE_LOGS, selectedPod);
            deployerLogUtil.processLogs(authConfig, appName, serviceName, selectedPod, namespace, lines, isTail);
        } catch (HyscaleException ex) {
            logger.error("Error while getting deployment logs for service: {}, in namespace: {}", serviceName,
                    namespace, ex);
            if (ex.getHyscaleErrorCode() == DeployerErrorCodes.FAILED_TO_RETRIEVE_POD) {
                WorkflowLogger.error(ControllerActivity.SERVICE_NOT_CREATED);
            } else {
                context.setFailed(true);
                WorkflowLogger.error(ControllerActivity.FAILED_TO_STREAM_SERVICE_LOGS, ex.getMessage());
            }
            WorkflowLogger.error(ControllerActivity.CHECK_SERVICE_STATUS);
        } finally {
            WorkflowLogger.footer();
        }
    }

    /**
     * implementation :
     * <b>
     * 1. Replica name provided by user, validate and return replica name
     * 2. Replica name not provided by user
     *      a. If single replica, return replica name
     *      b. else prompt user to provide replica name
     * </b>
     *
     * @param authConfig
     * @param appName
     * @param serviceName
     * @param replicaName
     * @param namespace
     * @return replica name
     * @throws HyscaleException
     */
    private String validateAndGetReplicaName(AuthConfig authConfig, String appName, String serviceName, String replicaName,
                                             String namespace) throws HyscaleException {

        // Fetch latest replicas of the given service & app in the namespace
        // if replica is not provided by the user , validation happens on all the replicas of that service
        // if replica is provides by the user, validation happens only on the latest replicas of the service
        List<ReplicaInfo> replicaInfoList = replicaProcessingService.getReplicas(appName, serviceName, namespace, replicaName != null ? false : true);
        if (replicaInfoList == null || replicaInfoList.isEmpty()) {
            if (replicaProcessingService.hasService(authConfig, appName, serviceName, namespace)) {
                WorkflowLogger.error(DeployerActivity.SERVICE_WITH_ZERO_REPLICAS);
            } else {
                WorkflowLogger.error(ControllerActivity.SERVICE_NOT_CREATED);
                WorkflowLogger.error(ControllerActivity.CHECK_SERVICE_STATUS);
            }
            WorkflowLogger.footer();
            return null;
        }

        // Replica is not provided by the user
        if (replicaName == null) {
            ReplicaInfo replicaInfo = null;
            // If a single replica exists , consume replica name from cluster directly
            if (replicaInfoList.size() == 1) {
                replicaInfo = replicaInfoList.get(0);
            }
            // interactively consume the replica name from the user if replicaInfo is null
            replicaName = replicaInfo != null ? replicaInfo.getName() : serviceLogsInputHandler.getPodFromUser(replicaInfoList);
        }

        if (!replicaProcessingService.doesReplicaExist(replicaName, replicaInfoList)) {
            StringBuilder pods = new StringBuilder();
            replicaInfoList.stream().filter(Objects::nonNull).forEach(each -> pods.append(each.getName() + ", "));
            WorkflowLogger.error(ControllerActivity.REPLICA_DOES_NOT_EXIT, replicaName,
                    HyscaleStringUtil.removeSuffixStr(pods, ", "));
            WorkflowLogger.footer();
            HyscaleException ex = new HyscaleException(DeployerErrorCodes.REPLICA_DOES_NOT_EXIT, replicaName);
            throw ex;
        }

        return replicaName;
    }


    /**
     * Image build and push logs
     *
     * @param context
     */
    public void imageBuilderLogs(WorkflowContext context) throws HyscaleException {
        BuildContext buildContext = new BuildContext();
        buildContext.setAppName(context.getAppName());
        buildContext.setServiceName(context.getServiceName());
        Boolean tail = (Boolean) context.getAttribute(WorkflowConstants.TAIL_LOGS);
        logger.debug("Getting Image Builder logs");
        tail = tail == null ? false : tail;
        buildContext.setTail(tail);
        imageLogUtil.handleLogs(buildContext);

    }


}
