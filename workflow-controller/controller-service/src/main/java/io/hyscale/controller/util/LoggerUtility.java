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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.builder.core.models.BuildContext;
import io.hyscale.builder.services.util.ImageLogUtil;
import io.hyscale.commons.exception.CommonErrorCode;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.TableFields;
import io.hyscale.commons.logger.TableFormatter;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.AuthConfig;
import io.hyscale.commons.models.K8sAuthorisation;
import io.hyscale.commons.utils.HyscaleInputReader;
import io.hyscale.commons.utils.HyscaleStringUtil;
import io.hyscale.commons.utils.ResourceSelectorUtil;
import io.hyscale.controller.activity.ControllerActivity;
import io.hyscale.controller.builder.K8sAuthConfigBuilder;
import io.hyscale.controller.constants.WorkflowConstants;
import io.hyscale.controller.model.WorkflowContext;
import io.hyscale.deployer.core.model.ReplicaInfo;
import io.hyscale.deployer.core.model.ResourceKind;
import io.hyscale.deployer.services.exception.DeployerErrorCodes;
import io.hyscale.deployer.services.handler.ResourceHandlers;
import io.hyscale.deployer.services.handler.impl.V1PodHandler;
import io.hyscale.deployer.services.provider.K8sClientProvider;
import io.hyscale.deployer.services.util.DeployerLogUtil;
import io.hyscale.deployer.services.util.K8sDeployerUtil;
import io.hyscale.deployer.services.util.K8sReplicaUtil;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.models.V1Pod;

/**
 * Utility to fetch deployment logs of the service specified.
 */
@Component
public class LoggerUtility {

    private static final Logger logger = LoggerFactory.getLogger(LoggerUtility.class);

    @Autowired
    private ImageLogUtil imageLogUtil;

    @Autowired
    private K8sAuthConfigBuilder authConfigBuilder;

    @Autowired
    private DeployerLogUtil deployerLogUtil;

    @Autowired
    private K8sClientProvider clientProvider;

    public void getLogs(WorkflowContext workflowContext) throws HyscaleException {
        // Ignore image logs as they can be viewed at the directory
        // imageBuilderLogs(workflowContext);
        deploymentLogs(workflowContext);
    }

    /**
     * Deployment logs 
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
        AuthConfig authConfig = authConfigBuilder.getAuthConfig();

        String selectedPod = null;
        try {
            selectedPod = getPodName(authConfig, appName, serviceName, replicaName, namespace);
        } catch (HyscaleException e) {
            context.setFailed(true);
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
     * @param authConfig
     * @param appName
     * @param serviceName
     * @param replicaName
     * @param namespace
     * @return replica name
     * @throws HyscaleException
     */
    private String getPodName(AuthConfig authConfig, String appName, String serviceName, String replicaName,
            String namespace) throws HyscaleException {
        List<V1Pod> existingPods = null;
        try {
            existingPods = getExistingPods(authConfig, appName, serviceName, namespace);
        } catch (HyscaleException ex) {
            WorkflowLogger.error(ControllerActivity.FAILED_TO_STREAM_SERVICE_LOGS, ex.getMessage());
            throw ex;
        }
        if (existingPods == null || existingPods.isEmpty()) {
            WorkflowLogger.error(ControllerActivity.SERVICE_NOT_CREATED);
            WorkflowLogger.error(ControllerActivity.CHECK_SERVICE_STATUS);
            WorkflowLogger.footer();
            return null;
        }
        
        if (replicaName != null) {
            boolean isReplicaNameValid = existingPods.stream().anyMatch(each -> each.getMetadata().getName().equals(replicaName));
            if (!isReplicaNameValid) {
                StringBuilder pods = new StringBuilder();
                existingPods.forEach(each -> pods.append(each.getMetadata().getName() + ", "));
                WorkflowLogger.error(ControllerActivity.REPLICA_DOES_NOT_EXIT, replicaName,
                        HyscaleStringUtil.removeSuffixStr(pods, ", "));
                WorkflowLogger.footer();
                HyscaleException ex = new HyscaleException(DeployerErrorCodes.REPLICA_DOES_NOT_EXIT, replicaName);
                throw ex;
            }
            return replicaName;
        }
        
        if (existingPods.size() == 1) {
            return existingPods.get(0).getMetadata().getName();
        }
        
        ApiClient apiClient = clientProvider.get((K8sAuthorisation) authConfig);
        existingPods = K8sDeployerUtil.filterPods(apiClient, appName, serviceName, namespace, existingPods);
        return getPodFromUser(authConfig, appName, serviceName, namespace, existingPods);
    }

    private List<V1Pod> getExistingPods(AuthConfig authConfig, String appName, String serviceName, String namespace)
            throws HyscaleException {

        V1PodHandler v1PodHandler = (V1PodHandler) ResourceHandlers.getHandlerOf(ResourceKind.POD.getKind());
        String selector = ResourceSelectorUtil.getServiceSelector(appName, serviceName);
        ApiClient apiClient = clientProvider.get((K8sAuthorisation) authConfig);
        return v1PodHandler.getBySelector(apiClient, selector, true, namespace);
    }

    /**
     * 
     * @param authConfig
     * @param appName
     * @param serviceName
     * @param namespace
     * @return pod name selected by user based on index or name
     * @throws HyscaleException
     */
    private String getPodFromUser(AuthConfig authConfig, String appName, String serviceName, String namespace,
            List<V1Pod> podList) throws HyscaleException {
        
        if (podList == null || podList.isEmpty()) {
            return null;
        }
        
        if (podList.size() == 1) {
            return podList.get(0).getMetadata().getName();
        }
        
        List<ReplicaInfo> replicasInfo = K8sReplicaUtil.getReplicaInfo(podList);
        
        List<String> replicas = replicasInfo.stream().map(each -> each.getName()).collect(Collectors.toList());
        Map<Integer, ReplicaInfo> indexedReplicasInfo = new HashMap<Integer, ReplicaInfo>();

        Integer replicaIndex = 1;
        for (ReplicaInfo replicaInfo : replicasInfo) {
            indexedReplicasInfo.put(replicaIndex++, replicaInfo);
        }
        printReplicasInfo(indexedReplicasInfo);

        WorkflowLogger.action(ControllerActivity.INPUT_REPLICA_DETAIL);
        int inputAttempt = 0;

        do {
            boolean isInvalidInput = false;
            inputAttempt++;
            String input = HyscaleInputReader.readInput();
            try {
                replicaIndex = Integer.parseInt(input);
                if (indexedReplicasInfo.containsKey(replicaIndex)) {
                    return indexedReplicasInfo.get(replicaIndex).getName();
                } else {
                    isInvalidInput = true;
                }
            } catch (NumberFormatException e) {
                if (replicas.contains(input)) {
                    return input;
                } else {
                    isInvalidInput = true;
                }
            }
            if (isInvalidInput && inputAttempt < HyscaleInputReader.MAX_RETRIES) {
                WorkflowLogger.warn(ControllerActivity.INVALID_INPUT_RETRY, input);
            }
        } while (inputAttempt < HyscaleInputReader.MAX_RETRIES);

        HyscaleException hex = new HyscaleException(CommonErrorCode.FAILED_TO_GET_VALID_INPUT);
        WorkflowLogger.error(ControllerActivity.INVALID_INPUT, hex.getMessage());
        WorkflowLogger.footer();
        throw hex;
    }

    private static void printReplicasInfo(Map<Integer, ReplicaInfo> indexedReplicasInfo) {
        TableFormatter replicaTable = new TableFormatter.Builder()
                .addField(TableFields.INDEX.getFieldName(), TableFields.INDEX.getLength())
                .addField(TableFields.REPLICA_NAME.getFieldName(), TableFields.REPLICA_NAME.getLength())
                .addField(TableFields.STATUS.getFieldName())
                .addField(TableFields.AGE.getFieldName(), TableFields.AGE.getLength()).build();

        indexedReplicasInfo.entrySet().forEach(replicaInfoSet -> {
            String[] replicaData = StatusUtil.getReplicasData(replicaInfoSet.getValue());
            String[] rowData = ArrayUtils.insert(0, replicaData, replicaInfoSet.getKey().toString());
            replicaTable.addRow(rowData);
        });

        WorkflowLogger.logTable(replicaTable);
        WorkflowLogger.footer();

    }

    /**
     * Image build and push logs
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
